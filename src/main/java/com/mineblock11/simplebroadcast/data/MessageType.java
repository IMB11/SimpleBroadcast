package com.mineblock11.simplebroadcast.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public abstract class MessageType {
    public static final HashMap<Identifier, MessageType> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(new Identifier("minecraft:vanilla"), new VanillaMessageType());
        REGISTRY.put(new Identifier("bukkit:default"), new BukkitMessageType());
        REGISTRY.put(new Identifier("minecraft:plain"), new PlainMessageType());
    }

    protected @Nullable Text prefix, suffix;

    @Nullable
    public Text getPrefix() {
        return prefix;
    }

    @Nullable
    public Text getSuffix() {
        return suffix;
    }

    public abstract Text formatMessageContents(MutableText contents);

    public static class VanillaMessageType extends MessageType {
        public VanillaMessageType() {
            this.prefix = Text.literal("[Server]");
        }

        @Override
        public Text formatMessageContents(MutableText contents) {
            return Text.empty()
                    .append(this.getPrefix())
                    .append(Text.of(" "))
                    .append(contents);
        }
    }

    public static class BukkitMessageType extends MessageType {
        public BukkitMessageType() {
            this.prefix = Text.empty()
                    .append(Text.literal("[").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal("BROADCAST").formatted(Formatting.DARK_RED))
                    .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
        }

        @Override
        public Text formatMessageContents(MutableText contents) {
            return Text.empty()
                    .append(this.getPrefix())
                    .append(Text.of(" "))
                    .append(contents);
        }
    }

    public static class PlainMessageType extends MessageType {
        @Override
        public Text formatMessageContents(MutableText contents) {
            return contents;
        }
    }
}
