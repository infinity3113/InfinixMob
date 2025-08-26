package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ItemManager {

    private final InfinixMob plugin;
    private final Map<String, CustomItem> customItems = new HashMap<>();

    public ItemManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void loadItems() {
        customItems.clear();
        File itemsFolder = new File(plugin.getDataFolder(), "Items");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
        }
        
        plugin.saveResource("Items/SpawnerCore.yml", false);

        File[] listFiles = itemsFolder.listFiles();
        if (listFiles == null) return;
        
        for (File itemFile : listFiles) {
            if (itemFile.getName().endsWith(".yml")) {
                FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
                for (String key : itemConfig.getKeys(false)) {
                    CustomItem item = new CustomItem(plugin, key, itemConfig.getConfigurationSection(key));
                    customItems.put(key.toLowerCase(), item);
                    plugin.getLogger().info("Cargado Item: " + key);
                }
            }
        }
    }

    public Optional<CustomItem> getItem(String id) {
        return Optional.ofNullable(customItems.get(id.toLowerCase()));
    }

    // MÉTODO AÑADIDO PARA SOLUCIONAR EL ERROR
    public Set<String> getLoadedItemIds() {
        return customItems.keySet();
    }
}