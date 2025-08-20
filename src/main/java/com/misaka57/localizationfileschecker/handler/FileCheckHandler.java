package com.misaka57.localizationfileschecker.handler;

import com.misaka57.localizationfileschecker.config.CheckerConfig;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class FileCheckHandler {
    private final CheckerConfig config;
    private final List<String> missingFiles = new ArrayList<>();
    private final List<MismatchInfo> mismatchedFiles = new ArrayList<>();
    public static class MismatchInfo {
        public final String path;
        public final String reason;

        public MismatchInfo(String path, String reason) {
            this.path = path;
            this.reason = reason;
        }
    }

    public FileCheckHandler(CheckerConfig config) {
        this.config = config;
    }

    public boolean checkAllFiles() {
        missingFiles.clear();
        mismatchedFiles.clear();
        File gameDir = Minecraft.getInstance().gameDirectory;
        boolean allValid = true;

        for (CheckerConfig.FileEntry entry : config.getFiles()) {
            if (!entry.required) {
                continue;
            }

            File file = new File(gameDir, entry.path);

            if (!file.exists()) {
                missingFiles.add(entry.path);
                allValid = false;
            } else {
                if (file.length() != entry.size) {
                    mismatchedFiles.add(new MismatchInfo(entry.path, "文件大小不匹配"));
                    allValid = false;
                    continue;
                }

                if (entry.hash != null && !entry.hash.isEmpty()) {
                    try {
                        String localHash = calculateFileHash(file);
                        if (!localHash.equalsIgnoreCase(entry.hash)) {
                            mismatchedFiles.add(new MismatchInfo(entry.path, "文件哈希值不匹配"));
                            allValid = false;
                        }
                    } catch (Exception e) {
                        mismatchedFiles.add(new MismatchInfo(entry.path, "无法计算哈希值"));
                        allValid = false;
                    }
                }
            }
        }
        return allValid;
    }

    private String calculateFileHash(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                md.update(buffer, 0, read);
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public List<String> getMissingFiles() {
        return missingFiles;
    }

    public List<MismatchInfo> getMismatchedFiles() {
        return mismatchedFiles;
    }
}