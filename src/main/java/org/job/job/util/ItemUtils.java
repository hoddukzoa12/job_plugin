package org.job.job.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment; // Import Enchantment
import org.bukkit.inventory.ItemFlag; // Import ItemFlag
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.job.job.Job;

import java.util.List;

public class ItemUtils {

    private static final NamespacedKey JOB_ITEM_KEY = new NamespacedKey(Job.getInstance(), "job_item_type");
    private static final NamespacedKey PROTECTED_TOOL_KEY = new NamespacedKey(Job.getInstance(), "protected_job_tool"); // New key for protection

    public static ItemStack createJobItem(Material material, String displayName, List<String> lore, String itemType) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(JOB_ITEM_KEY, PersistentDataType.STRING, itemType);

            // Add glowing effect
            meta.addEnchant(Enchantment.DURABILITY, 1, true); // Unbreaking 1, ignore level restriction
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); // Hide the enchantment description

            // Add protection tag
            meta.getPersistentDataContainer().set(PROTECTED_TOOL_KEY, PersistentDataType.BYTE, (byte) 1); // Set to 1 to indicate protected

            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getJobItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(JOB_ITEM_KEY, PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(JOB_ITEM_KEY, PersistentDataType.STRING);
        }
        return null;
    }

    public static boolean isJobItem(ItemStack item, String itemType) {
        String type = getJobItemType(item);
        return type != null && type.equals(itemType);
    }

    // New method to check if an item is a protected job tool
    public static boolean isProtectedJobTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(PROTECTED_TOOL_KEY, PersistentDataType.BYTE);
    }
}
