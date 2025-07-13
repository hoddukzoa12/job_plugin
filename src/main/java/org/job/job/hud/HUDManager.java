package org.job.job.hud;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.job.job.Job;
import org.job.job.data.PlayerDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HUDManager {

    private final PlayerDataManager playerDataManager;
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();

    public HUDManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void createOrUpdateBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bossBar = playerBossBars.get(uuid);

        var config = playerDataManager.getPlayerConfig(uuid);
        int level = config.getInt("level", 1);
        int xp = config.getInt("xp", 0);
        int xpToLevelUp = level * 100; // Simple formula

        String title = String.format("§a[Lv. %d] §f경험치: §e%d / %d", level, xp, xpToLevelUp);
        double progress = (double) xp / xpToLevelUp;

        // Job.getInstance().getLogger().info(String.format("BossBar for %s: Level=%d, XP=%d, XPToLevelUp=%d, Progress=%.2f", player.getName(), level, xp, xpToLevelUp, progress));

        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(title, BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);
            playerBossBars.put(uuid, bossBar);
        } else {
            bossBar.setTitle(title);
        }
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
        bossBar.setVisible(true);
    }

    public void removeBossBar(Player player) {
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.setVisible(false);
        }
    }
}
