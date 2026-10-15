package com.maxut.smpcore.storage;

import java.util.UUID;

public interface PlayerDataStore {
    long getSeconds(UUID uuid);
    void addSeconds(UUID uuid, long seconds);
    void setSeconds(UUID uuid, long seconds);
    void saveAll();
    void loadAll();
}