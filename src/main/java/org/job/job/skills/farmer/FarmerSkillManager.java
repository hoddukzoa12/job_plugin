package org.job.job.skills.farmer;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

public class FarmerSkillManager {

    // 플레이어 UUID → 스킬 포인트
    private final Map<UUID, Integer> skillPoints = new HashMap<>();

    public int getSkillPoints(UUID uuid) {
        return skillPoints.getOrDefault(uuid, 0);
    }

    public void addSkillPoint(UUID uuid, int amount) {
        skillPoints.put(uuid, getSkillPoints(uuid) + amount);
    }

    public boolean useSkillPoint(UUID uuid) {
        int current = getSkillPoints(uuid);
        if (current <= 0) return false;
        skillPoints.put(uuid, current - 1);
        return true;
    }

    public void reset(UUID uuid) {
        skillPoints.remove(uuid);
    }

    // TODO: 저장 및 로딩 구현 가능
}
