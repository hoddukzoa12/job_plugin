package org.job.job.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.job.job.Job;
import org.job.job.jobs.JobType;

public class JobSelectionGUI {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§a직업을 선택하세요");

        gui.setItem(3, createSelectionItem(Material.WOODEN_HOE, "§a농부", "작물을 수확하는 전문가"));
        gui.setItem(5, createSelectionItem(Material.WOODEN_PICKAXE, "§9광부", "광물을 캐는 전문가"));

        player.openInventory(gui);
    }

    private static ItemStack createSelectionItem(Material material, String name, String loreText) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.List.of("§7" + loreText, "§e클릭하여 선택"));
        item.setItemMeta(meta);
        return item;
    }

    // 외부에서 GUI 클릭 시 사용할 메서드
    public static void assignJob(Player player, JobType job) {
        Job.getInstance().getJobManager().setJob(player.getUniqueId(), job);
        giveJobTool(player, job);
        player.sendMessage("§a" + job.name() + " 직업을 선택했습니다!");
        player.closeInventory();
    }

    private static void giveJobTool(Player player, JobType job) {
        ItemStack tool;

        switch (job) {
            case FARMER -> tool = new ItemStack(Material.WOODEN_HOE);
            case MINER -> tool = new ItemStack(Material.WOODEN_PICKAXE);
            default -> {
                return;
            }
        }

        ItemMeta meta = tool.getItemMeta();
        meta.setDisplayName("§6[" + job.name() + "의 도구]");
        meta.setUnbreakable(true); // ✅ 내구도 무제한
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Job.getInstance(), "jobtool"),
                PersistentDataType.STRING,
                job.name()
        );
        tool.setItemMeta(meta);

        player.getInventory().addItem(tool);
    }

}
