package com.maxut.smpcore.command;

import com.maxut.smpcore.SmpCorePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SmpCoreCommand implements CommandExecutor {

    private final SmpCorePlugin plugin;

    public SmpCoreCommand(SmpCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Проверка права smpcore.admin
        if (!sender.hasPermission("smpcore.admin")) {
            plugin.getMessageService().send(sender, "errors.no-permission");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getMessageService().reload();
            plugin.getHourlyIncomeModule().reload();

            plugin.getMessageService().send(sender, "admin.reloaded");
            return true;
        }

        // Если просто написали /smpcore без аргументов
        sender.sendMessage("§e[SMP] Usage: /smpcore reload");
        return true;
    }
}