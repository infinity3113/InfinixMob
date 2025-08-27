package com.infinity3113.infinixmob.listeners;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemUpdaterListener implements Listener {

    private final InfinixMob plugin;

    public ItemUpdaterListener(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        boolean updated = false;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack oldItem = inventory.getItem(i);
            if (oldItem == null || oldItem.getItemMeta() == null) {
                continue;
            }

            PersistentDataContainer container = oldItem.getItemMeta().getPersistentDataContainer();
            if (!container.has(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING)) {
                continue;
            }

            String itemId = container.get(CustomItem.CUSTOM_TAG_KEY, PersistentDataType.STRING);
            Optional<CustomItem> customItemOpt = plugin.getItemManager().getItem(itemId);

            if (customItemOpt.isPresent()) {
                CustomItem customItem = customItemOpt.get();
                int currentRevision = customItem.getConfig().getInt("revision-id", 1);
                int itemRevision = container.getOrDefault(CustomItem.REVISION_ID_KEY, PersistentDataType.INTEGER, 1);

                if (itemRevision < currentRevision) {
                    ItemStack newItem = updateItem(oldItem, customItem);
                    inventory.setItem(i, newItem);
                    updated = true;
                }
            }
        }

        if (updated) {
            player.sendMessage(ChatColor.GOLD + "¡Algunos de tus ítems han sido actualizados a una nueva versión!");
        }
    }

    private ItemStack updateItem(ItemStack oldItem, CustomItem customItem) {
        ItemStack newItem = customItem.buildItemStack();
        newItem.setAmount(oldItem.getAmount());

        ConfigurationSection keepDataConfig = plugin.getConfig().getConfigurationSection("item-revision.keep-data");
        if (keepDataConfig == null) {
            return newItem; // No hay configuración, devolver el ítem nuevo tal cual
        }

        ItemMeta oldMeta = oldItem.getItemMeta();
        ItemMeta newMeta = newItem.getItemMeta();

        // Conservar encantamientos
        if (keepDataConfig.getBoolean("enchantments", true) && oldMeta.hasEnchants()) {
            for (Map.Entry<Enchantment, Integer> entry : oldMeta.getEnchants().entrySet()) {
                newMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        // Conservar nombre personalizado
        if (keepDataConfig.getBoolean("display-name", true) && oldMeta.hasDisplayName()) {
            // Comparamos con el nombre original para no sobreescribir si es el mismo
            String oldDisplayName = oldMeta.getDisplayName();
            String originalDisplayName = customItem.getConfig().getString("display-name");
            if(originalDisplayName != null && !oldDisplayName.equals(ChatColor.translateAlternateColorCodes('&', originalDisplayName))) {
                 newMeta.setDisplayName(oldDisplayName);
            }
        }

        // Conservar lore personalizado
        if (keepDataConfig.getBoolean("lore", false) && oldMeta.hasLore()) {
            List<String> newLore = newMeta.getLore() != null ? new ArrayList<>(newMeta.getLore()) : new ArrayList<>();
            String prefix = ChatColor.translateAlternateColorCodes('&', keepDataConfig.getString("kept-lore-prefix", "&7"));
            for (String line : oldMeta.getLore()) {
                if (!newLore.contains(line) && line.startsWith(prefix)) {
                    newLore.add(line);
                }
            }
            newMeta.setLore(newLore);
        }

        newItem.setItemMeta(newMeta);
        return newItem;
    }
}