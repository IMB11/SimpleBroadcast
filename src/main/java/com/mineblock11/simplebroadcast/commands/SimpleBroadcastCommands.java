package com.mineblock11.simplebroadcast.commands;

import com.mineblock11.simplebroadcast.commands.arguments.BroadcastLocationArgument;
import com.mineblock11.simplebroadcast.commands.arguments.MessageTypeArgument;
import com.mineblock11.simplebroadcast.commands.arguments.MessageTypeSuggestionProvider;
import com.mineblock11.simplebroadcast.data.BroadcastLocation;
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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SimpleBroadcastCommands {
    public void registerCommands(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(
                literal("broadcast")
                        .then(literal("help").executes(this::displayHelpPrompt))
                        .then(
                                argument("type", new MessageTypeArgument())
                                .suggests(new MessageTypeSuggestionProvider())
                                .then(
                                        argument("location", new BroadcastLocationArgument())
                                                .suggests((a, builder) -> CommandSource.suggestMatching(Arrays.stream(BroadcastLocation.values()).map(value -> value.asString()), builder))
                                        .then(argument("contents", StringArgumentType.string())
                                                .executes(this::executeLocationBroadcast)))
                                .then(
                                        argument("contents", StringArgumentType.string())
                                                .executes(this::executeChatBroadcast)))
        );
    }

    private int displayHelpPrompt(CommandContext<ServerCommandSource> commandContext) {
        String helpPrompt = "<color:gold>SimpleBroadcast<r> <dark_gray>%server:mod_version simplebroadcast%<r>\n" +
                "<color:blue><underline><italic><url:'https://docs.mineblock11.dev/simplebroadcast'>https://docs.mineblock11.dev/simplebroadcast<r>";
        PlaceholderContext context = PlaceholderContext.of(commandContext.getSource());
        ParentTextNode contents = TextParserUtils.formatNodes(helpPrompt);
        Text parsed = Placeholders.parseText(contents, context);
        commandContext.getSource().sendFeedback(parsed, false);
        return Command.SINGLE_SUCCESS;
    }

    private int executeLocationBroadcast(CommandContext<ServerCommandSource> commandContext) {
        BroadcastLocation location = BroadcastLocationArgument.get(commandContext, "location");
        MessageType type = MessageTypeArgument.get(commandContext, "type");
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        ParentTextNode contents = TextParserUtils.formatNodes(rawContents);
        PlaceholderContext context = PlaceholderContext.of(commandContext.getSource());
        Text parsed = Placeholders.parseText(contents, context);
        BroadcastMessage message = new BroadcastMessage(parsed, type, null, location);
        message.broadcast(commandContext.getSource().getServer());
        return Command.SINGLE_SUCCESS;
    }

    private int executeChatBroadcast(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = MessageTypeArgument.get(commandContext, "type");
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        ParentTextNode contents = TextParserUtils.formatNodes(rawContents);
        PlaceholderContext context = PlaceholderContext.of(commandContext.getSource());
        Text parsed = Placeholders.parseText(contents, context);
        BroadcastMessage message = new BroadcastMessage(parsed, type, null, BroadcastLocation.CHAT);
        message.broadcast(commandContext.getSource().getServer());
        return Command.SINGLE_SUCCESS;
    }
}
