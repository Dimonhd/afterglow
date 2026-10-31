package com.afterglow.physics;

import java.util.ArrayList;
import java.util.List;

/**
 * Электрическая цепь постоянного тока (с переходными процессами).
 * Модифицированный узловой анализ; конденсаторы и катушки — компаньон-модели
 * (обратный метод Эйлера), поэтому метод устойчив при любом шаге dt.
 *
 * Соглашения о знаках:
 *  - напряжение элемента U = V(a) − V(b);
 *  - ток пассивного элемента положителен, если течёт от a к b;
 *  - у источника a — «+», b — «−»; ток положителен, если источник отдаёт его
 *    во внешнюю цепь (из «+»).
 */
public final class Circuit {

    public abstract static class Element {
        public final int a, b;
        public double voltage;   // В
        public double current;   // А
        public double energy;    // Дж: рассеянная (R) либо отданная (источник) за всё время

        Element(int a, int b) { this.a = a; this.b = b; }

        /** Мощность, Вт. Для источника — отдаваемая, для остальных — потребляемая. */
        public double power() { return voltage * current; }

        abstract void stamp(double[][] g, double[] rhs, int[] idx, double dt);
        abstract void update(double dt);
    }

    public static final class Resistor extends Element {
        public double resistance;
        Resistor(int a, int b, double r) { super(a, b); this.resistance = r; }

        @Override void stamp(double[][] g, double[] rhs, int[] idx, double dt) {
            stampConductance(g, idx, a, b, 1.0 / resistance);
        }
        @Override void update(double dt) {
            current = voltage / resistance;
            energy += power() * dt;
        }
    }

    public static final class VoltageSource extends Element {
        public double emf;
        public final double internalResistance;
        VoltageSource(int a, int b, double emf, double r) {
            super(a, b); this.emf = emf; this.internalResistance = r;
        }

        @Override void stamp(double[][] g, double[] rhs, int[] idx, double dt) {
            double gr = 1.0 / internalResistance;
            stampConductance(g, idx, a, b, gr);
            if (idx[a] >= 0) rhs[idx[a]] += emf * gr;
            if (idx[b] >= 0) rhs[idx[b]] -= emf * gr;
        }
        @Override void update(double dt) {
            current = (emf - voltage) / internalResistance;
            energy += power() * dt;
        }
    }

    public static final class Capacitor extends Element {
        public final double capacitance;
        double vPrev;
        Capacitor(int a, int b, double c, double v0) { super(a, b); this.capacitance = c; this.vPrev = v0; }

        @Override void stamp(double[][] g, double[] rhs, int[] idx, double dt) {
            double gc = capacitance / dt;
            stampConductance(g, idx, a, b, gc);
            if (idx[a] >= 0) rhs[idx[a]] += gc * vPrev;
            if (idx[b] >= 0) rhs[idx[b]] -= gc * vPrev;
        }
        @Override void update(double dt) {
            current = capacitance / dt * (voltage - vPrev);
            vPrev = voltage;
        }
        /** Запасённая энергия ½CU², Дж. */
        public double storedEnergy() { return 0.5 * capacitance * voltage * voltage; }
    }

    public static final class Inductor extends Element {
        public final double inductance;
        double iPrev;
        Inductor(int a, int b, double l, double i0) { super(a, b); this.inductance = l; this.iPrev = i0; }

        @Override void stamp(double[][] g, double[] rhs, int[] idx, double dt) {
            double gl = dt / inductance;
            stampConductance(g, idx, a, b, gl);
            if (idx[a] >= 0) rhs[idx[a]] -= iPrev;
            if (idx[b] >= 0) rhs[idx[b]] += iPrev;
        }
        @Override void update(double dt) {
            current = iPrev + dt / inductance * voltage;
            iPrev = current;
        }
        /** Запасённая энергия ½LI², Дж. */
        public double storedEnergy() { return 0.5 * inductance * current * current; }
    }

