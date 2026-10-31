package com.afterglow.registry;

import com.afterglow.Afterglow;
import com.afterglow.block.SocketBlock;
import com.afterglow.block.SourceBlock;
import com.afterglow.block.SwitchBlock;
import com.afterglow.block.WireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Afterglow.MODID);

    public static final DeferredBlock<WireBlock> WIRE = BLOCKS.registerBlock("wire", WireBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5f)
                    .sound(SoundType.COPPER).noOcclusion());

    public static final DeferredBlock<SourceBlock> SOURCE = BLOCKS.registerBlock("source", SourceBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f)
                    .sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<SwitchBlock> SWITCH = BLOCKS.registerBlock("switch", SwitchBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f)
                    .sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<SocketBlock> SOCKET = BLOCKS.registerBlock("socket", SocketBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f)
                    .sound(SoundType.METAL).noOcclusion()
                    .lightLevel(state -> state.getValue(SocketBlock.LIGHT)));

    private ModBlocks() {}
}
