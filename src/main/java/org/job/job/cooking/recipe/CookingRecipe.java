package org.job.job.cooking.recipe;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CookingRecipe {

    private final StationType station;
    private final Map<String, Integer> inputMap; // <재료 ID, 필요 개수>
    private final ItemStack result;
    private final float successRate;
    private final int time;

    public CookingRecipe(StationType station, Map<String, Integer> inputMap, ItemStack result, float successRate, int time) {
        this.station = station;
        this.inputMap = inputMap;
        this.result = result;
        this.successRate = successRate;
        this.time = time;
    }

    public StationType getStation() {
        return station;
    }

    public Map<String, Integer> getInputMap() {
        return inputMap;
    }

    public ItemStack getResult() {
        return result.clone();
    }

    public float getSuccessRate() {
        return successRate;
    }

    public int getTime() {
        return time;
    }
}
