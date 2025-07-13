package org.job.job.cooking.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.job.job.cooking.recipe.CookingRecipe;

public class CookingCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final CookingRecipe recipe;
    private final boolean success;

    public CookingCompleteEvent(Player player, CookingRecipe recipe, boolean success) {
        this.player = player;
        this.recipe = recipe;
        this.success = success;
    }

    public Player getPlayer() {
        return player;
    }

    public CookingRecipe getRecipe() {
        return recipe;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
