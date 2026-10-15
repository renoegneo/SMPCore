package com.maxut.smpcore;

import com.maxut.smpcore.afk.AfkProvider;
import com.maxut.smpcore.afk.EssentialsAfkProvider;
import com.maxut.smpcore.afk.NoAfkProvider;
import com.maxut.smpcore.config.MessageService;
import com.maxut.smpcore.module.HourlyIncomeModule;
import com.maxut.smpcore.storage.PlayerDataStore;
import com.maxut.smpcore.storage.YamlPlayerDataStore;
import com.maxut.smpcore.command.SmpCoreCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmpCorePlugin extends JavaPlugin {

    private PlayerDataStore dataStore;
    private MessageService messageService;
    private HourlyIncomeModule hourlyIncomeModule;

    @Override
    public void onEnable() {
        // 1. Конфиги
        saveDefaultConfig();
        this.messageService = new MessageService(this);

        // 2. Хранилище
        this.dataStore = new YamlPlayerDataStore(this);
        this.dataStore.loadAll();

        // 3. AFK Провайдер
        String afkMode = getConfig().getString("afk.mode", "none");
        AfkProvider afkProvider;
        if ("essentials".equalsIgnoreCase(afkMode)) {
            afkProvider = new EssentialsAfkProvider();
        } else {
            afkProvider = new NoAfkProvider();
        }





        // 4. Модуль выплат
        this.hourlyIncomeModule = new HourlyIncomeModule(this, dataStore, afkProvider);
        this.hourlyIncomeModule.enable();

        // 5. Команды
        org.bukkit.command.PluginCommand command = getCommand("smpcore");
        if (command != null) {
            command.setExecutor(new SmpCoreCommand(this));
        }

        getLogger().info("SmpCore has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        if (hourlyIncomeModule != null) {
            hourlyIncomeModule.disable();
        }
        if (dataStore != null) {
            dataStore.saveAll();
        }

        getLogger().info("SmpCore has been disabled.");
    }

    public PlayerDataStore getDataStore() {
        return dataStore;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public HourlyIncomeModule getHourlyIncomeModule() {
        return hourlyIncomeModule;
    }
}