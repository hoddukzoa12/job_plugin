package org.job.job.listeners.farmer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.job.job.data.PlayerDataManager;
import org.job.job.cooking.event.CookingCompleteEvent;
import org.job.job.jobs.JobType;
import org.job.job.skills.SkillManager;
import org.job.job.skills.SkillType;

public class FarmerCookingListener implements Listener {

    private final PlayerDataManager playerDataManager;
    private final SkillManager skillManager;

    public FarmerCookingListener(PlayerDataManager playerDataManager, SkillManager skillManager) {
        this.playerDataManager = playerDataManager;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onCookingComplete(CookingCompleteEvent event) {
        Player player = event.getPlayer();
        JobType playerJob = JobType.valueOf(playerDataManager.getPlayerConfig(player.getUniqueId()).getString("job"));

        // 요리 성공 여부와 관계없이 농부 스킬 적용
        if (playerJob == JobType.FARMER) {
            // COOKING_SUCCESS_RATE 스킬 적용 (성공률은 이미 GUIClickListener에서 처리됨)
            // COOKING_GREAT_SUCCESS 스킬 적용
            if (skillManager.getSkillLevel(player, SkillType.COOKING_GREAT_SUCCESS) > 0) {
                double greatSuccessChance = skillManager.getSkillConfigManager().getChancePerLevel(playerJob.name(), SkillType.COOKING_GREAT_SUCCESS.name()) * skillManager.getSkillLevel(player, SkillType.COOKING_GREAT_SUCCESS);
                if (Math.random() < greatSuccessChance) {
                    // 대성공 로직 (예: 결과물 2배 지급)
                    player.getInventory().addItem(event.getRecipe().getResult());
                    player.sendMessage("§a[농부 스킬] 요리 대성공! 결과물이 2배가 되었습니다!");
                }
            }

            // COOKING_TIME_REDUCTION 스킬 적용 (시간 감소는 GUIClickListener에서 처리되어야 함)
            // 이 리스너에서는 요리 완료 후 추가적인 효과만 다룸

            // 요리 경험치 지급 (농부 직업에만 해당)
            // TODO: SkillConfig.yml에 요리 경험치 관련 설정 추가 및 적용
            // 현재는 요리 자체로 경험치를 주지 않으므로, 이 부분은 추후 필요시 구현
        }
    }
}
