package org.job.job.skills.farmer;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.job.job.Job;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FarmerHUDManager {

    private final Map<UUID, BossBar> bars = new HashMap<>();

    public FarmerLevelManager updateHUD(Player player) {
        UUID uuid = player.getUniqueId();
        FarmerLevelManager levelManager = Job.getInstance().getFarmerLevelManager();

        int level = levelManager.getLevel(uuid);
        int current = levelManager.getExp(uuid);
        int required = levelManager.getRequiredExp(level);

        BossBar bar = bars.get(uuid);
        if (bar == null) {
            bar = Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SEGMENTED_10); // ✅ 노란색으로 설정
            bar.addPlayer(player);
            bars.put(uuid, bar);
        }


        bar.setTitle("§e농부 레벨 §6Lv." + level + " §7(" + current + " / " + required + ")");
        double progress = required > 0 ? Math.min(1.0, (double) current / required) : 0.0;
        bar.setProgress(progress);

        bar.setVisible(true);
        return levelManager;
    }

    public void removeHUD(Player player) {
        BossBar bar = bars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void removeAll() {
        for (BossBar bar : bars.values()) {
            bar.removeAll();
        }
        bars.clear();
    }
}
