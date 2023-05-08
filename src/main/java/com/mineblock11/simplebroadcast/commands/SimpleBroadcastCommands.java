package com.mineblock11.simplebroadcast.commands;

import com.mineblock11.simplebroadcast.commands.arguments.MessageTypeSuggestionProvider;
import com.mineblock11.simplebroadcast.data.BroadcastLocation;
import com.mineblock11.simplebroadcast.data.BroadcastMessage;
import com.mineblock11.simplebroadcast.data.ConfigurationManager;
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
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SimpleBroadcastCommands {
    public void registerCommands(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(
                literal("broadcast")
                        .then(argument("contents", StringArgumentType.string()).executes(this::quickBroadcast))
                        .then(literal("types")
                                .then(
                                        argument("type", IdentifierArgumentType.identifier())
                                                .suggests(new MessageTypeSuggestionProvider())
                                                .then(literal("location").executes(this::getMessageTypeLocation).then(
                                                        argument("location", StringArgumentType.word())
                                                                .suggests((a, builder) -> CommandSource.suggestMatching(Arrays.stream(BroadcastLocation.values()).map(value -> value.asString()), builder))
                                                                .executes(this::setMessageTypeLocation)))
                                                .then(literal("prefix").executes(this::getMessageTypePrefix)
                                                        .then(argument("value", StringArgumentType.string())
                                                                .executes(this::setMessageTypePrefix)))
                                                .then(literal("suffix").executes(this::getMessageTypeSuffix)
                                                        .then(
                                                                argument("value", StringArgumentType.string())
                                                                        .executes(this::setMessageTypeSuffix))))
                                .then(
                                        literal("create").then(
                                                argument("id", IdentifierArgumentType.identifier()).executes(this::createMessageType)))
                        )
                        .then(literal("help").executes(this::displayHelpPrompt))
                        .then(
                                argument("type", IdentifierArgumentType.identifier())
                                        .suggests(new MessageTypeSuggestionProvider())
                                        .then(
                                                argument("location", StringArgumentType.word())
                                                        .suggests((a, builder) -> CommandSource.suggestMatching(Arrays.stream(BroadcastLocation.values()).map(value -> value.asString()), builder))
                                                        .then(argument("contents", StringArgumentType.string())
                                                                .executes(this::executeLocationBroadcast)))
                                        .then(
                                                argument("contents", StringArgumentType.string())
                                                        .executes(this::executeChatBroadcast)))
        );
    }

    private int quickBroadcast(CommandContext<ServerCommandSource> commandContext) {
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        BroadcastMessage message = new BroadcastMessage(rawContents, new MessageType.SimpleBroadcastDefaultMessageType(), BroadcastLocation.CHAT);
        message.broadcast(commandContext.getSource().getServer(), commandContext.getSource());
        return Command.SINGLE_SUCCESS;
    }

    private int setMessageTypeLocation(CommandContext<ServerCommandSource> commandContext) {
        BroadcastLocation location = BroadcastLocation.valueOf(StringArgumentType.getString(commandContext, "location").toUpperCase());
        MessageType type = ConfigurationManager.REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        type.setDefaultLocation(location);
        String resultPrompt = "<color:gray>" + type.getID() + "<r><color:gold> default display location is now:<r> <color:gray>" + location.asString();
        commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt), true);
        ConfigurationManager.saveConfig();
        return Command.SINGLE_SUCCESS;
    }

    private int getMessageTypeLocation(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String resultPrompt = "<color:gray>" + type.getID() + "<r><color:gold> default display location is:<r> <color:gray>" + type.getDefaultLocation().asString();
        commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt), true);
        return Command.SINGLE_SUCCESS;
    }

    private int setMessageTypeSuffix(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String rawContents = StringArgumentType.getString(commandContext, "value");
        type.setSuffix(rawContents);
        String resultPrompt = "<color:gray>" + type.getID() + "<r><color:gold> now has the following suffix:<r> ";
        commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt).copy().append(type.getSuffixAsText().copy().formatted(Formatting.GRAY)), true);
        ConfigurationManager.saveConfig();
        return Command.SINGLE_SUCCESS;
    }

    private int getMessageTypeSuffix(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));

        if (!type.hasSuffix()) {
            String resultPrompt = "<color:gray>" + type.getID() + "<r><color:gold> does not have a suffix.<r>";
            commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt), true);
        } else {
            String resultPrompt = "<color:gray>" + type.getID() + "<r><color:gold> has the following suffix:<r> ";
            commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt).copy().append(type.getSuffixAsText().copy().formatted(Formatting.GRAY)), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int setMessageTypePrefix(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String rawContents = StringArgumentType.getString(commandContext, "value");
        type.setPrefix(rawContents);
        String resultPrompt = "<color:gray>" + type.getID() + "<r><color:gold> now has the following prefix:<r> ";
        commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt).copy().append(type.getPrefixAsText().copy().formatted(Formatting.GRAY)), true);
        ConfigurationManager.saveConfig();
        return Command.SINGLE_SUCCESS;
    }

    private int getMessageTypePrefix(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));

        if (!type.hasPrefix()) {
            String resultPrompt = "<color:gray>" + type.getID() + "<r><color:gold> does not have a prefix.<r>";
            commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt), true);
        } else {
            String resultPrompt = "<color:gray>" + type.getID() + "<r><color:gold> has the following prefix:<r> ";
            commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt).copy().append(type.getPrefixAsText().copy().formatted(Formatting.GRAY)), true);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int createMessageType(CommandContext<ServerCommandSource> commandContext) {
        Identifier id = IdentifierArgumentType.getIdentifier(commandContext, "id");
        String resultPrompt = "<color:gold>Created new broadcast message type: <color:gray>" + id;
        commandContext.getSource().sendFeedback(TextParserUtils.formatText(resultPrompt), true);
        MessageType.CustomMessageType messageType = new MessageType.CustomMessageType(null, null, null);
        ConfigurationManager.REGISTRY.put(id, messageType);
        ConfigurationManager.saveConfig();
        return Command.SINGLE_SUCCESS;
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
        BroadcastLocation location = BroadcastLocation.valueOf(StringArgumentType.getString(commandContext, "location").toUpperCase());
        MessageType type = ConfigurationManager.REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        BroadcastMessage message = new BroadcastMessage(rawContents, type, location);
        message.broadcast(commandContext.getSource().getServer(), commandContext.getSource());
        return Command.SINGLE_SUCCESS;
    }

    private int executeChatBroadcast(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        BroadcastMessage message = new BroadcastMessage(rawContents, type, type.getDefaultLocation());
        message.broadcast(commandContext.getSource().getServer(), commandContext.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
