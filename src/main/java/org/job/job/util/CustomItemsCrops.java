package org.job.job.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.job.job.Job;
import org.job.job.config.RareDropConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItemsCrops {

    private final Map<Material, ItemStack> specialCrops = new HashMap<>();

    public CustomItemsCrops(RareDropConfig config) {
        // This constructor might not be needed if createRareCrop is static
        // but keeping it for now if other parts of the plugin use it.
    }

    public static ItemStack createRareCrop(RareDropConfig.CropDropData data) {
        ItemStack item = new ItemStack(data.material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(data.name);
            meta.setLore(Collections.singletonList(data.lore));

            // 반짝임 처리
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // 고유 식별 ID 저장
            NamespacedKey key = new NamespacedKey(Job.getInstance(), "special_crop");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, data.id);

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getSpecialCrop(Material cropType) {
        ItemStack item = specialCrops.get(cropType);
        if (item == null) {
            Job.getInstance().getLogger().warning("[CustomItemsCrops] ❌ 해당 작물 없음: " + cropType);
        }
        return item;
    }

    public boolean isSpecialCrop(ItemStack item, String expectedId) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Job.getInstance(), "special_crop");
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        return expectedId.equals(id);
    }

    public String getSpecialCropId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Job.getInstance(), "special_crop");
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}

