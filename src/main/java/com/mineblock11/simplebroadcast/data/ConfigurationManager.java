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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigurationManager {
    public static final HashMap<Identifier, MessageType> MESSAGE_TYPE_REGISTRY = new HashMap<>();
    public static final HashMap<Identifier, BroadcastMessage> MESSAGE_PRESET_REGISTRY = new HashMap<>();
//    public static final HashMap<Identifier> MESSAGE_POOL_REGISTRY = new HashMap<>();
//    public static final HashMap<Identifier, Object> SCHEDULE_REGISTRY = new HashMap<>();

    static {
        var $default = new MessageType.SimpleBroadcastDefaultMessageType();
        ConfigurationManager.MESSAGE_TYPE_REGISTRY.put(new Identifier("minecraft:vanilla"), new MessageType.VanillaMessageType());
        ConfigurationManager.MESSAGE_TYPE_REGISTRY.put(new Identifier("simplebroadcast:default"), $default);
        ConfigurationManager.MESSAGE_TYPE_REGISTRY.put(new Identifier("minecraft:plain"), new MessageType.PlainMessageType());

        ConfigurationManager.MESSAGE_PRESET_REGISTRY.put(new Identifier("simplebroadcast:hello"), new BroadcastMessage("Hello World!", $default, $default.getDefaultLocation()));
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
        for (Map.Entry<Identifier, MessageType> identifierMessageTypeEntry : MESSAGE_TYPE_REGISTRY.entrySet()) {
            JsonObject object = gson.toJsonTree(identifierMessageTypeEntry.getValue()).getAsJsonObject();
            object.addProperty("id", identifierMessageTypeEntry.getKey().toString());
            arrayOfMessageTypes.add(object);
        }
        config.add("message_types", arrayOfMessageTypes);

        JsonArray arrayOfMessagePresets = new JsonArray();
        for (Map.Entry<Identifier, BroadcastMessage> identifierBroadcastMessageEntry : MESSAGE_PRESET_REGISTRY.entrySet()) {
            JsonObject object = gson.toJsonTree(identifierBroadcastMessageEntry.getValue()).getAsJsonObject();
            object.remove("messageType");
            object.addProperty("messageType", identifierBroadcastMessageEntry.getValue().getMessageType().getID().toString());
            object.addProperty("id", identifierBroadcastMessageEntry.getKey().toString());
            arrayOfMessagePresets.add(object);
        }
        config.add("message_presets", arrayOfMessagePresets);

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
            ConfigurationManager.MESSAGE_TYPE_REGISTRY.clear();
            for (JsonElement arrayOfMessageType : arrayOfMessageTypes) {
                JsonObject obj = arrayOfMessageType.getAsJsonObject();
                Identifier ID = Identifier.tryParse(obj.get("id").getAsString());
                obj.remove("id");

                MessageType type = gson.fromJson(obj, MessageType.CustomMessageType.class);
                ConfigurationManager.MESSAGE_TYPE_REGISTRY.put(ID, type);
            }

            if(!config.has("message_presets")) {
                config.add("message_presets", new JsonArray());
            }

            JsonArray arrayOfMessagePresets = config.getAsJsonArray("message_presets");
            ConfigurationManager.MESSAGE_PRESET_REGISTRY.clear();
            for (JsonElement arrayOfMessagePreset : arrayOfMessagePresets) {
                JsonObject obj = arrayOfMessagePreset.getAsJsonObject();
                Identifier messageTypeID = Identifier.tryParse(obj.get("messageType").getAsString());
                Identifier ID = Identifier.tryParse(obj.get("id").getAsString());
                obj.remove("messageType");
                obj.remove("id");

                BroadcastMessage message = gson.fromJson(obj, BroadcastMessage.class);
                message.setMessageType(ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(messageTypeID));
                ConfigurationManager.MESSAGE_PRESET_REGISTRY.put(ID, message);
            }

            // TODO: Schedules + Message Pools
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
