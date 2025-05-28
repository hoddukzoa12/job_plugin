package org.job.job.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.job.job.Job;
import org.job.job.gui.JobSelectionGUI;

import java.util.UUID;

public class ChangeJobCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        UUID uuid = player.getUniqueId();

        // 커스텀 경험치 및 레벨 초기화
        Job.getInstance().getFarmerLevelManager().reset(uuid);
        Job.getInstance().getFarmerHUDManager().removeHUD(player); // BossBar도 제거

        // 바닐라 레벨은 그대로 두기 (지금 설정)
        // 바닐라 레벨 초기화 필요 시 아래 주석 해제
        // player.setLevel(0);
        // player.setExp(0f);
        // player.setTotalExperience(0);

        // 직업 자체 초기화
        Job.getInstance().getJobManager().resetJob(uuid);

        // GUI 열기
        JobSelectionGUI.open(player);

        return true;
    }

}
