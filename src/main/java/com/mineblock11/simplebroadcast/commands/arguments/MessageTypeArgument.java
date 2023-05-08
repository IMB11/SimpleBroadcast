package com.mineblock11.simplebroadcast.commands.arguments;

import com.mineblock11.simplebroadcast.data.ConfigurationManager;
import com.mineblock11.simplebroadcast.data.MessageType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.IdentifierArgumentType;

public class MessageTypeArgument implements ArgumentType<MessageType> {
    public static MessageType get(final CommandContext<?> context, final String name) {
        return context.getArgument(name, MessageType.class);
    }

    public MessageType parse(StringReader stringReader) throws CommandSyntaxException {
        return ConfigurationManager.REGISTRY.get(IdentifierArgumentType.identifier().parse(stringReader));
    }
}
