package org.job.job.listeners.farmer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.job.job.Job;
import org.job.job.data.PlayerDataManager;
import org.job.job.jobs.JobType;
import org.job.job.skills.SkillType;
import org.job.job.config.CropXPConfig;
import org.job.job.config.RareDropConfig;
import org.job.job.util.CustomItemsCrops;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class HarvestMasteryListener implements Listener {

    private final Job plugin;
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>(); // Player UUID -> Cooldown End Time (millis)
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>(); // Player UUID -> Active Task
    private final Map<UUID, Long> activationLock = new HashMap<>(); // Prevent double-firing
    private final Random random = new Random();

    public HarvestMasteryListener(Job plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 스킬 중복 발동 방지 (0.5초 락)
        long now = System.currentTimeMillis();
        if (activationLock.containsKey(player.getUniqueId()) && (now - activationLock.get(player.getUniqueId())) < 500) {
            return;
        }

        // 괭이 우클릭 확인
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !isHoe(item.getType())) {
            return;
        }

        var config = playerDataManager.getPlayerConfig(player.getUniqueId());

        // 농부 직업 확인
        if (!config.getString("job", "NONE").equals(JobType.FARMER.name())) {
            // 농부가 아니면 일반 괭이질을 막지 않도록 그냥 return
            return;
        }

        // 스킬 레벨 확인
        int skillLevel = plugin.getSkillManager().getSkillLevel(player, SkillType.HARVEST_MASTERY);
        if (skillLevel == 0) {
            // 스킬이 없으면 메시지만 보내고 일반 괭이질을 막지 않도록 return
            player.sendMessage("§c'수확의 정석' 스킬을 배우지 않았습니다.");
            return;
        }

        // 쿨타임 확인
        if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
            long timeLeft = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            player.sendMessage("§c'수확의 정석' 스킬은 아직 쿨타임 중입니다! (" + timeLeft + "초 남음)");
            event.setCancelled(true); // 쿨타임 중일 때는 스킬 발동을 막아야 하므로 이벤트 취소
            return;
        }

        // 모든 조건을 통과했으므로, 스킬을 활성화하고 기본 괭이질을 막습니다.
        event.setCancelled(true);
        activationLock.put(player.getUniqueId(), System.currentTimeMillis()); // 락 설정
        player.sendMessage("§a'수확의 정석' 스킬을 사용합니다!");
        activateHarvestMastery(player, skillLevel, event.getClickedBlock());
    }

    private boolean isHoe(Material material) {
        return material.name().endsWith("_HOE");
    }

    private void activateHarvestMastery(Player player, int skillLevel, Block clickedBlock) {
        long durationSeconds = plugin.getSkillConfigManager().getHarvestMasteryDuration();
        long cooldownSeconds = plugin.getSkillConfigManager().getHarvestMasteryCooldown();
        int range = plugin.getSkillConfigManager().getHarvestMasteryRange(skillLevel); // 스킬 레벨에 따른 범위

        // 기존 작업이 있다면 취소
        if (activeTasks.containsKey(player.getUniqueId())) {
            activeTasks.get(player.getUniqueId()).cancel();
            activeTasks.remove(player.getUniqueId());
        }

        // 쿨타임 설정
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000));

        // 스킬 작업 시작
        BukkitRunnable task = new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = (int) (durationSeconds * 20); // 1초 = 20틱

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    player.sendMessage("§a'수확의 정석' 스킬 지속 시간이 종료되었습니다.");
                    cancel();
                    activeTasks.remove(player.getUniqueId());
                    return;
                }

                // 플레이어 주변 작물 수확 로직
                harvestCropsInArea(player, player.getLocation().getBlock(), range);

                ticks += 10; // 0.5초마다 수확 시도
            }
        };
        task.runTaskTimer(plugin, 0L, 10L); // 즉시 시작, 0.5초마다 반복
        activeTasks.put(player.getUniqueId(), task);
    }

    private void harvestCropsInArea(Player player, Block centerBlock, int range) {
        int radius = range / 2;

        for (int x = -radius; x <= radius; x++) {
            for (int yOffset = -radius; yOffset <= radius; yOffset++) { // Y축도 정육면체 탐색
                for (int z = -radius; z <= radius; z++) {
                    Block targetBlock = centerBlock.getRelative(x, yOffset, z);

                    if (isHarvestableCrop(targetBlock)) {
                        processHarvest(player, targetBlock);
                    }
                }
            }
        }
    }

    private void processHarvest(Player player, Block targetBlock) {
        Material originalCropType = targetBlock.getType();

        // 아이템 드랍 처리
        processDrops(player, targetBlock);

        // 핸들러를 사용하여 경험치 처리 (희귀 작물 드랍은 호출 안함)
        plugin.getFarmerActionHandler().processXP(player, originalCropType);

        // 자동 심기 처리
        replant(targetBlock, originalCropType);
    }

    private void processDrops(Player player, Block block) {
        java.util.Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
        block.setType(Material.AIR); // 블록을 먼저 공기로 만들어 아이템 중복 드랍 방지

        boolean autoPickupEnabled = plugin.getSkillManager().getSkillToggle(player, SkillType.AUTO_PICKUP);
        if (autoPickupEnabled) {
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(drops.toArray(new ItemStack[0]));
            if (!remaining.isEmpty()) {
                for (ItemStack item : remaining.values()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
        } else {
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
    }

    private void replant(Block block, Material originalCropType) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() == Material.AIR) { // 블록이 여전히 비어있는지 확인
                    Material replantMaterial = getReplantMaterial(originalCropType);
                    if (replantMaterial != null) {
                        block.setType(replantMaterial);
                        if (block.getBlockData() instanceof Ageable ageable) {
                            ageable.setAge(0);
                            block.setBlockData(ageable);
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    private boolean isHarvestableCrop(Block block) {
        Material type = block.getType();

        // 사탕수수와 코코아콩은 수확의 정석 스킬 적용 대상에서 제외
        if (type == Material.SUGAR_CANE || type == Material.COCOA) {
            return false;
        }

        // FarmerHarvestListener의 isHarvestable 로직과 유사하게 구현
        if (plugin.getCropXPConfig().isConfigured(type)) {
            if (block.getBlockData() instanceof Ageable ageable) {
                return ageable.getAge() == ageable.getMaximumAge();
            } else return type == Material.PUMPKIN || type == Material.MELON;
        }
        return false;
    }

    private Material getReplantMaterial(Material cropMaterial) {
        return switch (cropMaterial) {
            case WHEAT -> Material.WHEAT;
            case CARROTS -> Material.CARROTS;
            case POTATOES -> Material.POTATOES;
            case BEETROOTS -> Material.BEETROOTS;
            case COCOA -> Material.COCOA;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }
}
