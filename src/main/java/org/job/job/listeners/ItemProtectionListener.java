package org.job.job.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import java.util.HashMap;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.job.job.Job;
import org.job.job.util.ItemUtils;

public class ItemProtectionListener implements Listener {

    // 도구 드롭 막기
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (ItemUtils.isProtectedJobTool(dropped)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c직업 도구는 버릴 수 없습니다! (DROP)");
            // Job.getInstance().getLogger().info("[DEBUG] Protected tool dropped: " + dropped.getType().name());
            event.getPlayer().updateInventory(); // 인벤토리 강제 업데이트
        }
    }

    // 인벤토리 클릭 이벤트 (버리기, 이동)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // 보호된 아이템이 클릭되었을 때
        if (ItemUtils.isProtectedJobTool(clickedItem)) {
            // Job.getInstance().getLogger().info("[DEBUG] Protected tool clicked: " + clickedItem.getType().name() + ", ClickType: " + event.getClick().name() + ", SlotType: " + event.getSlotType().name() + ", ClickedInvType: " + (event.getClickedInventory() != null ? event.getClickedInventory().getType().name() : "null"));

            // 인벤토리 밖으로 버리려는 시도 (SHIFT + 클릭 또는 드래그 후 인벤토리 밖 클릭)
            if (event.getClickedInventory() == player.getInventory() && event.getClick().isShiftClick() && event.getSlotType() == InventoryType.SlotType.QUICKBAR) {
                event.setCancelled(true);
                player.sendMessage("§c직업 도구는 버릴 수 없습니다! (SHIFT+CLICK)");
                player.updateInventory(); // 인벤토리 강제 업데이트
                return;
            }
            if (event.getClickedInventory() == null) { // 인벤토리 밖 클릭
                event.setCancelled(true);
                player.sendMessage("§c직업 도구는 버릴 수 없습니다! (CLICK OUTSIDE)");
                player.updateInventory(); // 인벤토리 강제 업데이트
                return;
            }

            // 엔더 상자로의 이동은 허용
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.ENDER_CHEST) {
                // Job.getInstance().getLogger().info("[DEBUG] Protected tool moved to Ender Chest.");
                return; // 엔더 상자는 허용
            }

            // 다른 인벤토리로 이동 시도 (예: 상자, 화로 등)
            if (event.getClickedInventory() != player.getInventory() && event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
                player.sendMessage("§c직업 도구는 다른 인벤토리로 옮길 수 없습니다!");
                player.updateInventory(); // 인벤토리 강제 업데이트
                return;
            }
        }

        // 보호된 아이템을 들고 인벤토리 밖으로 버리려는 시도
        if (ItemUtils.isProtectedJobTool(cursorItem)) {
            // Job.getInstance().getLogger().info("[DEBUG] Protected tool on cursor: " + cursorItem.getType().name() + ", ClickType: " + event.getClick().name() + ", ClickedInvType: " + (event.getClickedInventory() != null ? event.getClickedInventory().getType().name() : "null"));
            // 보호된 아이템을 들고 인벤토리 밖으로 버리려는 시도
            if (event.getClickedInventory() == null) { // 인벤토리 밖 클릭
                event.setCancelled(true);
                player.sendMessage("§c직업 도구는 버릴 수 없습니다! (CURSOR OUTSIDE)");
                // 아이템을 인벤토리로 돌려보내거나 드롭
                if (cursorItem != null) {
                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(cursorItem);
                    if (!remaining.isEmpty()) {
                        for (ItemStack item : remaining.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        }
                    }
                }
                player.setItemOnCursor(null); // 커서 아이템 제거
                player.updateInventory(); // 인벤토리 강제 업데이트
            }

            // 보호된 아이템을 들고 다른 인벤토리로 옮기려는 시도
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER && event.getClickedInventory().getType() != InventoryType.ENDER_CHEST) {
                event.setCancelled(true);
                player.sendMessage("§c직업 도구는 다른 인벤토리로 옮길 수 없습니다! (CURSOR MOVE)");
                // 아이템을 인벤토리로 돌려보내거나 드롭
                if (cursorItem != null) {
                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(cursorItem);
                    if (!remaining.isEmpty()) {
                        for (ItemStack item : remaining.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        }
                    }
                }
                player.setItemOnCursor(null); // 커서 아이템 제거
                player.updateInventory(); // 인벤토리 강제 업데이트
            }
        }
    }

    // 인벤토리 드래그 이벤트 (아이템을 인벤토리 밖으로 드래그하여 버리는 경우)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // 드래그 중인 아이템이 보호된 도구인지 확인
        if (ItemUtils.isProtectedJobTool(event.getOldCursor())) {
            // Job.getInstance().getLogger().info("[DEBUG] Protected tool dragged: " + event.getOldCursor().getType().name());
            // 인벤토리 밖으로 드래그하여 놓는 경우
            if (event.getRawSlots().stream().anyMatch(slot -> slot < 0)) { // Raw slot < 0 means outside inventory
                event.setCancelled(true);
                player.sendMessage("§c직업 도구는 버릴 수 없습니다! (DRAG OUTSIDE)");
                // 아이템을 인벤토리로 돌려보내거나 드롭
                if (event.getOldCursor() != null) {
                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(event.getOldCursor());
                    if (!remaining.isEmpty()) {
                        for (ItemStack item : remaining.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        }
                    }
                }
                player.setItemOnCursor(null); // 커서 아이템 강제 제거
                player.updateInventory(); // 인벤토리 강제 업데이트
            }
        }
    }

    // 호퍼, 드로퍼 등을 통한 아이템 이동 막기 (엔더 상자 제외)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        // Job.getInstance().getLogger().info("[DEBUG] InventoryMoveItemEvent: Item=" + item.getType().name() + ", Source=" + event.getSource().getType().name() + ", Destination=" + event.getDestination().getType().name());

        if (ItemUtils.isProtectedJobTool(item)) {
            // Job.getInstance().getLogger().info("[DEBUG] Protected tool in InventoryMoveItemEvent: " + item.getType().name());
            // 목적지 인벤토리가 엔더 상자가 아니라면 이동 막기
            if (event.getDestination().getType() != InventoryType.ENDER_CHEST) {
                event.setCancelled(true);
                // Job.getInstance().getLogger().info("[DEBUG] InventoryMoveItemEvent cancelled for protected tool.");
            }
        }
    }

    // 주민과 상호작용 시 도구 들고 있으면 막기
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVillagerTrade(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.VILLAGER) return;

        ItemStack heldItem = event.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isProtectedJobTool(heldItem)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c직업 도구는 주민과 거래할 수 없습니다!");
        }
    }
}