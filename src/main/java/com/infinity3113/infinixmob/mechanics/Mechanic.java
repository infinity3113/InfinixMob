package com.infinity3113.infinixmob.mechanics;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.Map;

public interface Mechanic {
    void execute(LivingEntity caster, Entity target, Map<String, Object> params);
}