package com.infinity3113.infinixmob.mechanics.impl;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.Map;

public class ProjectileMechanic implements Mechanic {
    private final InfinixMob plugin;
    public ProjectileMechanic(InfinixMob plugin) { this.plugin = plugin; }

    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        String skillOnHit = (String) params.get("skill_on_hit");
        if (skillOnHit == null) return;

        String parentSkillId = (String) params.get("skillId");

        try {
            EntityType projectileType = EntityType.valueOf(((String) params.getOrDefault("projectile", "FIREBALL")).toUpperCase());
            double speed = ((Number) params.getOrDefault("speed", 1.5)).doubleValue();

            if (Projectile.class.isAssignableFrom(projectileType.getEntityClass())) {
                @SuppressWarnings("unchecked")
                Class<? extends Projectile> projectileClass = (Class<? extends Projectile>) projectileType.getEntityClass();
                
                Projectile projectile = caster.launchProjectile(projectileClass);
                projectile.setVelocity(caster.getEyeLocation().getDirection().multiply(speed));
                
                if (projectile instanceof Fireball) {
                    Fireball fireball = (Fireball) projectile;
                    fireball.setYield(0.0F);
                    fireball.setIsIncendiary(false);
                }
                projectile.setMetadata("infinix:skill_projectile", new FixedMetadataValue(plugin, true));
                
                projectile.setMetadata("InfinixMob_ProjectileSkill", new FixedMetadataValue(plugin, skillOnHit));
                projectile.setMetadata("InfinixMob_Owner", new FixedMetadataValue(plugin, caster.getUniqueId().toString()));

                if (parentSkillId != null) {
                    projectile.setMetadata("InfinixMob_ParentSkill", new FixedMetadataValue(plugin, parentSkillId));
                }

            } else {
                plugin.getLogger().warning("La entidad '" + projectileType.name() + "' no es un proyectil y no puede ser lanzada.");
            }

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Tipo de proyectil inválido en la mecánica PROJECTILE: " + params.get("projectile"));
        }
    }
}