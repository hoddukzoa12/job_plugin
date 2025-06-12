package org.job.job.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.job.job.Job;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RareDropConfig {

    private final File file;
    private final FileConfiguration config;
    private final Map<Material, CropDropData> cropDropMap = new HashMap<>();

    public RareDropConfig() {
        file = new File(Job.getInstance().getDataFolder(), "rare-drops.yml");
        config = YamlConfiguration.loadConfiguration(file);

        boolean needsInit = !file.exists() || file.length() == 0 || !config.contains("crops");

        if (needsInit) {
            Job.getInstance().getLogger().info("[RareDropConfig] 📝 rare-drops.yml을 새로 생성하거나 초기화합니다.");
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("rare-drops.yml 생성 실패", e);
            }
            saveDefaultValues();
        }

        load();
    }

    private void saveDefaultValues() {
        saveCrop("WHEAT", "wheat", "WHEAT", "§e황금 밀 이삭", "§7희귀한 밀 이삭입니다.", 0.05);
        saveCrop("CARROTS", "carrot", "GOLDEN_CARROT", "§6빛나는 당근", "§7빛나는 당근입니다.", 0.10);
        saveCrop("POTATOES", "potato", "BAKED_POTATO", "§6빛나는 감자", "§7귀한 감자입니다.", 0.10);
        saveCrop("BEETROOTS", "beetroot", "BEETROOT", "§c선홍빛 비트루트", "§7강렬한 빛깔의 비트루트입니다.", 0.08);
        saveCrop("COCOA", "cocoa", "COCOA_BEANS", "§6특제 코코아빈", "§7특별한 향이 나는 코코아빈입니다.", 0.07);
        saveCrop("NETHER_WART", "nether_wart", "NETHER_WART", "§5심연의 네더와트", "§7강력한 성분을 가진 네더와트입니다.", 0.06);
        saveCrop("PUMPKIN", "pumpkin", "PUMPKIN", "§6황금 호박", "§7황금빛 광택이 도는 호박입니다.", 0.04);
        saveCrop("MELON", "melon", "MELON_SLICE", "§a향기로운 수박 조각", "§7탐스러운 수박 조각입니다.", 0.04);
        saveCrop("SUGAR_CANE", "sugar_cane", "SUGAR_CANE", "§f순백의 사탕수수", "§7정제된 고급 사탕수수입니다.", 0.02);

        try {
            config.save(file);
            Job.getInstance().getLogger().info("[RareDropConfig] ✅ 기본 드롭 데이터가 저장되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCrop(String key, String id, String material, String name, String lore, double chance) {
        String path = "crops." + key;
        config.set(path + ".id", id);
        config.set(path + ".material", material);
        config.set(path + ".name", name);
        config.set(path + ".lore", lore);
        config.set(path + ".chance", chance);
    }

    private void load() {
        if (!config.contains("crops")) {
            Job.getInstance().getLogger().warning("[RareDropConfig] ⚠️ crops 섹션이 존재하지 않습니다.");
            return;
        }

        for (String key : config.getConfigurationSection("crops").getKeys(false)) {
            try {
                Material cropType = Material.valueOf(key.toUpperCase());
                String path = "crops." + key;
                String id = config.getString(path + ".id");
                Material itemMaterial = Material.valueOf(config.getString(path + ".material"));
                String name = config.getString(path + ".name");
                String lore = config.getString(path + ".lore");
                double chance = config.getDouble(path + ".chance");

                cropDropMap.put(cropType, new CropDropData(id, itemMaterial, name, lore, chance));
            } catch (Exception e) {
                Job.getInstance().getLogger().warning("[RareDropConfig] ⚠️ 잘못된 드롭 항목: " + key);
            }
        }
    }

    public CropDropData getData(Material cropType) {
        return cropDropMap.get(cropType);
    }

    public Set<Material> getCropTypes() {
        return cropDropMap.keySet();
    }

    public static class CropDropData {
        public final String id;
        public final Material material;
        public final String name;
        public final String lore;
        public final double chance;

        public CropDropData(String id, Material material, String name, String lore, double chance) {
            this.id = id;
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.chance = chance;
        }
    }
}
