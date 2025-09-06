package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;

public class SwapMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target != null) {
            Location casterLoc = caster.getLocation();
            Location targetLoc = target.getLocation();
            caster.teleport(targetLoc);
            target.teleport(casterLoc);
        }
    }
}