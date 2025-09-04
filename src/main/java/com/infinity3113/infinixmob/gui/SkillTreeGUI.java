package com.infinity3113.infinixmob.gui;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.editor.MenuGUI;
import com.infinity3113.infinixmob.playerclass.PlayerClass;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.SkillValueCalculator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkillTreeGUI extends MenuGUI {

    private final PlayerData playerData;
    private final PlayerClass playerClass;

    public SkillTreeGUI(InfinixMob plugin, Player player) {
        super(plugin, player);
        this.playerData = plugin.getPlayerClassManager().getPlayerData(player);
        this.playerClass = playerData.getPlayerClass();
    }

    @Override
    public String getMenuName() {
        return playerClass.getDisplayName() + " - Puntos: " + playerData.getSkillPoints();
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE || clickedItem.getType() == Material.AIR) {
            return;
        }

        String skillId = clickedItem.getItemMeta().getPersistentDataContainer().get(GUI_ITEM_KEY, PersistentDataType.STRING);
        if (skillId == null) return;

        Optional<ConfigurationSection> skillConfigOpt = plugin.getSkillManager().getSkillConfig(skillId);
        if (!skillConfigOpt.isPresent()) return;
        ConfigurationSection skillConfig = skillConfigOpt.get();

        int currentSkillLevel = playerData.getSkillLevel(skillId);
        int maxSkillLevel = skillConfig.getInt("max-level", 1);
        int playerLevelReq = skillConfig.getInt("skill-req", 1);

        if (playerData.getSkillPoints() > 0 && currentSkillLevel < maxSkillLevel && playerData.getLevel() >= playerLevelReq) {
            playerData.setSkillPoints(playerData.getSkillPoints() - 1);
            playerData.setSkillLevel(skillId, currentSkillLevel + 1);
            open(); // Recarga la GUI para mostrar los cambios
        }
    }

    @Override
    public void setItems() {
        setPlayerInfoPanel();

        Map<Integer, String> skills = playerClass.getSkills();

        if (skills.isEmpty()) {
            player.sendMessage(ChatColor.RED + "[DEBUG] No se encontraron habilidades en la configuración de la clase '" + playerClass.getId() + "'. Revisa el archivo 'plugins/InfinixMob/classes/" + playerClass.getId() + ".yml'.");
        }

        for (Map.Entry<Integer, String> entry : skills.entrySet()) {
            int slot = 9 + entry.getKey();
            if (slot >= getSlots()) continue;

            String skillId = entry.getValue();
            plugin.getSkillManager().getSkillConfig(skillId).ifPresent(skillConfig -> {
                inventory.setItem(slot, createSkillItem(skillId, skillConfig));
            });
        }
        fillEmptySlots();
    }

    private void setPlayerInfoPanel() {
        // ... (Este método no necesita cambios)
        ItemStack profile = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta profileMeta = (SkullMeta) profile.getItemMeta();
        if (profileMeta != null) {
            profileMeta.setOwningPlayer(player);
            profileMeta.setDisplayName(ChatColor.GREEN + player.getName());
            profileMeta.setLore(List.of(
                    ChatColor.GRAY + "Clase: " + playerClass.getDisplayName(),
                    ChatColor.GRAY + "Nivel: " + ChatColor.YELLOW + playerData.getLevel()
            ));
            profile.setItemMeta(profileMeta);
        }
        inventory.setItem(3, profile);

        ItemStack expBar = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta expMeta = expBar.getItemMeta();
        expMeta.setDisplayName(ChatColor.GOLD + "Experiencia");
        expMeta.setLore(List.of(
                ChatColor.GRAY + "Progreso: " + ChatColor.YELLOW + (int) playerData.getExperience() + " / " + (int) playerData.getNextLevelExp()
        ));
        expBar.setItemMeta(expMeta);
        inventory.setItem(4, expBar);
        
        ItemStack skillPoints = new ItemStack(Material.NETHER_STAR);
        ItemMeta pointsMeta = skillPoints.getItemMeta();
        pointsMeta.setDisplayName(ChatColor.AQUA + "Puntos de Habilidad");
        pointsMeta.setLore(List.of(
                ChatColor.GRAY + "Disponibles: " + ChatColor.YELLOW + playerData.getSkillPoints()
        ));
        skillPoints.setItemMeta(pointsMeta);
        inventory.setItem(5, skillPoints);
    }

    private ItemStack createSkillItem(String skillId, ConfigurationSection skillConfig) {
        int currentLevel = playerData.getSkillLevel(skillId);
        int maxLevel = skillConfig.getInt("max-level", 1);
        int playerLevelReq = skillConfig.getInt("skill-req", 1);
        boolean isLocked = playerData.getLevel() < playerLevelReq;

        Material icon;
        if (isLocked) {
            icon = Material.BARRIER;
        } else {
            icon = Material.matchMaterial(skillConfig.getString("icon", "BARRIER"));
            if (icon == null) {
                icon = Material.BARRIER;
            }
        }
        
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', skillConfig.getString("display-name", skillId)));
        
        List<String> baseLore = new ArrayList<>();
        for (String line : skillConfig.getStringList("lore")) {
            baseLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        
        List<String> finalLore = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([a-zA-Z0-9_.-]+)\\}");

        for (String line : baseLore) {
            Matcher matcher = pattern.matcher(line);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String key = matcher.group(1);
                String replacement = "";
                Object valueObject = skillConfig.get(key);

                try {
                    if (valueObject instanceof ConfigurationSection) {
                        ConfigurationSection valueConfig = (ConfigurationSection) valueObject;
                        double currentValue = SkillValueCalculator.calculate(valueConfig, currentLevel > 0 ? currentLevel : 1);
                        replacement = ChatColor.WHITE + String.format("%.1f", currentValue);

                        if (currentLevel > 0 && currentLevel < maxLevel && playerData.getSkillPoints() > 0) {
                            double nextValue = SkillValueCalculator.calculate(valueConfig, currentLevel + 1);
                            if (nextValue != currentValue) {
                               replacement += ChatColor.GRAY + " -> " + ChatColor.GREEN + String.format("%.1f", nextValue);
                            }
                        }
                    } else if (valueObject instanceof Number) {
                        replacement = ChatColor.WHITE + String.format("%.1f", ((Number) valueObject).doubleValue());
                    }
                } catch (Exception e) {
                    replacement = ChatColor.RED + "Error";
                }
                
                if (!replacement.isEmpty()) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
            }
            matcher.appendTail(sb);
            finalLore.add(sb.toString());
        }
        
        finalLore.add("");

        if (isLocked) {
            finalLore.add(ChatColor.RED + "Bloqueado (Requiere Nivel " + playerLevelReq + ")");
        } else if (currentLevel == 0) {
            finalLore.add(ChatColor.GRAY + "Nivel: 0 / " + maxLevel);
             if (playerData.getSkillPoints() > 0) {
                finalLore.add(ChatColor.YELLOW + "¡Click para aprender!");
            } else {
                finalLore.add(ChatColor.RED + "Puntos Insuficientes");
            }
        } else if (currentLevel >= maxLevel) {
            finalLore.add(ChatColor.GOLD + "Nivel: " + currentLevel + " / " + maxLevel + " (MAX)");
        } else {
            finalLore.add(ChatColor.GRAY + "Nivel: " + currentLevel + " / " + maxLevel);
            if (playerData.getSkillPoints() > 0) {
                finalLore.add(ChatColor.GREEN + "¡Click para subir de nivel!");
            } else {
                finalLore.add(ChatColor.RED + "Puntos Insuficientes");
            }
        }

        meta.setLore(finalLore);
        meta.getPersistentDataContainer().set(GUI_ITEM_KEY, PersistentDataType.STRING, skillId);
        item.setItemMeta(meta);

        return item;
    }
}