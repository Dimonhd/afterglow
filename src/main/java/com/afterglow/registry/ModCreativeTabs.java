package com.afterglow.registry;

import com.afterglow.Afterglow;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Afterglow.MODID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.afterglow"))
            .icon(() -> new ItemStack(ModItems.LAMP.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.SOURCE.get());
                output.accept(ModItems.WIRE.get());
                output.accept(ModItems.SWITCH.get());
                output.accept(ModItems.SOCKET.get());
                output.accept(ModItems.LAMP.get());
            })
            .build());

    private ModCreativeTabs() {}
}
