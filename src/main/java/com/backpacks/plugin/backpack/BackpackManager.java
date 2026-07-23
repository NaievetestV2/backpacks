package com.backpacks.plugin.backpack;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;

public class BackpackManager {

    private static final Map<UUID, BackpackData> backpacks = new HashMap<>();
    private static BackpackManager instance;
    private final File file;

    public BackpackManager(File dataFolder) {
        instance = this;
        this.file = new File(dataFolder, "backpacks.yml");
        load();
    }

    public static BackpackManager getInstance() {
        return instance;
    }

    public static Collection<BackpackData> getAllBackpacks() {
        return backpacks.values();
    }

    public BackpackData createBackpack(BackpackTier tier) {
        UUID id = UUID.randomUUID();
        BackpackData data = new BackpackData(id, tier);
        backpacks.put(id, data);
        save();
        return data;
    }

    public BackpackData getBackpack(UUID id) {
        return backpacks.get(id);
    }

    public void removeBackpack(UUID id) {
        backpacks.remove(id);
        save();
    }

    public void register(BackpackData data) {
        backpacks.put(data.id(), data);
    }

    public void save() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("backpacks", null);
            int count = 0;
            for (Map.Entry<UUID, BackpackData> entry : backpacks.entrySet()) {
                String path = "backpacks." + entry.getKey();
                config.set(path + ".tier", entry.getValue().tier().key());
                List<String> serialized = new ArrayList<>();
                for (ItemStack item : entry.getValue().items()) {
                    if (item != null && !item.getType().isAir()) {
                        try (ByteArrayOutputStream stream = new ByteArrayOutputStream(); BukkitObjectOutputStream out = new BukkitObjectOutputStream(stream)) {
                            out.writeObject(item);
                            serialized.add(Base64.getEncoder().encodeToString(stream.toByteArray()));
                        } catch (Exception e) {
                            serialized.add(null);
                        }
                    } else {
                        serialized.add(null);
                    }
                }
                config.set(path + ".items", serialized);
                count++;
            }
            config.set("count", count);
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (!file.exists()) return;
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            backpacks.clear();
            if (config.contains("backpacks")) {
                for (String key : config.getConfigurationSection("backpacks").getKeys(false)) {
                    try {
                        UUID id = UUID.fromString(key);
                        String tierKey = config.getString("backpacks." + key + ".tier");
                        BackpackTier tier = BackpackTier.fromKey(tierKey);
                        if (tier == null) continue;
                        BackpackData data = new BackpackData(id, tier);
                        List<String> serialized = config.getStringList("backpacks." + key + ".items");
                        List<ItemStack> items = data.items();
                        for (int i = 0; i < Math.min(serialized.size(), items.size()); i++) {
                            String encoded = serialized.get(i);
                            if (encoded == null || encoded.isEmpty()) continue;
                            try (ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(encoded)); BukkitObjectInputStream in = new BukkitObjectInputStream(stream)) {
                                items.set(i, (ItemStack) in.readObject());
                            } catch (Exception e) {
                                items.set(i, null);
                            }
                        }
                        backpacks.put(id, data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
