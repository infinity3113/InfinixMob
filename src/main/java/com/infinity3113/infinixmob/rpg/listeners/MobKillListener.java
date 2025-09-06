package com.infinity3113.infinixmob.rpg.listeners;

import com.infinity3113.infinixmob.rpg.managers.ClassConfigManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class MobKillListener implements Listener {

    private final PlayerClassManager playerClassManager;
    private final ClassConfigManager classConfigManager;
    private final Map<EntityType, Double> xpValues = new HashMap<>();

    public MobKillListener(PlayerClassManager playerClassManager, ClassConfigManager classConfigManager) {
        this.playerClassManager = playerClassManager;
        this.classConfigManager = classConfigManager;
        initializeXpValues();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        if (!playerClassManager.hasClass(killer)) {
            return;
        }
        
        double xpToGive = xpValues.getOrDefault(entity.getType(), 0.0);
        if (xpToGive > 0) {
            playerClassManager.addExperience(killer, xpToGive);
        }
    }

    private void initializeXpValues() {
        xpValues.put(EntityType.ZOMBIE, 10.0);
        xpValues.put(EntityType.SKELETON, 10.0);
        xpValues.put(EntityType.SPIDER, 8.0);
        xpValues.put(EntityType.CREEPER, 15.0);
        xpValues.put(EntityType.ENDERMAN, 25.0);
        xpValues.put(EntityType.PIGLIN, 12.0);
        xpValues.put(EntityType.HOGLIN, 20.0);
        xpValues.put(EntityType.BLAZE, 20.0);
        xpValues.put(EntityType.GHAST, 30.0);
        xpValues.put(EntityType.WITHER, 500.0);
        xpValues.put(EntityType.ENDER_DRAGON, 1000.0);
        xpValues.put(EntityType.COW, 2.0);
        xpValues.put(EntityType.PIG, 2.0);
        xpValues.put(EntityType.SHEEP, 2.0);
        xpValues.put(EntityType.CHICKEN, 1.0);
    }
}