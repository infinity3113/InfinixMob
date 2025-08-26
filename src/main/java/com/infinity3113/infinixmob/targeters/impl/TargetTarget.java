package com.infinity3113.infinixmob.targeters.impl;
import com.infinity3113.infinixmob.targeters.Targeter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.ArrayList;
import java.util.List;
public class TargetTarget implements Targeter {
    @Override
    public List<Entity> getTargets(LivingEntity caster, Entity initialTarget, String rawParameters) {
        List<Entity> targets = new ArrayList<>();
        if (initialTarget != null) {
            targets.add(initialTarget);
        }
        return targets;
    }
}