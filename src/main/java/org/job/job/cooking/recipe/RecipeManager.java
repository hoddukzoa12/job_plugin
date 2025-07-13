package org.job.job.cooking.recipe;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class RecipeManager {

    private final List<CookingRecipe> recipes = new ArrayList<>();

    public void loadRecipes(File dataFolder) {
        File file = new File(dataFolder, "recipes.yml");

        if (!file.exists()) {
            System.out.println("[CookingPlugin] ⚠ recipes.yml 파일이 존재하지 않음. 기본 생성 필요.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("recipes");

        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection r = section.getConfigurationSection(key);
            if (r == null) continue;

            try {
                StationType station = StationType.valueOf(r.getString("station").toUpperCase());
                float successRate = (float) r.getDouble("success", 1.0);
                int cookTime = r.getInt("time", 0);

                // ✅ 정량 조리용 inputMap 읽기
                ConfigurationSection inputSection = r.getConfigurationSection("inputs");
                if (inputSection == null) {
                    System.out.println("❌ 레시피 '" + key + "'의 inputs가 비어 있음");
                    continue;
                }

                Map<String, Integer> inputMap = new HashMap<>();
                for (String id : inputSection.getKeys(false)) {
                    int amount = inputSection.getInt(id, 1);
                    inputMap.put(id, amount);
                }

                // 🎁 결과 아이템 생성
                ConfigurationSection resultSection = r.getConfigurationSection("result");
                if (resultSection == null) {
                    System.out.println("❌ 레시피 '" + key + "'의 result 정보 없음");
                    continue;
                }

                Material mat = Material.valueOf(resultSection.getString("material").toUpperCase());
                String name = resultSection.getString("name", "요리 결과");
                String lore = resultSection.getString("lore", "");

                ItemStack result = new ItemStack(mat);
                ItemMeta meta = result.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    meta.setLore(Collections.singletonList(lore));
                    result.setItemMeta(meta);
                }

                // ✅ 새 구조의 CookingRecipe 생성
                CookingRecipe recipe = new CookingRecipe(station, inputMap, result, successRate, cookTime);
                recipes.add(recipe);

            } catch (Exception e) {
                System.out.println("❌ 레시피 '" + key + "' 로드 실패: " + e.getMessage());
            }
        }

        System.out.println("✅ 레시피 " + recipes.size() + "개 로드됨");
    }

    /**
     * 해당 스테이션과 재료 Map이 레시피 조건과 일치하는지 찾기
     */
    public Optional<CookingRecipe> findRecipe(StationType station, Map<String, Integer> availableItems) {
        for (CookingRecipe recipe : recipes) {
            if (recipe.getStation() != station) continue;

            boolean match = true;
            for (Map.Entry<String, Integer> entry : recipe.getInputMap().entrySet()) {
                String id = entry.getKey();
                int required = entry.getValue();

                if (availableItems.getOrDefault(id, 0) < required) {
                    match = false;
                    break;
                }
            }

            if (match) return Optional.of(recipe);
        }

        return Optional.empty();
    }

    public List<CookingRecipe> getAllRecipes() {
        return recipes;
    }
}
