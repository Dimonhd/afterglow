package com.afterglow.block;

import com.afterglow.blockentity.SwitchBlockEntity;
import com.afterglow.grid.CircuitManager;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Выключатель: ПКМ замыкает/размыкает цепь. */
public class SwitchBlock extends ElectricComponentBlock {
    public static final MapCodec<SwitchBlock> CODEC = simpleCodec(SwitchBlock::new);
    /** true — контакты замкнуты, ток может течь. */
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    public SwitchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CLOSED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CLOSED);
    }

    @Override
    protected MapCodec<SwitchBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SwitchBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof BlockItem) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockState toggled = state.cycle(CLOSED);
            level.setBlock(pos, toggled, 3);
            CircuitManager.markDirty(level);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f,
                    toggled.getValue(CLOSED) ? 0.6f : 0.5f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
