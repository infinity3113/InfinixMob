package com.infinity3113.infinixmob.targeters;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.List;

public interface Targeter {
    List<Entity> getTargets(LivingEntity caster, Entity initialTarget, String rawParameters);
}