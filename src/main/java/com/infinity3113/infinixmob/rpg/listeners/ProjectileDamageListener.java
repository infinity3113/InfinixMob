package com.infinity3113.infinixmob.rpg.listeners;

import com.infinity3113.infinixmob.rpg.managers.RpgSkillManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class ProjectileDamageListener implements Listener {

    private final RpgSkillManager skillManager;

    public ProjectileDamageListener(RpgSkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }

        Projectile projectile = (Projectile) event.getDamager();
        if (!projectile.hasMetadata("skillKey") || !projectile.hasMetadata("skillLevel") || !projectile.hasMetadata("ownerUUID")) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();
        Player owner = Bukkit.getPlayer(UUID.fromString(projectile.getMetadata("ownerUUID").get(0).asString()));

        if (owner != null && victim.equals(owner)) {
            event.setCancelled(true);
            return;
        }

        String skillKey = projectile.getMetadata("skillKey").get(0).asString();
        int skillLevel = projectile.getMetadata("skillLevel").get(0).asInt();

        if (skillLevel <= 0) {
            return;
        }

        double damage = skillManager.getSkillStat(skillKey, skillLevel, "damage");
        event.setDamage(damage);

        if (skillKey.equalsIgnoreCase("ice_shard")) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
        }
    }
}