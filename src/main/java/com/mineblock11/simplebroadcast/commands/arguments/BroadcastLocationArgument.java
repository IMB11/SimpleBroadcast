package com.mineblock11.simplebroadcast.commands.arguments;

import com.mineblock11.simplebroadcast.data.BroadcastLocation;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class BroadcastLocationArgument implements ArgumentType<BroadcastLocation> {
    public static BroadcastLocation get(final CommandContext<?> context, final String name) {
        return context.getArgument(name, BroadcastLocation.class);
    }

    @Override
    public BroadcastLocation parse(StringReader reader) throws CommandSyntaxException {
        return BroadcastLocation.valueOf(StringArgumentType.word().parse(reader).toUpperCase());
    }
}
