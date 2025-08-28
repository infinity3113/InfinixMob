package com.infinity3113.infinixmob.commands;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.SpawnerListGui;
import com.infinity3113.infinixmob.items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final InfinixMob plugin;
    private FileConfiguration lang;

    public CommandManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    public void loadLanguage() {
        String langCode = plugin.getConfig().getString("language", "en");
        File langFile = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");
        
        if (!langFile.exists()) {
            plugin.getLogger().warning("¡No se encontró el archivo de idioma '" + langCode + ".yml'! Usando 'en.yml' por defecto.");
            plugin.saveResource("lang/en.yml", false);
            langFile = new File(plugin.getDataFolder(), "lang/en.yml");
        }
        
        this.lang = YamlConfiguration.loadConfiguration(langFile);
        plugin.getLogger().info("Archivo de idioma '" + langFile.getName() + "' cargado.");
    }

    private String getMsg(String path) {
        String message = lang.getString(path, "&cError: Mensaje no encontrado '" + path + "'");
        return ChatColor.translateAlternateColorCodes('&', lang.getString("prefix", "&8[&cInfinix&6Mob&8] &r") + message);
    }

    private String getRawMsg(String path) {
        String message = lang.getString(path, "&cError: Mensaje no encontrado '" + path + "'");
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(getRawMsg("help-header"));
            sender.sendMessage(getRawMsg("help-spawn"));
            sender.sendMessage(getRawMsg("help-item"));
            sender.sendMessage(getRawMsg("help-skills"));
            sender.sendMessage(getRawMsg("help-cast"));
            sender.sendMessage(getRawMsg("help-getspawner"));
            sender.sendMessage(getRawMsg("help-spawners"));
            if (sender.hasPermission("infinixmob.admin")) {
                sender.sendMessage(getRawMsg("help-reload"));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "meta":
                if (!sender.hasPermission("infinixmob.admin")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Uso: /im meta <uuid> <set|get|remove> <key> [value]");
                    return true;
                }
                try {
                    UUID uuid = UUID.fromString(args[1]);
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null) {
                        sender.sendMessage(ChatColor.RED + "No se encontró ninguna entidad con ese UUID.");
                        return true;
                    }
                    String action = args[2].toLowerCase();
                    String key = args[3];
                    NamespacedKey nsk = new NamespacedKey(plugin, key);

                    switch (action) {
                        case "set":
                            if (args.length < 5) {
                                sender.sendMessage(ChatColor.RED + "Uso: /im meta <uuid> set <key> <value>");
                                return true;
                            }
                            String value = args[4];
                            entity.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, value);
                            sender.sendMessage(ChatColor.GREEN + "Metadata establecida: " + key + " -> " + value);
                            break;
                        case "get":
                            if (entity.getPersistentDataContainer().has(nsk, PersistentDataType.STRING)) {
                                String retrievedValue = entity.getPersistentDataContainer().get(nsk, PersistentDataType.STRING);
                                sender.sendMessage(ChatColor.GREEN + "Metadata '" + key + "': " + retrievedValue);
                            } else {
                                sender.sendMessage(ChatColor.YELLOW + "La entidad no tiene la metadata '" + key + "'.");
                            }
                            break;
                        case "remove":
                            if (entity.getPersistentDataContainer().has(nsk, PersistentDataType.STRING)) {
                                entity.getPersistentDataContainer().remove(nsk);
                                sender.sendMessage(ChatColor.GREEN + "Metadata '" + key + "' eliminada.");
                            } else {
                                sender.sendMessage(ChatColor.YELLOW + "La entidad no tiene la metadata '" + key + "'.");
                            }
                            break;
                        default:
                            sender.sendMessage(ChatColor.RED + "Acción desconocida. Usa set, get, o remove.");
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "El UUID proporcionado no es válido.");
                }
                break;

            case "reload":
                if (!sender.hasPermission("infinixmob.admin")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                plugin.reload();
                sender.sendMessage(getMsg("reload"));
                break;

            case "spawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMsg("player-only"));
                    return true;
                }
                if (!sender.hasPermission("infinixmob.spawn")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(getMsg("usage-spawn"));
                    return true;
                }
                Player playerSpawn = (Player) sender;
                String mobId = args[1];
                if (plugin.getMobManager().spawnMob(mobId, playerSpawn.getLocation()) != null) {
                    playerSpawn.sendMessage(getMsg("mob-spawned").replace("%mob%", mobId));
                } else {
                    playerSpawn.sendMessage(getMsg("mob-not-found").replace("%mob%", mobId));
                }
                break;
                
            case "item":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMsg("player-only"));
                    return true;
                }
                if (!sender.hasPermission("infinixmob.item")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(getMsg("usage-item"));
                    return true;
                }
                Player pItem = (Player) sender;
                String itemId = args[1];
                plugin.getItemManager().getItem(itemId).ifPresentOrElse(
                        customItem -> {
                            pItem.getInventory().addItem(customItem.buildItemStack());
                            pItem.sendMessage(getMsg("item-received").replace("%item%", itemId));
                        },
                        () -> pItem.sendMessage(getMsg("item-not-found").replace("%item%", itemId))
                );
                break;

            case "skills":
                if (!sender.hasPermission("infinixmob.skills")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "--- Skills Cargadas ---");
                plugin.getSkillManager().getLoadedSkillNames().forEach(skillName -> {
                    sender.sendMessage(ChatColor.YELLOW + "- " + skillName);
                });
                break;
                
            case "cast":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMsg("player-only"));
                    return true;
                }
                if (!sender.hasPermission("infinixmob.cast")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(getMsg("usage-cast"));
                    return true;
                }
                Player caster = (Player) sender;
                String skillId = args[1];
                RayTraceResult rayTraceResult = caster.getWorld().rayTraceEntities(caster.getEyeLocation(), caster.getEyeLocation().getDirection(), 20, entity -> entity instanceof LivingEntity && !entity.equals(caster));
                Entity target = (rayTraceResult != null && rayTraceResult.getHitEntity() != null) ? rayTraceResult.getHitEntity() : null;
                if (target == null) {
                    caster.sendMessage(getMsg("no-target-found"));
                    return true;
                }
                plugin.getSkillManager().executeSkill(skillId, caster, target);
                caster.sendMessage(getMsg("cast-success")
                        .replace("%skill%", skillId)
                        .replace("%target%", target.getName()));
                break;
                
            case "getspawner":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMsg("player-only"));
                    return true;
                }
                if (!sender.hasPermission("infinixmob.getspawner")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                Player playerSpawner = (Player) sender;
                plugin.getItemManager().getItem("SpawnerCore").ifPresent(item -> {
                    playerSpawner.getInventory().addItem(item.buildItemStack());
                    playerSpawner.sendMessage(getMsg("getspawner-success"));
                });
                break;
                
            case "spawners":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMsg("player-only"));
                    return true;
                }
                if (!sender.hasPermission("infinixmob.spawners")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                Player playerSpawners = (Player) sender;
                if (args.length > 1 && args[1].equalsIgnoreCase("list")) {
                    new SpawnerListGui(plugin, playerSpawners).open();
                } else if (args.length > 2 && args[1].equalsIgnoreCase("tp")) {
                    String spawnerName = args[2];
                    Map<Location, Map<String, String>> spawners = plugin.getSpawnerManager().getAllSpawners();
                    for (Map.Entry<Location, Map<String, String>> entry : spawners.entrySet()) {
                        String currentSpawnerName = ChatColor.stripColor(entry.getValue().getOrDefault("name", ""));
                        if (currentSpawnerName.equalsIgnoreCase(spawnerName)) {
                            playerSpawners.teleport(entry.getKey().add(0.5, 1, 0.5));
                            playerSpawners.sendMessage(getMsg("spawner-tp-success").replace("%name%", spawnerName));
                            return true;
                        }
                    }
                    playerSpawners.sendMessage(getMsg("spawner-not-found"));
                } else {
                    playerSpawners.sendMessage(getMsg("usage-spawners"));
                }
                break;

            default:
                sender.sendMessage(getRawMsg("help-header"));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("help", "spawn", "item", "skills", "cast", "getspawner", "spawners", "reload"));
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("spawn")) {
                StringUtil.copyPartialMatches(args[1], plugin.getMobManager().getLoadedMobIds(), completions);
            }
             if (args[0].equalsIgnoreCase("item")) {
                StringUtil.copyPartialMatches(args[1], plugin.getItemManager().getLoadedItemIds(), completions);
            }
            if (args[0].equalsIgnoreCase("cast")) {
                StringUtil.copyPartialMatches(args[1], plugin.getSkillManager().getLoadedSkillNames(), completions);
            }
        }
        
        return completions;
    }
}