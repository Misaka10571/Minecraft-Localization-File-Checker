package com.misaka57.localizationfileschecker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ClientHandler {
    public static void checkFilesAndShowMessage() {
        File configFile = new File(Minecraft.getInstance().gameDirectory, "config/" + LocalizationFilesChecker.MOD_ID + ".json");
        if (!configFile.exists()) {
            return;  // 无配置文件，跳过
        }

        try (FileReader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            String message = json.get("message").getAsString();
            List<String> files = gson.fromJson(json.get("files"), List.class);

            boolean allInstalled = true;
            for (String path : files) {
                File file = new File(Minecraft.getInstance().gameDirectory, path);
                if (!file.exists()) {
                    allInstalled = false;
                    break;
                }
            }

            if (allInstalled) {
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
            }
        } catch (IOException | JsonParseException e) {
            // 忽略错误，不弹出消息
        }
    }
}