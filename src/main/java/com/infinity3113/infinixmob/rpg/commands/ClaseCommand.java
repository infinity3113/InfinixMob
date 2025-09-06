package com.infinity3113.infinixmob.rpg.commands;

import com.infinity3113.infinixmob.rpg.guis.ClassSelectionGUI;
import com.infinity3113.infinixmob.rpg.managers.ClassConfigManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ClaseCommand implements CommandExecutor {

    private final PlayerClassManager playerClassManager;
    private final ClassConfigManager classConfigManager;
    private final ClassSelectionGUI classSelectionGUI;

    public ClaseCommand(PlayerClassManager playerClassManager, ClassConfigManager classConfigManager, ClassSelectionGUI classSelectionGUI) {
        this.playerClassManager = playerClassManager;
        this.classConfigManager = classConfigManager;
        this.classSelectionGUI = classSelectionGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (playerClassManager.hasClass(player)) {
            player.sendMessage(ChatColor.RED + "¡Ya tienes una clase! Eres " + playerClassManager.getPlayerClass(player) + ".");
            return true;
        }

        if (args.length == 0) {
            classSelectionGUI.open(player);
            return true;
        }

        String chosenClass = args[0].toLowerCase();
        FileConfiguration classConfig = classConfigManager.getClassConfig(chosenClass);

        if (classConfig == null) {
            player.sendMessage(ChatColor.RED + "La clase '" + args[0] + "' no existe.");
            return true;
        }

        playerClassManager.setPlayerClass(player, chosenClass, classConfig);
        player.sendMessage(ChatColor.GREEN + "¡Felicidades! Ahora eres un " + classConfig.getString("name", chosenClass) + ".");

        return true;
    }
}