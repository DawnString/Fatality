package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.items.normal.HeartOfLife;
import cn.dawnstring.fatality.utils.GameConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家数据持久化系统 - 管理玩家数据的保存和加载
 */
@Mod.EventBusSubscriber
public class PlayerDataSystem {
    private static final String DATA_NAME = "fatality_player_data";

    // 玩家数据存储实例
    private static PlayerDataStorage playerDataStorage = null;

    /**
     * 获取玩家数据存储实例
     */
    private static PlayerDataStorage getPlayerDataStorage(ServerPlayer player) {
        if (playerDataStorage == null) {
            playerDataStorage = player.getServer().overworld().getDataStorage().computeIfAbsent(
                    PlayerDataStorage::load,
                    PlayerDataStorage::new,
                    DATA_NAME
            );
        }
        return playerDataStorage;
    }

    /**
     * 保存玩家当前魔法值
     */
    public static void savePlayerMana(Player player, float currentMana) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            storage.savePlayerMana(player.getUUID(), currentMana);
        }
    }

    /**
     * 加载玩家当前魔法值
     */
    public static float loadPlayerMana(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            return storage.loadPlayerMana(player.getUUID());
        }
        // 对于非服务器玩家（如客户端），使用客户端数据同步系统
        // 避免调用ManaSystem.getCurrentMana导致无限递归
        return ManaSystem.getClientCurrentMana(player);
    }

    /**
     * 保存玩家通过物品增加的魔法值
     */
    public static void savePlayerBonusMana(Player player, float bonusMana) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            storage.savePlayerBonusMana(player.getUUID(), bonusMana);
        }
    }

    /**
     * 加载玩家通过物品增加的魔法值
     */
    public static float loadPlayerBonusMana(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            return storage.loadPlayerBonusMana(player.getUUID());
        }
        return 0.0f; // 默认值
    }

    /**
     * 保存玩家通过物品增加的生命值
     */
    public static void savePlayerBonusHealth(Player player, float bonusHealth) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            storage.savePlayerBonusHealth(player.getUUID(), bonusHealth);
        }
    }

    /**
     * 加载玩家通过物品增加的生命值
     */
    public static float loadPlayerBonusHealth(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            return storage.loadPlayerBonusHealth(player.getUUID());
        }
        return 0.0f; // 默认值
    }

    /**
     * 玩家数据存储类
     */
    public static class PlayerDataStorage extends SavedData {
        private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

        public PlayerDataStorage()
        {
        }

        public PlayerDataStorage(CompoundTag nbt) {
            // 加载所有玩家数据
            CompoundTag playersTag = nbt.getCompound("players");
            for (String playerId : playersTag.getAllKeys()) {
                UUID playerUUID = UUID.fromString(playerId);
                CompoundTag playerTag = playersTag.getCompound(playerId);

                PlayerData data = new PlayerData();
                data.currentMana = playerTag.getFloat("currentMana");
                data.bonusMana = playerTag.getFloat("bonusMana");
                data.bonusHealth = playerTag.getFloat("bonusHealth");

                playerDataMap.put(playerUUID, data);
            }
        }

        @Nonnull
        @Override
        public CompoundTag save(@Nonnull CompoundTag compound) {
            CompoundTag playersTag = new CompoundTag();

            for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putFloat("currentMana", entry.getValue().currentMana);
                playerTag.putFloat("bonusMana", entry.getValue().bonusMana);
                playerTag.putFloat("bonusHealth", entry.getValue().bonusHealth);

                playersTag.put(entry.getKey().toString(), playerTag);
            }

            compound.put("players", playersTag);
            return compound;
        }

        public static PlayerDataStorage load(CompoundTag nbt) {
            return new PlayerDataStorage(nbt);
        }

        // 保存玩家魔法值
        public void savePlayerMana(UUID playerUUID, float currentMana) {
            PlayerData data = playerDataMap.computeIfAbsent(playerUUID, k -> new PlayerData());
            data.currentMana = currentMana;
            setDirty();
        }

        // 加载玩家魔法值
        public float loadPlayerMana(UUID playerUUID) {
            PlayerData data = playerDataMap.get(playerUUID);
            return data != null ? data.currentMana : GameConstants.BASE_MAX_MANA * 0.5f;
        }

        // 保存玩家魔法值加成
        public void savePlayerBonusMana(UUID playerUUID, float bonusMana) {
            PlayerData data = playerDataMap.computeIfAbsent(playerUUID, k -> new PlayerData());
            data.bonusMana = bonusMana;
            setDirty();
        }

        // 加载玩家魔法值加成
        public float loadPlayerBonusMana(UUID playerUUID)
        {
            PlayerData data = playerDataMap.get(playerUUID);
            return data != null ? data.bonusMana : 0.0f;
        }

        // 保存玩家生命值加成
        public void savePlayerBonusHealth(UUID playerUUID, float bonusHealth) {
            PlayerData data = playerDataMap.computeIfAbsent(playerUUID, k -> new PlayerData());
            data.bonusHealth = bonusHealth;
            setDirty();
        }

        // 加载玩家生命值加成
        public float loadPlayerBonusHealth(UUID playerUUID) {
            PlayerData data = playerDataMap.get(playerUUID);
            return data != null ? data.bonusHealth : 0.0f;
        }
    }

    /**
     * 玩家数据类
     */
    private static class PlayerData {
        public float currentMana = GameConstants.BASE_MAX_MANA * 0.5f; // 默认当前魔法值
        public float bonusMana = 0.0f; // 物品增加的魔法值
        public float bonusHealth = 0.0f; // 物品增加的生命值
    }

    /**
     * 玩家登录时加载数据
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 加载玩家数据
            float bonusMana = loadPlayerBonusMana(player);
            float bonusHealth = loadPlayerBonusHealth(player);
            float currentMana = loadPlayerMana(player);

            // 应用生命值加成
            applyHealthBonus(player, bonusHealth);

            // 设置当前魔法值
            ManaSystem.setCurrentMana(player, currentMana);
        }
    }

    /**
     * 玩家退出时保存数据
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 保存玩家数据
            savePlayerMana(player, ManaSystem.getCurrentMana(player));
            savePlayerBonusMana(player, ManaSystem.getBonusMana(player));
            savePlayerBonusHealth(player, HeartOfLife.getBonusHealth(player));
        }
    }

    /**
     * 应用生命值加成
     */
    public static void applyHealthBonus(Player player, float bonus) {
        var maxHealthAttribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            String playerId = player.getUUID().toString();
            UUID modifierUUID = UUID.nameUUIDFromBytes(("persistent_health_bonus_" + playerId).getBytes());

            // 移除旧的修改器
            maxHealthAttribute.removeModifier(modifierUUID);

            // 添加新的修改器
            if (bonus > 0) {
                net.minecraft.world.entity.ai.attributes.AttributeModifier modifier =
                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                modifierUUID,
                                "Persistent Health Bonus",
                                bonus,
                                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION
                        );
                maxHealthAttribute.addPermanentModifier(modifier);
            }
        }
    }
}