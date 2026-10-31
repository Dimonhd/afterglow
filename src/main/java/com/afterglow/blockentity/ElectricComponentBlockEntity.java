package com.afterglow.blockentity;

import com.afterglow.grid.CircuitManager;
import com.afterglow.grid.ElectricComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Основа блок-сущности компонента: сама регистрируется в менеджере цепей. */
public abstract class ElectricComponentBlockEntity extends BlockEntity implements ElectricComponent {
    protected ElectricComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel sl) CircuitManager.get(sl).register(worldPosition);
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel sl) CircuitManager.get(sl).unregister(worldPosition);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel sl) CircuitManager.get(sl).unregister(worldPosition);
        super.onChunkUnloaded();
    }
}
