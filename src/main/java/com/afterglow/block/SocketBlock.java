package com.afterglow.block;

import com.afterglow.blockentity.SocketBlockEntity;
import com.afterglow.item.LampItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Цоколь для лампы. ПКМ с лампой — вставить, пустой рукой — вынуть. */
public class SocketBlock extends ElectricComponentBlock {
    public static final MapCodec<SocketBlock> CODEC = simpleCodec(SocketBlock::new);
    public static final BooleanProperty HAS_BULB = BooleanProperty.create("has_bulb");
    public static final IntegerProperty LIGHT = BlockStateProperties.LEVEL; // 0..15

    public SocketBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HAS_BULB, false).setValue(LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_BULB, LIGHT);
    }

    @Override
    protected MapCodec<SocketBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SocketBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof LampItem) {
            if (state.getValue(HAS_BULB)) return ItemInteractionResult.FAIL;
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof SocketBlockEntity be) {
                be.insertBulb(stack, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.getItem() instanceof BlockItem) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!state.getValue(HAS_BULB)) return InteractionResult.PASS;
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SocketBlockEntity be) {
            be.removeBulb(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SocketBlockEntity be) {
            be.dropBulb();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
