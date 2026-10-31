package com.afterglow.physics;

/** Самопроверка физического ядра. Запуск: javac + java com.afterglow.physics.PhysicsSelfTest */
public final class PhysicsSelfTest {
    private static int failures;

    private static void check(String name, double actual, double expected, double tol) {
        boolean ok = Math.abs(actual - expected) <= tol;
        System.out.printf("%s %-46s actual=%.6f expected=%.6f%n", ok ? "OK  " : "FAIL", name, actual, expected);
        if (!ok) failures++;
    }

    public static void main(String[] args) {
        // 1. Закон Ома: 12 В, r = 0.5, R = 14.4 -> I = 12/14.9
        {
            Circuit c = new Circuit();
            int p = c.addNode(), m = c.addNode();
            var s = c.addVoltageSource(p, m, 12, 0.5);
            var r = c.addResistor(p, m, 14.4);
            c.step(0.05);
            double i = 12 / 14.9;
            check("Ohm: I", r.current, i, 1e-9);
            check("Ohm: U on load", r.voltage, i * 14.4, 1e-9);
            check("Ohm: P = U*I", r.power(), i * i * 14.4, 1e-9);
            check("KCL: source I = load I", s.current, r.current, 1e-9);
            check("Terminal power = load power", s.power(), r.power(), 1e-9);
            check("Energy balance E*I = P_load + I^2*r", 12 * s.current, r.power() + i * i * 0.5, 1e-9);
        }
        // 2. Разомкнутая цепь: источник + лампа без замыкания -> ток 0
        {
            Circuit c = new Circuit();
            int p = c.addNode(), mid = c.addNode(), m = c.addNode();
            var s = c.addVoltageSource(p, m, 12, 0.5);
            var lamp = c.addResistor(mid, m, 14.4);
            // выключатель разомкнут: между p и mid нет элемента
            c.step(0.05);
            check("Open circuit: source current", s.current, 0, 1e-12);
            check("Open circuit: lamp current", lamp.current, 0, 1e-12);
            check("Open circuit: terminal voltage = EMF", s.voltage, 12, 1e-9);
        }
        // 3. Делитель напряжения 10 Ом + 20 Ом при 30 В (r=1e-6)
        {
            Circuit c = new Circuit();
            int p = c.addNode(), mid = c.addNode(), m = c.addNode();
            c.addVoltageSource(p, m, 30, 1e-6);
            c.addResistor(p, mid, 10);
            var r2 = c.addResistor(mid, m, 20);
            c.step(0.05);
            check("Divider: U2", r2.voltage, 20.0, 1e-3);
        }
        // 4. RC-зарядка: tau = R*C = 1 с; через 1 с Uc = E*(1-1/e) (погрешность метода Эйлера)
        {
            Circuit c = new Circuit();
            int p = c.addNode(), n = c.addNode(), m = c.addNode();
            c.addVoltageSource(p, m, 10, 1e-6);
            c.addResistor(p, n, 1000);
            var cap = c.addCapacitor(n, m, 1e-3, 0);
            for (int k = 0; k < 20; k++) c.step(0.05);
            check("RC charge at t=1s", cap.voltage, 10 * (1 - Math.exp(-1)), 0.25);
            for (int k = 0; k < 400; k++) c.step(0.05);
            check("RC charge at t=21s (full)", cap.voltage, 10, 1e-3);
            check("RC stored energy 1/2 C U^2", cap.storedEnergy(), 0.5 * 1e-3 * 100, 1e-4);
        }
        // 5. RL: tau = L/R = 1 с; в установившемся режиме I = E/R
        {
            Circuit c = new Circuit();
            int p = c.addNode(), n = c.addNode(), m = c.addNode();
            c.addVoltageSource(p, m, 10, 1e-6);
            c.addResistor(p, n, 10);
            var l = c.addInductor(n, m, 10, 0);
            for (int k = 0; k < 600; k++) c.step(0.05);
            check("RL steady current", l.current, 1.0, 1e-3);
            check("RL stored energy 1/2 L I^2", l.storedEnergy(), 0.5 * 10 * 1.0, 1e-2);
        }
        // 6. Изолированные узлы не ломают решатель
        {
            Circuit c = new Circuit();
            c.addNode(); c.addNode();
            c.step(0.05);
            check("Isolated nodes solved", c.voltageAt(1), 0, 1e-12);
        }
        System.out.println(failures == 0 ? "ALL TESTS PASSED" : failures + " TEST(S) FAILED");
        if (failures != 0) System.exit(1);
    }
}
