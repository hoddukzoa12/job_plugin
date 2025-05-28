package org.job.job.skills.farmer;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.job.job.Job;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FarmerLevelManager {

    private final Map<UUID, Integer> expMap = new HashMap<>();
    private final Map<UUID, Integer> levelMap = new HashMap<>();
    private final Map<UUID, Integer> skillPointMap = new HashMap<>();

    private final File file;
    private final YamlConfiguration config;

    public FarmerLevelManager() {
        file = new File(Job.getInstance().getDataFolder(), "farmer-data.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void addExp(UUID uuid, int amount) {
        int current = expMap.getOrDefault(uuid, 0);
        int level = levelMap.getOrDefault(uuid, 1);

        current += amount;
        boolean leveledUp = false;
        int levelUps = 0;

        while (current >= getRequiredExp(level)) {
            current -= getRequiredExp(level);
            level++;
            levelUps++;
            leveledUp = true;
        }

        expMap.put(uuid, current);
        levelMap.put(uuid, level);

        // ✅ 스킬 포인트 지급
        if (levelUps > 0) {
            int currentPoints = skillPointMap.getOrDefault(uuid, 0);
            skillPointMap.put(uuid, currentPoints + levelUps);
        }

        save(uuid);

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            if (leveledUp) {
                int totalSkillPoints = getSkillPoints(uuid);
                player.sendMessage("§a농부 레벨이 올랐습니다! §6Lv." + level);
                player.sendMessage("§b스킬 포인트 §e+" + levelUps + " §7(총 " + totalSkillPoints + " 포인트)");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }

            Job.getInstance().getFarmerHUDManager().updateHUD(player);
        }
    }

    public int getLevel(UUID uuid) {
        return levelMap.getOrDefault(uuid, 1);
    }

    public int getExp(UUID uuid) {
        return expMap.getOrDefault(uuid, 0);
    }

    public int getRequiredExp(int level) {
        return 100 * Math.max(1, level);
    }

    public int getSkillPoints(UUID uuid) {
        return skillPointMap.getOrDefault(uuid, 0);
    }

    public void setSkillPoints(UUID uuid, int amount) {
        skillPointMap.put(uuid, amount);
        save(uuid);
    }

    public void reset(UUID uuid) {
        expMap.remove(uuid);
        levelMap.remove(uuid);
        skillPointMap.remove(uuid);
        config.set(uuid.toString(), null);
        saveConfig();
    }

    private void save(UUID uuid) {
        config.set(uuid.toString() + ".level", getLevel(uuid));
        config.set(uuid.toString() + ".exp", getExp(uuid));
        config.set(uuid.toString() + ".skill-points", getSkillPoints(uuid));
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int level = config.getInt(key + ".level", 1);
                int exp = config.getInt(key + ".exp", 0);
                int skillPoints = config.getInt(key + ".skill-points", 0);

                levelMap.put(uuid, level);
                expMap.put(uuid, exp);
                skillPointMap.put(uuid, skillPoints);
            } catch (Exception e) {
                Job.getInstance().getLogger().warning("[FarmerLevelManager] 잘못된 UUID: " + key);
            }
        }
    }
}
