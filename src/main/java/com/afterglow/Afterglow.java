package com.afterglow;

import com.afterglow.grid.CircuitManager;
import com.afterglow.registry.ModBlockEntities;
import com.afterglow.registry.ModBlocks;
import com.afterglow.registry.ModCreativeTabs;
import com.afterglow.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@Mod(Afterglow.MODID)
public class Afterglow {
    public static final String MODID = "afterglow";

    public Afterglow(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onLevelTick);
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent e) -> CircuitManager.clearAll());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> CircuitManager.clearAll());
    }

    private void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            CircuitManager.get(level).tick(level);
        }
    }
}
