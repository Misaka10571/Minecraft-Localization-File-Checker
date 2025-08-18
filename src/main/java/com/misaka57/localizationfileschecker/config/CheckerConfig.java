package com.misaka57.localizationfileschecker.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckerConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private List<FileEntry> files = new ArrayList<>();
    private String successMessage = "§a[汉化检查] ✓ 汉化补丁已成功加载！";
    private String failureMessage = "§c[汉化检查] ✗ 汉化补丁未正确安装，请重新下载并安装！";
    private static final String CONFIG_FILE = "localization_checker.json";

    // 内部类，用于匹配JSON文件中的 "files" 数组元素
    public static class FileEntry {
        public String path;
        public String type;
        public long size;
        public boolean required = true;
        public String hash; // 可选的hash值
    }

    // 内部类，用于完整映射 scanner.py 生成的JSON文件的根结构
    private static class RootConfig {
        public Map<String, Object> metadata;
        public Map<String, Object> statistics;
        public List<FileEntry> files;
        @SerializedName("successMessage") // 使用注解确保字段名匹配
        public String successMessage;
        @SerializedName("failureMessage") // 使用注解确保字段名匹配
        public String failureMessage;
        public Map<String, Object> settings;
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
                // 使用 RootConfig 类来解析完整的JSON结构
                RootConfig loaded = gson.fromJson(reader, RootConfig.class);

                // 如果解析成功，则更新当前配置
                if (loaded != null) {
                    if (loaded.files != null) {
                        this.files = loaded.files;
                    }
                    if (loaded.successMessage != null && !loaded.successMessage.isEmpty()) {
                        this.successMessage = loaded.successMessage;
                    }
                    if (loaded.failureMessage != null && !loaded.failureMessage.isEmpty()) {
                        this.failureMessage = loaded.failureMessage;
                    }
                    LOGGER.info("成功加载汉化检查配置文件: " + CONFIG_FILE);
                }
            } catch (Exception e) {
                // 如果解析失败，只打印错误日志，不再创建默认文件
                LOGGER.error("无法解析汉化检查配置文件，请检查文件格式是否正确。", e);
            }
        } else {
            // 如果文件不存在，只打印日志，不再创建默认文件
            LOGGER.warn("未找到汉化检查配置文件: " + configFile.getAbsolutePath() + "。模组将不会执行检查。");
        }
    }

    // Getter方法
    public List<FileEntry> getFiles() { return files; }
    public String getSuccessMessage() { return successMessage; }
    public String getFailureMessage() { return failureMessage; }
}