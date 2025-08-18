package com.misaka57.localizationfileschecker.handler;

import com.misaka57.localizationfileschecker.config.CheckerConfig;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileCheckHandler {
    private final CheckerConfig config;
    private final List<String> missingFiles = new ArrayList<>();

    public FileCheckHandler(CheckerConfig config) {
        this.config = config;
    }

    public boolean checkAllFiles() {
        missingFiles.clear();
        File gameDir = Minecraft.getInstance().gameDirectory;
        boolean allValid = true;

        for (CheckerConfig.FileEntry entry : config.getFiles()) {
            File file = new File(gameDir, entry.path);

            if (!file.exists() && entry.required) {
                missingFiles.add(entry.path);
                allValid = false;
            }
        }

        return allValid;
    }

    public List<String> getMissingFiles() {
        return missingFiles;
    }
}