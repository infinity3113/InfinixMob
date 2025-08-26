package com.infinity3113.infinixmob.targeters.impl;
import com.infinity3113.infinixmob.targeters.Targeter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class TargetOwner implements Targeter {
    @Override
    public List<Entity> getTargets(LivingEntity caster, Entity initialTarget, String rawParameters) {
        List<Entity> targets = new ArrayList<>();
        if (caster.hasMetadata("InfinixMob_Owner")) {
            UUID ownerUUID = UUID.fromString(caster.getMetadata("InfinixMob_Owner").get(0).asString());
            Entity owner = Bukkit.getEntity(ownerUUID);
            if (owner != null) targets.add(owner);
        }
        return targets;
    }
}