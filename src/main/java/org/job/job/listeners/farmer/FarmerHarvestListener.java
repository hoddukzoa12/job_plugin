package org.job.job.listeners.farmer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.job.job.Job;
import org.job.job.jobs.JobType;
import org.job.job.skills.SkillManager;
import org.job.job.skills.SkillType;

import java.util.Collection;
import java.util.HashMap;

public class FarmerHarvestListener implements Listener {

    private final Job plugin;
    private final SkillManager skillManager;

    public FarmerHarvestListener(Job plugin) {
        this.plugin = plugin;
        this.skillManager = plugin.getSkillManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHarvest(BlockBreakEvent event) {
        Player player = event.getPlayer();
        var config = plugin.getPlayerDataManager().getPlayerConfig(player.getUniqueId());

        if (!config.getString("job", "NONE").equals(JobType.FARMER.name())) return;

        Block block = event.getBlock();
        Material type = block.getType();

        boolean isHarvestable = false;
        if (plugin.getCropXPConfig().isConfigured(type)) {
            if (block.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    isHarvestable = true;
                }
            } else if (type == Material.PUMPKIN || type == Material.MELON || type == Material.SUGAR_CANE || type == Material.CACTUS) {
                isHarvestable = true;
            }
        }

        if (!isHarvestable) {
            return;
        }

        // Process XP
        plugin.getFarmerActionHandler().processXP(player, type);

        // Process rare drops and collect them
        ItemStack rareDrop = plugin.getFarmerActionHandler().processRareDrops(player, type, block.getLocation());

        boolean autoPickupEnabled = skillManager.getSkillToggle(player, SkillType.AUTO_PICKUP);
        boolean autoReplantEnabled = skillManager.getSkillToggle(player, SkillType.AUTO_REPLANT);
        

        Collection<ItemStack> initialDrops = block.getDrops(player.getInventory().getItemInMainHand());
        Collection<ItemStack> finalDrops = new java.util.ArrayList<>(initialDrops);
        if (rareDrop != null) {
            finalDrops.add(rareDrop);
        }

        if (autoPickupEnabled) {
            handleAutoPickup(event, player, finalDrops);
        } else {
            // If auto-pickup is not enabled, drop all items naturally
            for (ItemStack drop : finalDrops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }

        // IMPORTANT: Get the original block type BEFORE setting to AIR for replanting
        Material originalBlockType = block.getType();

        // Ensure the block is set to AIR before replanting to prevent double drops
        block.setType(Material.AIR);

        if (autoReplantEnabled) {
            handleAutoReplant(player, block, autoPickupEnabled, finalDrops, originalBlockType);
        }
    }

    private void handleAutoPickup(BlockBreakEvent event, Player player, Collection<ItemStack> drops) {
        event.setDropItems(false);
        HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(drops.toArray(new ItemStack[0]));

        if (!remainingItems.isEmpty()) {
            for (ItemStack item : remainingItems.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }

    private void handleAutoReplant(Player player, Block block, boolean autoPickupEnabled, Collection<ItemStack> finalDrops, Material originalBlockType) {
        Material seedType = getSeedForCrop(originalBlockType);
        if (seedType == null) {
            return;
        }

        // For SUGAR_CANE and CACTUS, only replant if the base block is broken
        if (originalBlockType == Material.SUGAR_CANE || originalBlockType == Material.CACTUS) {
            Material blockBelow = block.getRelative(0, -1, 0).getType();
            if (blockBelow != Material.DIRT && blockBelow != Material.GRASS_BLOCK && blockBelow != Material.SAND && blockBelow != Material.RED_SAND) {
                return;
            }
        }

        if (player.getInventory().contains(seedType)) {
            player.getInventory().removeItem(new ItemStack(seedType, 1));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                block.setType(getBlockMaterialForReplant(originalBlockType));
                if (block.getBlockData() instanceof Ageable ageable) {
                    ageable.setAge(0);
                    block.setBlockData(ageable);
                }
            }, 1L);

        } else if (autoPickupEnabled) {
            // If auto-pickup is on and the player has no seeds, check the finalDrops
            ItemStack foundSeedInDrops = null;
            for (ItemStack drop : finalDrops) {
                if (drop.getType() == seedType) {
                    foundSeedInDrops = drop;
                    break;
                }
            }

            if (foundSeedInDrops != null && foundSeedInDrops.getAmount() >= 1) {
                foundSeedInDrops.setAmount(foundSeedInDrops.getAmount() - 1); // Reduce the amount in the original drop

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    block.setType(getBlockMaterialForReplant(originalBlockType));
                    if (block.getBlockData() instanceof Ageable ageable) {
                        ageable.setAge(0);
                        block.setBlockData(ageable);
                    }
                }, 1L);
            }
        } else {
        }
    }


    private Material getSeedForCrop(Material cropType) {
        return switch (cropType) {
            case WHEAT -> Material.WHEAT_SEEDS;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            case NETHER_WART -> Material.NETHER_WART;
            // For these, the item itself is the seed
            case SUGAR_CANE -> Material.SUGAR_CANE;
            case CACTUS -> Material.CACTUS;
            default -> null;
        };
    }

    private Material getBlockMaterialForReplant(Material cropType) {
        return switch (cropType) {
            case WHEAT -> Material.WHEAT;
            case CARROTS -> Material.CARROTS;
            case POTATOES -> Material.POTATOES;
            case BEETROOTS -> Material.BEETROOTS;
            case NETHER_WART -> Material.NETHER_WART;
            case SUGAR_CANE -> Material.SUGAR_CANE;
            case CACTUS -> Material.CACTUS;
            default -> null;
        };
    }
}
