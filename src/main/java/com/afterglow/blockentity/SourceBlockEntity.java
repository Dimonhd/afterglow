package com.afterglow.blockentity;

import com.afterglow.grid.CircuitManager;
import com.afterglow.physics.Circuit;
import com.afterglow.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/** Регулируемый источник постоянного напряжения: ЭДС 0–48 В, внутреннее сопротивление 0,5 Ом. */
public class SourceBlockEntity extends ElectricComponentBlockEntity {
    public static final int MAX_VOLTS = 48;
    public static final double INTERNAL_OHMS = 0.5;

    private int setVolts = 12;
    private Circuit.VoltageSource element;

    public SourceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOURCE.get(), pos, state);
    }

    public int getSetVolts() { return setVolts; }

    public void adjust(int delta) {
        setVolts = Mth.clamp(setVolts + delta, 0, MAX_VOLTS);
        setChanged();
        if (level != null) CircuitManager.markDirty(level);
    }

    @Override
    public void stamp(Circuit circuit, int[] nodes) {
        // вывод 0 (лицевая грань) — «+», вывод 1 — «−»
        element = circuit.addVoltageSource(nodes[0], nodes[1], setVolts, INTERNAL_OHMS);
    }

    public Component statusMessage() {
        double i = element != null ? element.current : 0;
        double p = element != null ? element.power() : 0;
        return Component.translatable("message.afterglow.source",
                setVolts, String.format(Locale.ROOT, "%.2f", i), String.format(Locale.ROOT, "%.2f", p));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Volts", setVolts);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Volts")) setVolts = Mth.clamp(tag.getInt("Volts"), 0, MAX_VOLTS);
    }
}
