package com.afterglow.grid;

import com.afterglow.physics.Circuit;
import net.minecraft.server.level.ServerLevel;

/**
 * Двухполюсный компонент цепи. Вывод 0 — на лицевой грани блока (FACING),
 * вывод 1 — на противоположной.
 */
public interface ElectricComponent {
    /** Добавляет элементы компонента в цепь; terminalNodes[0], [1] — узлы выводов. */
    void stamp(Circuit circuit, int[] terminalNodes);

    /** Вызывается каждый тик после расчёта: прочитать токи/напряжения, обновить состояние. */
    default void afterSolve(ServerLevel level, double dt) {}

    /** true, если в компоненте есть конденсаторы/катушки (нужен расчёт каждый тик). */
    default boolean isDynamic() { return false; }
}
