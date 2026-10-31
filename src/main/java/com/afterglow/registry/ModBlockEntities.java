package com.afterglow.registry;

import com.afterglow.Afterglow;
import com.afterglow.blockentity.SocketBlockEntity;
import com.afterglow.blockentity.SourceBlockEntity;
import com.afterglow.blockentity.SwitchBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Afterglow.MODID);

    public static final Supplier<BlockEntityType<SourceBlockEntity>> SOURCE = BLOCK_ENTITIES.register("source",
            () -> BlockEntityType.Builder.of(SourceBlockEntity::new, ModBlocks.SOURCE.get()).build(null));

    public static final Supplier<BlockEntityType<SwitchBlockEntity>> SWITCH = BLOCK_ENTITIES.register("switch",
            () -> BlockEntityType.Builder.of(SwitchBlockEntity::new, ModBlocks.SWITCH.get()).build(null));

    public static final Supplier<BlockEntityType<SocketBlockEntity>> SOCKET = BLOCK_ENTITIES.register("socket",
            () -> BlockEntityType.Builder.of(SocketBlockEntity::new, ModBlocks.SOCKET.get()).build(null));

    private ModBlockEntities() {}
}
