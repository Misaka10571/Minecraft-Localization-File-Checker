package com.misaka57.localizationfileschecker;

import com.misaka57.localizationfileschecker.config.CheckerConfig;
import com.misaka57.localizationfileschecker.handler.FileCheckHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MinecraftLocalizationFilesChecker.MOD_ID)
public class MinecraftLocalizationFilesChecker {
    public static final String MOD_ID = "minecraftlocalizationfileschecker";
    private static CheckerConfig config;
    private static FileCheckHandler fileChecker;

    public MinecraftLocalizationFilesChecker(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::clientSetup);
        config = new CheckerConfig();
        fileChecker = new FileCheckHandler(config);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientEventHandler());
    }

    public static class ClientEventHandler {
        private boolean hasChecked = false;

        @SubscribeEvent
        public void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
            if (!hasChecked) {
                hasChecked = true;
                Minecraft mc = Minecraft.getInstance();

                boolean allFilesValid = fileChecker.checkAllFiles();

                String message = allFilesValid ?
                        config.getSuccessMessage() :
                        config.getFailureMessage();

                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendSystemMessage(Component.literal(message));

                        if (!allFilesValid) {
                            for (String missingFile : fileChecker.getMissingFiles()) {
                                mc.player.sendSystemMessage(
                                        Component.literal("§c缺失文件: " + missingFile)
                                );
                            }
                            for (FileCheckHandler.MismatchInfo mismatchedFile : fileChecker.getMismatchedFiles()) {
                                mc.player.sendSystemMessage(
                                        Component.literal("§c文件不匹配: " + mismatchedFile.path + " (§e" + mismatchedFile.reason + "§c)")
                                );
                            }
                        }
                    }
                });
            }
        }
    }
}