package org.job.job;

import org.bukkit.plugin.java.JavaPlugin;
import org.job.job.commands.ChangeJobCommand;
import org.job.job.config.CropXPConfig;
import org.job.job.jobs.JobManager;
import org.job.job.listeners.*;
import org.job.job.listeners.farmer.FarmerHarvestListener;
import org.job.job.listeners.farmer.FarmerLevelListener;
import org.job.job.skills.farmer.FarmerHUDManager;
import org.job.job.skills.farmer.FarmerLevelManager;
import org.job.job.skills.farmer.FarmerSkillManager;

public final class Job extends JavaPlugin {

    private FarmerSkillManager farmerSkillManager;
    private static Job instance;
    private JobManager jobManager;
    private CropXPConfig cropXPConfig;
    private FarmerLevelManager farmerLevelManager;
    private FarmerHUDManager farmerHUDManager;

    public static Job getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        jobManager = new JobManager();
        farmerSkillManager = new FarmerSkillManager();
        cropXPConfig = new CropXPConfig();
        farmerLevelManager = new FarmerLevelManager();
        farmerHUDManager = new FarmerHUDManager();


        getCommand("changejob").setExecutor(new ChangeJobCommand());
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new JobGUIListener(), this);
        getServer().getPluginManager().registerEvents(new FarmerLevelListener(), this);
        getServer().getPluginManager().registerEvents(new FarmerHarvestListener(), this);
        getServer().getPluginManager().registerEvents(new JoinQuitHUDListener(), this);


    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public FarmerSkillManager getFarmerSkillManager() {
        return farmerSkillManager;
    }
    public CropXPConfig getCropXPConfig() {
        return cropXPConfig;
    }
    public FarmerLevelManager getFarmerLevelManager() {
        return farmerLevelManager;
    }

    public FarmerHUDManager getFarmerHUDManager() {
        return farmerHUDManager;
    }
    @Override
    public void onDisable() {
        jobManager.saveJobs(); // 서버 종료 시 저장
    }

}
