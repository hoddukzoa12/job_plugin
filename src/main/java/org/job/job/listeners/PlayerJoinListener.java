package org.job.job.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.job.job.Job;
import org.job.job.data.PlayerDataManager;

public class PlayerJoinListener implements Listener {

    private final PlayerDataManager playerDataManager;

    public PlayerJoinListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerDataManager.createPlayerData(player);
        Job.getInstance().getLogger().info("Player " + player.getName() + " joined. Creating/Updating BossBar.");
        Job.getInstance().getHudManager().createOrUpdateBossBar(player);
    }
}
