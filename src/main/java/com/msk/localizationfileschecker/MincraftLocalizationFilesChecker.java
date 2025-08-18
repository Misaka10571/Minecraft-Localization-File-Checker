package com.msk.localizationfileschecker;

import com.mojang.logging.LogManager;
import com.mojang.logging.Logger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.loading.FMLPaths;

import com.msk.localizationfileschecker.config.CheckerConfig;
import com.msk.localizationfileschecker.util.FileValidator;

import java.nio.file.Path;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Mod(localizationfileschecker.MODID)
public class localizationfileschecker {
    public static final String MODID = "localizationfileschecker";
    private static final Logger LOGGER = LogManager.getLogger();

    private CheckerConfig config;
    private boolean hasChecked = false;
    private List<String> missingFiles;

    public localizationfileschecker(IEventBus modEventBus, ModContainer modContainer) {
        // 注册客户端设置事件
        modEventBus.addListener(this::onClientSetup);

        // 加载配置
        loadConfig();

        // 注册到 Forge 事件总线
        NeoForge.EVENT_BUS.register(this);
    }

    private void loadConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("localization_checker.json");
        config = CheckerConfig.load(configPath);
        LOGGER.info("Loaded localization checker config with {} files to check",
                config.getFilesToCheck().size());
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Localization Checker initializing...");
    }

    @SubscribeEvent
    public void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!hasChecked) {
            hasChecked = true;

            // 延迟3秒后显示消息，确保玩家完全进入游戏
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    checkAndNotify();
                }
            }, 3000);
        }
    }

    private void checkAndNotify() {
        Path gameDir = FMLPaths.GAMEDIR.get();
        FileValidator validator = new FileValidator(gameDir);

        missingFiles = validator.validateFiles(config.getFilesToCheck());

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (missingFiles.isEmpty()) {
                // 所有文件都已正确安装
                mc.player.sendSystemMessage(
                        Component.literal("[ATM10S 汉化] ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal("✓ 汉化补丁已正确安装！")
                                        .withStyle(ChatFormatting.WHITE))
                );

                if (config.isShowDetails()) {
                    mc.player.sendSystemMessage(
                            Component.literal("  已验证 " + config.getFilesToCheck().size() + " 个文件")
                                    .withStyle(ChatFormatting.GRAY)
                    );
                }
            } else {
                // 有文件缺失
                mc.player.sendSystemMessage(
                        Component.literal("[ATM10S 汉化] ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal("⚠ 检测到汉化补丁未完全安装")
                                        .withStyle(ChatFormatting.YELLOW))
                );

                mc.player.sendSystemMessage(
                        Component.literal("  缺失 " + missingFiles.size() + " 个文件")
                                .withStyle(ChatFormatting.RED)
                );

                if (config.isShowMissingFiles() && missingFiles.size() <= 5) {
                    for (String file : missingFiles) {
                        mc.player.sendSystemMessage(
                                Component.literal("  - " + file)
                                        .withStyle(ChatFormatting.GRAY)
                        );
                    }
                }

                mc.player.sendSystemMessage(
                        Component.literal("  请重新安装汉化补丁")
                                .withStyle(ChatFormatting.AQUA)
                );
            }
        }
    }
}