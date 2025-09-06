package com.infinity3113.infinixmob.rpg.commands;

import com.infinity3113.infinixmob.rpg.guis.SkillsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillsCommand implements CommandExecutor {

    private final SkillsGUI skillsGUI;

    public SkillsCommand(SkillsGUI skillsGUI) {
        this.skillsGUI = skillsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            skillsGUI.open((Player) sender);
        }
        return true;
    }
}