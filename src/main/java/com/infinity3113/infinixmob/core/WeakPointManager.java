package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona la mecánica de Puntos Débiles (Weak Points).
 */
public class WeakPointManager implements Listener {

    private final InfinixMob plugin;
    private final Map<UUID, WeakPoint> activeWeakPoints = new ConcurrentHashMap<>(); // ArmorStand UUID -> WeakPoint

    public WeakPointManager(InfinixMob plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startWeakPointTicker();
    }

    /**
     * Crea un punto débil en un jefe.
     * @param owner El jefe que tendrá el punto débil.
     * @param damageMultiplier Multiplicador de daño al atacar el punto.
     * @param skillOnDamage Skill a ejecutar cuando se daña el punto.
     * @param offset El desplazamiento relativo al jefe.
     */
    public void createWeakPoint(LivingEntity owner, double damageMultiplier, String skillOnDamage, Vector offset) {
        Location spawnLoc = owner.getEyeLocation().add(offset);
        ArmorStand armorStand = (ArmorStand) owner.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setMarker(true); // No tiene hitbox, se detecta por proximidad

        WeakPoint wp = new WeakPoint(owner, damageMultiplier, skillOnDamage, offset, armorStand);
        activeWeakPoints.put(armorStand.getUniqueId(), wp);
    }

    /**
     * Ticker para mantener los puntos débiles pegados a sus dueños y mostrar partículas.
     */
    private void startWeakPointTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                activeWeakPoints.values().forEach(wp -> {
                    if (wp.getOwner().isDead() || !wp.getOwner().isValid()) {
                        wp.getArmorStand().remove();
                        activeWeakPoints.remove(wp.getArmorStand().getUniqueId());
                    } else {
                        Location newLoc = wp.getOwner().getEyeLocation().add(wp.getOffset());
                        wp.getArmorStand().teleport(newLoc);
                        wp.getArmorStand().getWorld().spawnParticle(Particle.CRIT_MAGIC, newLoc, 5, 0.1, 0.1, 0.1, 0);
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 2L); // Actualiza la posición muy rápido
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Detecta si un jugador ha golpeado cerca de un punto débil.
        for (WeakPoint wp : activeWeakPoints.values()) {
            if (event.getDamager().getLocation().distanceSquared(wp.getArmorStand().getLocation()) < 2.0) {
                // Cancela el evento original para evitar dañar al ArmorStand.
                event.setCancelled(true);

                // Aplica el daño multiplicado al dueño del punto débil.
                double finalDamage = event.getDamage() * wp.getDamageMultiplier();
                wp.getOwner().damage(finalDamage, event.getDamager());

                // Ejecuta la skill asociada, si existe.
                if (wp.getSkillOnDamage() != null && !wp.getSkillOnDamage().isEmpty()) {
                    plugin.getSkillManager().executeSkill(wp.getSkillOnDamage(), wp.getOwner(), event.getDamager());
                }
                
                // Efecto visual de "golpeado"
                wp.getArmorStand().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, wp.getArmorStand().getLocation(), 1);
                break;
            }
        }
    }
    
    /**
     * Elimina todos los puntos débiles de una entidad.
     * @param owner La entidad cuyos puntos débiles se eliminarán.
     */
    public void removeWeakPointsFor(LivingEntity owner) {
        activeWeakPoints.entrySet().removeIf(entry -> {
            if (entry.getValue().getOwner().equals(owner)) {
                entry.getValue().getArmorStand().remove();
                return true;
            }
            return false;
        });
    }

    /**
     * Clase interna para almacenar la información de un punto débil.
     */
    private static class WeakPoint {
        private final LivingEntity owner;
        private final double damageMultiplier;
        private final String skillOnDamage;
        private final Vector offset;
        private final ArmorStand armorStand;

        public WeakPoint(LivingEntity owner, double damageMultiplier, String skillOnDamage, Vector offset, ArmorStand armorStand) {
            this.owner = owner;
            this.damageMultiplier = damageMultiplier;
            this.skillOnDamage = skillOnDamage;
            this.offset = offset;
            this.armorStand = armorStand;
        }

        public LivingEntity getOwner() { return owner; }
        public double getDamageMultiplier() { return damageMultiplier; }
        public String getSkillOnDamage() { return skillOnDamage; }
        public Vector getOffset() { return offset; }
        public ArmorStand getArmorStand() { return armorStand; }
    }
}