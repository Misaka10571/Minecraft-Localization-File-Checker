package com.example.localizationchecker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogManager;
import com.mojang.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CheckerConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private List<FileCheckEntry> filesToCheck = new ArrayList<>();
    private boolean showDetails = true;
    private boolean showMissingFiles = true;
    private String modpackName = "MyModpack";
    private String modpackVersion = "1.0.0";

    public static CheckerConfig load(Path configPath) {
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                return GSON.fromJson(json, CheckerConfig.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load config file", e);
            }
        } else {
            // 创建默认配置
            CheckerConfig defaultConfig = createDefault();
            try {
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, GSON.toJson(defaultConfig));
                LOGGER.info("Created default config file at {}", configPath);
            } catch (IOException e) {
                LOGGER.error("Failed to create default config", e);
            }
            return defaultConfig;
        }
        return new CheckerConfig();
    }

    private static CheckerConfig createDefault() {
        CheckerConfig config = new CheckerConfig();

        // 添加示例文件检查项
        config.filesToCheck.add(new FileCheckEntry("assets/minecraft/lang/zh_cn.json", "file"));
        config.filesToCheck.add(new FileCheckEntry("assets/examplemod/lang/zh_cn.json", "file"));
        config.filesToCheck.add(new FileCheckEntry("assets/minecraft/textures/gui/title/minecraft_zh.png", "file"));
        config.filesToCheck.add(new FileCheckEntry("config/examplemod-client.toml", "file"));
        config.filesToCheck.add(new FileCheckEntry("kubejs/assets/kubejs/lang/zh_cn.json", "file"));

        return config;
    }

    // Getters
    public List<FileCheckEntry> getFilesToCheck() {
        return filesToCheck;
    }

    public boolean isShowDetails() {
        return showDetails;
    }

    public boolean isShowMissingFiles() {
        return showMissingFiles;
    }

    public String getModpackName() {
        return modpackName;
    }

    public String getModpackVersion() {
        return modpackVersion;
    }
}