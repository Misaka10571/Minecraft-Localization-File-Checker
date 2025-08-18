package com.misaka57.localizationfileschecker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CheckerConfig {
    private List<FileEntry> files = new ArrayList<>();
    private String successMessage = "§a汉化补丁已正确安装！";
    private String failureMessage = "§c汉化补丁未正确安装，请检查！";
    private static final String CONFIG_FILE = "localization_checker.json";

    public static class FileEntry {
        public String path;
        public String type; // "json", "js", "png", etc.
        public boolean required = true;

        public FileEntry(String path, String type) {
            this.path = path;
            this.type = type;
        }
    }

    public CheckerConfig() {
        loadConfig();
    }

    private void loadConfig() {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        File configFile = new File(configDir, CONFIG_FILE);

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Gson gson = new Gson();
                CheckerConfig loaded = gson.fromJson(reader, CheckerConfig.class);
                this.files = loaded.files;
                this.successMessage = loaded.successMessage;
                this.failureMessage = loaded.failureMessage;
            } catch (Exception e) {
                createDefaultConfig(configFile);
            }
        } else {
            createDefaultConfig(configFile);
        }
    }

    private void createDefaultConfig(File configFile) {
        // 创建默认配置
        files.add(new FileEntry("config/example_mod_zh.json", "json"));
        files.add(new FileEntry("kubejs/client_scripts/lang_zh.js", "js"));
        files.add(new FileEntry("resources/assets/minecraft/textures/gui/title/minecraft_zh.png", "png"));

        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(this, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<FileEntry> getFiles() { return files; }
    public String getSuccessMessage() { return successMessage; }
    public String getFailureMessage() { return failureMessage; }
}