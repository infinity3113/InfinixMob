package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class TeleportRandomMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target == null) return;
        int radius = ((Number) params.getOrDefault("radius", 10)).intValue();
        Location center = target.getLocation();
        int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int y = center.getWorld().getHighestBlockYAt(x, z) + 1;
        target.teleport(new Location(center.getWorld(), x, y, z));
    }
}