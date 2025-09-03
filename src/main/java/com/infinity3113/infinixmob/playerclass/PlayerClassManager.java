package com.infinity3113.infinixmob.playerclass;

import com.infinity3113.infinixmob.InfinixMob;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerClassManager {

    private final InfinixMob plugin;
    private final Map<String, PlayerClass> classes = new HashMap<>();
    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    public PlayerClassManager(InfinixMob plugin) {
        this.plugin = plugin;
        loadClasses();
        startResourceRegenTask();
    }

    public void loadClasses() {
        classes.clear();
        File classesFolder = new File(plugin.getDataFolder(), "classes");
        if (!classesFolder.exists()) {
            classesFolder.mkdirs();
        }

        File[] files = classesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File classFile : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(classFile);
            String classId = classFile.getName().replace(".yml", "");
            classes.put(classId.toLowerCase(), new PlayerClass(classId, config));
            plugin.getLogger().info("Clase cargada: " + classId);
        }
    }

    public PlayerData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), PlayerData::new);
    }

    public PlayerClass getClass(String id) {
        return classes.get(id.toLowerCase());
    }
    
    public Collection<PlayerClass> getAllClasses() {
        return classes.values();
    }

    public void loadPlayerData(Player player) {
        File playerFile = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            playerData.put(player.getUniqueId(), new PlayerData(player.getUniqueId()));
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        String className = config.getString("class");
        PlayerClass playerClass = className != null ? getClass(className) : null;
        int level = config.getInt("level", 1);
        double experience = config.getDouble("experience", 0);
        double currentResource = config.getDouble("current-resource", 0);
        int skillPoints = config.getInt("skill-points", 1);
        
        Map<String, Integer> skillLevels = new HashMap<>();
        if (config.isConfigurationSection("skill-levels")) {
            ConfigurationSection skillsSection = config.getConfigurationSection("skill-levels");
            for (String key : skillsSection.getKeys(false)) {
                skillLevels.put(key, skillsSection.getInt(key));
            }
        }

        PlayerData data = new PlayerData(player.getUniqueId(), playerClass, level, experience, currentResource, skillPoints, skillLevels);
        playerData.put(player.getUniqueId(), data);
    }

    public void savePlayerData(Player player) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;

        File playerFile = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        data.saveToConfig(config);
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar los datos del jugador " + player.getName(), e);
        }
    }

    public void saveAllPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerData(player);
        }
    }
    
    public void removePlayerData(Player player){
        playerData.remove(player.getUniqueId());
    }

    public void startResourceRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData data = getPlayerData(player);
                    PlayerClass pClass = data.getPlayerClass();

                    if (pClass != null) {
                        // Regeneración de maná/energía
                        double maxResource = pClass.getMaxResource();
                        double regen = pClass.getResourceRegen();
                        if (data.getCurrentResource() < maxResource) {
                            data.setCurrentResource(Math.min(maxResource, data.getCurrentResource() + regen));
                        }

                        // Barra de acción (HUD)
                        String healthBar = ChatColor.DARK_RED + "❤ " + ChatColor.RED + (int)player.getHealth() + "/" + (int)player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                        String resourceBar;
                        String resourceSymbol = pClass.getResourceType().equalsIgnoreCase("MANA") ? "■" : "⚡";
                        ChatColor resourceColor = pClass.getResourceType().equalsIgnoreCase("MANA") ? ChatColor.AQUA : ChatColor.GREEN;
                        ChatColor darkResourceColor = pClass.getResourceType().equalsIgnoreCase("MANA") ? ChatColor.DARK_AQUA : ChatColor.DARK_GREEN;
                        
                        resourceBar = darkResourceColor + resourceSymbol + " " + resourceColor + (int)data.getCurrentResource() + "/" + (int)maxResource;
                        
                        String expBar = ChatColor.GOLD + "XP: " + ChatColor.YELLOW + (int)data.getExperience() + "/" + (int)data.getNextLevelExp();
                        String level = ChatColor.DARK_GREEN +"Lvl: "+ ChatColor.GREEN + data.getLevel();

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(level + " " + healthBar + "  " + resourceBar + "  " + expBar));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Cada segundo
    }
}