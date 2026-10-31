package com.afterglow.blockentity;

import com.afterglow.block.SocketBlock;
import com.afterglow.grid.CircuitManager;
import com.afterglow.item.LampItem;
import com.afterglow.physics.Circuit;
import com.afterglow.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Цоколь: хранит вставленную лампу; лампа — резистор R = U²/P, светит пропорционально P/Pном. */
public class SocketBlockEntity extends ElectricComponentBlockEntity {
    /** Лампа перегорает, если мощность выше номинальной в это число раз... */
    public static final double OVERLOAD_RATIO = 2.0;
    /** ...дольше этого числа тиков (40 тиков = 2 с). */
    public static final int BURNOUT_TICKS = 40;

    private ItemStack bulb = ItemStack.EMPTY;
    private int overloadTicks;
    private Circuit.Resistor element;

    public SocketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOCKET.get(), pos, state);
    }

    public boolean hasBulb() { return !bulb.isEmpty(); }

    public void insertBulb(ItemStack held, Player player) {
        bulb = held.copyWithCount(1);
        if (!player.getAbilities().instabuild) held.shrink(1);
        overloadTicks = 0;
        setBlockFlags(true, 0);
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.2f);
        }
    }

    public void removeBulb(Player player) {
        ItemStack out = bulb;
        bulb = ItemStack.EMPTY;
        overloadTicks = 0;
        setBlockFlags(false, 0);
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
        }
        if (!out.isEmpty()) player.getInventory().placeItemBackInInventory(out);
    }

    public void dropBulb() {
        if (level != null && !bulb.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, bulb);
        }
        bulb = ItemStack.EMPTY;
    }

    private void setBlockFlags(boolean hasBulb, int light) {
        setChanged();
        if (level == null) return;
        BlockState st = getBlockState();
        level.setBlock(worldPosition, st.setValue(SocketBlock.HAS_BULB, hasBulb).setValue(SocketBlock.LIGHT, light), 3);
        CircuitManager.markDirty(level);
    }

    @Override
    public void stamp(Circuit circuit, int[] nodes) {
        element = null;
        if (!bulb.isEmpty() && bulb.getItem() instanceof LampItem lamp) {
            element = circuit.addResistor(nodes[0], nodes[1], lamp.resistance());
        }
    }

    @Override
    public void afterSolve(ServerLevel serverLevel, double dt) {
        BlockState st = getBlockState();
        if (!(st.getBlock() instanceof SocketBlock)) return;

        int newLight = 0;
        if (element != null && bulb.getItem() instanceof LampItem lamp) {
            double ratio = element.power() / lamp.ratedPower();
            if (ratio > 0.01) newLight = Mth.clamp((int) Math.round(ratio * 15.0), 1, 15);
            if (ratio > OVERLOAD_RATIO) overloadTicks++;
            else if (overloadTicks > 0) overloadTicks--;
            if (overloadTicks >= BURNOUT_TICKS) {
                burnOut(serverLevel);
                return;
            }
        }
        if (st.getValue(SocketBlock.LIGHT) != newLight) {
            serverLevel.setBlock(worldPosition, st.setValue(SocketBlock.LIGHT, newLight), 3);
        }
    }

    private void burnOut(ServerLevel serverLevel) {
        bulb = ItemStack.EMPTY;
        overloadTicks = 0;
        setBlockFlags(false, 0);
        serverLevel.playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8f, 1.4f);
        serverLevel.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.6,
                worldPosition.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.02);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!bulb.isEmpty()) tag.put("Bulb", bulb.save(registries));
        tag.putInt("Overload", overloadTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        bulb = tag.contains("Bulb") ? ItemStack.parse(registries, tag.getCompound("Bulb")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        overloadTicks = tag.getInt("Overload");
    }
}
