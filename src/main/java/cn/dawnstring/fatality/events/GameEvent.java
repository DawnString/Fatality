package cn.dawnstring.fatality.events;

import cn.dawnstring.fatality.gamestage.GameStage;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * 游戏事件枚举 - 基于游戏阶段区分强度版本
 */
public enum GameEvent {
    // 日食事件 - 根据游戏阶段决定强度
    SOLAR_ECLIPSE("solar_eclipse", "日食",
            Arrays.asList(
                    // 阶段0: 初始日食
                    new EventVersion("初始日食", GameStage.STAGE_0, GameStage.STAGE_0,
                            1.2f, 1.1f,
                            Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.PHANTOM),
                            Arrays.asList(EntityType.CREEPER, EntityType.SPIDER),
                            "solar_eclipse_initial"
                    ),
                    // 阶段1-5: 守门人后日食
                    new EventVersion("守门人后日食", GameStage.STAGE_1, GameStage.STAGE_5,
                            1.5f, 1.3f,
                            Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.PHANTOM,
                                    EntityType.ENDERMAN, EntityType.WITCH, EntityType.BLAZE),
                            Arrays.asList(EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER),
                            "solar_eclipse_Gatekeeper_of_Darkness"
                    ),
                    // 阶段6-9: 黑暗之形后日食
                    new EventVersion("黑暗之形后日食", GameStage.STAGE_6, GameStage.STAGE_9,
                            1.8f, 1.5f,
                            Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.PHANTOM,
                                    EntityType.ENDERMAN, EntityType.WITCH, EntityType.BLAZE,
                                    EntityType.WITHER_SKELETON, EntityType.GUARDIAN),
                            Arrays.asList(EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER,
                                    EntityType.SILVERFISH, EntityType.ENDERMITE),
                            "solar_eclipse_Form_of_Darkness"
                    ),
                    // 阶段10-11: 圣炎灾劫后日食
                    new EventVersion("圣炎灾劫后日食", GameStage.STAGE_10, GameStage.STAGE_11,
                            1.8f, 1.5f,
                            Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.PHANTOM,
                                    EntityType.ENDERMAN, EntityType.WITCH, EntityType.BLAZE,
                                    EntityType.WITHER_SKELETON, EntityType.GUARDIAN),
                            Arrays.asList(EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER,
                                    EntityType.SILVERFISH, EntityType.ENDERMITE),
                            "solar_eclipse_Holy_Flame_Calamity"
                    )
            ),
            0.1f, // 10% 触发概率
            24000  // 持续1天
    ),

    // 血月事件 - 根据游戏阶段决定强度
    BLOOD_MOON("blood_moon", "血月",
            Arrays.asList(
                    // 阶段0: 初始血月
                    new EventVersion("初始血月", GameStage.STAGE_0, GameStage.STAGE_0,
                            1.3f, 1.2f,
                            Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.DROWNED),
                            Arrays.asList(EntityType.SPIDER, EntityType.CAVE_SPIDER),
                            "blood_moon_initial"
                    ),
                    // 阶段1-5: 守门人后血月
                    new EventVersion("守门人后血月", GameStage.STAGE_1, GameStage.STAGE_5,
                            1.6f, 1.4f,
                            Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.DROWNED,
                                    EntityType.HUSK, EntityType.ZOMBIFIED_PIGLIN, EntityType.PHANTOM),
                            Arrays.asList(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH),
                            "blood_moon_Gatekeeper_of_Darkness"
                    ),
                    // 阶段6-10: 黑暗之形后血月
                    new EventVersion("黑暗之形后血月", GameStage.STAGE_6, GameStage.STAGE_10,
                            1.9f, 1.6f,
                            Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.DROWNED,
                                    EntityType.HUSK, EntityType.ZOMBIFIED_PIGLIN, EntityType.PHANTOM,
                                    EntityType.WITHER_SKELETON, EntityType.GUARDIAN, EntityType.ENDERMAN),
                            Arrays.asList(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH,
                                    EntityType.ENDERMITE, EntityType.MAGMA_CUBE),
                            "blood_moon_Form_of_Darkness"
                    ),
                    // 阶段11: 灾厄之神后血月
                    new EventVersion("灾厄之神后血月", GameStage.STAGE_11, GameStage.STAGE_11,
                            1.9f, 1.6f,
                            Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.DROWNED,
                                    EntityType.HUSK, EntityType.ZOMBIFIED_PIGLIN, EntityType.PHANTOM,
                                    EntityType.WITHER_SKELETON, EntityType.GUARDIAN, EntityType.ENDERMAN),
                            Arrays.asList(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH,
                                    EntityType.ENDERMITE, EntityType.MAGMA_CUBE),
                            "blood_moon_God_of_Calamity"
                    )
            ),
            0.1f, // 10% 触发概率
            24000  // 持续1天
    );

    private final String id;
    private final String displayName;
    private final List<EventVersion> versions;
    private final float triggerProbability;
    private final int duration;

    GameEvent(String id, String displayName, List<EventVersion> versions,
              float triggerProbability, int duration) {
        this.id = id;
        this.displayName = displayName;
        this.versions = versions;
        this.triggerProbability = triggerProbability;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<EventVersion> getVersions() {
        return versions;
    }

    public float getTriggerProbability() {
        return triggerProbability;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * 根据游戏阶段获取对应的事件版本
     */
    public EventVersion getVersionForStage(GameStage gameStage) {
        for (EventVersion version : versions) {
            if (version.isValidForStage(gameStage)) {
                return version;
            }
        }
        // 如果没有找到匹配的版本，返回最后一个版本（最高阶段）
        return versions.get(versions.size() - 1);
    }

    /**
     * 检查是否应该触发事件
     */
    public boolean shouldTrigger(Level level, Random random) {
        // 只在夜晚触发
        if (!level.isNight()) {
            return false;
        }

        // 检查概率
        return random.nextFloat() < triggerProbability;
    }

    /**
     * 根据ID获取事件
     */
    public static GameEvent getById(String id) {
        for (GameEvent event : values()) {
            if (event.id.equals(id)) {
                return event;
            }
        }
        return null;
    }

    /**
     * 事件版本类 - 基于游戏阶段
     */
    public static class EventVersion {
        private final String name;
        private final GameStage minStage;
        private final GameStage maxStage;
        private final float spawnRateMultiplier;
        private final float spawnCapMultiplier;
        private final List<EntityType<? extends LivingEntity>> increasedSpawnTypes;
        private final List<EntityType<? extends LivingEntity>> decreasedSpawnTypes;
        private final String weatherType;

        public EventVersion(String name, GameStage minStage, GameStage maxStage,
                            float spawnRateMultiplier, float spawnCapMultiplier,
                            List<EntityType<? extends LivingEntity>> increasedSpawnTypes,
                            List<EntityType<? extends LivingEntity>> decreasedSpawnTypes,
                            String weatherType) {
            this.name = name;
            this.minStage = minStage;
            this.maxStage = maxStage;
            this.spawnRateMultiplier = spawnRateMultiplier;
            this.spawnCapMultiplier = spawnCapMultiplier;
            this.increasedSpawnTypes = increasedSpawnTypes;
            this.decreasedSpawnTypes = decreasedSpawnTypes;
            this.weatherType = weatherType;
        }

        public String getName() {
            return name;
        }

        public GameStage getMinStage() {
            return minStage;
        }

        public GameStage getMaxStage() {
            return maxStage;
        }

        public float getSpawnRateMultiplier() {
            return spawnRateMultiplier;
        }

        public float getSpawnCapMultiplier() {
            return spawnCapMultiplier;
        }

        public List<EntityType<? extends LivingEntity>> getIncreasedSpawnTypes() {
            return increasedSpawnTypes;
        }

        public List<EntityType<? extends LivingEntity>> getDecreasedSpawnTypes() {
            return decreasedSpawnTypes;
        }

        public String getWeatherType() {
            return weatherType;
        }

        /**
         * 检查此版本是否适用于指定的游戏阶段
         */
        public boolean isValidForStage(GameStage gameStage) {
            int stageOrdinal = gameStage.ordinal();
            int minOrdinal = minStage.ordinal();
            int maxOrdinal = maxStage.ordinal();
            return stageOrdinal >= minOrdinal && stageOrdinal <= maxOrdinal;
        }

        /**
         * 获取显示名称（包含版本信息）
         */
        public String getFullDisplayName() {
            return name;
        }
    }
}
