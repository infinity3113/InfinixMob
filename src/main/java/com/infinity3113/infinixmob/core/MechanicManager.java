package com.infinity3113.infinixmob.core;

import com.infinity3113.infinixmob.InfinixMob;
import com.infinity3113.infinixmob.mechanics.Mechanic;
import com.infinity3113.infinixmob.mechanics.impl.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class MechanicManager {

    private final InfinixMob plugin;
    private final Map<String, Mechanic> registeredMechanics = new HashMap<>();

    public MechanicManager(InfinixMob plugin) {
        this.plugin = plugin;
        registerMechanics();
    }

    private void registerMechanics() {
        // --- Mecánicas Originales ---
        registeredMechanics.put("DAMAGE", new DamageMechanic());
        registeredMechanics.put("HEAL", new HealMechanic());
        registeredMechanics.put("POTION", new PotionMechanic());
        registeredMechanics.put("MESSAGE", new MessageMechanic());
        registeredMechanics.put("SOUND", new SoundMechanic());
        registeredMechanics.put("PARTICLE", new ParticleMechanic());
        registeredMechanics.put("SUMMON", new SummonMechanic(plugin));
        registeredMechanics.put("TELEPORT", new TeleportMechanic());
        registeredMechanics.put("COMMAND", new CommandMechanic());
        registeredMechanics.put("LIGHTNING", new LightningMechanic());
        registeredMechanics.put("METEOR_SHOWER", new MeteorShowerMechanic(plugin));
        registeredMechanics.put("ARROW_RAIN", new ArrowRainMechanic(plugin));
        registeredMechanics.put("PUSH", new PushMechanic());
        registeredMechanics.put("PULL", new PullMechanic());
        registeredMechanics.put("BLOCK_REPLACE", new BlockReplaceMechanic(plugin));
        registeredMechanics.put("TITLE_MESSAGE", new TitleMessageMechanic());
        registeredMechanics.put("ACTION_BAR", new ActionBarMechanic());
        registeredMechanics.put("DISARM", new DisarmMechanic());
        registeredMechanics.put("SPAWN_FALLING_BLOCK", new SpawnFallingBlockMechanic(plugin));
        registeredMechanics.put("REMOVE_POTION", new RemovePotionMechanic());
        
        // --- ¡NUEVAS MECÁNICAS! ---
        registeredMechanics.put("CUSTOM_STATUS_EFFECT", new CustomStatusEffectMechanic(plugin));
        registeredMechanics.put("THREAT", new ThreatMechanic(plugin));
        registeredMechanics.put("IGNITE", new IgniteMechanic());
        registeredMechanics.put("FREEZE", new FreezeMechanic(plugin));
        registeredMechanics.put("VAMPIRISM", new VampirismMechanic());
        registeredMechanics.put("LAUNCH", new LaunchMechanic());
        registeredMechanics.put("SWAP", new SwapMechanic());
        registeredMechanics.put("EXPLOSION", new ExplosionMechanic());
        registeredMechanics.put("BLINDNESS", new BlindnessMechanic());
        registeredMechanics.put("SPAWN_WEB", new SpawnWebMechanic());
        registeredMechanics.put("AREA_DAMAGE", new AreaDamageMechanic());
        registeredMechanics.put("POTION_AREA", new PotionAreaMechanic());
        registeredMechanics.put("HUNGER", new HungerMechanic());
        registeredMechanics.put("EXPERIENCE", new ExperienceMechanic());
        registeredMechanics.put("ITEM_COOLDOWN", new ItemCooldownMechanic());
        registeredMechanics.put("TELEPORT_RANDOM", new TeleportRandomMechanic());
        registeredMechanics.put("APPLY_SHIELD", new ApplyShieldMechanic());
		registeredMechanics.put("SOUL_LINK", new SoulLinkMechanic(plugin));
		registeredMechanics.put("SCRAMBLE_INVENTORY", new ScrambleInventoryMechanic());
		registeredMechanics.put("CREATE_WEAK_POINT", new CreateWeakPointMechanic(plugin));
		registeredMechanics.put("PLACE_TRAP", new PlaceTrapMechanic(plugin));
        registeredMechanics.put("SUMMON_STRUCTURE", new SummonStructureMechanic(plugin));
        registeredMechanics.put("DYNAMIC_ARENA", new DynamicArenaMechanic());
        registeredMechanics.put("SILENT_REMOVE", new SilentRemoveMechanic());
		registeredMechanics.put("TELEPORT_LOOK", new TeleportLookMechanic());
		registeredMechanics.put("PROJECTILE", new ProjectileMechanic(plugin));
        registeredMechanics.put("DELAY", new DelayMechanic(plugin)); // <-- MECÁNICA NUEVA
    }

    public void executeMechanic(String type, LivingEntity caster, Entity target, Map<String, Object> params) {
        Mechanic mechanic = registeredMechanics.get(type.toUpperCase());
        if (mechanic != null) {
            mechanic.execute(caster, target, params);
        } else {
            plugin.getLogger().warning("Unknown mechanic type: " + type);
        }
    }
}