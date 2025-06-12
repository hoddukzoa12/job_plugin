package org.job.job.listeners.farmer;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.job.job.config.RareDropConfig;
import org.job.job.jobs.JobManager;
import org.job.job.util.CustomItemsCrops;

import java.util.Random;
import java.util.UUID;

public class RareCropDropListener implements Listener {

    private final CustomItemsCrops customItems;
    private final RareDropConfig rareDropConfig;
    private final JobManager jobManager;
    private final Random random = new Random();

    public RareCropDropListener(CustomItemsCrops customItems, RareDropConfig rareDropConfig, JobManager jobManager) {
        this.customItems = customItems;
        this.rareDropConfig = rareDropConfig;
        this.jobManager = jobManager;
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();
        UUID uuid = player.getUniqueId();

        // ✅ 농부 직업 확인
        if (!jobManager.isPlayerInJob(uuid, "farmer")) return;

        // ✅ rare-drops.yml에 등록된 작물인지 확인
        RareDropConfig.CropDropData data = rareDropConfig.getData(type);
        if (data == null) return;

        if (type != Material.SUGAR_CANE) {
            // ✅ 성숙 여부 확인 (Ageable만 처리)
            if (block.getBlockData() instanceof Ageable ageable) {
                int age = ageable.getAge();
                int max = ageable.getMaximumAge();

                if (age < max) return; // ❌ 미성숙 작물이면 경험치 주지 않음
            }
        }

        // ✅ 확률 계산
        if (random.nextDouble() <= data.chance) {
            ItemStack drop = customItems.getSpecialCrop(type);
            if (drop != null) {
                player.getInventory().addItem(drop); // ✅ 인벤토리에 직접 추가
                String itemName = drop.getItemMeta().hasDisplayName() ? drop.getItemMeta().getDisplayName() : drop.getType().name();
                player.sendMessage("§a✨ 희귀 작물 §6[" + itemName + "§6]§a을 획득했습니다!");

            } else {
                player.sendMessage("§c[디버그] 아이템 생성 실패 (null)");
            }
        }
    }

}
