package org.job.job.skills;

public enum SkillType {
    // 포인트 투자 스킬
    HARVEST_MASTERY("수확의 정석", "농부", true, 0),
    RARE_CROP_CHANCE("특수 작물 확률 업", "농부", true, 0),
    COOKING_SUCCESS_RATE("요리 성공 확률 업", "농부", true, 0),
    COOKING_GREAT_SUCCESS("요리 대성공 확률 업", "농부", true, 0),
    COOKING_TIME_REDUCTION("요리 시간 단축", "농부", true, 0),

    // 레벨 달성 자동 스킬
    AUTO_REPLANT("자동 심기", "농부", false, 30),
    AUTO_PICKUP("자동 줍기", "농부", false, 60);

    private final String skillName;
    private final String jobName;
    private final boolean isPointSkill;
    private final int levelRequired;

    SkillType(String skillName, String jobName, boolean isPointSkill, int levelRequired) {
        this.skillName = skillName;
        this.jobName = jobName;
        this.isPointSkill = isPointSkill;
        this.levelRequired = levelRequired;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getJobName() {
        return jobName;
    }

    public boolean isPointSkill() {
        return isPointSkill;
    }

    public int getLevelRequired() {
        return levelRequired;
    }
}
