package com.afterglow.grid;

import com.afterglow.block.ElectricComponentBlock;
import com.afterglow.block.WireBlock;
import com.afterglow.physics.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Хранит и рассчитывает электрические цепи одного измерения.
 * Цепь пересобирается при любом изменении топологии (dirty) и решается узловым анализом.
 */
public final class CircuitManager {
    /** Шаг моделирования: один тик = 0,05 с. */
    public static final double DT = 0.05;
    /** Сопротивление стыка двух блоков провода, Ом. */
    public static final double WIRE_JOINT_OHMS = 0.02;
    /** Переходное сопротивление контакта провод—вывод и вывод—вывод, Ом. */
    public static final double CONTACT_OHMS = 0.01;

    private static final Map<ResourceKey<Level>, CircuitManager> MANAGERS = new HashMap<>();

    public static CircuitManager get(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level.dimension(), k -> new CircuitManager());
    }

    /** Сообщить, что топология цепи могла измениться. Безопасно вызывать на клиенте (ничего не делает). */
    public static void markDirty(Level level) {
        if (level instanceof ServerLevel sl) get(sl).dirty = true;
    }

    public static void clearAll() { MANAGERS.clear(); }

    private record TKey(long pos, int face) {}

    private final Set<BlockPos> components = new HashSet<>();
    private final List<ElectricComponent> active = new ArrayList<>();
    private Circuit circuit;
    private boolean dirty = true;
    private boolean dynamic;

    public void register(BlockPos pos) { components.add(pos.immutable()); dirty = true; }
    public void unregister(BlockPos pos) { components.remove(pos); dirty = true; }

    public void tick(ServerLevel level) {
        boolean rebuilt = false;
        if (dirty) {
            dirty = false;
            rebuild(level);
            rebuilt = true;
        }
        if (circuit == null) return;
        if (rebuilt || dynamic) circuit.step(DT);
        for (ElectricComponent c : active) c.afterSolve(level, DT);
    }

    private void rebuild(ServerLevel level) {
        circuit = new Circuit();
        active.clear();
        dynamic = false;

        Map<TKey, Integer> terminals = new HashMap<>();
        Set<BlockPos> stale = new HashSet<>();

        for (BlockPos pos : components) {
            if (!level.hasChunkAt(pos)) continue;
            BlockState st = level.getBlockState(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof ElectricComponent comp) || !(st.getBlock() instanceof ElectricComponentBlock)) {
                stale.add(pos);
                continue;
            }
            Direction facing = st.getValue(ElectricComponentBlock.FACING);
            int n0 = circuit.addNode();
            int n1 = circuit.addNode();
            terminals.put(new TKey(pos.asLong(), facing.get3DDataValue()), n0);
            terminals.put(new TKey(pos.asLong(), facing.getOpposite().get3DDataValue()), n1);
            comp.stamp(circuit, new int[]{n0, n1});
            active.add(comp);
            if (comp.isDynamic()) dynamic = true;
        }
        components.removeAll(stale);

        Map<Long, Integer> wireNodes = new HashMap<>();
        for (Map.Entry<TKey, Integer> e : new ArrayList<>(terminals.entrySet())) {
            BlockPos pos = BlockPos.of(e.getKey().pos());
            Direction face = Direction.from3DDataValue(e.getKey().face());
            int node = e.getValue();
            BlockPos np = pos.relative(face);
            if (!level.hasChunkAt(np)) continue;
            BlockState ns = level.getBlockState(np);
            if (ns.getBlock() instanceof WireBlock) {
                int wn = ensureWire(level, np, wireNodes);
                circuit.addResistor(node, wn, CONTACT_OHMS);
            } else if (ns.getBlock() instanceof ElectricComponentBlock) {
                Integer other = terminals.get(new TKey(np.asLong(), face.getOpposite().get3DDataValue()));
                if (other != null && pos.asLong() < np.asLong()) {
                    circuit.addResistor(node, other, CONTACT_OHMS);
                }
            }
        }
    }

    /** Создаёт узлы для всей связной группы проводов, начиная с start, возвращает узел start. */
    private int ensureWire(ServerLevel level, BlockPos start, Map<Long, Integer> wireNodes) {
        Integer existing = wireNodes.get(start.asLong());
        if (existing != null) return existing;

        wireNodes.put(start.asLong(), circuit.addNode());
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos w = queue.poll();
            int wn = wireNodes.get(w.asLong());
            for (Direction d : Direction.values()) {
                BlockPos np = w.relative(d);
                if (!level.hasChunkAt(np)) continue;
                if (!(level.getBlockState(np).getBlock() instanceof WireBlock)) continue;
                Integer nn = wireNodes.get(np.asLong());
                if (nn == null) {
                    nn = circuit.addNode();
                    wireNodes.put(np.asLong(), nn);
                    queue.add(np);
                }
                if (w.asLong() < np.asLong()) circuit.addResistor(wn, nn, WIRE_JOINT_OHMS);
            }
        }
        return wireNodes.get(start.asLong());
    }
}
