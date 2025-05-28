package org.job.job.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.job.job.Job;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CropXPConfig {

    private final Map<Material, Integer> xpMap = new HashMap<>();

    public CropXPConfig() {
        loadConfig();
    }

    public void loadConfig() {
        File file = new File(Job.getInstance().getDataFolder(), "crop-xp.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                // 기본값 저장
                config.set("WHEAT", 2);
                config.set("CARROTS", 3);
                config.set("POTATOES", 5);
                config.set("BEETROOTS", 2);
                config.set("MELON", 4);
                config.set("PUMPKIN", 4);

                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        xpMap.clear();

        for (String key : config.getKeys(false)) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                int xp = config.getInt(key);
                xpMap.put(material, xp);
            } catch (IllegalArgumentException e) {
                Job.getInstance().getLogger().warning("알 수 없는 재료(Material): " + key);
            }
        }
    }

    public int getXP(Material material) {
        return xpMap.getOrDefault(material, 0);
    }

    public boolean isConfigured(Material material) {
        return xpMap.containsKey(material);
    }
}
