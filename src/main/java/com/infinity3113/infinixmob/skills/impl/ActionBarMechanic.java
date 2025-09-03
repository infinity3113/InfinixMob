package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import com.infinity3113.infinixmob.utils.PlaceholderParser;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.Map;
public class ActionBarMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        String message = (String) params.getOrDefault("message", "");
        String finalMessage = PlaceholderParser.parse(message, caster, target);
        double radius = ((Number) params.getOrDefault("radius", 20.0)).doubleValue();
        caster.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(caster.getLocation()) <= radius)
                .forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(finalMessage)));
    }
}