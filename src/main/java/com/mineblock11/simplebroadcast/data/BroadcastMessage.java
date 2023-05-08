package com.mineblock11.simplebroadcast.data;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BroadcastMessage {
    private final String contents;
    private final MessageType messageType;
    private final @Nullable BroadcastLocation broadcastLocation;

    public BroadcastMessage(String contents, MessageType messageType, @Nullable BroadcastLocation broadcastLocation) {
        this.contents = contents;
        this.messageType = messageType;
        this.broadcastLocation = broadcastLocation;
    }

    public void broadcast(MinecraftServer server, @Nullable ServerCommandSource source) {
        MessageType type = getMessageType();
        Text result = type.formatMessageContents(getContentsAsText(server, source).copy(), server, source);
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

    public Text getContentsAsText(MinecraftServer server, @Nullable ServerCommandSource source) {
        ParentTextNode contents = TextParserUtils.formatNodes(this.contents);
        PlaceholderContext context = source == null ? PlaceholderContext.of(server) : PlaceholderContext.of(source);
        return Placeholders.parseText(contents, context);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    @Nullable
    public BroadcastLocation getBroadcastLocation() {
        return broadcastLocation;
    }
}
