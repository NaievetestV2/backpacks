package com.backpacks.plugin.backpack;

import java.util.*;

public class BackpackManager {

    private final Map<UUID, BackpackData> backpacks = new HashMap<>();

    public BackpackData createBackpack(BackpackTier tier) {
        UUID id = UUID.randomUUID();
        BackpackData data = new BackpackData(id, tier);
        backpacks.put(id, data);
        return data;
    }

    public BackpackData getBackpack(UUID id) {
        return backpacks.get(id);
    }

    public void removeBackpack(UUID id) {
        backpacks.remove(id);
    }
}
