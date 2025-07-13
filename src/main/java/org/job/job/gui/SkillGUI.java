package org.job.job.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.job.job.Job;
import org.job.job.jobs.JobType;
import org.job.job.skills.SkillType;

import java.util.ArrayList;
import java.util.List;

public class SkillGUI {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§a스킬 투자");

        var skillManager = Job.getInstance().getSkillManager();
        var playerDataManager = Job.getInstance().getPlayerDataManager();
        var skillConfigManager = Job.getInstance().getSkillConfigManager();

        String playerJobName = playerDataManager.getPlayerConfig(player.getUniqueId()).getString("job", "NONE");
        JobType playerJob = JobType.valueOf(playerJobName);

        // Display skill points
        ItemStack skillPointsItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta spMeta = skillPointsItem.getItemMeta();
        spMeta.setDisplayName("§e남은 스킬 포인트: §b" + skillManager.getSkillPoints(player));
        skillPointsItem.setItemMeta(spMeta);
        gui.setItem(0, skillPointsItem);

        int slot = 9;
        for (SkillType skill : SkillType.values()) {
            // Only show skills relevant to the player's job
            if (!skill.getJobName().equalsIgnoreCase(playerJob.getJobName())) {
                continue;
            }

            ItemStack skillItem = new ItemStack(Material.BOOK);
            ItemMeta meta = skillItem.getItemMeta();
            List<String> lore = new ArrayList<>();

            int currentLevel = skillManager.getSkillLevel(player, skill);
            meta.setDisplayName("§f" + skill.getSkillName() + " §7[레벨: " + currentLevel + "]");

            String description = skillConfigManager.getSkillDescription(playerJob.name(), skill.name());
            lore.add("§7" + description);
            lore.add("");

            if (skill.isPointSkill()) {
                int maxLevel = skillConfigManager.getSkillMaxLevel(playerJob.name(), skill.name());
                int pointsToNextLevel = skillConfigManager.getPointsPerLevel(playerJob.name(), skill.name());

                lore.add("§7현재 레벨: " + currentLevel + " / " + maxLevel);
                lore.add("§7다음 레벨까지 필요한 포인트: " + pointsToNextLevel);
                lore.add("");
                lore.add("§e클릭하여 스킬 레벨업");
            } else { // Auto-acquired skills
                boolean isEnabled = skillManager.getSkillToggle(player, skill);
                lore.add("§7상태: " + (isEnabled ? "§a활성화" : "§c비활성화"));
                lore.add("");
                lore.add("§e클릭하여 스킬 토글");
            }

            meta.setLore(lore);
            skillItem.setItemMeta(meta);
            gui.setItem(slot++, skillItem);
        }

        player.openInventory(gui);
    }
}
