package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;

public class ExplosionMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        float power = ((Number) params.getOrDefault("power", 3.0f)).floatValue();
        boolean setFire = (boolean) params.getOrDefault("set_fire", false);
        boolean breakBlocks = (boolean) params.getOrDefault("break_blocks", false);
        target.getWorld().createExplosion(target.getLocation(), power, setFire, breakBlocks, caster);
    }
}