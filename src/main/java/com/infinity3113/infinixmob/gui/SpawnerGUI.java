package com.infinity3113.infinixmob.gui;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpawnerGUI implements InventoryHolder {

    private final InfinixMob plugin;
    private final Player player;
    private final Block spawnerBlock;
    private final Inventory inventory;

    public SpawnerGUI(InfinixMob plugin, Player player, Block spawnerBlock) {
        this.plugin = plugin;
        this.player = player;
        this.spawnerBlock = spawnerBlock;
        this.inventory = Bukkit.createInventory(this, 54, "Configurar Spawner");
        initializeItems();
    }

    private void initializeItems() {
        Map<String, String> data = plugin.getSpawnerManager().getSpawnerData(spawnerBlock);
        
        inventory.setItem(0, createControlItem(Material.CLOCK, "Intervalo", data.get("interval") + " segundos"));
        inventory.setItem(1, createControlItem(Material.BEACON, "Rango de Activación", data.get("activationRange") + " bloques"));
        inventory.setItem(2, createControlItem(Material.IRON_BARS, "Máximo de Mobs", data.get("maxMobs")));
        inventory.setItem(3, createControlItem(Material.STRING, "Radio de Aparición", data.get("radius") + " bloques"));
        inventory.setItem(5, createControlItem(Material.NAME_TAG, "Nombre del Spawner", data.getOrDefault("name", "Sin Nombre")));
        inventory.setItem(6, createControlItem(Material.BOOK, "Título de Zona", "Click para editar"));
        inventory.setItem(8, createControlItem(Material.LIME_STAINED_GLASS_PANE, "Añadir Mob", "Click para seleccionar"));

        Map<String, Integer> mobTypes = plugin.getSpawnerManager().deserializeMobTypes(data.get("mobTypes"));
        
        final int[] slot = {18};
        mobTypes.forEach((mobId, weight) -> {
            if (slot[0] >= 54) return;
            plugin.getMobManager().getMob(mobId).ifPresent(customMob -> {
                ItemStack mobItem = createMobItem(customMob.getInternalName(), weight);
                inventory.setItem(slot[0]++, mobItem);
            });
        });
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (slot >= 0 && slot <= 6) { 
            player.closeInventory();
            String key = "";
            switch(slot) {
                case 0: key = "interval"; break;
                case 1: key = "activationRange"; break;
                case 2: key = "maxMobs"; break;
                case 3: key = "radius"; break;
                case 5: key = "name"; break;
                case 6: key = "title"; break;
            }
            promptForInput(key);
        } else if (slot == 8) {
            new MobSelectionGUI(plugin, player, spawnerBlock).open();
        } else if (slot >= 18) {
            Map<String, String> data = plugin.getSpawnerManager().getSpawnerData(spawnerBlock);
            Map<String, Integer> mobTypes = plugin.getSpawnerManager().deserializeMobTypes(data.get("mobTypes"));
            
            String mobId = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (event.isLeftClick()) {
                mobTypes.put(mobId, mobTypes.getOrDefault(mobId, 0) + 1);
            } else if (event.isRightClick()) {
                mobTypes.put(mobId, Math.max(0, mobTypes.getOrDefault(mobId, 0) - 1));
            }
            if (event.getClick().isShiftClick()) {
                mobTypes.remove(mobId);
            }
            
            data.put("mobTypes", plugin.getSpawnerManager().serializeMobTypes(mobTypes));
            plugin.getSpawnerManager().saveSpawnerData(spawnerBlock, data);
            
            new SpawnerGUI(plugin, player, spawnerBlock).open();
        }
    }

    private void promptForInput(String key) {
        player.sendMessage(ChatColor.GOLD + "Escribe el nuevo valor para '" + key + "' en el chat. Escribe 'cancelar' para abortar.");
        
        plugin.getChatInputManager().requestInput(player, input -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (input.equalsIgnoreCase("cancelar")) {
                        player.sendMessage(ChatColor.RED + "Operación cancelada.");
                        new SpawnerGUI(plugin, player, spawnerBlock).open();
                        return;
                    }
                    
                    if (key.equals("title")) {
                        player.sendMessage(ChatColor.GOLD + "Ahora escribe el subtítulo (o 'ninguno').");
                        plugin.getChatInputManager().requestInput(player, subtitle -> {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Map<String, String> data = plugin.getSpawnerManager().getSpawnerData(spawnerBlock);
                                    data.put("title", input);
                                    data.put("subtitle", subtitle.equalsIgnoreCase("ninguno") ? "" : subtitle);
                                    plugin.getSpawnerManager().saveSpawnerData(spawnerBlock, data);
                                    
                                    player.sendMessage(ChatColor.GREEN + "¡Título y subtítulo actualizados!");
                                    new SpawnerGUI(plugin, player, spawnerBlock).open();
                                }
                            }.runTask(plugin);
                        });
                    } else {
                        Map<String, String> data = plugin.getSpawnerManager().getSpawnerData(spawnerBlock);
                        data.put(key, input);
                        plugin.getSpawnerManager().saveSpawnerData(spawnerBlock, data);
                        player.sendMessage(ChatColor.GREEN + "¡Valor de '" + key + "' actualizado a '" + input + "'!");
                        new SpawnerGUI(plugin, player, spawnerBlock).open();
                    }
                }
            }.runTask(plugin);
        });
    }

    private ItemStack createControlItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMobItem(String mobId, int weight) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + mobId);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Peso de Aparición: " + weight);
        lore.add("");
        lore.add(ChatColor.GREEN + "Click Izquierdo: +1 Peso");
        lore.add(ChatColor.RED + "Click Derecho: -1 Peso");
        lore.add(ChatColor.DARK_RED + "Shift+Click: Eliminar");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}