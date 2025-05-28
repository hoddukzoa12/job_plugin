package org.job.job.listeners.farmer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.job.job.Job;
import org.job.job.jobs.JobType;
import org.job.job.skills.farmer.FarmerSkillManager;

public class FarmerLevelListener implements Listener {

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();

        if (Job.getInstance().getJobManager().getJob(player.getUniqueId()) != JobType.FARMER) return;

        int oldLevel = event.getOldLevel();
        int newLevel = event.getNewLevel();

        if (newLevel > oldLevel) {
            FarmerSkillManager skillManager = Job.getInstance().getFarmerSkillManager();
            skillManager.addSkillPoint(player.getUniqueId(), 1);
            player.sendMessage("§a레벨 업! 스킬 포인트 +1");
        }
    }
}
