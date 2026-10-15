package com.maxut.smpcore.config;

import com.maxut.smpcore.SmpCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class MessageService {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final SmpCorePlugin plugin;
    private FileConfiguration file;

    public MessageService(SmpCorePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File configFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!configFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            file = YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load messages.yml!");
            e.printStackTrace();
        }
    }

    public Component get(String key, TagResolver... placeholders) {
        if (file == null) {
            return MM.deserialize("<red>[config not loaded]</red>");
        }

        String raw = file.getString(key);
        if (raw == null) {
            return MM.deserialize("<red>[no message: " + key + "]</red>");
        }
        String prefix = file.getString("prefix", "");
        return MM.deserialize(prefix + raw, placeholders);
    }

    public Component getWithoutPrefix(String key, TagResolver... placeholders) {
        if (file == null) {
            return MM.deserialize("<red>[config not loaded]</red>");
        }

        String raw = file.getString(key);
        if (raw == null) {
            return MM.deserialize("<red>[no message: " + key + "]</red>");
        }
        return MM.deserialize(raw, placeholders);
    }

    public void send(CommandSender to, String key, TagResolver... placeholders) {
        to.sendMessage(get(key, placeholders));
    }
}