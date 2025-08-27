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
import java.util.logging.Level;

public class ItemManager {

    private final InfinixMob plugin;
    private final Map<String, CustomItem> customItems = new HashMap<>();
    
    private FileConfiguration raritiesConfig;
    private FileConfiguration elementsConfig;
    private FileConfiguration loreFormatsConfig;
    private FileConfiguration statsConfig;

    public ItemManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void loadItems() {
        customItems.clear();
        
        // --- LÓGICA ACTUALIZADA PARA LA NUEVA ESTRUCTURA ---
        try {
            File raritiesFile = new File(plugin.getDataFolder(), "items/configurations/rarities.yml");
            if (raritiesFile.exists()) {
                raritiesConfig = YamlConfiguration.loadConfiguration(raritiesFile);
            }
            
            File elementsFile = new File(plugin.getDataFolder(), "items/configurations/elements.yml");
            if (elementsFile.exists()) {
                elementsConfig = YamlConfiguration.loadConfiguration(elementsFile);
            }
            
            File loreFormatsFile = new File(plugin.getDataFolder(), "items/configurations/lore-formats.yml");
            if (loreFormatsFile.exists()) {
                loreFormatsConfig = YamlConfiguration.loadConfiguration(loreFormatsFile);
            }
            
            File statsFile = new File(plugin.getDataFolder(), "items/configurations/stats.yml");
            if (statsFile.exists()) {
                statsConfig = YamlConfiguration.loadConfiguration(statsFile);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar archivos de configuración de ítems.", e);
            return;
        }

        // Carga los ítems de la carpeta 'misc'
        loadItemsFromDirectory(new File(plugin.getDataFolder(), "items/misc"));
        
        // Carga los ítems de la carpeta raíz 'items' (sword.yml, armor.yml, etc.)
        loadItemsFromDirectory(new File(plugin.getDataFolder(), "items"));
    }
    
    private void loadItemsFromDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] itemFiles = directory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (itemFiles == null) return;

        for (File itemFile : itemFiles) {
            try {
                FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
                for (String key : itemConfig.getKeys(false)) {
                    if (customItems.containsKey(key.toLowerCase())) {
                        plugin.getLogger().warning("Ítem duplicado encontrado: '" + key + "'. Saltando la carga desde " + itemFile.getName());
                        continue;
                    }
                    CustomItem item = new CustomItem(plugin, key, itemConfig.getConfigurationSection(key));
                    customItems.put(key.toLowerCase(), item);
                    plugin.getLogger().info("Cargado Item: " + key + " desde " + itemFile.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar el archivo de ítem '" + itemFile.getName() + "'. Revisa el formato YAML.", e);
            }
        }
    }

    public Optional<CustomItem> getItem(String id) {
        return Optional.ofNullable(customItems.get(id.toLowerCase()));
    }

    public Set<String> getLoadedItemIds() {
        return customItems.keySet();
    }
    
    public FileConfiguration getRaritiesConfig() {
        return raritiesConfig;
    }
    
    public FileConfiguration getElementsConfig() {
        return elementsConfig;
    }
    
    public FileConfiguration getLoreFormatsConfig() {
        return loreFormatsConfig;
    }
    
    public FileConfiguration getStatsConfig() {
        return statsConfig;
    }
}