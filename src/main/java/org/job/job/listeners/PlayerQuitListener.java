package org.job.job.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.job.job.Job;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Job.getInstance().getHudManager().removeBossBar(player);
    }
}
