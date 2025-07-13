package org.job.job.cooking.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.job.job.cooking.listener.GUIClickListener;
import org.job.job.cooking.recipe.StationType;
import org.job.job.cooking.CookingManager;

public class MortarGUI {

    private final Player player;

    public MortarGUI(Player player) {
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 36, "§8절구: 재료를 갈아보자");

        // 🔳 모든 슬롯 유리판으로 초기화
        ItemStack placeholder = createSlotPlaceholder();
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, placeholder);
        }

        // ✅ 재료 슬롯 비우기
        inv.setItem(10, null);
        inv.setItem(11, null);

        // 슬롯 15 - 조리 시작 버튼
        inv.setItem(15, createButton(Material.PISTON, "§a분쇄 시작"));

        player.openInventory(inv);
        CookingManager.getInstance().getGuiClickListener().updateCookButton(player, inv, StationType.MORTAR);
    }

    private ItemStack createButton(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSlotPlaceholder() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
