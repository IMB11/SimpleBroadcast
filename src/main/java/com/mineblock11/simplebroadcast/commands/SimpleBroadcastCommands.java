package com.mineblock11.simplebroadcast.commands;

import com.mineblock11.simplebroadcast.commands.arguments.MessageTypeArgument;
import com.mineblock11.simplebroadcast.commands.arguments.MessageTypeSuggestionProvider;
import com.mineblock11.simplebroadcast.data.BroadcastMessage;
import com.mineblock11.simplebroadcast.data.MessageType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.*;

public class SimpleBroadcastCommands {
    public void registerCommands(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(
                literal("broadcast")
                        .then(argument("type", new MessageTypeArgument()).suggests(new MessageTypeSuggestionProvider()).then(argument("contents", StringArgumentType.string()).executes(this::executeTemporaryBroadcast)))
        );
    }

    private int executeTemporaryBroadcast(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = MessageTypeArgument.get(commandContext, "type");
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        ParentTextNode contents = TextParserUtils.formatNodes(rawContents);
        PlaceholderContext context = PlaceholderContext.of(commandContext.getSource());
        Text parsed = Placeholders.parseText(contents, context);
        BroadcastMessage message = new BroadcastMessage(parsed, type, null);
        message.broadcast(commandContext.getSource().getServer());
        return Command.SINGLE_SUCCESS;
    }
}
