package com.infinity3113.infinixmob;

import com.google.gson.Gson;
import com.infinity3113.infinixmob.commands.CommandManager;
import com.infinity3113.infinixmob.core.*;
import com.infinity3113.infinixmob.listeners.*;
import com.infinity3113.infinixmob.rpg.commands.CastCommand;
import com.infinity3113.infinixmob.rpg.commands.ClaseCommand;
import com.infinity3113.infinixmob.rpg.commands.RPGAdminCommand;
import com.infinity3113.infinixmob.rpg.commands.SkillsCommand;
import com.infinity3113.infinixmob.rpg.guis.ClassSelectionGUI;
import com.infinity3113.infinixmob.rpg.guis.SkillsGUI;
import com.infinity3113.infinixmob.rpg.listeners.*;
import com.infinity3113.infinixmob.rpg.managers.ClassConfigManager;
import com.infinity3113.infinixmob.rpg.managers.PlayerClassManager;
import com.infinity3113.infinixmob.rpg.managers.RpgSkillManager; // Corregido
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public final class InfinixMob extends JavaPlugin {

    private static InfinixMob plugin;
    // Managers de InfinixMob
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
    private RecipeManager recipeManager;
    private CooldownManager cooldownManager;

    // Managers de InfiniClassRPG
    private PlayerClassManager playerClassManager;
    private ClassConfigManager classConfigManager;
    private RpgSkillManager rpgSkillManager; // Renombrado para evitar conflictos

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

        // Guardar archivos de InfinixMob
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
        saveResource("Skills/mage-skills.yml", false);
        saveResource("Skills/archer-skills.yml", false);
        saveResource("Skills/paladin-skills.yml", false);
        saveResource("Skills/GolpeBasico.yml", false);
        
        // Guardar archivos de InfiniClassRPG
        saveResource("rpg/classes/mago.yml", false);
        saveResource("rpg/skills/mago-skills.yml", false);


        // Inicializar todos los managers de InfinixMob
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
        this.recipeManager = new RecipeManager(this);
        this.cooldownManager = new CooldownManager();

        // Inicializar managers de InfiniClassRPG
        this.classConfigManager = new ClassConfigManager(this);
        this.playerClassManager = new PlayerClassManager(this);
        this.rpgSkillManager = new RpgSkillManager(this);
        this.rpgSkillManager.loadSkills();
        ClassSelectionGUI classSelectionGUI = new ClassSelectionGUI(playerClassManager, classConfigManager);
        SkillsGUI skillsGUI = new SkillsGUI(playerClassManager, classConfigManager, rpgSkillManager);

        // Cargar configuraciones de clases
        this.classConfigManager.loadClasses();

        this.commandManager = new CommandManager(this);
        getCommand("infinixmob").setExecutor(commandManager);
        getCommand("infinixmob").setTabCompleter(commandManager);
        
        // Registrar comandos de InfiniClassRPG
        getCommand("clase").setExecutor(new ClaseCommand(playerClassManager, classConfigManager, classSelectionGUI));
        getCommand("rpgadmin").setExecutor(new RPGAdminCommand(playerClassManager, classConfigManager));
        getCommand("skills").setExecutor(new SkillsCommand(skillsGUI));
        getCommand("cast").setExecutor(new CastCommand(this, playerClassManager, rpgSkillManager));

        // Registrar todos los listeners de InfinixMob
        this.mobListener = new MobListener(this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(this.mobListener, this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemUpdaterListener(this), this);
        getServer().getPluginManager().registerEvents(new SmithingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        
        // Registrar listeners de InfiniClassRPG
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(playerClassManager), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(playerClassManager, classConfigManager), this);
        getServer().getPluginManager().registerEvents(classSelectionGUI, this);
        getServer().getPluginManager().registerEvents(skillsGUI, this);
        // getServer().getPluginManager().registerEvents(new MageListener(playerClassManager, classConfigManager, this), this); // LÍNEA COMENTADA/ELIMINADA
        getServer().getPluginManager().registerEvents(new ProjectileDamageListener(rpgSkillManager), this);
        getServer().getPluginManager().registerEvents(new SkillBindListener(this, playerClassManager, rpgSkillManager), this);

        // Cargar datos de jugadores ya conectados (en caso de /reload)
        for (Player player : getServer().getOnlinePlayers()) {
            playerClassManager.loadPlayerData(player);
        }

        reload();
        
        this.recipeManager.registerCustomRecipes();

        getLogger().info("InfinixMob ha sido activado!");
    }
    
    public class PlayerConnectionListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
        }
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
        // Guardar datos de todos los jugadores conectados antes de apagar
        for (Player player : getServer().getOnlinePlayers()) {
            playerClassManager.savePlayerData(player);
        }
        getLogger().info("InfinixMob ha sido deshabilitado.");
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

    // Getters de InfinixMob
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
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public Gson getGson() {
        return gson;
    }

    // Getters para InfiniClassRPG
    public PlayerClassManager getPlayerClassManager() {
        return playerClassManager;
    }

    public ClassConfigManager getClassConfigManager() {
        return classConfigManager;
    }
    
    public RpgSkillManager getRpgSkillManager() {
        return rpgSkillManager;
    }
}