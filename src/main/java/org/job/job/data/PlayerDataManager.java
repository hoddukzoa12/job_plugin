package org.job.job.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.job.job.Job;
import org.job.job.skills.SkillType;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {

    private final Job plugin;

    public PlayerDataManager(Job plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getPlayerConfig(UUID uuid) {
        File playerFile = new File(plugin.getDataFolder(), "playerdata/" + uuid.toString() + ".yml");
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    public void savePlayerConfig(UUID uuid, FileConfiguration config) {
        File playerFile = new File(plugin.getDataFolder(), "playerdata/" + uuid.toString() + ".yml");
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + uuid);
            e.printStackTrace();
        }
    }

    public void createPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(plugin.getDataFolder(), "playerdata/" + uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            playerFile.getParentFile().mkdirs();
            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

            playerConfig.set("username", player.getName());
            playerConfig.set("job", "NONE");
            playerConfig.set("level", 1);
            playerConfig.set("xp", 0);
            playerConfig.set("skill-points", 0);

            for (SkillType skill : SkillType.values()) {
                playerConfig.set("skill-levels." + skill.name(), 0);
            }

            playerConfig.set("skill-toggles.AUTO_REPLANT", false);
            playerConfig.set("skill-toggles.AUTO_PICKUP", false);

            savePlayerConfig(uuid, playerConfig);
        }
    }
}
