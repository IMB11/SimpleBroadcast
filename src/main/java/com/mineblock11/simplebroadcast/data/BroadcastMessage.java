package com.mineblock11.simplebroadcast.data;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.fabric.impl.biome.modification.BuiltInRegistryKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BroadcastMessage {
    private final Text contents;
    private final MessageType messageType;
    private final @Nullable Identifier ID;

    public BroadcastMessage(Text contents, MessageType messageType, @Nullable Identifier id) {
        this.contents = contents;
        this.messageType = messageType;
        this.ID = id;
    }

    public void broadcast(MinecraftServer server) {
        MessageType type = getMessageType();
        Text result = type.formatMessageContents(getContents().copy());
        server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(result));
    }

    public Text getContents() {
        return contents;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    @Nullable
    public Identifier getID() {
        return ID;
    }
}
