package org.job.job.cooking.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.List;

public class GUICloseListener implements Listener {

    private final List<Integer> inputSlots = List.of(10, 11);

    @EventHandler
    public void onGuiClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        // 조리 GUI가 아닌 경우 무시
        if (!title.contains("절구") && !title.contains("냄비")
                && !title.contains("오븐") && !title.contains("팬") && !title.contains("믹서기")) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // 남은 재료 → 유저 인벤토리로 이동
        for (int slot : inputSlots) {
            ItemStack item = inv.getItem(slot);
            if (item != null) {
                player.getInventory().addItem(item);
                inv.setItem(slot, null); // 슬롯 정리 (안 해도 무방하긴 함)
            }
        }
    }
}
