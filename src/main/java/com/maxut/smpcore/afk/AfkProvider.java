package com.maxut.smpcore.afk;

import org.bukkit.entity.Player;

public interface AfkProvider {
    boolean isAfk(Player player);
}