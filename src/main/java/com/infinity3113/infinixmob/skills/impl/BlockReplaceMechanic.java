package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;
public class BlockReplaceMechanic implements Mechanic {
    private final InfinixMob plugin;
    public BlockReplaceMechanic(InfinixMob plugin) { this.plugin = plugin; }
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        int radius = ((Number) params.getOrDefault("radius", 5)).intValue();
        Material from = Material.matchMaterial(((String) params.getOrDefault("from_block", "GRASS_BLOCK")).toUpperCase());
        Material to = Material.matchMaterial(((String) params.getOrDefault("to_block", "DIRT")).toUpperCase());
        if (from == null || to == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                Location center = caster.getLocation();
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block block = center.clone().add(x, y, z).getBlock();
                            if (block.getType() == from) {
                                block.setType(to);
                            }
                        }
                    }
                }
            }
        }.runTask(plugin);
    }
}