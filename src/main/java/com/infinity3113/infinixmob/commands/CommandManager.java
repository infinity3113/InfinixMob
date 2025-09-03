package com.infinity3113.infinixmob.commands;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.gui.SpawnerListGui;
import com.infinity3113.infinixmob.gui.editor.ItemTypeSelectorGUI;
import com.infinity3113.infinixmob.playerclass.PlayerClass;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final InfinixMob plugin;
    private FileConfiguration lang;

    public CommandManager(InfinixMob plugin) {
        this.plugin = plugin;
    }
    
    // ... (tus métodos loadLanguage, getMsg, getRawMsg no cambian) ...
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
        if (command.getName().equalsIgnoreCase("infinixmob")) {
            return handleInfinixMobCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("cast")) {
            return handlePlayerCastCommand(sender, args);
        }
        return false;
    }
    
    // --- ESTE ES EL MÉTODO QUE VAMOS A DEPURAR ---
    private boolean handlePlayerCastCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMsg("player-only"));
            return true;
        }
        Player player = (Player) sender;
        
        player.sendMessage(ChatColor.YELLOW + "[DEBUG] Comando /cast ejecutado.");

        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "[DEBUG] Falla: No hay argumentos. Mostrando uso.");
            sender.sendMessage(getMsg("usage-cast-player"));
            return true;
        }
        
        player.sendMessage(ChatColor.YELLOW + "[DEBUG] Argumento detectado: " + args[0]);

        PlayerData pData = plugin.getPlayerClassManager().getPlayerData(player);
        PlayerClass pClass = pData.getPlayerClass();

        if (pClass == null) {
            player.sendMessage(ChatColor.YELLOW + "[DEBUG] Falla: El jugador no tiene clase seleccionada.");
            player.sendMessage(getMsg("cast-no-class"));
            return true;
        }
        
        player.sendMessage(ChatColor.YELLOW + "[DEBUG] Clase del jugador: " + pClass.getId());

        int slot;
        try {
            slot = Integer.parseInt(args[0]);
            if (slot < 1 || slot > 4) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.YELLOW + "[DEBUG] Falla: El argumento no es un número entre 1 y 4.");
            player.sendMessage(getMsg("usage-cast-player"));
            return true;
        }
        
        player.sendMessage(ChatColor.YELLOW + "[DEBUG] Slot seleccionado: " + slot);

        String skillId = pClass.getSkills().get(slot);
        if (skillId == null) {
            player.sendMessage(ChatColor.YELLOW + "[DEBUG] Falla: No se encontró habilidad para el slot " + slot);
            player.sendMessage(getMsg("cast-no-skill-in-slot").replace("%slot%", String.valueOf(slot)));
            return true;
        }
        
        player.sendMessage(ChatColor.YELLOW + "[DEBUG] ID de la habilidad: " + skillId);

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), skillId)) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId(), skillId);
            String timeLeft = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(remaining) + 1);
            player.sendMessage(ChatColor.YELLOW + "[DEBUG] Falla: Habilidad en enfriamiento.");
            player.sendMessage(getMsg("cast-on-cooldown").replace("%time%", timeLeft));
            return true;
        }

        Optional<ConfigurationSection> skillConfigOpt = plugin.getSkillManager().getSkillConfig(skillId);
        if (!skillConfigOpt.isPresent()) {
            player.sendMessage(ChatColor.RED + "[DEBUG] Falla CRÍTICA: La habilidad '" + skillId + "' existe en la clase pero no está cargada en SkillManager.");
            return true;
        }
        
        ConfigurationSection skillConfig = skillConfigOpt.get();
        double resourceCost = skillConfig.getDouble("resource-cost", 0);
        
        player.sendMessage(ChatColor.YELLOW + "[DEBUG] Coste de recurso: " + resourceCost + ". Recurso actual: " + pData.getCurrentResource());

        if (pData.getCurrentResource() < resourceCost) {
            player.sendMessage(ChatColor.YELLOW + "[DEBUG] Falla: Recursos insuficientes.");
            player.sendMessage(getMsg("cast-no-resource").replace("%resource_type%", pClass.getResourceType().toLowerCase()));
            return true;
        }
        
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 40, entity -> entity instanceof LivingEntity && !entity.equals(player));
        Entity target = (rayTraceResult != null && rayTraceResult.getHitEntity() != null) ? rayTraceResult.getHitEntity() : null;
        
        boolean requiresTarget = skillConfig.getBoolean("requires-target", true);
        
        player.sendMessage(ChatColor.YELLOW + "[DEBUG] Habilidad requiere objetivo: " + requiresTarget + ". Objetivo encontrado: " + (target != null ? target.getName() : "Ninguno"));

        if (requiresTarget && target == null) {
            player.sendMessage(ChatColor.YELLOW + "[DEBUG] Falla: Se requiere objetivo pero no se encontró.");
            player.sendMessage(getMsg("no-target-found"));
            return true;
        }
        
        Entity finalTarget = target != null ? target : player;
        
        player.sendMessage(ChatColor.GREEN + "[DEBUG] ¡ÉXITO! Lanzando habilidad '" + skillId + "' sobre " + finalTarget.getName());

        pData.setCurrentResource(pData.getCurrentResource() - resourceCost);
        int cooldown = skillConfig.getInt("cooldown", 0);
        plugin.getCooldownManager().setCooldown(player.getUniqueId(), skillId, cooldown);
        
        plugin.getSkillManager().executeSkill(skillId, player, finalTarget);
        return true;
    }

    // ... (El resto de tus métodos handleInfinixMobCommand, handleAdminCast, handleClassCommand, onTabComplete) ...
    // ... DEBEN ESTAR AQUÍ. No los borres, solo reemplaza el archivo completo. ...
    private boolean handleInfinixMobCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(getRawMsg("help-header"));
            sender.sendMessage(getRawMsg("help-spawn"));
            sender.sendMessage(getRawMsg("help-item"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/im edit &7- Abre la GUI para editar ítems."));
            sender.sendMessage(getRawMsg("help-skills"));
            sender.sendMessage(getRawMsg("help-cast"));
            sender.sendMessage(getRawMsg("help-getspawner"));
            sender.sendMessage(getRawMsg("help-spawners"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/im class <select|info|list> &7- Gestiona tu clase."));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/cast <1-4> &7- Lanza una habilidad de tu clase."));
            if (sender.hasPermission("infinixmob.admin")) {
                sender.sendMessage(getRawMsg("help-reload"));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("infinixmob.admin")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                plugin.reload();
                sender.sendMessage(getMsg("reload"));
                return true;

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
                return true;

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
                return true;

            case "edit":
                if (!(sender instanceof Player)) { sender.sendMessage(getMsg("player-only")); return true; }
                if (!sender.hasPermission("infinixmob.admin")) { sender.sendMessage(getMsg("no-permission")); return true; }
                new ItemTypeSelectorGUI(plugin, (Player) sender).open();
                return true;

            case "skills":
                if (!sender.hasPermission("infinixmob.skills")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "--- Skills Cargadas ---");
                plugin.getSkillManager().getLoadedSkillNames().forEach(skillName -> sender.sendMessage(ChatColor.YELLOW + "- " + skillName));
                return true;

            case "cast":
                handleAdminCast(sender, args);
                return true;

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
                return true;

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
                    Optional<Location> spawnerLoc = plugin.getSpawnerManager().getAllSpawners().entrySet().stream()
                        .filter(entry -> ChatColor.stripColor(entry.getValue().getOrDefault("name", "")).equalsIgnoreCase(spawnerName))
                        .map(Map.Entry::getKey)
                        .findFirst();

                    if (spawnerLoc.isPresent()) {
                        playerSpawners.teleport(spawnerLoc.get().add(0.5, 1, 0.5));
                        playerSpawners.sendMessage(getMsg("spawner-tp-success").replace("%name%", spawnerName));
                    } else {
                        playerSpawners.sendMessage(getMsg("spawner-not-found"));
                    }
                } else {
                    playerSpawners.sendMessage(getMsg("usage-spawners"));
                }
                return true;
            
            case "class":
                handleClassCommand(sender, args);
                return true;

            default:
                sender.sendMessage(getRawMsg("help-header"));
                return true;
        }
    }
    private void handleAdminCast(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage(getMsg("player-only")); return; }
        if (!sender.hasPermission("infinixmob.cast")) { sender.sendMessage(getMsg("no-permission")); return; }
        if (args.length < 2) { sender.sendMessage(getMsg("usage-cast")); return; }
        
        Player caster = (Player) sender;
        String skillId = args[1];

        RayTraceResult rayTraceResult = caster.getWorld().rayTraceEntities(caster.getEyeLocation(), caster.getEyeLocation().getDirection(), 20, entity -> entity instanceof LivingEntity && !entity.equals(caster));
        Entity target = (rayTraceResult != null && rayTraceResult.getHitEntity() != null) ? rayTraceResult.getHitEntity() : null;
        
        if (target == null) { caster.sendMessage(getMsg("no-target-found")); return; }
        
        plugin.getSkillManager().executeSkill(skillId, caster, target);
        caster.sendMessage(getMsg("cast-success").replace("%skill%", skillId).replace("%target%", target.getName()));
    }
    private void handleClassCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage(getMsg("player-only")); return; }
        Player player = (Player) sender;
        if (args.length < 2) { player.sendMessage(getMsg("usage-class")); return; }

        switch (args[1].toLowerCase()) {
            case "select":
                if (args.length < 3) { player.sendMessage(getMsg("usage-class")); return; }
                PlayerClass pClass = plugin.getPlayerClassManager().getClass(args[2]);
                if (pClass != null) {
                    plugin.getPlayerClassManager().getPlayerData(player).setPlayerClass(pClass);
                    player.sendMessage(getMsg("class-select-success").replace("%class%", pClass.getDisplayName()));
                } else {
                    player.sendMessage(getMsg("class-select-fail").replace("%class%", args[2]));
                }
                break;
            case "info":
                PlayerData data = plugin.getPlayerClassManager().getPlayerData(player);
                if (data.getPlayerClass() != null) {
                    player.sendMessage(getRawMsg("class-info-header"));
                    player.sendMessage(getRawMsg("class-info-entry").replace("{label}", "Clase").replace("{value}", data.getPlayerClass().getDisplayName()));
                    player.sendMessage(getRawMsg("class-info-entry").replace("{label}", "Nivel").replace("{value}", String.valueOf(data.getLevel())));
                    player.sendMessage(getRawMsg("class-info-entry").replace("{label}", "Experiencia").replace("{value}", (int)data.getExperience() + "/" + (int)data.getNextLevelExp()));
                } else {
                    player.sendMessage(getMsg("class-info-none"));
                }
                break;
            case "list":
                player.sendMessage(getRawMsg("class-list-header"));
                plugin.getPlayerClassManager().getAllClasses().forEach(c -> player.sendMessage(ChatColor.YELLOW + "- " + c.getDisplayName() + " (" + c.getId() + ")"));
                break;
            default:
                player.sendMessage(getMsg("usage-class"));
                break;
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("infinixmob")) {
             if (args.length == 1) {
                List<String> subCommands = new ArrayList<>(Arrays.asList("help", "spawn", "item", "edit", "skills", "cast", "getspawner", "spawners", "reload", "class"));
                StringUtil.copyPartialMatches(args[0], subCommands, completions);
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("spawn")) { StringUtil.copyPartialMatches(args[1], plugin.getMobManager().getLoadedMobIds(), completions); }
                if (args[0].equalsIgnoreCase("item")) { StringUtil.copyPartialMatches(args[1], plugin.getItemManager().getLoadedItemIds(), completions); }
                if (args[0].equalsIgnoreCase("cast")) { StringUtil.copyPartialMatches(args[1], plugin.getSkillManager().getLoadedSkillNames(), completions); }
                if (args[0].equalsIgnoreCase("class")) { StringUtil.copyPartialMatches(args[1], Arrays.asList("select", "info", "list"), completions); }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("class") && args[1].equalsIgnoreCase("select")) {
                    List<String> classNames = plugin.getPlayerClassManager().getAllClasses().stream().map(PlayerClass::getId).collect(Collectors.toList());
                    StringUtil.copyPartialMatches(args[2], classNames, completions);
                }
            }
        }
        return completions;
    }
}