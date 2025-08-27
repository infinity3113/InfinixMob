package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    private final InfinixMob plugin;
    private final String originalId;
    private String id;
    private YamlConfiguration config;

    // Constructor para un ítem existente
    public ItemBuilder(InfinixMob plugin, CustomItem customItem) {
        this.plugin = plugin;
        this.originalId = customItem.getId();
        this.id = customItem.getId();
        this.config = new YamlConfiguration();
        this.config.set(this.id, customItem.getConfig());
    }

    // Constructor para un ítem nuevo
    public ItemBuilder(InfinixMob plugin, String id, String type) {
        this.plugin = plugin;
        this.originalId = id;
        this.id = id;
        this.config = new YamlConfiguration();
        this.config.set(this.id + ".id", "STONE");
        this.config.set(this.id + ".display-name", "&f" + id);
        this.config.set(this.id + ".type", type.toUpperCase());
        this.config.set(this.id + ".rarity", "COMMON");
        this.config.set(this.id + ".revision-id", 1);
    }

    public ItemStack buildPreview() {
        ConfigurationSection itemSection = config.getConfigurationSection(this.id);
        if (itemSection == null) return null;
        CustomItem previewItem = new CustomItem(plugin, this.id, itemSection);
        return previewItem.buildItemStack();
    }

    public void set(String path, Object value) {
        config.set(this.id + "." + path, value);
    }

    public String getString(String path) {
        return config.getString(this.id + "." + path);
    }
    
    public double getDouble(String path) {
        return config.getDouble(this.id + "." + path);
    }

    public int getInt(String path) {
        return config.getInt(this.id + "." + path);
    }

    public List<String> getLore() {
        return config.getStringList(this.id + ".lore");
    }

    public void setLore(List<String> lore) {
        set("lore", lore);
    }

    public Map<String, Double> getElementalDamage() {
        Map<String, Double> map = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection(this.id + ".elemental-damage");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                map.put(key, section.getDouble(key));
            }
        }
        return map;
    }
    
    public Map<String, Double> getBaseStats() {
        Map<String, Double> map = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection(this.id + ".base-stats");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                map.put(key, section.getDouble(key));
            }
        }
        return map;
    }

    public void save() throws IOException {
        String itemType = getString("type").toLowerCase();
        File itemFile = new File(plugin.getDataFolder(), "items/" + itemType + ".yml");
        
        YamlConfiguration fileConfig = new YamlConfiguration();
        if(itemFile.exists()) {
            try {
                fileConfig.load(itemFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!this.id.equals(this.originalId)) {
            fileConfig.set(this.originalId, null);
        }
        
        fileConfig.set(this.id, this.config.getConfigurationSection(this.id));
        fileConfig.save(itemFile);
    }

    public String getId() {
        return id;
    }

    public void setId(String newId) {
        ConfigurationSection section = config.getConfigurationSection(this.id);
        config.set(this.id, null);
        this.id = newId;
        config.set(this.id, section);
    }
}