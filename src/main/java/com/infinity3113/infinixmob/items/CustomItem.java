package com.infinity3113.infinixmob.items;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomItem {

    private final InfinixMob plugin;
    private final String id;
    private final ConfigurationSection config;

    public static final NamespacedKey CUSTOM_TAG_KEY = new NamespacedKey(InfinixMob.getPlugin(), "infinix_custom_item");
    public static final NamespacedKey REVISION_ID_KEY = new NamespacedKey(InfinixMob.getPlugin(), "revision_id");
    public static final NamespacedKey WEAKNESSES_KEY = new NamespacedKey(InfinixMob.getPlugin(), "elemental_weaknesses");
    
    public static final NamespacedKey RARITY_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_rarity");
    public static final NamespacedKey DAMAGE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_damage");
    public static final NamespacedKey ATTACK_SPEED_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_attack_speed");
    public static final NamespacedKey CRIT_CHANCE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_crit_chance");
    public static final NamespacedKey CRIT_DAMAGE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_crit_damage");
    public static final NamespacedKey ELEMENTAL_DAMAGE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_elemental_damage");
    public static final NamespacedKey ITEM_TYPE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_type");

    public CustomItem(InfinixMob plugin, String id, ConfigurationSection config) {
        this.plugin = plugin;
        this.id = id;
        this.config = config;
    }

    public ItemStack buildItemStack() {
        Material material = Material.matchMaterial(config.getString("id", "STONE"));
        ItemStack item = new ItemStack(material != null ? material : Material.STONE, 1);
        ItemMeta meta = item.getItemMeta();
        
        FileConfiguration raritiesConfig = plugin.getItemManager().getRaritiesConfig();
        FileConfiguration elementsConfig = plugin.getItemManager().getElementsConfig();
        FileConfiguration loreFormatsConfig = plugin.getItemManager().getLoreFormatsConfig();
        FileConfiguration statsConfig = plugin.getItemManager().getStatsConfig();

        // --- GUARDAR DATOS EN EL ITEM ---
        meta.getPersistentDataContainer().set(CUSTOM_TAG_KEY, PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(REVISION_ID_KEY, PersistentDataType.INTEGER, config.getInt("revision-id", 1));
        
        String itemType = config.getString("type", "MISC");
        meta.getPersistentDataContainer().set(ITEM_TYPE_KEY, PersistentDataType.STRING, itemType);
        
        String rarityKey = config.getString("rarity", "COMMON");
        meta.getPersistentDataContainer().set(RARITY_KEY, PersistentDataType.STRING, rarityKey.toLowerCase());
        
        if (config.contains("damage")) meta.getPersistentDataContainer().set(DAMAGE_KEY, PersistentDataType.DOUBLE, config.getDouble("damage"));
        if (config.contains("attack-speed")) meta.getPersistentDataContainer().set(ATTACK_SPEED_KEY, PersistentDataType.DOUBLE, config.getDouble("attack-speed"));
        if (config.contains("crit-chance")) meta.getPersistentDataContainer().set(CRIT_CHANCE_KEY, PersistentDataType.DOUBLE, config.getDouble("crit-chance"));
        if (config.contains("crit-damage")) meta.getPersistentDataContainer().set(CRIT_DAMAGE_KEY, PersistentDataType.DOUBLE, config.getDouble("crit-damage"));

        Map<String, Double> elementalDamageMap = new HashMap<>();
        if (config.isConfigurationSection("elemental-damage")) {
            ConfigurationSection elementalSection = config.getConfigurationSection("elemental-damage");
            for (String element : elementalSection.getKeys(false)) {
                elementalDamageMap.put(element, elementalSection.getDouble(element));
            }
        }
        String elementalJson = plugin.getGson().toJson(elementalDamageMap);
        meta.getPersistentDataContainer().set(ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING, elementalJson);

        // --- CONSTRUIR LORE DINÁMICO ---
        List<String> lore = new ArrayList<>();
        String rarityColor = raritiesConfig.getString(rarityKey.toLowerCase() + ".color", "&7");
        String rarityName = raritiesConfig.getString(rarityKey.toLowerCase() + ".display-name", "Común");
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rarityColor + config.getString("display-name")));

        List<String> format = loreFormatsConfig.getStringList("format");
        ConfigurationSection entries = loreFormatsConfig.getConfigurationSection("entries");

        for (String placeholder : format) {
            switch (placeholder.toLowerCase()) {
                case "#item-type#":
                    String itemTypeName = statsConfig.getString("item-types." + itemType.toUpperCase(), itemType);
                    lore.add(format(entries.getString("item-type"), "{value}", itemTypeName));
                    break;
                case "#damage#":
                    if (config.contains("damage")) {
                        String name = statsConfig.getString("display-names.damage", "Damage");
                        String value = String.format("%.1f", config.getDouble("damage"));
                        lore.add(format(entries.getString("damage"), "{display_name}", name, "{value}", value));
                    }
                    break;
                case "#attack-speed#":
                     if (config.contains("attack-speed")) {
                        String name = statsConfig.getString("display-names.attack-speed", "Attack Speed");
                        String value = String.format("%.1f", config.getDouble("attack-speed"));
                        lore.add(format(entries.getString("attack-speed"), "{display_name}", name, "{value}", value));
                    }
                    break;
                case "#crit-chance#":
                    if (config.contains("crit-chance")) {
                        String name = statsConfig.getString("display-names.crit-chance", "Crit Chance");
                        String value = String.format("%.1f", config.getDouble("crit-chance"));
                        lore.add(format(entries.getString("crit-chance"), "{display_name}", name, "{value}", value));
                    }
                    break;
                case "#crit-damage#":
                    if (config.contains("crit-damage")) {
                        String name = statsConfig.getString("display-names.crit-damage", "Crit Damage");
                        String value = String.format("%.1f", config.getDouble("crit-damage"));
                        lore.add(format(entries.getString("crit-damage"), "{display_name}", name, "{value}", value));
                    }
                    break;
                case "#elements#":
                    if (!elementalDamageMap.isEmpty()) {
                        lore.add(format(entries.getString("elements_title")));
                        for (Map.Entry<String, Double> entry : elementalDamageMap.entrySet()) {
                            String name = elementsConfig.getString(entry.getKey().toLowerCase(), entry.getKey());
                            String value = String.format("%.1f", entry.getValue());
                            lore.add(format(entries.getString("element_entry"), "{display_name}", name, "{value}", value));
                        }
                    }
                    break;
                case "#base-stats#":
                    if (config.isConfigurationSection("base-stats")) {
                        lore.add(format(entries.getString("base_stats_title")));
                        ConfigurationSection baseStats = config.getConfigurationSection("base-stats");
                        for (String key : baseStats.getKeys(false)) {
                            String name = statsConfig.getString("display-names." + key.toLowerCase(), key);
                            String value = String.valueOf(baseStats.getDouble(key));
                            lore.add(format(entries.getString("base_stat_entry"), "{display_name}", name, "{value}", value));
                        }
                    }
                    break;
                case "#lore#":
                    if (config.isList("lore")) {
                        for (String line : config.getStringList("lore")) {
                            lore.add(format(entries.getString("lore_entry"), "{value}", line));
                        }
                    }
                    break;
                case "#rarity#":
                    lore.add(format(entries.getString("rarity"), "{color}", rarityColor, "{value}", rarityName));
                    break;
                case "{bar}":
                    lore.add(format(entries.getString("bar")));
                    break;
            }
        }

        meta.setLore(lore);

        // --- APLICAR ATRIBUTOS Y FLAGS ---
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        
        double attackSpeed = config.getDouble("attack-speed", 0);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "infinix.damage", -0.99, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlot.HAND));
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "infinix.attackspeed", attackSpeed > 0 ? attackSpeed - 4 : -3.9, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        if (config.isConfigurationSection("base-stats")) {
            ConfigurationSection baseStatsSection = config.getConfigurationSection("base-stats");
            for (String statKey : baseStatsSection.getKeys(false)) {
                Attribute attribute = getAttributeFromString(statKey);
                if (attribute != null) {
                    double statValue = baseStatsSection.getDouble(statKey);
                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "infinix." + statKey, statValue, AttributeModifier.Operation.ADD_NUMBER, getEquipmentSlot(itemType, material));
                    meta.addAttributeModifier(attribute, modifier);
                }
            }
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private String format(String text, String... replacements) {
        if (text == null) return "";
        for (int i = 0; i < replacements.length; i += 2) {
            String key = replacements[i];
            String value = replacements[i+1];
            if (value == null) {
                value = ""; 
            }
            text = text.replace(key, value);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private Attribute getAttributeFromString(String key) {
        switch (key.toLowerCase()) {
            case "max-health": return Attribute.GENERIC_MAX_HEALTH;
            case "armor": return Attribute.GENERIC_ARMOR;
            case "armor-toughness": return Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "knockback-resistance": return Attribute.GENERIC_KNOCKBACK_RESISTANCE;
            case "movement-speed": return Attribute.GENERIC_MOVEMENT_SPEED;
            default: return null;
        }
    }

    private EquipmentSlot getEquipmentSlot(String itemType, Material material) {
        String type = itemType.toUpperCase();
        if (type.equals("ARMOR")) {
            if (material == null) return EquipmentSlot.HAND;
            String materialName = material.name();
            if (materialName.endsWith("_HELMET")) return EquipmentSlot.HEAD;
            if (materialName.endsWith("_CHESTPLATE")) return EquipmentSlot.CHEST;
            if (materialName.endsWith("_LEGGINGS")) return EquipmentSlot.LEGS;
            if (materialName.endsWith("_BOOTS")) return EquipmentSlot.FEET;
        }
        return EquipmentSlot.HAND;
    }

    public String getId() {
        return id;
    }

    public ConfigurationSection getConfig() {
        return config;
    }
}