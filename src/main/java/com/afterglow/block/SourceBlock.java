package com.afterglow.block;

import com.afterglow.blockentity.SourceBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Регулируемый источник напряжения. ПКМ: +1 В, Shift+ПКМ: −1 В. Лицевая грань — «+». */
public class SourceBlock extends ElectricComponentBlock {
    public static final MapCodec<SourceBlock> CODEC = simpleCodec(SourceBlock::new);

    public SourceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SourceBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SourceBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        // блоки (провода и т.д.) можно ставить на источник; остальное — регулировка
        if (stack.getItem() instanceof BlockItem) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SourceBlockEntity src) {
            src.adjust(player.isShiftKeyDown() ? -1 : 1);
            player.displayClientMessage(src.statusMessage(), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
