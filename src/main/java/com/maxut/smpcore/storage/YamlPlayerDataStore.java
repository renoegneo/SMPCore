package com.maxut.smpcore.storage;

import com.maxut.smpcore.SmpCorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class YamlPlayerDataStore implements PlayerDataStore {

    private final SmpCorePlugin plugin;
    private final File file;
    private FileConfiguration config;

    private final Map<UUID, Long> cache = new HashMap<>();

    public YamlPlayerDataStore(SmpCorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
        loadAll();
    }

    @Override
    public long getSeconds(UUID uuid) {
        return cache.getOrDefault(uuid, 0L);
    }

    @Override
    public void addSeconds(UUID uuid, long seconds) {
        setSeconds(uuid, getSeconds(uuid) + seconds);
    }

    @Override
    public void setSeconds(UUID uuid, long seconds) {
        cache.put(uuid, seconds);
    }

    @Override
    public void loadAll() {
        if (!file.exists()) {
            config = new YamlConfiguration();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
        cache.clear();

        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long seconds = config.getLong(key);
                cache.put(uuid, seconds);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void saveAll() {
        if (config == null) {
            config = new YamlConfiguration();
        }

        for (Map.Entry<UUID, Long> entry : cache.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not save playerdata.yml!", e);
        }
    }
}