package cn.dawnstring.fatality.gamestage;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.api.systems.IModSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameStageManager implements IModSystem {

    private static final Logger LOGGER = Fatality.LOGGER;
    private static final GameStageManager INSTANCE = new GameStageManager();
    private static final Map<String, WorldStageData> worldStages = new HashMap<>();
    private static final String DATA_NAME = "fatality_world_stage";

    public static GameStageManager getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSystemId() {
        return "game_stage";
    }

    @Override
    public void initialize() {
        LOGGER.info("GameStage system initialized");
    }

    public static GameStage getWorldStage(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            WorldStageData data = getWorldStageData(serverLevel);
            return data.getCurrentStage();
        }
        return GameStage.STAGE_0;
    }

    public static void setWorldStage(Level level, GameStage stage) {
        if (level instanceof ServerLevel serverLevel) {
            WorldStageData data = getWorldStageData(serverLevel);
            data.setCurrentStage(stage);
            data.setDirty();
        }
    }

    @Override
    public void onLivingDeath(LivingEntity killed) {
        if (killed.level().isClientSide()) return;

        EntityType<?> entityType = killed.getType();
        GameStage bossStage = getGameStageByEntityType(entityType);
        if (bossStage == null) return;

        if (killed.getLastDamageSource() != null
                && killed.getLastDamageSource().getEntity() instanceof Player player) {
            Level level = killed.level();

            if (level instanceof ServerLevel serverLevel) {
                GameStage currentStage = getWorldStage(level);

                if (bossStage.ordinal() > currentStage.ordinal()) {
                    setWorldStage(level, bossStage);

                    String message = String.format("§6世界阶段已提升至 %s！击败了 %s",
                            bossStage.getDisplayName(),
                            bossStage.getBossEntityType().getDescription().getString());

                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                            net.minecraft.network.chat.Component.literal(message), false);

                    MinecraftForge.EVENT_BUS.post(new WorldStageUpgradeEvent(serverLevel, currentStage, bossStage));
                    reapplyStageModifiersToAllEntities(serverLevel);
                }
            }
        }
    }

    @Override
    public void onWorldLoad(ServerLevel level) {
        getWorldStageData(level);
    }

    public static void reapplyStageModifiersToAllEntities(ServerLevel level) {
        level.getEntities().getAll().forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity && !(livingEntity instanceof Player)) {
                applyStageModifiers(livingEntity);
            }
        });
    }

    private static WorldStageData getWorldStageData(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(WorldStageData::load, WorldStageData::new, DATA_NAME);
    }

    public static void applyStageModifiers(LivingEntity entity) {
        if (entity instanceof Player) return;

        GameStage worldStage = getWorldStage(entity.level());

        var maxHealthAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        var attackDamageAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        var movementSpeedAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        var armorAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
        var armorToughnessAttribute = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);

        UUID baseUUID = entity.getUUID();
        UUID healthUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_health_" + worldStage.getId()).getBytes());
        UUID damageUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_damage_" + worldStage.getId()).getBytes());
        UUID speedUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_speed_" + worldStage.getId()).getBytes());
        UUID armorUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_armor_" + worldStage.getId()).getBytes());
        UUID toughnessUUID = UUID.nameUUIDFromBytes((baseUUID.toString() + "_toughness_" + worldStage.getId()).getBytes());

        if (maxHealthAttribute != null) {
            maxHealthAttribute.removeModifier(healthUUID);
            maxHealthAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    healthUUID,
                    "world_stage_health_" + worldStage.getId(),
                    worldStage.getHealthMultiplier() - 1.0f,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        if (attackDamageAttribute != null) {
            attackDamageAttribute.removeModifier(damageUUID);
            attackDamageAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    damageUUID,
                    "world_stage_damage_" + worldStage.getId(),
                    worldStage.getDamageMultiplier() - 1.0f,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        if (movementSpeedAttribute != null) {
            movementSpeedAttribute.removeModifier(speedUUID);
            movementSpeedAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    speedUUID,
                    "world_stage_speed_" + worldStage.getId(),
                    worldStage.getSpeedMultiplier() - 1.0f,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        if (armorAttribute != null) {
            armorAttribute.removeModifier(armorUUID);
            armorAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    armorUUID,
                    "world_stage_armor_" + worldStage.getId(),
                    worldStage.getArmorMultiplier() - 1.0f,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        if (armorToughnessAttribute != null) {
            armorToughnessAttribute.removeModifier(toughnessUUID);
            armorToughnessAttribute.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    toughnessUUID,
                    "world_stage_toughness_" + worldStage.getId(),
                    worldStage.getArmorMultiplier() - 1.0f,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        entity.setHealth(entity.getMaxHealth());
    }

    public static void resetWorldStage(Level level) {
        setWorldStage(level, GameStage.STAGE_1);
    }

    public static int getStageCount() {
        return GameStage.values().length;
    }

    public static GameStage[] getAllStages() {
        return GameStage.values();
    }

    private static GameStage getGameStageByEntityType(EntityType<?> entityType) {
        for (GameStage stage : GameStage.values()) {
            if (stage.getBossEntityType().equals(entityType)) {
                return stage;
            }
        }
        return null;
    }

    public static class WorldStageData extends SavedData {
        private GameStage currentStage = GameStage.STAGE_1;

        public WorldStageData() {}

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
        }
    }

    public static class WorldStageUpgradeEvent extends net.minecraftforge.eventbus.api.Event {
        private final ServerLevel level;
        private final GameStage oldStage;
        private final GameStage newStage;

        public WorldStageUpgradeEvent(ServerLevel level, GameStage oldStage, GameStage newStage) {
            this.level = level;
            this.oldStage = oldStage;
            this.newStage = newStage;
        }

        public ServerLevel getLevel() { return level; }
        public GameStage getOldStage() { return oldStage; }
        public GameStage getNewStage() { return newStage; }
    }
}
