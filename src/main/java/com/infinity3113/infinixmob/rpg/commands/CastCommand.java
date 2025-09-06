package com.infinity3113.infinixmob.rpg.commands;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager.PlayerData;
import com.infinity3113.infinixmob.rpg.managers.RpgSkillManager;
import com.infinity3113.infinixmob.rpg.util.SkillCaster;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CastCommand implements CommandExecutor {

    private final InfinixMob plugin;
    private final PlayerClassManager playerManager;
    private final RpgSkillManager skillManager;

    public CastCommand(InfinixMob plugin, PlayerClassManager playerManager, RpgSkillManager skillManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.skillManager = skillManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        PlayerData data = playerManager.getPlayerData(player);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "Primero debes elegir una clase con /clase.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Uso: /cast <habilidad>");
            return true;
        }

        String skillId = args[0].toLowerCase();
        int skillLevel = data.getSkillLevel(skillId);
        if (skillLevel <= 0) {
            player.sendMessage(ChatColor.RED + "No conoces esa habilidad o no pertenece a tu clase.");
            return true;
        }

        if (playerManager.isOnCooldown(player, skillId)) {
            player.sendMessage(ChatColor.RED + "Esa habilidad se está recargando.");
            return true;
        }

        double manaCost = skillManager.getSkillStat(skillId, skillLevel, "mana_cost");
        if (!data.hasEnoughMana(manaCost)) {
            player.sendMessage(ChatColor.BLUE + "No tienes suficiente maná.");
            return true;
        }

        data.removeMana(manaCost);
        double cooldown = skillManager.getSkillStat(skillId, skillLevel, "cooldown");
        playerManager.setCooldown(player, skillId, (int) cooldown);

        SkillCaster.executeSkill(player, skillId, skillLevel, plugin, skillManager);
        return true;
    }
}