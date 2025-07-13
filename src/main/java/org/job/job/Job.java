package org.job.job;

import org.bukkit.plugin.java.JavaPlugin;
import org.job.job.commands.ChangeJobCommand;
import org.job.job.commands.JobCommand;
import org.job.job.config.CropXPConfig;
import org.job.job.config.RareDropConfig;
import org.job.job.config.SkillConfigManager;
import org.job.job.data.PlayerDataManager;
import org.job.job.hud.HUDManager;
import org.job.job.listeners.JobGUIListener;
import org.job.job.listeners.PlayerJoinListener;
import org.job.job.listeners.PlayerQuitListener;
import org.job.job.listeners.farmer.FarmerHarvestListener;
import org.job.job.listeners.farmer.FarmerToolListener;
import org.job.job.listeners.SkillGUIListener;
import org.job.job.skills.SkillManager;

import org.job.job.handlers.FarmerActionHandler;
import org.job.job.cooking.CookingManager;

public final class Job extends JavaPlugin {

    private static Job instance;
    private PlayerDataManager playerDataManager;
    private SkillConfigManager skillConfigManager;
    private SkillManager skillManager;
    private CropXPConfig cropXPConfig;
    private RareDropConfig rareDropConfig;
    private HUDManager hudManager;
    private FarmerActionHandler farmerActionHandler;
    private CookingManager cookingManager;

    public static Job getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Load default config
        saveDefaultConfig();

        // Configs
        cropXPConfig = new CropXPConfig();
        rareDropConfig = new RareDropConfig();
        skillConfigManager = new SkillConfigManager(this);

        // Managers
        playerDataManager = new PlayerDataManager(this);
        skillManager = new SkillManager(playerDataManager, skillConfigManager);
        hudManager = new HUDManager(playerDataManager);

        // Handlers
        farmerActionHandler = new FarmerActionHandler(this, playerDataManager, skillManager);

        // Cooking Manager
        cookingManager = new CookingManager(this, skillManager);
        cookingManager.enable();

        // Register Commands
        getCommand("changejob").setExecutor(new ChangeJobCommand());
        getCommand("job").setExecutor(new JobCommand());

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(playerDataManager), this);
        getServer().getPluginManager().registerEvents(new JobGUIListener(), this);
        getServer().getPluginManager().registerEvents(new FarmerToolListener(skillManager), this);
        getServer().getPluginManager().registerEvents(new FarmerHarvestListener(this), this);
        getServer().getPluginManager().registerEvents(new SkillGUIListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new org.job.job.listeners.farmer.HarvestMasteryListener(this, playerDataManager), this);
        getServer().getPluginManager().registerEvents(new org.job.job.listeners.farmer.FarmerCookingListener(playerDataManager, skillManager), this);
        getServer().getPluginManager().registerEvents(new org.job.job.listeners.ItemProtectionListener(), this);

        getLogger().info("Job Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Job Plugin has been disabled!");
    }

    public int getXpForLevelUp(int level) {
        // 기본 공식: base + (per-level * (level - 1))
        // 예: level 1 -> 100, level 2 -> 150, level 3 -> 200
        int base = getConfig().getInt("level-up.base", 100);
        int perLevel = getConfig().getInt("level-up.per-level", 50);
        return base + (perLevel * (level - 1));
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public SkillConfigManager getSkillConfigManager() {
        return skillConfigManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public CropXPConfig getCropXPConfig() {
        return cropXPConfig;
    }

    public RareDropConfig getRareDropConfig() {
        return rareDropConfig;
    }

    public HUDManager getHudManager() {
        return hudManager;
    }

    public FarmerActionHandler getFarmerActionHandler() {
        return farmerActionHandler;
    }
}
