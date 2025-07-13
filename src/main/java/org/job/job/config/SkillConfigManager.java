package org.job.job.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.job.job.Job;

import java.io.File;

public class SkillConfigManager {

    private final Job plugin;
    private FileConfiguration skillConfig;

    public SkillConfigManager(Job plugin) {
        this.plugin = plugin;
        loadSkillConfig();
    }

    private void loadSkillConfig() {
        File skillConfigFile = new File(plugin.getDataFolder(), "SkillConfig.yml");
        if (!skillConfigFile.exists()) {
            plugin.saveResource("SkillConfig.yml", false);
        }
        skillConfig = YamlConfiguration.loadConfiguration(skillConfigFile);
        plugin.getLogger().info("Loaded SkillConfig.yml: " + skillConfig.saveToString());
    }

    public FileConfiguration getConfig() {
        return skillConfig;
    }

    public int getSkillMaxLevel(String job, String skill) {
        return skillConfig.getInt("skills." + job + "." + skill + ".max-level", 1);
    }

    public int getPointsPerLevel(String job, String skill) {
        return skillConfig.getInt("skills." + job + "." + skill + ".points-per-level", 1);
    }

    public double getChancePerLevel(String job, String skill) {
        return skillConfig.getDouble("skills." + job + "." + skill + ".chance-per-level", 0.0);
    }

    public double getSuccessRatePerLevel(String job, String skill) {
        double value = skillConfig.getDouble("skills." + job + "." + skill + ".success-rate-per-level", 0.0);
        plugin.getLogger().info("getSuccessRatePerLevel for " + job + "." + skill + ": " + value);
        return value;
    }

    public double getGreatSuccessChancePerLevel(String job, String skill) {
        double value = skillConfig.getDouble("skills." + job + "." + skill + ".great-success-chance-per-level", 0.0);
        plugin.getLogger().info("getGreatSuccessChancePerLevel for " + job + "." + skill + ": " + value);
        return value;
    }

    public int getRangeForLevel(String job, String skill, int level) {
        return skillConfig.getInt("skills." + job + "." + skill + ".range-per-level." + level, 0);
    }

    public int getHarvestMasteryRange(int skillLevel) {
        int baseRange = skillConfig.getInt("skills.FARMER.HARVEST_MASTERY.base-range", 3); // Default 3x3
        int rangePerLevel = skillConfig.getInt("skills.FARMER.HARVEST_MASTERY.range-per-level", 1); // Default +1 per level
        return baseRange + (skillLevel * rangePerLevel);
    }

    public int getHarvestMasteryDuration() {
        return skillConfig.getInt("skills.FARMER.HARVEST_MASTERY.duration-seconds", 8); // Default 8 seconds
    }

    public long getHarvestMasteryCooldown() {
        return skillConfig.getLong("skills.FARMER.HARVEST_MASTERY.cooldown-seconds", 60); // Default 60 seconds (1 minute)
    }

    public String getSkillDescription(String job, String skill) {
        return skillConfig.getString("skills." + job + "." + skill + ".description", "설명 없음");
    }

    public double getTimeReductionPerLevel(String job, String skill) {
        double value = skillConfig.getDouble("skills." + job + "." + skill + ".time-reduction-per-level", 0.0);
        plugin.getLogger().info("getTimeReductionPerLevel for " + job + "." + skill + ": " + value);
        return value;
    }
}
