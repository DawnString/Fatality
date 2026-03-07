package cn.dawnstring.fatality.gamestage;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.entity.BaseBoss;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 游戏阶段管理器 - 基于世界的全局阶段系统
 * 负责管理整个世界的游戏阶段
 */
@Mod.EventBusSubscriber(modid = Fatality.MODID, value = Dist.DEDICATED_SERVER)
public class GameStageManager {

    private static final Map<String, WorldStageData> worldStages = new HashMap<>();

    // 世界数据存储键
    private static final String DATA_NAME = "fatality_world_stage";

    /**
     * 获取指定世界的游戏阶段
     */
    public static GameStage getWorldStage(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            WorldStageData data = getWorldStageData(serverLevel);
            return data.getCurrentStage();
        }
        return GameStage.STAGE_0; // 客户端默认返回第一阶段
    }

    /**
     * 设置指定世界的游戏阶段
     */
    public static void setWorldStage(Level level, GameStage stage) {
        if (level instanceof ServerLevel serverLevel) {
            WorldStageData data = getWorldStageData(serverLevel);
            data.setCurrentStage(stage);
            data.setDirty(); // 标记数据需要保存
        }
    }

    /**
     * 当Boss被击败时触发世界阶段升级
     */
    @SubscribeEvent
    public static void onBossDefeated(LivingDeathEvent event) {
        // 首先检查被击杀的实体是否是Boss，避免玩家死亡时触发
        LivingEntity killedEntity = event.getEntity();
        EntityType<?> entityType = killedEntity.getType();

        // 检查被击杀的实体是否是阶段Boss
        GameStage bossStage = getGameStageByEntityType(entityType);
        if (bossStage == null) {
            return; // 不是Boss，直接返回，避免干扰玩家重生机制
        }

        // 然后检查攻击者是否是玩家
        if (event.getSource().getEntity() instanceof Player player) {
            Level level = killedEntity.level();

            if (level instanceof ServerLevel serverLevel) {
                GameStage currentStage = getWorldStage(level);

                // 如果击败的Boss阶段比当前世界阶段高，则升级世界阶段
                if (bossStage.ordinal() > currentStage.ordinal()) {
                    setWorldStage(level, bossStage);

                    // 广播世界阶段升级消息
                    String message = String.format("§6世界阶段已提升至 %s！击败了 %s",
                            bossStage.getDisplayName(),
                            bossStage.getBossEntityType().getDescription().getString());

                    // 向所有在线玩家广播消息
                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                            net.minecraft.network.chat.Component.literal(message), false);

                    // 触发阶段升级事件
                    MinecraftForge.EVENT_BUS.post(new WorldStageUpgradeEvent(serverLevel, currentStage, bossStage));

                    // 重新应用增益到所有已存在的实体
                    reapplyStageModifiersToAllEntities(serverLevel);
                }
            }
        }
    }

    /**
     * 处理玩家死亡事件 - 通知活跃的Boss
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        // 检查死亡的实体是否是玩家
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 只在服务器端处理
        if (player.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) player.level();

        // 在玩家周围64格范围内查找所有BaseBoss实体
        net.minecraft.world.phys.AABB searchArea = player.getBoundingBox().inflate(64.0);
        java.util.List<BaseBoss> bosses = serverLevel.getEntitiesOfClass(BaseBoss.class, searchArea);

        // 通知每个Boss玩家死亡事件
        for (BaseBoss boss : bosses) {
            if (boss.isAlive()) {
                boss.onPlayerDeath(player);
            }
        }
    }


    /**
     * 重新应用阶段增益到世界中的所有实体
     */
    public static void reapplyStageModifiersToAllEntities(ServerLevel level) {
        level.getEntities().getAll().forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity && !(livingEntity instanceof Player)) {
                applyStageModifiers(livingEntity);
            }
        });
    }

    /**
     * 世界加载时初始化阶段数据
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // 确保世界阶段数据被加载
            getWorldStageData(serverLevel);
        }
    }

    /**
     * 获取或创建世界阶段数据
     */
    private static WorldStageData getWorldStageData(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(WorldStageData::load, WorldStageData::new, DATA_NAME);
    }

    /**
     * 应用阶段增益到实体
     */
    public static void applyStageModifiers(LivingEntity entity) {
        if (entity instanceof Player) return; // 不对玩家应用增益

        GameStage worldStage = getWorldStage(entity.level());

        // 获取生物的基础属性
        var maxHealthAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        var attackDamageAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        var movementSpeedAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        var armorAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
        var armorToughnessAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);

        // 为每个属性生成唯一的UUID（基于实体UUID和阶段ID）
        UUID baseUUID = entity.getUUID();
        UUID healthUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_health_" + worldStage.getId()).getBytes());
        UUID damageUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_damage_" + worldStage.getId()).getBytes());
        UUID speedUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_speed_" + worldStage.getId()).getBytes());
        UUID armorUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_armor_" + worldStage.getId()).getBytes());
        UUID toughnessUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_toughness_" + worldStage.getId()).getBytes());

        // 应用生命值增益
        if (maxHealthAttribute != null) {
            // 先移除之前的增益（如果有）
            maxHealthAttribute.removeModifier(healthUUID);

            // 添加新的增益
            maxHealthAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    healthUUID,
                    "world_stage_health_" + worldStage.getId(),
                    worldStage.getHealthMultiplier() - 1.0f, // 乘数减1，因为AttributeModifier是加法
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 应用攻击伤害增益
        if (attackDamageAttribute != null) {
            attackDamageAttribute.removeModifier(damageUUID);
            attackDamageAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    damageUUID,
                    "world_stage_damage_" + worldStage.getId(),
                    worldStage.getDamageMultiplier() - 1.0f,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 应用移动速度增益
        if (movementSpeedAttribute != null) {
            movementSpeedAttribute.removeModifier(speedUUID);
            movementSpeedAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    speedUUID,
                    "world_stage_speed_" + worldStage.getId(),
                    worldStage.getSpeedMultiplier() - 1.0f,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 应用护甲增益
        if (armorAttribute != null) {
            armorAttribute.removeModifier(armorUUID);
            armorAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    armorUUID,
                    "world_stage_armor_" + worldStage.getId(),
                    worldStage.getArmorMultiplier() - 1.0f,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 应用护甲韧性增益
        if (armorToughnessAttribute != null) {
            armorToughnessAttribute.removeModifier(toughnessUUID);
            armorToughnessAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    toughnessUUID,
                    "world_stage_toughness_" + worldStage.getId(),
                    worldStage.getArmorMultiplier() - 1.0f, // 使用护甲乘数作为韧性乘数
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 设置当前生命值为最大生命值
        entity.setHealth(entity.getMaxHealth());
    }

    /**
     * 重置指定世界的阶段
     */
    public static void resetWorldStage(Level level) {
        setWorldStage(level, GameStage.STAGE_1);
    }

    /**
     * 获取阶段数量
     */
    public static int getStageCount() {
        return GameStage.values().length;
    }

    /**
     * 获取所有阶段信息
     */
    public static GameStage[] getAllStages() {
        return GameStage.values();
    }

    /**
     * 根据EntityType获取对应的GameStage
     */
    private static GameStage getGameStageByEntityType(EntityType<?> entityType) {
        for (GameStage stage : GameStage.values()) {
            if (stage.getBossEntityType().equals(entityType)) {
                return stage;
            }
        }
        return null;
    }

    /**
     * 世界阶段数据存储类
     */
    public static class WorldStageData extends SavedData {
        private GameStage currentStage = GameStage.STAGE_1;

        public WorldStageData() {
        }

        public WorldStageData(CompoundTag nbt) {
            String stageId = nbt.getString("currentStage");
            this.currentStage = GameStage.getById(stageId);
        }

        @Nonnull
        @Override
        public CompoundTag save(@Nonnull CompoundTag compound) {
            compound.putString("currentStage", currentStage.getId());
            return compound;
        }

        public static WorldStageData load(CompoundTag nbt) {
            return new WorldStageData(nbt);
        }

        public GameStage getCurrentStage() {
            return currentStage;
        }

        public void setCurrentStage(GameStage stage) {
            this.currentStage = stage;
            setDirty();
        }
    }

    /**
     * 世界阶段升级事件
     */
    public static class WorldStageUpgradeEvent extends net.minecraftforge.event.level.LevelEvent {
        private final GameStage oldStage;
        private final GameStage newStage;

        public WorldStageUpgradeEvent(ServerLevel level, GameStage oldStage, GameStage newStage) {
            super(level);
            this.oldStage = oldStage;
            this.newStage = newStage;
        }

        public GameStage getOldStage() {
            return oldStage;
        }

        public GameStage getNewStage() {
            return newStage;
        }

        public ServerLevel getServerLevel() {
            return (ServerLevel) getLevel();
        }
    }
}