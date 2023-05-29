package com.mineblock11.simplebroadcast.commands;

import com.mineblock11.simplebroadcast.commands.arguments.MessagePresetSuggestionProvider;
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
import net.minecraft.util.Identifier;

import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SimpleBroadcastCommands {
    public void registerCommands(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(
                literal("broadcast")
                        .requires(source -> source.hasPermissionLevel(4))
                        .then(argument("contents", StringArgumentType.string()).executes(this::quickBroadcast))
                        .then(literal("preset")
                                .then(
                                        literal("create")
                                                .then(
                                                        argument("id", IdentifierArgumentType.identifier())
                                                                .then(
                                                                        argument("type", IdentifierArgumentType.identifier())
                                                                                .suggests(new MessageTypeSuggestionProvider())
                                                                                .executes(this::createBlankPreset)
                                                                                .then(
                                                                                        argument("contents", StringArgumentType.string())
                                                                                                .executes(this::createContentPreset)
                                                                                )
                                                                )

                                                )
                                )
                                .then(
                                        argument("id", IdentifierArgumentType.identifier())
                                                .suggests(new MessagePresetSuggestionProvider())
                                                .executes(this::executePresetBroadcast)
                                                .then(
                                                        literal("contents")
                                                                .executes(this::getPresetContents)
                                                                .then(
                                                                        argument("value", StringArgumentType.string())
                                                                                .executes(this::setPresetContents)
                                                                )
                                                )
                                                .then(
                                                        literal("location")
                                                                .executes(this::getPresetLocation)
                                                                .then(
                                                                        argument("location", StringArgumentType.word())
                                                                                .suggests((a, builder) -> CommandSource.suggestMatching(Arrays.stream(BroadcastLocation.values()).map(value -> value.asString()), builder))
                                                                                .executes(this::setPresetLocation)
                                                                )
                                                )
                                                .then(
                                                        literal("type")
                                                                .executes(this::getPresetType)
                                                                .then(
                                                                        argument("type", IdentifierArgumentType.identifier())
                                                                                .suggests(new MessageTypeSuggestionProvider())
                                                                                .executes(this::setPresetType)
                                                                )
                                                )
                                                .then(literal("delete").executes(this::deletePreset))
                                )
                        )
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

    private void sendFeedback(CommandContext<ServerCommandSource> commandContext, String feedback) {
        commandContext.getSource().sendFeedback(TextParserUtils.formatText(feedback), true);
    }

    private int createContentPreset(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");
        String contents = StringArgumentType.getString(commandContext, "contents");
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        BroadcastMessage message = new BroadcastMessage(contents, type, type.getDefaultLocation());

        ConfigurationManager.MESSAGE_PRESET_REGISTRY.put(ID, message);

        sendFeedback(commandContext, "<color:gold>Created a new message preset.\nPlease configure it using the <color:gray>/broadcast preset " + ID + " ...<color:gold> command.");

        ConfigurationManager.saveConfig();

        return Command.SINGLE_SUCCESS;
    }

    private int createBlankPreset(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        BroadcastMessage message = new BroadcastMessage("<color:gold>This is a message preset, please configure it using the <color:gray>/broadcast preset " + ID + " ...<color:gold> command.", type, type.getDefaultLocation());

        ConfigurationManager.MESSAGE_PRESET_REGISTRY.put(ID, message);

        sendFeedback(commandContext, "<color:gold>Created a new message preset.\nPlease configure it using the <color:gray>/broadcast preset " + ID + " ...<color:gold> command.");

        ConfigurationManager.saveConfig();

        return Command.SINGLE_SUCCESS;
    }

    private int deletePreset(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");

        ConfigurationManager.MESSAGE_PRESET_REGISTRY.remove(ID);

        sendFeedback(commandContext, "<color:gray>" + ID + "<color:gold> has been deleted.");

        ConfigurationManager.saveConfig();

        return Command.SINGLE_SUCCESS;
    }

    private int getPresetContents(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");
        BroadcastMessage preset = ConfigurationManager.MESSAGE_PRESET_REGISTRY.get(ID);

        sendFeedback(commandContext, "<color:gray>" + ID + "<color:gold> has the following contents:\n<r>" + preset.getRawContents());

        return Command.SINGLE_SUCCESS;
    }

    private int setPresetContents(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");
        BroadcastMessage preset = ConfigurationManager.MESSAGE_PRESET_REGISTRY.get(ID);
        String contents = StringArgumentType.getString(commandContext, "value");

        preset.setRawContents(contents);

        sendFeedback(commandContext, "<color:gray>" + ID + "<color:gold> now has the following contents:\n<r>" + preset.getRawContents());

        ConfigurationManager.saveConfig();

        return Command.SINGLE_SUCCESS;
    }

    private int getPresetLocation(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");
        BroadcastMessage preset = ConfigurationManager.MESSAGE_PRESET_REGISTRY.get(ID);

        sendFeedback(commandContext, "<color:gray>" + ID + "<color:gold> is shown at the following location: <color:gray>" + preset.getBroadcastLocation().asString());

        return Command.SINGLE_SUCCESS;
    }

    private int setPresetLocation(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");
        BroadcastMessage preset = ConfigurationManager.MESSAGE_PRESET_REGISTRY.get(ID);
        BroadcastLocation location = BroadcastLocation.valueOf(StringArgumentType.getString(commandContext, "location").toUpperCase());

        preset.setBroadcastLocation(location);

        sendFeedback(commandContext, "<color:gray>" + ID + "<color:gold> is now shown at the following location: <color:gray>" + preset.getBroadcastLocation().asString());

        ConfigurationManager.saveConfig();

        return Command.SINGLE_SUCCESS;
    }

    private int getPresetType(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");
        BroadcastMessage preset = ConfigurationManager.MESSAGE_PRESET_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "id"));

        sendFeedback(commandContext, "<color:gray>" + ID + "<color:gold> uses the <color:gray>" + preset.getMessageType().getID() + "<color:gold> message type.");

        return Command.SINGLE_SUCCESS;
    }

    private int setPresetType(CommandContext<ServerCommandSource> commandContext) {
        Identifier ID = IdentifierArgumentType.getIdentifier(commandContext, "id");
        BroadcastMessage preset = ConfigurationManager.MESSAGE_PRESET_REGISTRY.get(ID);
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));

        preset.setMessageType(type);

        sendFeedback(commandContext, "<color:gray>" + ID + "<color:gold> now uses the <color:gray>" + type.getID() + "<color:gold> message type.");

        ConfigurationManager.saveConfig();

        return Command.SINGLE_SUCCESS;
    }

    private int executePresetBroadcast(CommandContext<ServerCommandSource> commandContext) {
        BroadcastMessage preset = ConfigurationManager.MESSAGE_PRESET_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "id"));
        preset.broadcast(commandContext.getSource().getServer(), commandContext.getSource());
        return Command.SINGLE_SUCCESS;
    }

    private int quickBroadcast(CommandContext<ServerCommandSource> commandContext) {
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        BroadcastMessage message = new BroadcastMessage(rawContents, new MessageType.SimpleBroadcastDefaultMessageType(), BroadcastLocation.CHAT);
        message.broadcast(commandContext.getSource().getServer(), commandContext.getSource());
        return Command.SINGLE_SUCCESS;
    }

    private int setMessageTypeLocation(CommandContext<ServerCommandSource> commandContext) {
        BroadcastLocation location = BroadcastLocation.valueOf(StringArgumentType.getString(commandContext, "location").toUpperCase());
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        type.setDefaultLocation(location);

        sendFeedback(commandContext, "<color:gray>" + type.getID() + "<r><color:gold> default display location is now:<r> <color:gray>" + location.asString());

        ConfigurationManager.saveConfig();
        return Command.SINGLE_SUCCESS;
    }

    private int getMessageTypeLocation(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));

        sendFeedback(commandContext, "<color:gray>" + type.getID() + "<r><color:gold> default display location is:<r> <color:gray>" + type.getDefaultLocation().asString());
        return Command.SINGLE_SUCCESS;
    }

    private int setMessageTypeSuffix(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String rawContents = StringArgumentType.getString(commandContext, "value");
        type.setSuffix(rawContents);

        sendFeedback(commandContext, "<color:gray>" + type.getID() + "<r><color:gold> now has the following suffix:<r> ");

        ConfigurationManager.saveConfig();
        return Command.SINGLE_SUCCESS;
    }

    private int getMessageTypeSuffix(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));

        if (!type.hasSuffix()) {
            sendFeedback(commandContext, "<color:gray>" + type.getID() + "<r><color:gold> does not have a suffix.<r>");
        } else {
            sendFeedback(commandContext, "<color:gray>" + type.getID() + "<r><color:gold> has the following suffix:<r> ");
        }

        return Command.SINGLE_SUCCESS;
    }

    private int setMessageTypePrefix(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String rawContents = StringArgumentType.getString(commandContext, "value");
        type.setPrefix(rawContents);

        sendFeedback(commandContext, "<color:gray>" + type.getID() + "<r><color:gold> now has the following prefix:<r> ");

        ConfigurationManager.saveConfig();
        return Command.SINGLE_SUCCESS;
    }

    private int getMessageTypePrefix(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));

        if (!type.hasPrefix()) {
            sendFeedback(commandContext, "<color:gray>" + type.getID() + "<r><color:gold> does not have a prefix.<r>");
        } else {
            sendFeedback(commandContext, "<color:gray>" + type.getID() + "<r><color:gold> has the following prefix:<r> ");
        }

        return Command.SINGLE_SUCCESS;
    }

    private int createMessageType(CommandContext<ServerCommandSource> commandContext) {
        Identifier id = IdentifierArgumentType.getIdentifier(commandContext, "id");
        sendFeedback(commandContext, "<color:gold>Created new broadcast message type: <color:gray>" + id);
        MessageType.CustomMessageType messageType = new MessageType.CustomMessageType(null, null, null);
        ConfigurationManager.MESSAGE_TYPE_REGISTRY.put(id, messageType);
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
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        BroadcastMessage message = new BroadcastMessage(rawContents, type, location);
        message.broadcast(commandContext.getSource().getServer(), commandContext.getSource());
        return Command.SINGLE_SUCCESS;
    }

    private int executeChatBroadcast(CommandContext<ServerCommandSource> commandContext) {
        MessageType type = ConfigurationManager.MESSAGE_TYPE_REGISTRY.get(IdentifierArgumentType.getIdentifier(commandContext, "type"));
        String rawContents = StringArgumentType.getString(commandContext, "contents");
        BroadcastMessage message = new BroadcastMessage(rawContents, type, type.getDefaultLocation());
        message.broadcast(commandContext.getSource().getServer(), commandContext.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
