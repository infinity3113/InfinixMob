package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;
public class SoundMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        try {
            Sound sound = Sound.valueOf(((String) params.getOrDefault("sound", "UI_BUTTON_CLICK")).toUpperCase());
            float volume = ((Number) params.getOrDefault("volume", 1.0)).floatValue();
            float pitch = ((Number) params.getOrDefault("pitch", 1.0)).floatValue();
            target.getWorld().playSound(target.getLocation(), sound, volume, pitch);
        } catch (Exception e) {}
    }
}