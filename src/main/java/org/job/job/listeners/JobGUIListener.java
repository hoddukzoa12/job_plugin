package org.job.job.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.job.job.Job;
import org.job.job.gui.JobSelectionGUI;
import org.job.job.jobs.JobType;
import org.job.job.util.ItemUtils;

public class JobGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.equals("§a직업을 선택하세요")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        String itemType = ItemUtils.getJobItemType(clickedItem);
        if (itemType == null) return;

        switch (itemType) {
            case "JOB_SELECT_FARMER" -> JobSelectionGUI.assignJob(player, JobType.FARMER);
            case "JOB_SELECT_FISHERMAN" -> JobSelectionGUI.assignJob(player, JobType.FISHERMAN);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        var config = Job.getInstance().getPlayerDataManager().getPlayerConfig(player.getUniqueId());

        // 직업이 아직 없는 경우, 1틱 후에 다시 GUI를 엽니다.
        if (config.getString("job", "NONE").equals("NONE")) {
            Bukkit.getScheduler().runTaskLater(Job.getInstance(), () -> JobSelectionGUI.open(player), 1L);
        }
    }
}
