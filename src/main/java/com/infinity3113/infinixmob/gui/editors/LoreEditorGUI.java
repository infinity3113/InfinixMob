package com.infinity3113.infinixmob.gui.editors;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoreEditorGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final ItemBuilder builder;
    private final Inventory inventory;

    public LoreEditorGUI(InfinixMob plugin, Player player, ItemBuilder builder) {
        this.plugin = plugin;
        this.player = player;
        this.builder = builder;
        this.inventory = Bukkit.createInventory(this, 54, "Editor de Lore");
        initializeItems();
    }

    private void initializeItems() {
        inventory.clear();
        List<String> lore = builder.getLore();
        int slot = 0;
        for (String line : lore) {
            if (slot >= 45) break;
            ItemStack lineItem = createButton(Material.PAPER, "&f" + line, "&aClick Izquierdo para editar", "&cClick Derecho para eliminar");
            inventory.setItem(slot++, lineItem);
        }

        inventory.setItem(45, createButton(Material.LIME_STAINED_GLASS_PANE, "&aAñadir Línea", "&7Agrega una nueva línea al final."));
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
            player.sendMessage(ChatColor.GOLD + "Escribe la nueva línea de lore en el chat:");
            plugin.getChatInputManager().requestInput(player, input -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<String> currentLore = new ArrayList<>(builder.getLore());
                        currentLore.add(input);
                        builder.setLore(currentLore);
                        new LoreEditorGUI(plugin, player, builder).open();
                    }
                }.runTask(plugin);
            });
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
            int lineIndex = event.getSlot();
            List<String> currentLore = new ArrayList<>(builder.getLore());

            if (event.getClick() == ClickType.RIGHT) { // Eliminar
                currentLore.remove(lineIndex);
                builder.setLore(currentLore);
                new LoreEditorGUI(plugin, player, builder).open();
            } else { // Editar
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "Escribe el nuevo texto para esta línea:");
                 plugin.getChatInputManager().requestInput(player, input -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            currentLore.set(lineIndex, input);
                            builder.setLore(currentLore);
                            new LoreEditorGUI(plugin, player, builder).open();
                        }
                    }.runTask(plugin);
                });
            }
        }
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