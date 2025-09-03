package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

public class DynamicArenaMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        String action = ((String) params.getOrDefault("action", "REPLACE")).toUpperCase();
        int radius = ((Number) params.getOrDefault("radius", 10)).intValue();
        
        if (action.equals("REPLACE")) {
            try {
                Material from = Material.valueOf(((String) params.get("from")).toUpperCase());
                Material to = Material.valueOf(((String) params.get("to")).toUpperCase());
                Location center = target != null ? target.getLocation() : caster.getLocation();

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (center.clone().add(x, y, z).distanceSquared(center) <= radius * radius) {
                                Block block = center.clone().add(x, y, z).getBlock();
                                if (block.getType() == from) {
                                    block.setType(to);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Log error
            }
        }
    }
}