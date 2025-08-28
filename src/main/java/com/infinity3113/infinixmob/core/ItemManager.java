package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.configuration.ConfigurationSection;
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
    // NUEVO: Mapa para rastrear el archivo de origen de cada ítem
    private final Map<String, File> itemSourceFiles = new HashMap<>();

    private FileConfiguration raritiesConfig;
    private FileConfiguration elementsConfig;
    private FileConfiguration loreFormatsConfig;
    private FileConfiguration statsConfig;

    public ItemManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void loadItems() {
        customItems.clear();
        itemSourceFiles.clear(); // Limpiar el rastreador de archivos

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

        loadItemsFromDirectory(new File(plugin.getDataFolder(), "items/misc"));
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
                    // NUEVO: Guardar el archivo de origen del ítem
                    itemSourceFiles.put(key.toLowerCase(), itemFile);
                    plugin.getLogger().info("Cargado Item: " + key + " desde " + itemFile.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar el archivo de ítem '" + itemFile.getName() + "'. Revisa el formato YAML.", e);
            }
        }
    }

    // --- NUEVO MÉTODO PARA GUARDAR ÍTEMS ---
    public void saveItem(CustomItem itemToSave) {
        String itemId = itemToSave.getId().toLowerCase();
        File itemFile = itemSourceFiles.get(itemId);
        if (itemFile == null) {
            // Si el ítem es nuevo, crea un nuevo archivo para él.
            itemFile = new File(plugin.getDataFolder(), "items/" + itemToSave.getId() + ".yml");
            itemSourceFiles.put(itemId, itemFile);
        }

        FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(itemFile);
        // Usamos el ID original (con mayúsculas/minúsculas) como la clave principal en el YML
        itemConfig.set(itemToSave.getId(), itemToSave.getConfig());

        try {
            itemConfig.save(itemFile);
            // Recargar el ítem en memoria para reflejar los cambios
            customItems.put(itemId, new CustomItem(plugin, itemToSave.getId(), itemToSave.getConfig()));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar el ítem " + itemToSave.getId() + " en el archivo " + itemFile.getName(), e);
        }
    }


    public Optional<CustomItem> getItem(String id) {
        return Optional.ofNullable(customItems.get(id.toLowerCase()));
    }

    public Set<String> getLoadedItemIds() {
        return customItems.keySet();
    }

    public FileConfiguration getRaritiesConfig() { return raritiesConfig; }
    public FileConfiguration getElementsConfig() { return elementsConfig; }
    public FileConfiguration getLoreFormatsConfig() { return loreFormatsConfig; }
    public FileConfiguration getStatsConfig() { return statsConfig; }
}