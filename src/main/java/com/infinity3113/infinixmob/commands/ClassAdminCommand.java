package com.infinity3113.infinixmob.commands;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.playerclass.PlayerClass;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassAdminCommand implements CommandExecutor, TabCompleter {

    private final InfinixMob plugin;

    public ClassAdminCommand(InfinixMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("infinixmob.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        if (args.length < 4) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String action = args[1].toLowerCase();
        Player target = Bukkit.getPlayer(args[2]);
        double amount;

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + args[2]);
            return true;
        }

        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "La cantidad debe ser un número.");
            return true;
        }

        // CORRECCIÓN: Se pasa el objeto Player, no el UUID.
        PlayerData playerData = plugin.getPlayerClassManager().getPlayerData(target);
        if (playerData == null) {
            sender.sendMessage(ChatColor.RED + "No se pudieron cargar los datos del jugador.");
            return true;
        }

        switch (subCommand) {
            case "exp":
                handleExpCommand(sender, target, playerData, action, amount);
                break;
            case "skillpoints":
                handleSkillPointsCommand(sender, target, playerData, action, (int) amount);
                break;
            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void handleExpCommand(CommandSender sender, Player target, PlayerData playerData, String action, double amount) {
        // CORRECCIÓN: Se usa getPlayerClass() que sí existe.
        PlayerClass playerClass = playerData.getPlayerClass();
        if (playerClass == null) {
            sender.sendMessage(ChatColor.RED + "El jugador no tiene una clase seleccionada.");
            return;
        }

        switch (action) {
            case "add":
                // CORRECCIÓN: Se llama al método addExperience con los parámetros correctos.
                playerData.addExperience(amount);
                sender.sendMessage(ChatColor.GREEN + "Se añadieron " + amount + " de EXP a " + target.getName() + ".");
                target.sendMessage(ChatColor.YELLOW + "Has recibido " + amount + " de EXP.");
                break;
            case "set":
                // CORRECCIÓN: Se llama al método setExperience con los parámetros correctos.
                playerData.setExperience(amount);
                sender.sendMessage(ChatColor.GREEN + "Se estableció la EXP de " + target.getName() + " a " + amount + ".");
                target.sendMessage(ChatColor.YELLOW + "Tu EXP ha sido establecida a " + amount + ".");
                break;
            default:
                sendUsage(sender);
                return;
        }

        // CORRECCIÓN: Se implementa la lógica de subida de nivel aquí.
        while (playerData.canLevelUp()) {
            playerData.levelUp();
            target.sendMessage(ChatColor.GOLD + "¡Has subido al nivel " + playerData.getLevel() + "!");
            // Aquí puedes añadir más efectos si quieres, como en tu ClassListener
        }
    }

    private void handleSkillPointsCommand(CommandSender sender, Player target, PlayerData playerData, String action, int amount) {
        switch (action) {
            case "add":
                playerData.setSkillPoints(playerData.getSkillPoints() + amount);
                sender.sendMessage(ChatColor.GREEN + "Se añadieron " + amount + " puntos de habilidad a " + target.getName() + ".");
                target.sendMessage(ChatColor.YELLOW + "Has recibido " + amount + " puntos de habilidad.");
                break;
            case "set":
                playerData.setSkillPoints(amount);
                sender.sendMessage(ChatColor.GREEN + "Se establecieron los puntos de habilidad de " + target.getName() + " a " + amount + ".");
                target.sendMessage(ChatColor.YELLOW + "Tus puntos de habilidad han sido establecidos a " + amount + ".");
                break;
            default:
                sendUsage(sender);
                break;
        }
    }


    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Comandos de Administración de Clases ---");
        sender.sendMessage(ChatColor.YELLOW + "/classadmin exp <add|set> <jugador> <cantidad>");
        sender.sendMessage(ChatColor.YELLOW + "/classadmin skillpoints <add|set> <jugador> <cantidad>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("exp", "skillpoints"));
        } else if (args.length == 2) {
            completions.addAll(Arrays.asList("add", "set"));
        } else if (args.length == 3) {
            return null; // Autocompletado de jugadores por defecto de Bukkit
        }
        
        String currentArg = args[args.length - 1];
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg.toLowerCase()))
                .collect(Collectors.toList());
    }
}