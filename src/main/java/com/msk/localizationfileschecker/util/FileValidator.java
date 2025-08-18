package com.example.localizationchecker.util;

import com.example.localizationchecker.config.FileCheckEntry;
import com.mojang.logging.LogManager;
import com.mojang.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FileValidator {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Path gameDirectory;

    public FileValidator(Path gameDirectory) {
        this.gameDirectory = gameDirectory;
    }

    public List<String> validateFiles(List<FileCheckEntry> entries) {
        List<String> missingFiles = new ArrayList<>();

        for (FileCheckEntry entry : entries) {
            Path filePath = gameDirectory.resolve(entry.getPath());

            switch (entry.getType()) {
                case "file":
                    if (!validateFile(filePath, entry)) {
                        missingFiles.add(entry.getPath());
                    }
                    break;

                case "directory":
                    if (!Files.isDirectory(filePath)) {
                        missingFiles.add(entry.getPath());
                    }
                    break;

                case "pattern":
                    // 可以扩展支持通配符匹配
                    if (!validatePattern(filePath.getParent(), filePath.getFileName().toString())) {
                        missingFiles.add(entry.getPath());
                    }
                    break;

                default:
                    LOGGER.warn("Unknown file type: {}", entry.getType());
            }
        }

        return missingFiles;
    }

    private boolean validateFile(Path filePath, FileCheckEntry entry) {
        if (!Files.exists(filePath)) {
            return false;
        }

        // 验证文件大小（如果指定）
        if (entry.getSize() > 0) {
            try {
                long actualSize = Files.size(filePath);
                if (actualSize != entry.getSize()) {
                    LOGGER.debug("File size mismatch for {}: expected {}, got {}",
                            filePath, entry.getSize(), actualSize);
                    return false;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to check file size", e);
                return false;
            }
        }

        // 验证文件哈希（如果指定）
        if (entry.getHash() != null && !entry.getHash().isEmpty()) {
            try {
                String actualHash = calculateMD5(filePath);
                if (!actualHash.equalsIgnoreCase(entry.getHash())) {
                    LOGGER.debug("File hash mismatch for {}", filePath);
                    return false;
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                LOGGER.error("Failed to check file hash", e);
                return false;
            }
        }

        return true;
    }

    private boolean validatePattern(Path directory, String pattern) {
        // 简单的通配符匹配实现
        if (!Files.isDirectory(directory)) {
            return false;
        }

        try {
            return Files.list(directory)
                    .anyMatch(path -> path.getFileName().toString().matches(
                            pattern.replace("*", ".*").replace("?", ".")
                    ));
        } catch (IOException e) {
            LOGGER.error("Failed to list directory", e);
            return false;
        }
    }

    private String calculateMD5(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileBytes = Files.readAllBytes(file);
        byte[] digest = md.digest(fileBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}