package com.infinity3113.infinixmob.items;

import com.google.gson.Gson;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomItem {

    private final InfinixMob plugin;
    private final String id;
    private final ConfigurationSection config;
    private final Gson gson = new Gson();

    public static final NamespacedKey CUSTOM_TAG_KEY = new NamespacedKey(InfinixMob.getPlugin(), "infinix_custom_item");
    public static final NamespacedKey REVISION_ID_KEY = new NamespacedKey(InfinixMob.getPlugin(), "revision_id");
    public static final NamespacedKey ITEM_TYPE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_type");
    public static final NamespacedKey RARITY_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_rarity");
    public static final NamespacedKey STATS_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_stats");
    public static final NamespacedKey ELEMENTAL_DAMAGE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_elemental_damage");
    public static final NamespacedKey WEAKNESSES_KEY = new NamespacedKey(InfinixMob.getPlugin(), "elemental_weaknesses");
    // --- NUEVA LÍNEA ---
    public static final NamespacedKey SKILL_MODIFIERS_KEY = new NamespacedKey(InfinixMob.getPlugin(), "skill_modifiers");


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
        
        Map<String, Double> allStats = new HashMap<>();
        if (config.isConfigurationSection("stats")) {
            ConfigurationSection statsSection = config.getConfigurationSection("stats");
            for (String key : statsSection.getKeys(false)) {
                allStats.put(key.toLowerCase(), statsSection.getDouble(key));
            }
        }
        meta.getPersistentDataContainer().set(STATS_KEY, PersistentDataType.STRING, gson.toJson(allStats));

        Map<String, Double> elementalDamageMap = new HashMap<>();
        if (config.isConfigurationSection("elemental-damage")) {
            ConfigurationSection elementalSection = config.getConfigurationSection("elemental-damage");
            for (String element : elementalSection.getKeys(false)) {
                elementalDamageMap.put(element.toLowerCase(), elementalSection.getDouble(element));
            }
        }
        meta.getPersistentDataContainer().set(ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING, gson.toJson(elementalDamageMap));
        
        // --- NUEVA LÓGICA PARA SKILL MODIFIERS ---
        Map<String, Double> skillModifiersMap = new HashMap<>();
        if (config.isConfigurationSection("skill-modifiers")) {
            ConfigurationSection skillModifiersSection = config.getConfigurationSection("skill-modifiers");
            for (String skillId : skillModifiersSection.getKeys(false)) {
                skillModifiersMap.put(skillId.toLowerCase(), skillModifiersSection.getDouble(skillId));
            }
        }
        meta.getPersistentDataContainer().set(SKILL_MODIFIERS_KEY, PersistentDataType.STRING, gson.toJson(skillModifiersMap));
        
        String rarityColor = raritiesConfig.getString(rarityKey.toLowerCase() + ".color", "&7");
        String rarityName = raritiesConfig.getString(rarityKey.toLowerCase() + ".display-name", "Común");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rarityColor + config.getString("display-name")));
        
        // --- CONSTRUIR LORE DINÁMICO ---
        List<String> rawLore = new ArrayList<>();
        List<String> statsLines = new ArrayList<>();
        List<String> elementsLines = new ArrayList<>();
        List<String> skillModifiersLines = new ArrayList<>(); // <-- NUEVA LÍNEA
        List<String> handStyleLines = new ArrayList<>();
        List<String> customLoreLines = new ArrayList<>();
        String rarityLine = null;

        ConfigurationSection entries = loreFormatsConfig.getConfigurationSection("entries");

        if (!allStats.isEmpty()) {
            statsLines.add(format(entries.getString("stats_title")));
            for (Map.Entry<String, Double> entry : allStats.entrySet()) {
                String statKey = entry.getKey();
                String statName = statsConfig.getString("display-names." + statKey, statKey);
                String value = String.format("%.2f", entry.getValue());
                String statFormat = entries.getString("stat_entry." + statKey, entries.getString("stat_entry.default"));
                statsLines.add(format(statFormat, "{display_name}", statName, "{value}", value));
            }
        }

        if (!elementalDamageMap.isEmpty()) {
            elementsLines.add(format(entries.getString("elements_title")));
            for (Map.Entry<String, Double> entry : elementalDamageMap.entrySet()) {
                String name = elementsConfig.getString(entry.getKey().toLowerCase(), entry.getKey());
                String value = String.format("%.1f", entry.getValue());
                elementsLines.add(format(entries.getString("element_entry"), "{display_name}", name, "{value}", value));
            }
        }
        
        // --- NUEVA LÓGICA PARA AÑADIR SKILL MODIFIERS AL LORE ---
        if (!skillModifiersMap.isEmpty()) {
            skillModifiersLines.add(format(entries.getString("skill_modifiers_title")));
            for (Map.Entry<String, Double> entry : skillModifiersMap.entrySet()) {
                // Obtener el nombre de la habilidad desde el SkillManager para mostrarlo bonito
                String skillName = plugin.getSkillManager().getSkillConfig(entry.getKey())
                        .map(cfg -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', cfg.getString("display-name", entry.getKey()))))
                        .orElse(entry.getKey());
                String value = String.format("%.1f", entry.getValue());
                skillModifiersLines.add(format(entries.getString("skill_modifier_entry"), "{display_name}", skillName, "{value}", value));
            }
        }

        String handStyle = config.getString("hand-style", null);
        if (handStyle != null) {
            String handStyleName = handStyle.equalsIgnoreCase("ONE_HANDED") ? "Una Mano" : "Dos Manos";
            handStyleLines.add(format(entries.getString("hand_style"), "{value}", handStyleName));
        }

        if (config.isList("lore")) {
            for (String line : config.getStringList("lore")) {
                customLoreLines.add(format(entries.getString("lore_entry"), "{value}", line));
            }
        }

        if (raritiesConfig != null) {
            rarityLine = format(entries.getString("rarity"), "{color}", rarityColor, "{value}", rarityName);
        }

        List<List<String>> sections = new ArrayList<>();
        if (!statsLines.isEmpty()) sections.add(statsLines);
        if (!elementsLines.isEmpty()) sections.add(elementsLines);
        if (!skillModifiersLines.isEmpty()) sections.add(skillModifiersLines); // <-- AÑADIR SECCIÓN
        if (!handStyleLines.isEmpty()) sections.add(handStyleLines);
        if (!customLoreLines.isEmpty()) sections.add(customLoreLines);
        if (rarityLine != null) sections.add(Collections.singletonList(rarityLine));

        for (int i = 0; i < sections.size(); i++) {
            rawLore.addAll(sections.get(i));
            if (i < sections.size() - 1 && !sections.get(i).isEmpty()) {
                rawLore.add(format(entries.getString("bar")));
            }
        }

        meta.setLore(rawLore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

        for(Map.Entry<String, Double> stat : allStats.entrySet()){
            Attribute attribute = getAttributeFromString(stat.getKey());
            if (attribute != null) {
                double value = stat.getValue();
                if (attribute == Attribute.GENERIC_MAX_HEALTH) {
                    value *= 2;
                }
                AttributeModifier modifier = new AttributeModifier(
                    UUID.randomUUID(), 
                    "infinix." + stat.getKey(), 
                    value, 
                    AttributeModifier.Operation.ADD_NUMBER,
                    getEquipmentSlot(itemType, material)
                );
                meta.addAttributeModifier(attribute, modifier);
            }
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private String format(String text, String... replacements) {
        if (text == null) return "";
        for (int i = 0; i < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i+1]);
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
            case "damage": return Attribute.GENERIC_ATTACK_DAMAGE;
            case "attack-speed": return Attribute.GENERIC_ATTACK_SPEED;
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