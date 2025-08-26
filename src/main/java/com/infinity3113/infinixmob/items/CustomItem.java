package com.infinity3113.infinixmob.items;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

public class CustomItem {

    private final InfinixMob plugin;
    private final String internalName;
    private final ConfigurationSection config;

    public CustomItem(InfinixMob plugin, String internalName, ConfigurationSection config) {
        this.plugin = plugin;
        this.internalName = internalName;
        this.config = config;
    }

    public ItemStack buildItemStack() {
        Material material = Material.matchMaterial(config.getString("id", "STONE"));
        ItemStack item = new ItemStack(material != null ? material : Material.STONE, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("display-name", internalName)));

        List<String> lore = config.getStringList("lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
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
        
        // --- ¡NUEVA LÓGICA! ---
        // Si el item tiene un custom-tag, se lo añadimos como dato persistente.
        if (config.contains("custom-tag")) {
            String tag = config.getString("custom-tag");
            NamespacedKey key = new NamespacedKey(plugin, tag);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}