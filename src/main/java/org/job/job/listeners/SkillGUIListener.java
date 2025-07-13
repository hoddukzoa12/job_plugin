package org.job.job.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.job.job.Job;
import org.job.job.gui.SkillGUI;
import org.job.job.skills.SkillType;

public class SkillGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.equals("§a스킬 투자")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = clickedItem.getItemMeta().getDisplayName();
        if (displayName == null) return;

        SkillType clickedSkill = null;
        for (SkillType skill : SkillType.values()) {
            if (displayName.contains(skill.getSkillName())) {
                clickedSkill = skill;
                break;
            }
        }

        if (clickedSkill == null) return;

        var skillManager = Job.getInstance().getSkillManager();
        var skillConfigManager = Job.getInstance().getSkillConfigManager();

        if (clickedSkill.isPointSkill()) {
            int currentLevel = skillManager.getSkillLevel(player, clickedSkill);
            int maxLevel = skillConfigManager.getSkillMaxLevel(Job.getInstance().getPlayerDataManager().getPlayerConfig(player.getUniqueId()).getString("job"), clickedSkill.name());
            int pointsNeeded = skillConfigManager.getPointsPerLevel(Job.getInstance().getPlayerDataManager().getPlayerConfig(player.getUniqueId()).getString("job"), clickedSkill.name());
            int availablePoints = skillManager.getSkillPoints(player);

            if (currentLevel < maxLevel) {
                if (availablePoints >= pointsNeeded) {
                    var config = Job.getInstance().getPlayerDataManager().getPlayerConfig(player.getUniqueId());
                    skillManager.setSkillLevel(config, clickedSkill, currentLevel + 1);
                    skillManager.addSkillPoints(config, -pointsNeeded);
                    Job.getInstance().getPlayerDataManager().savePlayerConfig(player.getUniqueId(), config);
                    player.sendMessage("§a" + clickedSkill.getSkillName() + " 스킬이 " + (currentLevel + 1) + "레벨이 되었습니다!");
                    SkillGUI.open(player); // Refresh GUI
                } else {
                    player.sendMessage("§c스킬 포인트가 부족합니다! (" + pointsNeeded + " 필요)");
                }
            } else {
                player.sendMessage("§c" + clickedSkill.getSkillName() + " 스킬은 이미 최대 레벨입니다.");
            }
        } else { // Toggle skill
            // 스킬 레벨 제한 확인
            int playerLevel = Job.getInstance().getPlayerDataManager().getPlayerConfig(player.getUniqueId()).getInt("level", 1);
            int requiredLevel = clickedSkill.getLevelRequired();

            if (playerLevel < requiredLevel) {
                player.sendMessage("§c이 스킬을 사용하려면 레벨 " + requiredLevel + "이(가) 필요합니다.");
                SkillGUI.open(player); // Refresh GUI to show current state
                return;
            }

            boolean currentState = skillManager.getSkillToggle(player, clickedSkill);
            skillManager.setSkillToggle(player, clickedSkill, !currentState);
            player.sendMessage("§a" + clickedSkill.getSkillName() + " 스킬이 " + (!currentState ? "활성화" : "비활성화") + "되었습니다.");
            SkillGUI.open(player); // Refresh GUI
        }
    }
}
