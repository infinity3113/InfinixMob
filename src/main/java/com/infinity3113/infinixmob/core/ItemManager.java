package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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
        
        // CORRECCIÓN: Manejamos las excepciones de carga para evitar que el plugin se caiga
        try {
            File raritiesFile = new File(plugin.getDataFolder(), "items/rarities.yml");
            if (raritiesFile.exists()) {
                raritiesConfig = new YamlConfiguration();
                raritiesConfig.load(raritiesFile);
            } else {
                plugin.getLogger().warning("No se encontró el archivo de rarezas en items/rarities.yml.");
            }
            
            File elementsFile = new File(plugin.getDataFolder(), "items/elements.yml");
            if (elementsFile.exists()) {
                elementsConfig = new YamlConfiguration();
                elementsConfig.load(elementsFile);
            } else {
                plugin.getLogger().warning("No se encontró el archivo de elementos en items/elements.yml.");
            }
            
            File loreFormatsFile = new File(plugin.getDataFolder(), "items/lore-formats.yml");
            if (loreFormatsFile.exists()) {
                loreFormatsConfig = new YamlConfiguration();
                loreFormatsConfig.load(loreFormatsFile);
            } else {
                plugin.getLogger().warning("No se encontró el archivo de formatos de lore en items/lore-formats.yml.");
            }
            
            File statsFile = new File(plugin.getDataFolder(), "items/stats.yml");
            if (statsFile.exists()) {
                statsConfig = new YamlConfiguration();
                statsConfig.load(statsFile);
            } else {
                plugin.getLogger().warning("No se encontró el archivo de estadísticas en items/stats.yml.");
            }
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar archivos de configuración de ítems. Revisa el formato YAML.", e);
            return;
        }

        File itemsFolder = new File(plugin.getDataFolder(), "items");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
        }
        
        File[] itemTypeFolders = itemsFolder.listFiles(File::isDirectory);
        if (itemTypeFolders == null) return;
        
        for (File typeFolder : itemTypeFolders) {
            File[] itemFiles = typeFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (itemFiles == null) continue;
            
            for (File itemFile : itemFiles) {
                try {
                    FileConfiguration itemFileConfig = new YamlConfiguration();
                    itemFileConfig.load(itemFile);
                    for (String key : itemFileConfig.getKeys(false)) {
                        CustomItem item = new CustomItem(plugin, key, itemFileConfig.getConfigurationSection(key));
                        customItems.put(key.toLowerCase(), item);
                        plugin.getLogger().info("Cargado Item: " + key);
                    }
                } catch (IOException | InvalidConfigurationException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error al cargar el ítem '" + itemFile.getName() + "'. Revisa el formato YAML.", e);
                }
            }
        }
        
        File[] rootItemFiles = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("elements.yml") && !name.equals("rarities.yml") && !name.equals("lore-formats.yml") && !name.equals("stats.yml"));
        if (rootItemFiles == null) return;

        for (File itemFile : rootItemFiles) {
             try {
                FileConfiguration itemFileConfig = new YamlConfiguration();
                itemFileConfig.load(itemFile);
                for (String key : itemFileConfig.getKeys(false)) {
                    CustomItem item = new CustomItem(plugin, key, itemFileConfig.getConfigurationSection(key));
                    customItems.put(key.toLowerCase(), item);
                    plugin.getLogger().info("Cargado Item: " + key);
                }
            } catch (IOException | InvalidConfigurationException e) {
                 plugin.getLogger().log(Level.SEVERE, "Error al cargar el ítem '" + itemFile.getName() + "'. Revisa el formato YAML.", e);
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