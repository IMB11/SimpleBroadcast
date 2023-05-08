package com.mineblock11.simplebroadcast.data;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class MessageType {

    protected @Nullable String prefix, suffix;
    protected BroadcastLocation defaultLocation = BroadcastLocation.CHAT;

    public BroadcastLocation getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(BroadcastLocation _location) {
        this.defaultLocation = _location;
    }

    @Nullable
    public Identifier getID() {
        for (var entry : ConfigurationManager.REGISTRY.entrySet()) {
            if (entry.getValue().equals(this)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Nullable
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String _prefix) {
        this.prefix = _prefix;
    }

    @Nullable
    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String _suffix) {
        this.suffix = _suffix;
    }

    public Text getPrefixAsText() {
        return TextParserUtils.formatText(getPrefix());
    }

    public Text getSuffixAsText() {
        return TextParserUtils.formatText(getSuffix());
    }

    public Text getPrefixAsText(MinecraftServer server, @Nullable ServerCommandSource source) {
        ParentTextNode contents = TextParserUtils.formatNodes(getPrefix());
        PlaceholderContext context = source == null ? PlaceholderContext.of(server) : PlaceholderContext.of(source);
        return Placeholders.parseText(contents, context);
    }

    public Text getSuffixAsText(MinecraftServer server, @Nullable ServerCommandSource source) {
        ParentTextNode contents = TextParserUtils.formatNodes(getSuffix());
        PlaceholderContext context = source == null ? PlaceholderContext.of(server) : PlaceholderContext.of(source);
        return Placeholders.parseText(contents, context);
    }

    public abstract Text formatMessageContents(MutableText contents, MinecraftServer server, @Nullable ServerCommandSource source);

    public boolean hasSuffix() {
        return this.suffix != null;
    }

    public boolean hasPrefix() {
        return this.prefix != null;
    }

    public static class VanillaMessageType extends CustomMessageType {
        public VanillaMessageType() {
            super("[Server]", null, null);
        }
    }

    public static class SimpleBroadcastDefaultMessageType extends CustomMessageType {
        public SimpleBroadcastDefaultMessageType() {
            super("<color:dark_gray>[<color:dark_red><bold>BROADCAST<r><color:dark_gray>]", null, null);
        }
    }

    public static class PlainMessageType extends CustomMessageType {
        public PlainMessageType() {
            super(null, null, null);
        }
    }

    public static class CustomMessageType extends MessageType {
        public CustomMessageType(@Nullable String prefix, @Nullable String suffix, @Nullable BroadcastLocation defaultLocation) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.defaultLocation = Objects.requireNonNullElse(defaultLocation, BroadcastLocation.CHAT);
        }

        @Override
        public Text formatMessageContents(MutableText contents, MinecraftServer server, @Nullable ServerCommandSource source) {
            MutableText content = Text.empty();
            if (this.getPrefix() != null) {
                content = content
                        .append(this.getPrefixAsText(server, source))
                        .append(" ");
            }
            content = content.append(contents);
            if (this.getSuffix() != null) {
                content = content
                        .append(" ")
                        .append(this.getSuffixAsText(server, source));
            }
            return content;
        }
    }
}
