package com.maxut.smpcore.module;

import com.maxut.smpcore.SmpCorePlugin;
import com.maxut.smpcore.afk.AfkProvider;
import com.maxut.smpcore.storage.PlayerDataStore;
import com.maxut.smpcore.timer.TimerService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class HourlyIncomeModule {

    private final SmpCorePlugin plugin;
    private final PlayerDataStore dataStore;
    private final AfkProvider afkProvider;
    private final TimerService timerService;
    private Economy economy;

    public HourlyIncomeModule(SmpCorePlugin plugin, PlayerDataStore dataStore, AfkProvider afkProvider) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        this.afkProvider = afkProvider;
        setupEconomy();
        this.timerService = new TimerService(plugin, dataStore, afkProvider, economy);
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found; income payouts are disabled. Install Vault and an economy provider such as EssentialsX.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Vault is installed, but no economy provider is registered; income payouts are disabled.");
            return;
        }
        economy = rsp.getProvider();
        plugin.getLogger().info("Economy provider connected: " + economy.getName());
    }

    public void enable() {
        timerService.start();
        plugin.getLogger().info("HourlyIncomeModule enabled successfully!");
    }

    public void disable() {
        timerService.stop();
        dataStore.saveAll();
        plugin.getLogger().info("HourlyIncomeModule disabled, data saved.");
    }

    public void reload() {
        timerService.start();
        plugin.getLogger().info("HourlyIncomeModule settings reloaded.");
    }
}