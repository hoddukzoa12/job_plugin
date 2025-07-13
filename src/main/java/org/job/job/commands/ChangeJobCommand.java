package org.job.job.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.job.job.Job;
import org.job.job.gui.JobSelectionGUI;
import org.job.job.skills.SkillType;

public class ChangeJobCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        // Reset all player data related to job
        var playerDataConfig = Job.getInstance().getPlayerDataManager().getPlayerConfig(player.getUniqueId());
        playerDataConfig.set("job", "NONE");
        playerDataConfig.set("level", 1);
        playerDataConfig.set("xp", 0);
        playerDataConfig.set("skill-points", 0);
        for (SkillType skill : SkillType.values()) {
            playerDataConfig.set("skill-levels." + skill.name(), 0);
            if (!skill.isPointSkill()) { // Reset auto-toggles as well
                playerDataConfig.set("skill-toggles." + skill.name(), false);
            }
        }
        Job.getInstance().getPlayerDataManager().savePlayerConfig(player.getUniqueId(), playerDataConfig);

        // Open GUI
        JobSelectionGUI.open(player);
        player.sendMessage("§a직업이 초기화되었습니다. 새로운 직업을 선택해주세요.");

        return true;
    }
}
