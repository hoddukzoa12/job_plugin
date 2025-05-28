package org.job.job.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.job.job.Job;
import org.job.job.gui.JobSelectionGUI;
import org.job.job.jobs.JobType;

import java.util.UUID;

public class JobGUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // 제목을 getView().getTitle()로 가져오기
        String title = event.getView().getTitle();
        if (!title.equals("§a직업을 선택하세요")) return;

        event.setCancelled(true); // GUI 내 이동 금지

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        Material type = clicked.getType();
        if (type == Material.WOODEN_HOE) {
            JobSelectionGUI.assignJob(player, JobType.FARMER);
        } else if (type == Material.WOODEN_PICKAXE) {
            JobSelectionGUI.assignJob(player, JobType.MINER);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 직업이 아직 없는 경우 → 다시 선택 UI 열기
        if (Job.getInstance().getJobManager().getJob(uuid) == null) {
            Bukkit.getScheduler().runTaskLater(Job.getInstance(), () -> {
                JobSelectionGUI.open(player);
            }, 1L); // 한 틱 후에 열어야 안정적으로 작동
        }
    }

}
