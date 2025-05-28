package org.job.job.jobs;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.job.job.Job;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JobManager {

    private final Map<UUID, JobType> playerJobs = new HashMap<>();
    private final File file;
    private final FileConfiguration config;

    public JobManager() {
        file = new File(Job.getInstance().getDataFolder(), "jobs.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadJobs();
    }

    public void setJob(UUID uuid, JobType job) {
        playerJobs.put(uuid, job);
        config.set(uuid.toString(), job.name());
        saveJobs();
    }

    public void resetJob(UUID uuid) {
        playerJobs.remove(uuid);
        config.set(uuid.toString(), null);
        saveJobs();
    }

    public JobType getJob(UUID uuid) {
        return playerJobs.get(uuid);
    }

    public boolean hasJob(UUID uuid) {
        return playerJobs.containsKey(uuid);
    }

    public void loadJobs() {
        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            try {
                JobType job = JobType.valueOf(config.getString(key));
                playerJobs.put(uuid, job);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[JobSystem] 알 수 없는 직업: " + config.getString(key));
            }
        }
    }

    public void saveJobs() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
