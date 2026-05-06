package cn.dawnstring.fatality.system;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.api.systems.IModSystem;
import cn.dawnstring.fatality.items.normal.HeartOfLife;
import cn.dawnstring.fatality.utils.GameConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataSystem implements IModSystem {

    private static final Logger LOGGER = Fatality.LOGGER;
    private static final PlayerDataSystem INSTANCE = new PlayerDataSystem();
    private static final String DATA_NAME = "fatality_player_data";
    private static PlayerDataStorage playerDataStorage = null;

    public static PlayerDataSystem getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSystemId() {
        return "player_data";
    }

    @Override
    public void initialize() {
        LOGGER.info("PlayerData system initialized");
    }

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

    public static void savePlayerMana(Player player, float currentMana) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            storage.savePlayerMana(player.getUUID(), currentMana);
        }
    }

    public static float loadPlayerMana(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            return storage.loadPlayerMana(player.getUUID());
        }
        return ManaSystem.getClientCurrentMana(player);
    }

    public static void savePlayerBonusMana(Player player, float bonusMana) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            storage.savePlayerBonusMana(player.getUUID(), bonusMana);
        }
    }

    public static float loadPlayerBonusMana(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            return storage.loadPlayerBonusMana(player.getUUID());
        }
        return 0.0f;
    }

    public static void savePlayerBonusHealth(Player player, float bonusHealth) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            storage.savePlayerBonusHealth(player.getUUID(), bonusHealth);
        }
    }

    public static float loadPlayerBonusHealth(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage storage = getPlayerDataStorage(serverPlayer);
            return storage.loadPlayerBonusHealth(player.getUUID());
        }
        return 0.0f;
    }

    @Override
    public void onPlayerJoin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            float bonusMana = loadPlayerBonusMana(serverPlayer);
            float bonusHealth = loadPlayerBonusHealth(serverPlayer);
            float currentMana = loadPlayerMana(serverPlayer);

            applyHealthBonus(serverPlayer, bonusHealth);
            ManaSystem.setCurrentMana(serverPlayer, currentMana);
        }
    }

    @Override
    public void onPlayerRespawn(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            float bonusHealth = loadPlayerBonusHealth(serverPlayer);
            applyHealthBonus(serverPlayer, bonusHealth);
            float mana = loadPlayerMana(serverPlayer);
            ManaSystem.setCurrentMana(serverPlayer, mana);
            player.setHealth(player.getMaxHealth() / 2.0f);
            LOGGER.info("Restored persistent bonuses for {}: health={}, mana={}, set health to half={}",
                    player.getName().getString(), bonusHealth, mana, player.getMaxHealth() / 2.0f);
        }
    }

    @Override
    public void onPlayerLeave(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            savePlayerMana(serverPlayer, ManaSystem.getCurrentMana(serverPlayer));
            savePlayerBonusMana(serverPlayer, ManaSystem.getBonusMana(serverPlayer));
            savePlayerBonusHealth(serverPlayer, HeartOfLife.getBonusHealth(serverPlayer));
        }
    }

    public static void applyHealthBonus(Player player, float bonus) {
        var maxHealthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            String playerId = player.getUUID().toString();
            UUID modifierUUID = UUID.nameUUIDFromBytes(("persistent_health_bonus_" + playerId).getBytes());
            maxHealthAttribute.removeModifier(modifierUUID);
            if (bonus > 0) {
                var modifier = new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        modifierUUID, "Persistent Health Bonus", bonus,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
                maxHealthAttribute.addPermanentModifier(modifier);
            }
        }
    }

    public static class PlayerDataStorage extends SavedData {
        private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

        public PlayerDataStorage() {}

        public PlayerDataStorage(CompoundTag nbt) {
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

        public void savePlayerMana(UUID playerUUID, float currentMana) {
            PlayerData data = playerDataMap.computeIfAbsent(playerUUID, k -> new PlayerData());
            data.currentMana = currentMana;
            setDirty();
        }

        public float loadPlayerMana(UUID playerUUID) {
            PlayerData data = playerDataMap.get(playerUUID);
            return data != null ? data.currentMana : GameConstants.BASE_MAX_MANA * 0.5f;
        }

        public void savePlayerBonusMana(UUID playerUUID, float bonusMana) {
            PlayerData data = playerDataMap.computeIfAbsent(playerUUID, k -> new PlayerData());
            data.bonusMana = bonusMana;
            setDirty();
        }

        public float loadPlayerBonusMana(UUID playerUUID) {
            PlayerData data = playerDataMap.get(playerUUID);
            return data != null ? data.bonusMana : 0.0f;
        }

        public void savePlayerBonusHealth(UUID playerUUID, float bonusHealth) {
            PlayerData data = playerDataMap.computeIfAbsent(playerUUID, k -> new PlayerData());
            data.bonusHealth = bonusHealth;
            setDirty();
        }

        public float loadPlayerBonusHealth(UUID playerUUID) {
            PlayerData data = playerDataMap.get(playerUUID);
            return data != null ? data.bonusHealth : 0.0f;
        }
    }

    private static class PlayerData {
        public float currentMana = GameConstants.BASE_MAX_MANA * 0.5f;
        public float bonusMana = 0.0f;
        public float bonusHealth = 0.0f;
    }
}
