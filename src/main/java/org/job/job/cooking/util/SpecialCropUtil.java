package org.job.job.cooking.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SpecialCropUtil {

    private final NamespacedKey key;

    public SpecialCropUtil(Plugin plugin) {
        // Job 플러그인에서 저장한 key의 네임스페이스가 "job"임을 가정
        this.key = new NamespacedKey("job", "special_crop");
    }

    /**
     * 해당 아이템이 special_crop인지 확인
     */
    public boolean isSpecialCrop(ItemStack item) {
        return getSpecialCropId(item) != null;
    }

    /**
     * special_crop ID 값을 가져옴 (예: "cocoa", "wheat" 등)
     */
    public String getSpecialCropId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /**
     * 해당 아이템이 특정 ID인지 확인
     */
    public boolean isSpecialCrop(ItemStack item, String expectedId) {
        String id = getSpecialCropId(item);
        return expectedId.equals(id);
    }
}
