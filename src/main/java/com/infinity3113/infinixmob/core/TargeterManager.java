package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.targeters.Targeter;
import com.infinity3113.infinixmob.targeters.impl.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargeterManager {

    private final InfinixMob plugin;
    private final Map<String, Targeter> registeredTargeters = new HashMap<>();
    private static final Pattern TARGETER_PATTERN = Pattern.compile("@([a-zA-Z_]+)(?:\\{(.*)\\})?");

    public TargeterManager(InfinixMob plugin) {
        this.plugin = plugin;
        registerTargeters();
    }

    private void registerTargeters() {
        registeredTargeters.put("SELF", new TargetSelf());
        registeredTargeters.put("TARGET", new TargetTarget());
        registeredTargeters.put("OWNER", new TargetOwner());
        registeredTargeters.put("PLAYERSINRADIUS", new TargetPlayersInRadius());
        registeredTargeters.put("MOBSINRADIUS", new TargetMobsInRadius());
        registeredTargeters.put("ENTITIESINRADIUS", new TargetEntitiesInRadius());
        registeredTargeters.put("LINE", new TargetLine());
    }

    public List<Entity> getTargets(LivingEntity caster, Entity initialTarget, String targeterString) {
        if (targeterString == null || targeterString.isEmpty()) {
            targeterString = "@target";
        }

        Matcher matcher = TARGETER_PATTERN.matcher(targeterString.toUpperCase());
        if (matcher.matches()) {
            String type = matcher.group(1);
            String rawParameters = matcher.group(2);
            
            Targeter targeter = registeredTargeters.get(type);
            if (targeter != null) {
                return targeter.getTargets(caster, initialTarget, rawParameters);
            }
        }
        
        plugin.getLogger().warning("Unknown or malformed targeter: " + targeterString);
        return new ArrayList<>();
    }
}