    private static void stampConductance(double[][] g, int[] idx, int a, int b, double gv) {
        int ia = idx[a], ib = idx[b];
        if (ia >= 0) g[ia][ia] += gv;
        if (ib >= 0) g[ib][ib] += gv;
        if (ia >= 0 && ib >= 0) { g[ia][ib] -= gv; g[ib][ia] -= gv; }
    }

    private int nodeCount;
    private final List<Element> elements = new ArrayList<>();
    private double[] nodeVoltage = new double[0];

    public int addNode() { return nodeCount++; }
    public int nodeCount() { return nodeCount; }
    public List<Element> elements() { return elements; }
    public double voltageAt(int node) { return nodeVoltage[node]; }

    public Resistor addResistor(int a, int b, double ohms) {
        if (!(ohms > 0)) throw new IllegalArgumentException("R must be > 0");
        Resistor e = new Resistor(a, b, ohms); elements.add(e); return e;
    }
    public VoltageSource addVoltageSource(int plus, int minus, double emf, double internalOhms) {
        if (!(internalOhms > 0)) throw new IllegalArgumentException("r must be > 0");
        VoltageSource e = new VoltageSource(plus, minus, emf, internalOhms); elements.add(e); return e;
    }
    public Capacitor addCapacitor(int a, int b, double farads, double initialVolts) {
        if (!(farads > 0)) throw new IllegalArgumentException("C must be > 0");
        Capacitor e = new Capacitor(a, b, farads, initialVolts); elements.add(e); return e;
    }
    public Inductor addInductor(int a, int b, double henries, double initialAmps) {
        if (!(henries > 0)) throw new IllegalArgumentException("L must be > 0");
        Inductor e = new Inductor(a, b, henries, initialAmps); elements.add(e); return e;
    }

    /** Один шаг моделирования длительностью dt секунд. */
    public void step(double dt) {
        int[] parent = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) parent[i] = i;
        for (Element e : elements) union(parent, e.a, e.b);

        // Группируем узлы по компонентам связности; первый узел компонента — «земля».
        int[] idx = new int[nodeCount];
        java.util.Arrays.fill(idx, -1);
        nodeVoltage = new double[nodeCount];
        java.util.Map<Integer, List<Integer>> comps = new java.util.LinkedHashMap<>();
        for (int i = 0; i < nodeCount; i++) comps.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(i);

        for (List<Integer> nodes : comps.values()) {
            for (int k = 1; k < nodes.size(); k++) idx[nodes.get(k)] = k - 1;
            // idx[ground] остаётся -1
        }

        // Элементы по компонентам.
        java.util.Map<Integer, List<Element>> byComp = new java.util.HashMap<>();
        for (Element e : elements) byComp.computeIfAbsent(find(parent, e.a), k -> new ArrayList<>()).add(e);

        for (var entry : comps.entrySet()) {
            List<Integer> nodes = entry.getValue();
            int n = nodes.size() - 1;
            if (n <= 0) continue;
            double[][] g = new double[n][n];
            double[] rhs = new double[n];
            for (Element e : byComp.getOrDefault(entry.getKey(), List.of())) e.stamp(g, rhs, idx, dt);
            double[] x = LinearSolver.solve(g, rhs);
            if (x == null) throw new IllegalStateException("Singular circuit matrix");
            for (int k = 1; k < nodes.size(); k++) nodeVoltage[nodes.get(k)] = x[k - 1];
        }

        for (Element e : elements) {
            e.voltage = nodeVoltage[e.a] - nodeVoltage[e.b];
            e.update(dt);
        }
    }

    private static int find(int[] p, int x) {
        while (p[x] != x) { p[x] = p[p[x]]; x = p[x]; }
        return x;
    }
    private static void union(int[] p, int a, int b) {
        int ra = find(p, a), rb = find(p, b);
        if (ra != rb) p[ra] = rb;
    }
}
