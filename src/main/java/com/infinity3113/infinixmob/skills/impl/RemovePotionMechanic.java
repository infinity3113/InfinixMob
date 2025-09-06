package com.infinity3113.infinixmob.mechanics.impl;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import java.util.List;
import java.util.Map;
public class RemovePotionMechanic implements Mechanic {
    @Override
    public void execute(LivingEntity caster, Entity target, Map<String, Object> params) {
        if (target instanceof LivingEntity) {
            List<String> effects = (List<String>) params.get("effects");
            if (effects == null) return;
            for (String effectName : effects) {
                PotionEffectType effect = PotionEffectType.getByName(effectName.toUpperCase());
                if (effect != null) {
                    ((LivingEntity) target).removePotionEffect(effect);
                }
            }
        }
    }
}