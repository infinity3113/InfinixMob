package com.infinity3113.infinixmob;

import com.infinity3113.infinixmob.commands.CommandManager;
import com.infinity3113.infinixmob.core.*;
import com.infinity3113.infinixmob.listeners.*;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class InfinixMob extends JavaPlugin {

    private static InfinixMob plugin;
    private ChatInputManager chatInputManager;
    private SpawnerManager spawnerManager;
    private MobManager mobManager;
    private ItemManager itemManager;
    private SkillManager skillManager;
    private MechanicManager mechanicManager;
    private TargeterManager targeterManager;
    private StatusEffectManager statusEffectManager;
    private ThreatManager threatManager;
    private BehaviourManager behaviourManager;
    private SoulLinkManager soulLinkManager;
    private WeakPointManager weakPointManager;
    private BlockManager blockManager;
    private CommandManager commandManager;
    
    private MobListener mobListener;
    private BukkitTask timerSkillTask;

    public NamespacedKey TIMER_KEY;

    @Override
    public void onEnable() {
        plugin = this;
        
        this.TIMER_KEY = new NamespacedKey(this, "infinix_timer_skill_count");

        saveDefaultConfig();
        saveResource("lang/en.yml", false);
        saveResource("lang/es.yml", false);

        // Inicializar todos los managers
        this.chatInputManager = new ChatInputManager();
        this.threatManager = new ThreatManager();
        this.statusEffectManager = new StatusEffectManager(this);
        this.behaviourManager = new BehaviourManager(this);
        this.spawnerManager = new SpawnerManager(this);
        this.targeterManager = new TargeterManager(this);
        this.mechanicManager = new MechanicManager(this);
        this.skillManager = new SkillManager(this);
        this.mobManager = new MobManager(this);
        this.itemManager = new ItemManager(this);
        this.soulLinkManager = new SoulLinkManager(this);
        this.weakPointManager = new WeakPointManager(this);
        this.blockManager = new BlockManager(this);

        this.commandManager = new CommandManager(this);
        getCommand("infinixmob").setExecutor(commandManager);
        getCommand("infinixmob").setTabCompleter(commandManager);
        
        this.mobListener = new MobListener(this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(this.mobListener, this);
        // ZoneEnterListener no fue proporcionado, lo comento para evitar errores
        // getServer().getPluginManager().registerEvents(new ZoneEnterListener(this), this);

        reload();

        getLogger().info("InfinixMob ha sido activado!");
    }

    public void startTimerSkillTicker() {
        if (timerSkillTask != null) {
            timerSkillTask.cancel();
        }
        timerSkillTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (mobListener != null) {
                    for (LivingEntity mob : new java.util.HashSet<>(getMobManager().getActiveMobs())) {
                        if (mob.isValid() && !mob.isDead()) {
                            mobListener.handleTimerTick(mob);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
    }

    @Override
    public void onDisable() {
        getLogger().info("InfinixMob ha sido desactivado.");
    }
    
    public void reload() {
        this.reloadConfig();
        
        if (commandManager != null) {
            this.commandManager.loadLanguage();
        }
        
        this.statusEffectManager.loadStatusEffects();
        this.itemManager.loadItems();
        this.mobManager.loadMobs();
        this.skillManager.loadSkills();
        
        this.spawnerManager.startSpawnerTask();
        this.statusEffectManager.startEffectTicker();
        this.behaviourManager.startBehaviourTicker();
        
        this.startTimerSkillTicker();
        
        getLogger().info("InfinixMob ha sido recargado.");
    }

    // Getters
    public static InfinixMob getPlugin() { return plugin; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public SpawnerManager getSpawnerManager() { return spawnerManager; }
    public MobManager getMobManager() { return mobManager; }
    public ItemManager getItemManager() { return itemManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public MechanicManager getMechanicManager() { return mechanicManager; }
    public TargeterManager getTargeterManager() { return targeterManager; }
    public StatusEffectManager getStatusEffectManager() { return statusEffectManager; }
    public ThreatManager getThreatManager() { return threatManager; }
    public BehaviourManager getBehaviourManager() { return behaviourManager; }
    public SoulLinkManager getSoulLinkManager() { return soulLinkManager; }
    public WeakPointManager getWeakPointManager() { return weakPointManager; }
    public BlockManager getBlockManager() { return blockManager; }
    public MobListener getMobListener() { return mobListener; }
}