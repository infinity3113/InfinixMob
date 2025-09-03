package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.PlaceholderParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;
public class CommandMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        String command = (String) params.getOrDefault("command", "say Hello");
        String finalCommand = PlaceholderParser.parse(command, caster, target);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
    }
}