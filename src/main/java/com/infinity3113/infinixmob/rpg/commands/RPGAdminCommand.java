package com.infinity3113.infinixmob.rpg.commands;

import com.infinity3113.infinixmob.rpg.managers.ClassConfigManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RPGAdminCommand implements CommandExecutor {

    private final PlayerClassManager playerClassManager;
    private final ClassConfigManager classConfigManager;

    public RPGAdminCommand(PlayerClassManager playerClassManager, ClassConfigManager classConfigManager) {
        this.playerClassManager = playerClassManager;
        this.classConfigManager = classConfigManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("infiniclassrpg.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        if (args.length < 2) {
            sendHelpMessage(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "El jugador '" + args[1] + "' no está en línea.");
            return true;
        }

        PlayerData playerData = playerClassManager.getPlayerData(target);
        if (playerData == null) {
            sender.sendMessage(ChatColor.RED + "Ese jugador no tiene una clase asignada.");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "levelup":
                double baseXP = classConfigManager.getClassConfig(playerData.getClassName()).getDouble("leveling.base_xp_needed", 100);
                double xpIncrease = classConfigManager.getClassConfig(playerData.getClassName()).getDouble("leveling.xp_increase_per_level", 50);
                double xpNeeded = baseXP + (playerData.getLevel() - 1) * xpIncrease;
                playerClassManager.addExperience(target, xpNeeded - playerData.getExperience());
                sender.sendMessage(ChatColor.GREEN + "Has forzado la subida de nivel de " + target.getName() + ". Ahora es nivel " + playerData.getLevel() + ".");
                break;

            case "setlevel":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /rpgadmin setlevel <jugador> <nivel>");
                    return true;
                }
                try {
                    int level = Integer.parseInt(args[2]);
                    if (level < 1) {
                        sender.sendMessage(ChatColor.RED + "El nivel no puede ser menor que 1.");
                        return true;
                    }
                    playerData.setLevel(level);
                    playerData.setExperience(0); // Reiniciar XP
                    sender.sendMessage(ChatColor.GREEN + "Has establecido el nivel de " + target.getName() + " a " + level + ".");
                    target.sendMessage(ChatColor.GOLD + "Un administrador ha establecido tu nivel a " + level + ".");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "El nivel debe ser un número entero.");
                }
                break;

            case "addxp":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /rpgadmin addxp <jugador> <cantidad>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    playerClassManager.addExperience(target, amount);
                    sender.sendMessage(ChatColor.GREEN + "Has añadido " + amount + " de XP a " + target.getName() + ".");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "La cantidad de XP debe ser un número.");
                }
                break;

            default:
                sendHelpMessage(sender);
                break;
        }
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Comandos de InfiniClassRPG Admin ---");
        sender.sendMessage(ChatColor.YELLOW + "/rpgadmin levelup <jugador> - Sube un nivel al jugador.");
        sender.sendMessage(ChatColor.YELLOW + "/rpgadmin setlevel <jugador> <nivel> - Establece el nivel del jugador.");
        sender.sendMessage(ChatColor.YELLOW + "/rpgadmin addxp <jugador> <cantidad> - Añade experiencia al jugador.");
    }
}