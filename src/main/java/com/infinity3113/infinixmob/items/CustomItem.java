package com.infinity3113.infinixmob.items;

import com.infinity3113.infinixmob.InfinixMob;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

public class CustomItem {

    private final InfinixMob plugin;
    private final String internalName;
    private final ConfigurationSection config;

    public static final NamespacedKey CUSTOM_TAG_KEY = new NamespacedKey(InfinixMob.getPlugin(), "custom_tag");
    public static final NamespacedKey RARITY_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_rarity");
    public static final NamespacedKey DAMAGE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_damage");
    public static final NamespacedKey ATTACK_SPEED_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_attack_speed");
    public static final NamespacedKey CRIT_CHANCE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_crit_chance");
    public static final NamespacedKey ELEMENTAL_DAMAGE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_elemental_damage");
    public static final NamespacedKey ITEM_TYPE_KEY = new NamespacedKey(InfinixMob.getPlugin(), "item_type");

    public CustomItem(InfinixMob plugin, String internalName, ConfigurationSection config) {
        this.plugin = plugin;
        this.internalName = internalName;
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

        String itemType = config.getString("type", "MISC");
        meta.getPersistentDataContainer().set(ITEM_TYPE_KEY, PersistentDataType.STRING, itemType);
        
        String rarityKey = config.getString("rarity", "COMMON");
        meta.getPersistentDataContainer().set(RARITY_KEY, PersistentDataType.STRING, rarityKey.toLowerCase());
        
        double damage = config.getDouble("damage", 1.0);
        meta.getPersistentDataContainer().set(DAMAGE_KEY, PersistentDataType.DOUBLE, damage);
        
        double attackSpeed = config.getDouble("attack-speed", 1.6);
        meta.getPersistentDataContainer().set(ATTACK_SPEED_KEY, PersistentDataType.DOUBLE, attackSpeed);
        
        double critChance = config.getDouble("crit-chance", 0.0);
        meta.getPersistentDataContainer().set(CRIT_CHANCE_KEY, PersistentDataType.DOUBLE, critChance);

        Map<String, Double> elementalDamageMap = new HashMap<>();
        if (config.isConfigurationSection("elemental-damage")) {
            ConfigurationSection elementalSection = config.getConfigurationSection("elemental-damage");
            for (String element : elementalSection.getKeys(false)) {
                if (elementsConfig.contains(element.toLowerCase())) {
                    elementalDamageMap.put(element, elementalSection.getDouble(element));
                } else {
                    plugin.getLogger().warning("Elemento elemental inválido en el item '" + internalName + "': " + element);
                }
            }
        }
        String elementalJson = plugin.getGson().toJson(elementalDamageMap);
        meta.getPersistentDataContainer().set(ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING, elementalJson);

        List<String> lore = new ArrayList<>();
        
        String rarityColor = raritiesConfig.getString(rarityKey.toLowerCase() + ".color", "&7");
        String rarityName = raritiesConfig.getString(rarityKey.toLowerCase() + ".display-name", "Común");
        String itemTypeName = statsConfig.getString("item-types." + itemType.toLowerCase(), itemType);

        if (loreFormatsConfig.isList("lore-formats.header")) {
            for (String line : loreFormatsConfig.getStringList("lore-formats.header")) {
                line = line.replace("{item_type_color}", "&f")
                           .replace("{item_type_name}", itemTypeName)
                           .replace("{rarity_color}", rarityColor)
                           .replace("{rarity_name}", rarityName);
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        
        if (loreFormatsConfig.isList("lore-formats.stats")) {
            for (String line : loreFormatsConfig.getStringList("lore-formats.stats")) {
                line = line.replace("{rarity_color}", rarityColor)
                           .replace("{rarity_display_name}", rarityName)
                           .replace("{item_type_name}", itemTypeName)
                           .replace("{damage}", String.format("%.1f", damage))
                           .replace("{attack_speed}", String.format("%.1f", attackSpeed))
                           .replace("{crit_chance}", String.format("%.1f", critChance * 100));
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        
        if (!elementalDamageMap.isEmpty() && loreFormatsConfig.isList("lore-formats.elemental_damage_title")) {
            for (String line : loreFormatsConfig.getStringList("lore-formats.elemental_damage_title")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            if (loreFormatsConfig.isList("lore-formats.elemental_damage_entry")) {
                 String elementFormat = loreFormatsConfig.getStringList("lore-formats.elemental_damage_entry").get(0);
                 for (Map.Entry<String, Double> entry : elementalDamageMap.entrySet()) {
                    String element = entry.getKey();
                    double elemDamage = entry.getValue();
                    String elementColor = elementsConfig.getString(element.toLowerCase(), "&f");
                    String elementName = ChatColor.stripColor(elementsConfig.getString(element.toLowerCase(), element));
                    String line = elementFormat.replace("{element_color}", elementColor)
                                               .replace("{element_name}", Character.toUpperCase(elementName.charAt(0)) + elementName.substring(1))
                                               .replace("{element_damage}", String.format("%.1f", elemDamage));
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        }

        if (loreFormatsConfig.isList("lore-formats.footer")) {
            for (String line : loreFormatsConfig.getStringList("lore-formats.footer")) {
                 String itemLore = config.getStringList("lore").stream()
                                         .map(l -> ChatColor.translateAlternateColorCodes('&', l))
                                         .collect(Collectors.joining("\n"));
                 line = line.replace("{lore_description}", itemLore);
                 lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        meta.setLore(lore);

        if (config.contains("enchantments")) {
            for (String enchantString : config.getStringList("enchantments")) {
                String[] parts = enchantString.split(":");
                Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                if (enchantment != null) {
                    int level = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    meta.addEnchant(enchantment, level, true);
                }
            }
        }

        if (config.contains("custom-tag")) {
            String tag = config.getString("custom-tag");
            NamespacedKey key = new NamespacedKey(plugin, tag);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");
        }

        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "infinix.damage", damage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "infinix.attackspeed", attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public static Map<String, Object> getCustomItemData(ItemStack itemStack) {
        Map<String, Object> data = new HashMap<>();
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return data;
        }

        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Gson gson = InfinixMob.getPlugin().getGson();

        if (container.has(RARITY_KEY, PersistentDataType.STRING)) {
            data.put("rarity", container.get(RARITY_KEY, PersistentDataType.STRING));
        }
        if (container.has(DAMAGE_KEY, PersistentDataType.DOUBLE)) {
            data.put("damage", container.get(DAMAGE_KEY, PersistentDataType.DOUBLE));
        }
        if (container.has(ATTACK_SPEED_KEY, PersistentDataType.DOUBLE)) {
            data.put("attack-speed", container.get(ATTACK_SPEED_KEY, PersistentDataType.DOUBLE));
        }
        if (container.has(CRIT_CHANCE_KEY, PersistentDataType.DOUBLE)) {
            data.put("crit-chance", container.get(CRIT_CHANCE_KEY, PersistentDataType.DOUBLE));
        }
        if (container.has(ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING)) {
            String json = container.get(ELEMENTAL_DAMAGE_KEY, PersistentDataType.STRING);
            Type type = new TypeToken<Map<String, Double>>(){}.getType();
            data.put("elemental-damage", gson.fromJson(json, type));
        }
        return data;
    }
}