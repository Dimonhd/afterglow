package com.afterglow.grid;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Блок, участвующий в электрической цепи. */
public interface ElectricBlock {
    /** Есть ли у блока электрический вывод на грани {@code face} (грань самого блока). */
    boolean hasTerminal(BlockState state, Direction face);
}
