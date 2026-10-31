package com.afterglow.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

/** Лампа накаливания: номинальные U и P, сопротивление R = U²/P. */
public class LampItem extends Item {
    private final double ratedVolts;
    private final double ratedWatts;

    public LampItem(Properties properties, double ratedVolts, double ratedWatts) {
        super(properties);
        this.ratedVolts = ratedVolts;
        this.ratedWatts = ratedWatts;
    }

    public double ratedVoltage() { return ratedVolts; }
    public double ratedPower() { return ratedWatts; }

    /** Сопротивление нити, Ом. */
    public double resistance() { return ratedVolts * ratedVolts / ratedWatts; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.afterglow.lamp",
                fmt(ratedVolts), fmt(ratedWatts), fmt(resistance())).withStyle(ChatFormatting.GRAY));
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.1f", v);
    }
}
