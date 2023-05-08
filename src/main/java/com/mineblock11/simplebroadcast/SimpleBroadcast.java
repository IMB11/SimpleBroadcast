package com.mineblock11.simplebroadcast;

import com.mineblock11.simplebroadcast.commands.SimpleBroadcastCommands;
import com.mineblock11.simplebroadcast.data.ConfigurationManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class SimpleBroadcast implements ModInitializer {
    public SimpleBroadcastCommands commands = new SimpleBroadcastCommands();

    @Override
    public void onInitialize() {
//        ArgumentTypeRegistry.registerArgumentType(new Identifier("simplebroadcast", "message_type"), MessageTypeArgument.class, ConstantArgumentSerializer.of(MessageTypeArgument::new));
//        ArgumentTypeRegistry.registerArgumentType(new Identifier("simplebroadcast", "broadcast_location"), BroadcastLocationArgument.class, ConstantArgumentSerializer.of(BroadcastLocationArgument::new));

        ConfigurationManager.loadConfig();

        CommandRegistrationCallback.EVENT.register(commands::registerCommands);
    }
}
