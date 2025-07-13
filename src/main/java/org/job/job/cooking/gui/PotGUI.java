package org.job.job.cooking.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.job.job.cooking.CookingManager;
import org.job.job.cooking.recipe.StationType;

public class PotGUI {

    private final Player player;

    public PotGUI(Player player) {
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 36, "§8냄비: 끓이기");

        // 🔳 모든 슬롯 유리판으로 초기화
        ItemStack placeholder = createSlotPlaceholder();
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, placeholder);
        }

        // ✅ 재료 슬롯 비우기
        inv.setItem(10, null);
        inv.setItem(11, null);

        // 조리 시작 버튼 (15번)
        inv.setItem(15, createButton(Material.CAULDRON, "§a끓이기 시작"));

        player.openInventory(inv);
        CookingManager.getInstance().getGuiClickListener().updateCookButton(player, inv, StationType.POT);
    }

    private ItemStack createButton(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSlotPlaceholder() {
        ItemStack item = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
