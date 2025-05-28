package org.job.job.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.job.job.Job;

public class ItemProtectionListener implements Listener {

    private final NamespacedKey key = new NamespacedKey(Job.getInstance(), "jobtool");

    private boolean isProtectedTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(key, PersistentDataType.STRING);
    }

    // 도구 드롭 막기
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (isProtectedTool(dropped)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c직업 도구는 버릴 수 없습니다!");
        }
    }

    // 인벤토리 안에서 도구 이동 막기
//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent event) {
//        ItemStack current = event.getCurrentItem();
//        if (isProtectedTool(current)) {
//            event.setCancelled(true);
//            Player player = (Player) event.getWhoClicked();
//            player.sendMessage("§c직업 도구는 옮길 수 없습니다!");
//        }
//    }

    // 다른 엔티티가 도구 줍지 못하게 막기
    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (isProtectedTool(item)) {
            event.setCancelled(true);
        }
    }

    // 주민과 상호작용 시 도구 들고 있으면 막기
    @EventHandler
    public void onVillagerTrade(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.VILLAGER) return;

        ItemStack heldItem = event.getPlayer().getInventory().getItemInMainHand();
        if (isProtectedTool(heldItem)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c직업 도구는 주민과 거래할 수 없습니다!");
        }
    }
}
