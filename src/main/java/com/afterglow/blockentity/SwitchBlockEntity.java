package com.afterglow.blockentity;

import com.afterglow.block.SwitchBlock;
import com.afterglow.physics.Circuit;
import com.afterglow.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Выключатель: замкнут — сопротивление 2 мОм, разомкнут — цепь разорвана. */
public class SwitchBlockEntity extends ElectricComponentBlockEntity {
    public static final double CLOSED_OHMS = 0.002;

    public SwitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SWITCH.get(), pos, state);
    }

    @Override
    public void stamp(Circuit circuit, int[] nodes) {
        if (getBlockState().getValue(SwitchBlock.CLOSED)) {
            circuit.addResistor(nodes[0], nodes[1], CLOSED_OHMS);
        }
    }
}
