package org.job.job.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.job.job.Job;
import org.job.job.config.RareDropConfig;
import org.job.job.data.PlayerDataManager;
import org.job.job.skills.SkillManager;
import org.job.job.skills.SkillType;
import org.job.job.util.CustomItemsCrops;

import java.util.Random;

public class FarmerActionHandler {

    private final Job plugin;
    private final PlayerDataManager playerDataManager;
    private final SkillManager skillManager;
    private final Random random = new Random();

    public FarmerActionHandler(Job plugin, PlayerDataManager playerDataManager, SkillManager skillManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.skillManager = skillManager;
    }

    public void processXP(Player player, Material cropType) {
        int xp = plugin.getCropXPConfig().getXP(cropType);
        if (xp <= 0) return;

        var config = playerDataManager.getPlayerConfig(player.getUniqueId());
        int currentXp = config.getInt("xp", 0);
        int currentLevel = config.getInt("level", 1);
        int newXp = currentXp + xp;
        int xpToLevelUp = plugin.getXpForLevelUp(currentLevel);

        if (newXp >= xpToLevelUp) {
            int newLevel = currentLevel + 1;
            config.set("level", newLevel);
            config.set("xp", newXp - xpToLevelUp);
            skillManager.addSkillPoints(config, 1);
            player.sendMessage("§a레벨 업! 이제 " + newLevel + "레벨입니다. 스킬 포인트를 1 획득했습니다.");
        } else {
            config.set("xp", newXp);
        }
        playerDataManager.savePlayerConfig(player.getUniqueId(), config);
        plugin.getHudManager().createOrUpdateBossBar(player);
    }

    public ItemStack processRareDrops(Player player, Material cropType, Location location) {
        RareDropConfig.CropDropData dropData = plugin.getRareDropConfig().getData(cropType);
        if (dropData == null) return null;

        double baseChance = dropData.chance;
        int skillLevel = skillManager.getSkillLevel(player, SkillType.RARE_CROP_CHANCE);
        double bonusChance = skillManager.getSkillConfigManager().getChancePerLevel("FARMER", "RARE_CROP_CHANCE") * skillLevel;
        double finalChance = baseChance + bonusChance;

        if (random.nextDouble() < finalChance) {
            ItemStack rareDrop = CustomItemsCrops.createRareCrop(dropData);
            player.sendMessage("§e희귀 작물을 발견했습니다!");
            return rareDrop;
        }
        return null;
    }
}
