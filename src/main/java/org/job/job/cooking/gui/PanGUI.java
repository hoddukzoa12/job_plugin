package org.job.job.cooking.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.job.job.cooking.CookingManager;
import org.job.job.cooking.recipe.StationType;

public class PanGUI {

    private final Player player;

    public PanGUI(Player player) {
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 36, "§8팬: 튀기기");

        // 🔳 모든 슬롯 유리판으로 초기화
        ItemStack placeholder = createSlotPlaceholder();
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, placeholder);
        }

        // ✅ 재료 슬롯 비우기
        inv.setItem(10, null);
        inv.setItem(11, null);
        inv.setItem(15, createButton(Material.FIRE_CHARGE, "§a튀기기 시작"));

        player.openInventory(inv);
        CookingManager.getInstance().getGuiClickListener().updateCookButton(player, inv, StationType.PAN);
    }

    private ItemStack createButton(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSlotPlaceholder() {
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
