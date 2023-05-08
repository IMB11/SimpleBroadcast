package com.mineblock11.simplebroadcast.data;

import net.minecraft.util.StringIdentifiable;

public enum BroadcastLocation implements StringIdentifiable {
    ACTIONBAR,
    CHAT,
    TITLE;

    @Override
    public String asString() {
        return this.name().toLowerCase();
    }
}
