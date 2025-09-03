package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.playerclass.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.Map;

public class ThreatMechanic implements Mechanic {
    private final InfinixMob plugin;
    public ThreatMechanic(InfinixMob plugin) { this.plugin = plugin; }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params, PlayerData playerData) {
        if (target instanceof Player) {
            double amount = ((Number) params.getOrDefault("amount", 100.0)).doubleValue();
            plugin.getThreatManager().addThreat(caster, (Player) target, amount);
        }
    }
}