package org.job.job.skills;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.job.job.Job;
import org.job.job.config.SkillConfigManager;
import org.job.job.data.PlayerDataManager;

public class SkillManager {

    private final PlayerDataManager playerDataManager;
    private final SkillConfigManager skillConfigManager;

    public SkillManager(PlayerDataManager playerDataManager, SkillConfigManager skillConfigManager) {
        this.playerDataManager = playerDataManager;
        this.skillConfigManager = skillConfigManager;
    }

    public int getSkillLevel(Player player, SkillType skill) {
        return playerDataManager.getPlayerConfig(player.getUniqueId()).getInt("skill-levels." + skill.name(), 0);
    }

    public void setSkillLevel(FileConfiguration config, SkillType skill, int level) {
        config.set("skill-levels." + skill.name(), level);
    }

    public int getSkillPoints(Player player) {
        return playerDataManager.getPlayerConfig(player.getUniqueId()).getInt("skill-points", 0);
    }

    public void addSkillPoints(FileConfiguration config, int amount) {
        int currentPoints = config.getInt("skill-points", 0);
        config.set("skill-points", currentPoints + amount);
    }

    public SkillConfigManager getSkillConfigManager() {
        return skillConfigManager;
    }

    public boolean getSkillToggle(Player player, SkillType skill) {
        return playerDataManager.getPlayerConfig(player.getUniqueId()).getBoolean("skill-toggles." + skill.name(), false);
    }

    public void setSkillToggle(Player player, SkillType skill, boolean enabled) {
        var config = playerDataManager.getPlayerConfig(player.getUniqueId());
        config.set("skill-toggles." + skill.name(), enabled);
        playerDataManager.savePlayerConfig(player.getUniqueId(), config);
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
