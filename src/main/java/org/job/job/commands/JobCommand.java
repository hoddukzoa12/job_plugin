package org.job.job.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.job.job.Job;
import org.job.job.gui.SkillGUI;
import org.job.job.skills.SkillType;

public class JobCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§c사용법: /job <skill|toggle>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "skill":
                SkillGUI.open(player);
                break;
            case "toggle":
                if (args.length < 2) {
                    player.sendMessage("§c사용법: /job toggle <자동심기|자동줍기>");
                    return true;
                }
                SkillType skillToToggle = null;
                if (args[1].equalsIgnoreCase("자동심기")) {
                    skillToToggle = SkillType.AUTO_REPLANT;
                } else if (args[1].equalsIgnoreCase("자동줍기")) {
                    skillToToggle = SkillType.AUTO_PICKUP;
                }

                if (skillToToggle == null) {
                    player.sendMessage("§c알 수 없는 스킬입니다. (자동심기, 자동줍기 중 선택)");
                    return true;
                }

                // 스킬 레벨 제한 확인
                int playerLevel = Job.getInstance().getPlayerDataManager().getPlayerConfig(player.getUniqueId()).getInt("level", 1);
                int requiredLevel = skillToToggle.getLevelRequired();

                if (playerLevel < requiredLevel) {
                    player.sendMessage("§c이 스킬을 사용하려면 레벨 " + requiredLevel + "이(가) 필요합니다.");
                    return true;
                }

                boolean currentState = Job.getInstance().getSkillManager().getSkillToggle(player, skillToToggle);
                Job.getInstance().getSkillManager().setSkillToggle(player, skillToToggle, !currentState);
                player.sendMessage("§a" + skillToToggle.getSkillName() + " 스킬이 " + (!currentState ? "활성화" : "비활성화") + "되었습니다.");
                break;
            default:
                player.sendMessage("§c알 수 없는 명령어입니다. (skill, toggle 중 선택)");
                break;
        }

        return true;
    }
}
