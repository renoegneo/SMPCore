package com.maxut.smpcore.afk;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class EssentialsAfkProvider implements AfkProvider {

    private Essentials essentials;

    public EssentialsAfkProvider() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin != null) {
            this.essentials = (Essentials) (Object) plugin;
        }
    }

    @Override
    public boolean isAfk(Player player) {
        if (essentials == null) {
            return false; // If Essentials is not found, treat the player as not AFK
        }
        try {
            return essentials.getUser(player).isAfk();
        } catch (Exception e) {
            return false;
        }
    }
}