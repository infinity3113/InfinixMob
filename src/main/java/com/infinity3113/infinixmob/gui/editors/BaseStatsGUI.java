package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseStatsGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final ItemBuilder builder;
    private final Inventory inventory;

    public BaseStatsGUI(InfinixMob plugin, Player player, ItemBuilder builder) {
        this.plugin = plugin;
        this.player = player;
        this.builder = builder;
        this.inventory = Bukkit.createInventory(this, 54, "Editor de Estadísticas Base");
        initializeItems();
    }

    private void initializeItems() {
        inventory.clear();
        Map<String, Double> baseStats = builder.getBaseStats();
        FileConfiguration statsConfig = plugin.getItemManager().getStatsConfig();
        int slot = 0;
        for (Map.Entry<String, Double> entry : baseStats.entrySet()) {
            if (slot >= 45) break;
            String statKey = entry.getKey();
            String statName = statsConfig.getString("display-names." + statKey.toLowerCase(), statKey);
            ItemStack statItem = createButton(Material.GOLD_INGOT, "&e" + statName, "&fValor actual: &a" + entry.getValue(), "", "&aClick Izquierdo para editar", "&cClick Derecho para eliminar");
            inventory.setItem(slot++, statItem);
        }

        inventory.setItem(45, createButton(Material.LIME_STAINED_GLASS_PANE, "&aAñadir Estadística", "&7Agrega un nuevo atributo base."));
        inventory.setItem(53, createButton(Material.RED_STAINED_GLASS_PANE, "&cVolver", "&7Regresa al editor principal."));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot == 53) {
            new ItemEditorGUI(plugin, player, builder).open();
            return;
        }

        if (slot == 45) { // Añadir
            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + "Escribe el ID de la estadística a añadir (ej: max-health):");
            plugin.getChatInputManager().requestInput(player, statKey -> {
                player.sendMessage(ChatColor.GOLD + "Ahora escribe el valor numérico:");
                plugin.getChatInputManager().requestInput(player, statValue -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                double value = Double.parseDouble(statValue);
                                builder.set("base-stats." + statKey, value);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Valor inválido.");
                            }
                            new BaseStatsGUI(plugin, player, builder).open();
                        }
                    }.runTask(plugin);
                });
            });
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GOLD_INGOT) {
            String statName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            String statKey = getKeyFromName(statName);

            if (event.getClick() == ClickType.RIGHT) { // Eliminar
                builder.set("base-stats." + statKey, null);
                new BaseStatsGUI(plugin, player, builder).open();
            } else { // Editar
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "Escribe el nuevo valor para '" + statName + "':");
                plugin.getChatInputManager().requestInput(player, input -> {
                     new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                double value = Double.parseDouble(input);
                                builder.set("base-stats." + statKey, value);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Valor inválido.");
                            }
                            new BaseStatsGUI(plugin, player, builder).open();
                        }
                    }.runTask(plugin);
                });
            }
        }
    }

    private String getKeyFromName(String name) {
        FileConfiguration statsConfig = plugin.getItemManager().getStatsConfig();
        ConfigurationSection section = statsConfig.getConfigurationSection("display-names");
        if (section == null) return name;
        for (String key : section.getKeys(false)) {
            if (ChatColor.stripColor(statsConfig.getString("display-names." + key)).equalsIgnoreCase(name)) {
                return key;
            }
        }
        return name.toLowerCase().replace(" ", "-");
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> coloredLore = Arrays.stream(lore)
                                         .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                                         .collect(Collectors.toList());
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}