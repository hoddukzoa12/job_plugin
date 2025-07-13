package org.job.job.listeners.farmer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.job.job.Job;
import org.job.job.skills.SkillManager;
import org.job.job.skills.SkillType;
import org.job.job.util.ItemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FarmerToolListener implements Listener {

    private final SkillManager skillManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public FarmerToolListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isJobItem(itemInHand, "FARMER_HOE")) return;

        int skillLevel = skillManager.getSkillLevel(player, SkillType.HARVEST_MASTERY);
        if (skillLevel <= 0) return;

        event.setCancelled(true);

        if (checkCooldown(player)) return;

        harvestInArea(player, skillLevel);
    }

    private boolean checkCooldown(Player player) {
        long cooldownTime = Job.getInstance().getSkillConfigManager().getConfig().getLong("skills.FARMER.HARVEST_MASTERY.cooldown", 60) * 1000;
        if (cooldowns.containsKey(player.getUniqueId())) {
            long secondsLeft = ((cooldowns.get(player.getUniqueId()) + cooldownTime) - System.currentTimeMillis()) / 1000;
            if (secondsLeft > 0) {
                player.sendMessage("§c'수확의 정석'을 사용하려면 " + secondsLeft + "초를 더 기다려야 합니다.");
                return true;
            }
        }
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        return false;
    }

    private void harvestInArea(Player player, int skillLevel) {
        int range = Job.getInstance().getSkillConfigManager().getRangeForLevel("FARMER", "HARVEST_MASTERY", skillLevel);
        if (range == 0) return;

        Block centerBlock = player.getTargetBlock(null, 5);
        if (centerBlock.getType() == Material.AIR) {
            centerBlock = player.getLocation().getBlock().getRelative(0, -1, 0);
        }

        int radius = (range - 1) / 2;
        Location center = centerBlock.getLocation();

        boolean autoReplant = skillManager.getSkillToggle(player, SkillType.AUTO_REPLANT);
        boolean autoPickup = skillManager.getSkillToggle(player, SkillType.AUTO_PICKUP);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    if (block.getBlockData() instanceof Ageable ageable) {
                        if (ageable.getAge() == ageable.getMaximumAge()) {
                            Material cropType = block.getType();
                            Material seedType = getSeedType(cropType);

                            // Harvest
                            for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) {
                                if (autoPickup) {
                                    player.getInventory().addItem(drop);
                                } else {
                                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                                }
                            }

                            // Replant unconditionally if autoReplant is true
                            if (autoReplant && seedType != null) {
                                ageable.setAge(0);
                                block.setBlockData(ageable);
                            } else {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }
    }

    private Material getSeedType(Material cropType) {
        return switch (cropType) {
            case WHEAT -> Material.WHEAT_SEEDS;
            case POTATOES -> Material.POTATO;
            case CARROTS -> Material.CARROT;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }

    // removeItemFromInventory method is no longer needed for auto-replant
}
