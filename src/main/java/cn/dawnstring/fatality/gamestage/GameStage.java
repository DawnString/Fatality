package cn.dawnstring.fatality.gamestage;

import cn.dawnstring.fatality.entity.boss.BossList;
import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * 游戏阶段枚举
 * 每个阶段对应一个自定义Boss，阶段越靠后增益越大
 */
public enum GameStage
{
    //加成为乘算
    STAGE_0("stage_0", "开始", null, 1.0f, 1.0f, 1.0f, 1.0f),
    STAGE_1("stage_1", "守门人后", BossList.Gatekeeper_of_Darkness, 20.0f, 2.0f, 1.0f, 1.0f),
    STAGE_2("stage_2", "圣洁骑士后", BossList.Holy_Knight, 30.0f, 4.0f, 1.1f, 1.1f),
    STAGE_3("stage_3", "腐蚀灾虫后", BossList.Corrosion_infesting_Insect, 40.0f, 6.0f, 1.2f, 1.2f),
    STAGE_4("stage_4", "地狱领主后", BossList.Lord_of_Hell, 50.0f, 8.0f, 1.3f, 1.3f),
    STAGE_5("stage_5", "末地领主后", BossList.Lord_of_Ender, 60.0f, 10.0f, 1.4f, 1.4f),
    STAGE_6("stage_6", "黑暗之形后", BossList.Form_of_Darkness, 70.0f, 12.0f, 1.5f, 1.5f),
    STAGE_7("stage_7", "海啸神龙后", BossList.Tsunami_Dragon, 80.0f, 14.0f, 1.6f, 1.6f),
    STAGE_8("stage_8", "深渊游龙后", BossList.Abyssal_Dragon, 80.0f, 14.0f, 1.6f, 1.6f),
    STAGE_9("stage_9", "亡魂之主后", BossList.Lord_of_the_Dead, 90.0f, 16.0f, 1.7f, 1.7f),
    STAGE_10("stage_10", "圣炎灾劫后", BossList.Holy_Flame_Calamity, 100.0f, 18.0f, 1.8f, 1.8f),
    STAGE_11("stage_11", "灾厄之神后", BossList.God_of_Calamity, 120.0f, 20.0f, 2.0f, 2.0f);

    private final String id;
    private final String displayName;
    private final BossList bossEntity;
    private final float healthMultiplier;
    private final float damageMultiplier;
    private final float speedMultiplier;
    private final float armorMultiplier;

    GameStage(String id, String displayName, BossList bossEntity,
              float healthMultiplier, float damageMultiplier, float speedMultiplier, float armorMultiplier) {
        this.id = id;
        this.displayName = displayName;
        this.bossEntity = bossEntity;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.speedMultiplier = speedMultiplier;
        this.armorMultiplier = armorMultiplier;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BossList getBossEntity() {
        return bossEntity;
    }

    /**
     * 获取Boss对应的EntityType
     */
    public EntityType<? extends LivingEntity> getBossEntityType() {
        return getEntityTypeFromBossList(bossEntity);
    }

    public float getHealthMultiplier() {
        return healthMultiplier;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public float getArmorMultiplier() {
        return armorMultiplier;
    }

    /**
     * 获取下一个阶段
     */
    public GameStage getNextStage() {
        GameStage[] stages = values();
        int nextIndex = this.ordinal() + 1;
        return nextIndex < stages.length ? stages[nextIndex] : this;
    }

    /**
     * 获取前一个阶段
     */
    public GameStage getPreviousStage() {
        int prevIndex = this.ordinal() - 1;
        return prevIndex >= 0 ? values()[prevIndex] : this;
    }

    /**
     * 根据ID获取阶段
     */
    public static GameStage getById(String id) {
        for (GameStage stage : values()) {
            if (stage.id.equals(id)) {
                return stage;
            }
        }
        return STAGE_0; // 默认返回第一阶段
    }

    /**
     * 根据Boss实体类型获取阶段
     */
    public static GameStage getByBossEntity(EntityType<?> entityType) {
        for (GameStage stage : values()) {
            if (stage.getBossEntityType().equals(entityType)) {
                return stage;
            }
        }
        return null;
    }

    /**
     * 检查是否是最终阶段
     */
    public boolean isFinalStage() {
        return this == STAGE_11;
    }

    /**
     * 将BossList枚举值映射到对应的EntityType
     */
    private static EntityType<? extends LivingEntity> getEntityTypeFromBossList(BossList boss) {
        switch (boss) {
            case commander_of_the_undead_guard:
                // 暂时使用ExampleBoss作为默认实现
                return ModEntities.TRAINING_PUPPET.get();
            case Calamity_Mage:
                return ModEntities.TRAINING_PUPPET.get();
            case Acid_eroding_parasite:
                return ModEntities.TRAINING_PUPPET.get();
            case Jungle_turtle:
                return ModEntities.TRAINING_PUPPET.get();
            case Blood_red_slime:
                return ModEntities.TRAINING_PUPPET.get();
            case Stone_Giant:
                return ModEntities.TRAINING_PUPPET.get();
            case Flesh_and_blood_aggregation:
                return ModEntities.TRAINING_PUPPET.get();
            case Gatekeeper_of_Darkness:
                return ModEntities.TRAINING_PUPPET.get();
            case Reconnaissance_mechanical_bird:
                return ModEntities.TRAINING_PUPPET.get();
            case Thousand_faced_Spectre:
                return ModEntities.TRAINING_PUPPET.get();
            case Holy_Knight:
                return ModEntities.TRAINING_PUPPET.get();
            case Residual_soul_of_a_deity:
                return ModEntities.TRAINING_PUPPET.get();
            case Corrosion_infesting_Insect:
                return ModEntities.TRAINING_PUPPET.get();
            case Red_Flame_Demon:
                return ModEntities.TRAINING_PUPPET.get();
            case Lord_of_Hell:
                return ModEntities.TRAINING_PUPPET.get();
            case End_Dragon:
                return EntityType.ENDER_DRAGON; // 使用原版末影龙
            case Ender_servant:
                return ModEntities.TRAINING_PUPPET.get();
            case Lord_of_Ender:
                return ModEntities.TRAINING_PUPPET.get();
            case wither:
                return EntityType.WITHER; // 使用原版凋灵
            case Spirit_Fire_Elf:
                return ModEntities.TRAINING_PUPPET.get();
            case Form_of_Darkness:
                return ModEntities.TRAINING_PUPPET.get();
            case Mechanical_End_Dragon:
                return ModEntities.TRAINING_PUPPET.get();
            case Tsunami_Dragon:
                return ModEntities.TRAINING_PUPPET.get();
            case Abyssal_Dragon:
                return ModEntities.TRAINING_PUPPET.get();
            case Lord_of_the_Dead:
                return ModEntities.TRAINING_PUPPET.get();
            case Holy_Flame_Calamity:
                return ModEntities.TRAINING_PUPPET.get();
            case God_of_Calamity:
                return ModEntities.TRAINING_PUPPET.get();
            case Hunting_Dragon:
                return ModEntities.TRAINING_PUPPET.get();
            case Necromancer_Witch:
                return ModEntities.TRAINING_PUPPET.get();
            case Mage_portrait:
                return ModEntities.TRAINING_PUPPET.get();
            case End_of_Nightmare:
                return ModEntities.TRAINING_PUPPET.get(); // 使用已实现的EndOfNightmare
            default:
                return ModEntities.TRAINING_PUPPET.get(); // 默认返回ExampleBoss
        }
    }
}