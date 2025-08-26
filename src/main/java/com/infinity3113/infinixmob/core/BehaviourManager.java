package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gestiona la IA proactiva (Behaviours) de los mobs.
 * Este manager ejecuta un "tick" periódico para que los mobs tomen decisiones.
 */
public class BehaviourManager {

    private final InfinixMob plugin;

    public BehaviourManager(InfinixMob plugin) {
        this.plugin = plugin;
    }

    /**
     * Inicia el ticker que procesará los comportamientos de todos los mobs activos.
     */
    public void startBehaviourTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity mob : new java.util.HashSet<>(plugin.getMobManager().getActiveMobs())) {
                    if (mob.isValid() && !mob.isDead() && mob instanceof Creature) {
                        processBehaviours((Creature) mob);
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 20L);
    }

    /**
     * Procesa la lista de comportamientos para un mob específico.
     * @param mob El mob cuya IA se va a procesar.
     */
    private void processBehaviours(Creature mob) {
        plugin.getMobManager().getMob(mob).ifPresent(customMob -> {
            List<Map<?, ?>> behaviours = customMob.getConfig().getMapList("Behaviours");
            if (behaviours == null || behaviours.isEmpty()) {
                return;
            }

            behaviours.sort((map1, map2) -> {
                Number priority1 = (Number) map1.get("priority");
                Number priority2 = (Number) map2.get("priority");
                if (priority1 == null) priority1 = 99;
                if (priority2 == null) priority2 = 99;
                return Integer.compare(priority1.intValue(), priority2.intValue());
            });

            for (Map<?, ?> behaviourData : behaviours) {
                if (applyBehaviour(mob, behaviourData)) {
                    break;
                }
            }
        });
    }

    /**
     * Aplica la lógica de un comportamiento específico.
     * @param mob El mob que ejecuta el comportamiento.
     * @param behaviourData Los datos de configuración del comportamiento.
     * @return true si el comportamiento se aplicó, false en caso contrario.
     */
    private boolean applyBehaviour(Creature mob, Map<?, ?> behaviourData) {
        String type = (String) behaviourData.get("type");
        if (type == null) return false;

        switch (type.toUpperCase()) {
            case "TARGET_HIGHEST_AGGRO":
                Player target = plugin.getThreatManager().getHighestThreatTarget(mob);
                if (target != null) {
                    mob.setTarget(target);
                    return true;
                }
                break;
            case "FLEE_ON_LOW_HEALTH":
                Object rawHealthThreshold = behaviourData.get("health-threshold");
                double healthThreshold = (rawHealthThreshold instanceof Number) ? ((Number) rawHealthThreshold).doubleValue() : 0.15;
                
                if (mob.getHealth() / mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() <= healthThreshold) {
                    if (mob.getTarget() != null) {
                        Vector fleeVector = mob.getLocation().toVector().subtract(mob.getTarget().getLocation().toVector()).normalize();
                        
                        Object rawSpeedMultiplier = behaviourData.get("speed-multiplier");
                        double speedMultiplier = (rawSpeedMultiplier instanceof Number) ? ((Number) rawSpeedMultiplier).doubleValue() : 1.5;
                        
                        mob.setVelocity(fleeVector.multiply(0.35 * speedMultiplier).add(new Vector(0, 0.1, 0)));
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2));
                        return true;
                    }
                }
                break;
        }
        return false;
    }
}