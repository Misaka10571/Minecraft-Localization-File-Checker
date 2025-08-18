package com.misaka57.localizationfileschecker;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.ModContainer;

@Mod(value = LocalizationFilesChecker.MOD_ID, dist = Dist.CLIENT)  // 仅客户端加载
public class LocalizationFilesChecker {
    public static final String MOD_ID = "minecraftlocalizationfileschecker";

    public LocalizationFilesChecker(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(FMLClientSetupEvent.class, this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ClientHandler.checkFilesAndShowMessage();
    }
}