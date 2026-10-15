package com.maxut.smpcore.timer;

import com.maxut.smpcore.SmpCorePlugin;
import com.maxut.smpcore.config.MessageService;
import com.maxut.smpcore.afk.AfkProvider;
import com.maxut.smpcore.storage.PlayerDataStore;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class TimerService {

    private final SmpCorePlugin plugin;
    private final PlayerDataStore dataStore;
    private final AfkProvider afkProvider;
    private final Economy economy;
    private ScheduledTask task;
    private long lastTickTime;

    public TimerService(SmpCorePlugin plugin, PlayerDataStore dataStore, AfkProvider afkProvider, Economy economy) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        this.afkProvider = afkProvider;
        this.economy = economy;
    }

    public void start() {
        stop();
        long heartbeatSeconds = Math.max(1L, plugin.getConfig().getLong("timer.heartbeat-seconds", 5L));
        long periodTicks = heartbeatSeconds * 20L;

        lastTickTime = System.currentTimeMillis();

        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
            tick();
        }, 1L, periodTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();
        long maxDelta = Math.max(1L, plugin.getConfig().getLong("timer.max-delta-seconds", 15L));
        long elapsedMillis = now - lastTickTime;
        if (elapsedMillis < 0) {
            lastTickTime = now;
            return;
        }

        long deltaSeconds = elapsedMillis / 1000L;
        if (deltaSeconds > maxDelta) {
            deltaSeconds = maxDelta;
            lastTickTime = now;
        } else {
            lastTickTime += deltaSeconds * 1000L;
        }

        if (deltaSeconds <= 0) {
            return;
        }

        // 1. Проверяем, включен ли модуль выплат
        boolean incomeEnabled = plugin.getConfig().getBoolean("modules.income.enabled", true);
        if (!incomeEnabled) {
            return;
        }

        long payoutInterval = plugin.getConfig().contains("modules.income.interval-seconds")
                ? plugin.getConfig().getLong("modules.income.interval-seconds", 60L)
                : plugin.getConfig().getLong("modules.income.interval-minutes", 1L) * 60L;
        payoutInterval = Math.max(1L, payoutInterval);

        double rewardAmount = plugin.getConfig().getDouble("modules.income.amount", 10.0);
        String notifyType = plugin.getConfig().getString("modules.income.notify", "chat");
        List<String> disabledWorlds = plugin.getConfig().getStringList("modules.income.disabled-worlds");


        for (Player player : Bukkit.getOnlinePlayers()) {
            // Игнорируем игроков в отключенных мирах
            if (disabledWorlds.contains(player.getWorld().getName())) {
                continue;
            }

            // Игнорируем AFK игроков
            if (afkProvider.isAfk(player)) {
                continue;
            }

            UUID uuid = player.getUniqueId();
            dataStore.addSeconds(uuid, deltaSeconds);
            long totalSeconds = dataStore.getSeconds(uuid);

            while (totalSeconds >= payoutInterval && economy != null) {
                EconomyResponse response = economy.depositPlayer(player, rewardAmount);
                if (!response.transactionSuccess()) {
                    plugin.getLogger().warning("Could not pay income to " + player.getName() + ": " + response.errorMessage);
                    break;
                }
                dataStore.addSeconds(uuid, -payoutInterval);
                totalSeconds -= payoutInterval;

                // Отправляем уведомление по типу notify (chat | actionbar | none)
                String amountStr = String.valueOf((int) rewardAmount);
                MessageService messages = plugin.getMessageService();

                if ("chat".equalsIgnoreCase(notifyType)) {
                    messages.send(player, "income.received", Placeholder.parsed("amount", amountStr));
                } else if ("actionbar".equalsIgnoreCase(notifyType)) {
                    player.sendActionBar(messages.getWithoutPrefix("income.actionbar", Placeholder.parsed("amount", amountStr)));
                }
            }
        }
    }
}