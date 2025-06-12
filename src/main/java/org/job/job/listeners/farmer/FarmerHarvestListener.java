package org.job.job.listeners.farmer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.job.job.Job;
import org.job.job.jobs.JobType;
import org.job.job.skills.farmer.FarmerLevelManager;

public class FarmerHarvestListener implements Listener {

    @EventHandler
    public void onHarvest(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        if (Job.getInstance().getJobManager().getJob(player.getUniqueId()) != JobType.FARMER) return;
        if (type != Material.SUGAR_CANE) {
            // ✅ 성숙 여부 확인 (Ageable만 처리)
            if (block.getBlockData() instanceof Ageable ageable) {
                int age = ageable.getAge();
                int max = ageable.getMaximumAge();

                if (age < max) return; // ❌ 미성숙 작물이면 경험치 주지 않음
            }
        }
        int xp = Job.getInstance().getCropXPConfig().getXP(type);
        if (xp <= 0) return;

        FarmerLevelManager levelManager = Job.getInstance().getFarmerLevelManager();
        levelManager.addExp(player.getUniqueId(), xp);
    }
}
