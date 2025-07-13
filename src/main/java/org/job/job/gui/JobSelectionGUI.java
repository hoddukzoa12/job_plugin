package org.job.job.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.job.job.Job;
import org.job.job.jobs.JobType;
import org.job.job.util.ItemUtils;

import java.util.List;

public class JobSelectionGUI {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§a직업을 선택하세요"); // Increased size for more options

        ItemStack farmerItem = ItemUtils.createJobItem(
                Material.WOODEN_HOE,
                "§a농부로 전직",
                List.of("§7작물을 재배하고 수확하는 전문가.", "§e클릭하여 농부로 전직합니다."),
                "JOB_SELECT_FARMER"
        );

        ItemStack minerItem = ItemUtils.createJobItem(
                Material.WOODEN_PICKAXE,
                "§9광부로 전직",
                List.of("§7광물을 채굴하고 지하를 탐험하는 전문가.", "§e클릭하여 광부로 전직합니다."),
                "JOB_SELECT_MINER"
        );

        ItemStack fishermanItem = ItemUtils.createJobItem(
                Material.FISHING_ROD,
                "§b어부로 전직",
                List.of("§7물고기를 낚고 바다를 탐험하는 전문가.", "§e클릭하여 어부로 전직합니다."),
                "JOB_SELECT_FISHERMAN"
        );

        gui.setItem(11, farmerItem); // Centered for 3 options
        gui.setItem(13, minerItem);
        gui.setItem(15, fishermanItem);

        player.openInventory(gui);
    }

    public static void assignJob(Player player, JobType job) {
        var playerDataManager = Job.getInstance().getPlayerDataManager();
        var config = playerDataManager.getPlayerConfig(player.getUniqueId());

        

        config.set("job", job.name());
        playerDataManager.savePlayerConfig(player.getUniqueId(), config);

        giveJobTool(player, job);
        player.sendMessage("§a[" + job.getJobName() + "] 직업을 선택했습니다!");
        player.closeInventory();
    }

    private static void giveJobTool(Player player, JobType job) {
        ItemStack tool = null;
        switch (job) {
            case FARMER -> tool = ItemUtils.createJobItem(
                    Material.WOODEN_HOE,
                    "§6농부의 괭이",
                    List.of("§7농부의 영혼이 깃든 괭이입니다."),
                    "FARMER_HOE"
            );
            case MINER -> tool = ItemUtils.createJobItem(
                    Material.WOODEN_PICKAXE,
                    "§9광부의 곡괭이",
                    List.of("§7광부의 열정이 담긴 곡괭이입니다."),
                    "MINER_PICKAXE"
            );
            case FISHERMAN -> tool = ItemUtils.createJobItem(
                    Material.FISHING_ROD,
                    "§b어부의 낚싯대",
                    List.of("§7어부의 열정이 담긴 낚싯대입니다."),
                    "FISHERMAN_ROD"
            );
        }

        if (tool != null) {
            player.getInventory().addItem(tool);
        }
    }
}
