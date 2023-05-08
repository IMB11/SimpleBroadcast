package com.mineblock11.simplebroadcast.data;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {
    public static final HashMap<Identifier, MessageType> REGISTRY = new HashMap<>();

    static {
        ConfigurationManager.REGISTRY.put(new Identifier("minecraft:vanilla"), new MessageType.VanillaMessageType());
        ConfigurationManager.REGISTRY.put(new Identifier("simplebroadcast:default"), new MessageType.SimpleBroadcastDefaultMessageType());
        ConfigurationManager.REGISTRY.put(new Identifier("minecraft:plain"), new MessageType.PlainMessageType());
    }

    private static File getConfigurationFile() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return new File("simple-broadcast-debug-config.json");
        } else {
            return FabricLoader.getInstance().getConfigDir().resolve("simple-broadcast.json").toFile();
        }
    }

    public static void saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
        JsonObject config = new JsonObject();
        JsonArray arrayOfMessageTypes = new JsonArray();
        for (Map.Entry<Identifier, MessageType> identifierMessageTypeEntry : REGISTRY.entrySet()) {
            JsonObject object = gson.toJsonTree(identifierMessageTypeEntry.getValue()).getAsJsonObject();
            object.addProperty("id", identifierMessageTypeEntry.getKey().toString());
            arrayOfMessageTypes.add(object);
        }
        config.add("message_types", arrayOfMessageTypes);

        String json = gson.toJson(config);
        try {
            Files.writeString(Path.of(getConfigurationFile().getPath()), json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadConfig() {
        File configurationFile = getConfigurationFile();
        if (!configurationFile.exists()) saveConfig();
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
        try {
            // message_types
            JsonObject config = gson.fromJson(new JsonReader(new FileReader(configurationFile)), JsonObject.class);
            JsonArray arrayOfMessageTypes = config.getAsJsonArray("message_types");
            ConfigurationManager.REGISTRY.clear();
            for (JsonElement arrayOfMessageType : arrayOfMessageTypes) {
                JsonObject obj = arrayOfMessageType.getAsJsonObject();
                Identifier ID = Identifier.tryParse(obj.get("id").getAsString());
                obj.remove("id");

                MessageType type = gson.fromJson(obj, MessageType.CustomMessageType.class);
                ConfigurationManager.REGISTRY.put(ID, type);
            }

            // TODO: Schedules + Message Pools
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
