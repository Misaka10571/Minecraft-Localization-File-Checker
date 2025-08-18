package com.misaka57.localizationfileschecker;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.misaka57.localizationfileschecker.config.CheckerConfig;
import com.misaka57.localizationfileschecker.handler.FileCheckHandler;

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

                // 执行文件检查
                boolean allFilesValid = fileChecker.checkAllFiles();

                // 获取自定义消息
                String message = allFilesValid ?
                        config.getSuccessMessage() :
                        config.getFailureMessage();

                // 发送消息到聊天框
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendSystemMessage(Component.literal(message));

                        // 如果检查失败，显示缺失的文件
                        if (!allFilesValid) {
                            for (String missingFile : fileChecker.getMissingFiles()) {
                                mc.player.sendSystemMessage(
                                        Component.literal("§c缺失文件: " + missingFile)
                                );
                            }
                        }
                    }
                });
            }
        }
    }
}