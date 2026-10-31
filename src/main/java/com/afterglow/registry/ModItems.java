package com.afterglow.registry;

import com.afterglow.Afterglow;
import com.afterglow.item.LampItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Afterglow.MODID);

    public static final DeferredItem<BlockItem> WIRE = ITEMS.registerSimpleBlockItem(ModBlocks.WIRE);
    public static final DeferredItem<BlockItem> SOURCE = ITEMS.registerSimpleBlockItem(ModBlocks.SOURCE);
    public static final DeferredItem<BlockItem> SWITCH = ITEMS.registerSimpleBlockItem(ModBlocks.SWITCH);
    public static final DeferredItem<BlockItem> SOCKET = ITEMS.registerSimpleBlockItem(ModBlocks.SOCKET);

    /** Лампа накаливания 12 В / 10 Вт. */
    public static final DeferredItem<LampItem> LAMP = ITEMS.registerItem("lamp",
            props -> new LampItem(props, 12.0, 10.0), new Item.Properties());

    private ModItems() {}
}
