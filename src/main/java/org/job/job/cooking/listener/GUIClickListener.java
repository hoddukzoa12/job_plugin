package org.job.job.cooking.listener;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.job.job.cooking.CookingManager;
import org.job.job.cooking.recipe.CookingRecipe;
import org.job.job.cooking.recipe.RecipeManager;
import org.job.job.cooking.recipe.StationType;
import org.job.job.cooking.util.SpecialCropUtil;
import org.job.job.skills.SkillManager;
import org.job.job.skills.SkillType;

import java.util.*;

public class GUIClickListener implements Listener {

    private final List<Integer> inputSlots = List.of(10, 11);
    private final int cookButtonSlot = 15;
    private final Set<UUID> cookingPlayers = new HashSet<>();
    private final SkillManager skillManager;

    public GUIClickListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null || inv.getSize() != 36) return;

        String title = event.getView().getTitle();
        StationType station = getStationFromTitle(title);
        if (station == null) return;

        Player player = (Player) event.getWhoClicked();
        int clickedSlot = event.getRawSlot();

        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        if (clickedSlot == cookButtonSlot) {
            event.setCancelled(true);
            if (!cookingPlayers.contains(player.getUniqueId())) {
                SpecialCropUtil cropUtil = CookingManager.getInstance().getCropUtil();
                RecipeManager recipeManager = CookingManager.getInstance().getRecipeManager();

                Map<String, Integer> availableMap = new HashMap<>();
                for (int slot : inputSlots) {
                    ItemStack item = inv.getItem(slot);
                    if (item == null) continue;
                    String id = cropUtil.getSpecialCropId(item);
                    if (id == null) continue;
                    availableMap.put(id, availableMap.getOrDefault(id, 0) + item.getAmount());
                }

                Optional<CookingRecipe> optRecipe = recipeManager.findRecipe(station, availableMap);
                if (optRecipe.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "재료가 부족하거나 맞는 레시피가 없습니다!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }

                CookingRecipe recipe = optRecipe.get();
                double baseSuccessRate = recipe.getSuccessRate();
                int baseCookTime = recipe.getTime();

                String playerJobName = skillManager.getPlayerDataManager().getPlayerConfig(player.getUniqueId()).getString("job", "NONE");
                boolean isFarmer = playerJobName.equals(org.job.job.jobs.JobType.FARMER.name());

                double finalSuccessRate = baseSuccessRate;
                double greatSuccessChance = 0.0;
                int finalCookTime = baseCookTime;

                if (isFarmer) {
                    int successSkillLevel = skillManager.getSkillLevel(player, SkillType.COOKING_SUCCESS_RATE);
                    if (successSkillLevel > 0) {
                        finalSuccessRate = Math.min(1.0, baseSuccessRate + skillManager.getSkillConfigManager().getSuccessRatePerLevel(org.job.job.jobs.JobType.FARMER.name(), SkillType.COOKING_SUCCESS_RATE.name()) * successSkillLevel);
                    }

                    int greatSuccessSkillLevel = skillManager.getSkillLevel(player, SkillType.COOKING_GREAT_SUCCESS);
                    if (greatSuccessSkillLevel > 0) {
                        greatSuccessChance = skillManager.getSkillConfigManager().getGreatSuccessChancePerLevel(org.job.job.jobs.JobType.FARMER.name(), SkillType.COOKING_GREAT_SUCCESS.name()) * greatSuccessSkillLevel;
                    }

                    int timeSkillLevel = skillManager.getSkillLevel(player, SkillType.COOKING_TIME_REDUCTION);
                    if (timeSkillLevel > 0) {
                        double reductionRate = skillManager.getSkillConfigManager().getTimeReductionPerLevel(org.job.job.jobs.JobType.FARMER.name(), SkillType.COOKING_TIME_REDUCTION.name()) * timeSkillLevel;
                        finalCookTime = (int) (baseCookTime * (1.0 - reductionRate));
                        if (finalCookTime < 1) finalCookTime = 1;
                    }
                }
                handleCook(player, inv, station, finalSuccessRate, greatSuccessChance, finalCookTime, recipe);
                cookingPlayers.add(player.getUniqueId());
            }
            return;
        }
        // Always update the cook button after any click in the GUI, unless it's a cook button click
        if (clickedSlot != cookButtonSlot) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateCookButton(player, inv, station);
                }
            }.runTaskLater(CookingManager.getInstance().getPlugin(), 1L);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getSize() == 36 && getStationFromTitle(event.getView().getTitle()) != null) {
            Player player = (Player) event.getPlayer();
            Inventory inv = event.getInventory();
            StationType station = getStationFromTitle(event.getView().getTitle());
            updateCookButton(player, inv, station);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getSize() == 36 && getStationFromTitle(event.getView().getTitle()) != null) {
            Player player = (Player) event.getWhoClicked();
            Inventory inv = event.getInventory();
            StationType station = getStationFromTitle(event.getView().getTitle());
            // Check if any of the raw slots involved in the drag are input slots
            boolean affectsInputSlots = event.getRawSlots().stream().anyMatch(inputSlots::contains);
            if (affectsInputSlots) {
                updateCookButton(player, inv, station);
            }
        }
    }

    public void updateCookButton(Player player, Inventory inv, StationType station) {
        SpecialCropUtil cropUtil = CookingManager.getInstance().getCropUtil();
        RecipeManager recipeManager = CookingManager.getInstance().getRecipeManager();

        Map<String, Integer> availableMap = new HashMap<>();
        for (int slot : inputSlots) {
            ItemStack item = inv.getItem(slot);
            if (item == null) continue;
            String id = cropUtil.getSpecialCropId(item);
            if (id == null) continue;
            availableMap.put(id, availableMap.getOrDefault(id, 0) + item.getAmount());
        }

        // Debugging: Log availableMap content
        CookingManager.getInstance().getPlugin().getLogger().info("Available items in GUI for " + player.getName() + ": " + availableMap);

        Optional<CookingRecipe> optRecipe = recipeManager.findRecipe(station, availableMap);

        ItemStack button = inv.getItem(cookButtonSlot);
        if (button == null) {
            button = new ItemStack(Material.PISTON);
        }
        ItemMeta meta = button.getItemMeta();
        List<String> lore = new ArrayList<>(); // lore 리스트 초기화

        if (optRecipe.isEmpty()) {
            // Debugging: Log why recipe was not found
            CookingManager.getInstance().getPlugin().getLogger().info("Recipe not found for station " + station + " with available items: " + availableMap);
            // Iterate through all recipes to find potential matches and log why they don't match
            for (CookingRecipe recipe : recipeManager.getAllRecipes()) {
                if (recipe.getStation() != station) continue;
                boolean ingredientsMatch = true;
                for (Map.Entry<String, Integer> entry : recipe.getInputMap().entrySet()) {
                    String requiredId = entry.getKey();
                    int requiredAmount = entry.getValue();
                    int availableAmount = availableMap.getOrDefault(requiredId, 0);
                    if (availableAmount < requiredAmount) {
                        ingredientsMatch = false;
                        CookingManager.getInstance().getPlugin().getLogger().info("  - Recipe '" + recipe.getResult().getItemMeta().getDisplayName() + "' (ID: " + requiredId + ") requires " + requiredAmount + ", but only " + availableAmount + " available.");
                        break;
                    }
                }
                if (ingredientsMatch) {
                    CookingManager.getInstance().getPlugin().getLogger().info("  - Found a recipe that *should* match: " + recipe.getResult().getItemMeta().getDisplayName() + ", but findRecipe returned empty. This is unexpected.");
                }
            }

            meta.setDisplayName("§c레시피를 찾을 수 없음");
            if (availableMap.isEmpty()) {
                lore.add("§7재료를 넣어주세요.");
            } else {
                lore.add("§7재료가 부족하거나");
                lore.add("§7맞는 레시피가 없습니다.");
            }
        } else {
            CookingRecipe recipe = optRecipe.get();
            meta.setDisplayName("§a요리 시작"); // 레시피를 찾았을 때 버튼 이름 설정
            double baseSuccessRate = recipe.getSuccessRate();
            double finalSuccessRate = baseSuccessRate;
            int baseCookTime = recipe.getTime();
            int finalCookTime = baseCookTime;

            // 결과물 표시
            String resultName = recipe.getResult().hasItemMeta() ? recipe.getResult().getItemMeta().getDisplayName() : recipe.getResult().getType().name();
            lore.add("§7결과물: §f" + resultName + " §7(" + recipe.getResult().getAmount() + "개)");
            lore.add("");

            // 스킬 적용 (농부 직업 여부와 관계없이 계산)
            String playerJobName = skillManager.getPlayerDataManager().getPlayerConfig(player.getUniqueId()).getString("job", "NONE");
            boolean isFarmer = playerJobName.equals(org.job.job.jobs.JobType.FARMER.name());
            CookingManager.getInstance().getPlugin().getLogger().info("Player " + player.getName() + " isFarmer: " + isFarmer);

            // COOKING_SUCCESS_RATE
            double bonusSuccessRate = 0.0;
            if (isFarmer) {
                int successSkillLevel = skillManager.getSkillLevel(player, SkillType.COOKING_SUCCESS_RATE);
                if (successSkillLevel > 0) {
                    bonusSuccessRate = skillManager.getSkillConfigManager().getSuccessRatePerLevel(org.job.job.jobs.JobType.FARMER.name(), SkillType.COOKING_SUCCESS_RATE.name()) * successSkillLevel;
                }
                CookingManager.getInstance().getPlugin().getLogger().info("COOKING_SUCCESS_RATE Skill Level: " + successSkillLevel + ", Bonus: " + bonusSuccessRate);
            }
            finalSuccessRate = Math.min(1.0, baseSuccessRate + bonusSuccessRate);
            lore.add("§7성공 확률: §e" + String.format("%.1f", baseSuccessRate * 100) + "%" + (bonusSuccessRate > 0 ? " §a(+" + String.format("%.1f", bonusSuccessRate * 100) + "%)" : ""));

            // COOKING_GREAT_SUCCESS
            double greatSuccessChance = 0.0;
            if (isFarmer) {
                int greatSuccessSkillLevel = skillManager.getSkillLevel(player, SkillType.COOKING_GREAT_SUCCESS);
                if (greatSuccessSkillLevel > 0) {
                    greatSuccessChance = skillManager.getSkillConfigManager().getGreatSuccessChancePerLevel(org.job.job.jobs.JobType.FARMER.name(), SkillType.COOKING_GREAT_SUCCESS.name()) * greatSuccessSkillLevel;
                }
                CookingManager.getInstance().getPlugin().getLogger().info("COOKING_GREAT_SUCCESS Skill Level: " + greatSuccessSkillLevel + ", Chance: " + greatSuccessChance);
            }
            if (greatSuccessChance > 0) {
                lore.add("§7대성공 확률: §e" + String.format("%.1f", greatSuccessChance * 100) + "%");
            }

            // COOKING_TIME_REDUCTION
            double reductionRate = 0.0; // Initialize reductionRate as double
            if (isFarmer) {
                int timeSkillLevel = skillManager.getSkillLevel(player, SkillType.COOKING_TIME_REDUCTION);
                if (timeSkillLevel > 0) {
                    reductionRate = skillManager.getSkillConfigManager().getTimeReductionPerLevel(org.job.job.jobs.JobType.FARMER.name(), SkillType.COOKING_TIME_REDUCTION.name()) * timeSkillLevel;
                }
                // Log reductionRate directly
                CookingManager.getInstance().getPlugin().getLogger().info("COOKING_TIME_REDUCTION Skill Level: " + timeSkillLevel + ", Reduction Rate: " + reductionRate);
            }
            finalCookTime = (int) (baseCookTime * (1.0 - reductionRate)); // Calculate finalCookTime using double reductionRate
            if (finalCookTime < 1) finalCookTime = 1; // 최소 1초

            // Display cooking time with percentage reduction if applicable
            String timeReductionDisplay = "";
            if (reductionRate > 0) {
                timeReductionDisplay = " §c(-" + String.format("%.1f", reductionRate * 100) + "%)";
            }
            lore.add("§7요리 시간: §e" + baseCookTime + "초" + timeReductionDisplay);

            lore.add("");
            lore.add("§e클릭하여 요리 시작");
        }

        meta.setLore(lore);
        button.setItemMeta(meta);
        inv.setItem(cookButtonSlot, button);
    }

    private void handleCook(Player player, Inventory inv, StationType station, final double finalSuccessRate, final double greatSuccessChance, final int finalCookTime, final CookingRecipe recipe) {
        SpecialCropUtil cropUtil = CookingManager.getInstance().getCropUtil();
        RecipeManager recipeManager = CookingManager.getInstance().getRecipeManager();

        // 현재 GUI 상의 재료 개수 맵으로 정리
        Map<String, Integer> availableMap = new HashMap<>();
        for (int slot : inputSlots) {
            ItemStack item = inv.getItem(slot);
            if (item == null) continue;
            String id = cropUtil.getSpecialCropId(item);
            if (id == null) continue;
            availableMap.put(id, availableMap.getOrDefault(id, 0) + item.getAmount());
        }

        Optional<CookingRecipe> optRecipe = recipeManager.findRecipe(station, availableMap);
        if (optRecipe.isEmpty()) {
            player.sendMessage(ChatColor.RED + "재료가 부족하거나 맞는 레시피가 없습니다!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            cookingPlayers.remove(player.getUniqueId()); // Remove from cookingPlayers if recipe not found
            return;
        }

        // Consume ingredients
        for (Map.Entry<String, Integer> entry : recipe.getInputMap().entrySet()) {
            String ingredientId = entry.getKey();
            int requiredAmount = entry.getValue();
            int consumedAmount = 0;

            for (int slot : inputSlots) {
                ItemStack item = inv.getItem(slot);
                if (item == null) continue;
                String id = cropUtil.getSpecialCropId(item);
                if (id == null || !id.equals(ingredientId)) continue;

                int currentAmount = item.getAmount();
                int toConsume = Math.min(currentAmount, requiredAmount - consumedAmount);
                item.setAmount(currentAmount - toConsume);
                consumedAmount += toConsume;

                if (item.getAmount() == 0) {
                    inv.setItem(slot, null);
                }
                if (consumedAmount == requiredAmount) break;
            }
        }

        player.sendMessage(ChatColor.YELLOW + "요리를 시작합니다...");
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f); // Cooking start sound

        // Initialize progress bar
        int totalProgressBarSlots = 9; // Slots 27-35
        for (int i = 0; i < totalProgressBarSlots; i++) {
            inv.setItem(27 + i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

        new BukkitRunnable() {
            int ticksPassed = 0;
            final int totalTicks = finalCookTime * 20; // Convert seconds to ticks

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cookingPlayers.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                ticksPassed++;
                int filledSlots = (int) Math.ceil((double) ticksPassed / totalTicks * totalProgressBarSlots);

                for (int i = 0; i < totalProgressBarSlots; i++) {
                    if (i < filledSlots) {
                        inv.setItem(27 + i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                    } else {
                        inv.setItem(27 + i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
                    }
                }

                if (ticksPassed >= totalTicks) {
                    cancel(); // Stop the runnable

                    Random random = new Random();
                    double roll = random.nextDouble();

                    ItemStack resultItem;
                    if (roll <= greatSuccessChance) {
                        resultItem = recipe.getResult().clone();
                        resultItem.setAmount(resultItem.getAmount() + 1);
                        player.sendMessage(ChatColor.GOLD + "대성공! " + resultItem.getItemMeta().getDisplayName() + "을(를) 만들었습니다!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                    } else if (roll <= finalSuccessRate) {
                        resultItem = recipe.getResult();
                        player.sendMessage(ChatColor.GREEN + "요리 성공! " + resultItem.getItemMeta().getDisplayName() + "을(를) 만들었습니다!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    } else {
                        resultItem = null; // 실패 시 아무것도 지급하지 않음
                        player.sendMessage(ChatColor.RED + "요리 실패...");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                    }

                    // Give result to player
                    if (resultItem != null) {
                        player.getInventory().addItem(resultItem).values().forEach(item -> {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        });
                    }

                    // Clear progress bar
                    for (int i = 0; i < totalProgressBarSlots; i++) {
                        inv.setItem(27 + i, null);
                    }

                    cookingPlayers.remove(player.getUniqueId());
                    updateCookButton(player, inv, station); // Update button after cooking
                }
            }
        }.runTaskTimer(CookingManager.getInstance().getPlugin(), 0L, 1L); // Run every tick
    }

    private StationType getStationFromTitle(String title) {
        if (title.contains("절구")) return StationType.MORTAR;
        if (title.contains("냄비")) return StationType.POT;
        if (title.contains("오븐")) return StationType.OVEN;
        if (title.contains("팬")) return StationType.PAN;
        if (title.contains("믹서기")) return StationType.BLENDER;
        return null;
    }
}