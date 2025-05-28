package org.job.job.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.job.job.Job;
import org.job.job.jobs.JobType;

public class JoinQuitHUDListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Job.getInstance().getJobManager().getJob(player.getUniqueId()) == JobType.FARMER) {
            Job.getInstance().getFarmerHUDManager().updateHUD(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Job.getInstance().getFarmerHUDManager().removeHUD(event.getPlayer());
    }
}
