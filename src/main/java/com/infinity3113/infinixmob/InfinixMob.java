package com.infinity3113.infinixmob;

import com.google.gson.Gson;
import com.infinity3113.infinixmob.commands.CommandManager;
import com.infinity3113.infinixmob.core.*;
import com.infinity3113.infinixmob.listeners.*;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

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
    private RecipeManager recipeManager; // Variable para el RecipeManager
    
    private MobListener mobListener;
    private BukkitTask timerSkillTask;
    private Gson gson;

    public NamespacedKey TIMER_KEY;

    @Override
    public void onEnable() {
        plugin = this;
        this.gson = new Gson();

        this.TIMER_KEY = new NamespacedKey(this, "infinix_timer_skill_count");

        // Guardar archivos de configuración por defecto
        saveDefaultConfig();
        saveResource("lang/en.yml", false);
        saveResource("lang/es.yml", false);
        
        saveResource("items/configurations/elements.yml", false);
        saveResource("items/configurations/rarities.yml", false);
        saveResource("items/configurations/lore-formats.yml", false);
        saveResource("items/configurations/stats.yml", false);
        saveResource("items/misc/SpawnerCore.yml", false);
        
        saveResource("items/sword.yml", false); 
        saveResource("items/axe.yml", false);
        saveResource("items/bow.yml", false);
        saveResource("items/armor.yml", false);

        saveResource("StatusEffects/effects.yml", false);
        
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
        this.recipeManager = new RecipeManager(this); // Inicializar el RecipeManager

        this.commandManager = new CommandManager(this);
        getCommand("infinixmob").setExecutor(commandManager);
        getCommand("infinixmob").setTabCompleter(commandManager);
        
        // Registrar todos los listeners
        this.mobListener = new MobListener(this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(this.mobListener, this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemUpdaterListener(this), this);
        getServer().getPluginManager().registerEvents(new SmithingListener(this), this); // Registrar el SmithingListener

        reload();
        
        // Registrar las recetas personalizadas después de cargar todo
        this.recipeManager.registerCustomRecipes();

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
    
    public Gson getGson() {
        return gson;
    }
}