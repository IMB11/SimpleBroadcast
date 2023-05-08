package com.mineblock11.simplebroadcast.data;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.fabric.impl.biome.modification.BuiltInRegistryKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BroadcastMessage {
    private final Text contents;
    private final MessageType messageType;
    private final @Nullable Identifier ID;
    private final @Nullable BroadcastLocation broadcastLocation;

    public BroadcastMessage(Text contents, MessageType messageType, @Nullable Identifier id, @Nullable BroadcastLocation broadcastLocation) {
        this.contents = contents;
        this.messageType = messageType;
        this.ID = id;
        this.broadcastLocation = broadcastLocation;
    }

    public void broadcast(MinecraftServer server) {
        MessageType type = getMessageType();
        Text result = type.formatMessageContents(getContents().copy());
        switch (broadcastLocation) {
            case ACTIONBAR:
                server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(result, true));
                break;
            case TITLE:
                server.getPlayerManager().sendToAll(new TitleFadeS2CPacket(10, 100, 10));
                server.getPlayerManager().sendToAll(new TitleS2CPacket(result));
                break;
            default:
                server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(result));
                break;
        }
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

    @Nullable
    public BroadcastLocation getBroadcastLocation() {
        return broadcastLocation;
    }
}
