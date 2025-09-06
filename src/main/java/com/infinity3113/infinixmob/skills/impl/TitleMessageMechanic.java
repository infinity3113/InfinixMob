package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.utils.PlaceholderParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.Map;
public class TitleMessageMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        String title = PlaceholderParser.parse((String) params.getOrDefault("title", ""), caster, target);
        String subtitle = PlaceholderParser.parse((String) params.getOrDefault("subtitle", ""), caster, target);
        int fadeIn = ((Number) params.getOrDefault("fade_in", 10)).intValue();
        int stay = ((Number) params.getOrDefault("stay", 40)).intValue();
        int fadeOut = ((Number) params.getOrDefault("fade_out", 10)).intValue();
        double radius = ((Number) params.getOrDefault("radius", 20.0)).doubleValue();
        caster.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(caster.getLocation()) <= radius)
                .forEach(p -> p.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
    }
}