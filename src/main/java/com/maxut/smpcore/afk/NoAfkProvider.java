package com.maxut.smpcore.afk;

import org.bukkit.entity.Player;

public final class NoAfkProvider implements AfkProvider {
    @Override
    public boolean isAfk(Player player) {
        return false; // Nobody is ever AFK
    }
}