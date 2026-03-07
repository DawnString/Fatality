package cn.dawnstring.fatality.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * 训练人偶实体 - 用于测试DPS
 * 优化版：修复内存泄漏，增加更多功能
 */
public class TrainingPuppet extends Mob {
    // 同步数据定义
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
            SynchedEntityData.defineId(TrainingPuppet.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> DATA_START_TIME =
            SynchedEntityData.defineId(TrainingPuppet.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> DATA_CUSTOM_MAX_HEALTH =
            SynchedEntityData.defineId(TrainingPuppet.class, EntityDataSerializers.FLOAT);

    // DPS追踪相关
    private final Map<UUID, DpsTracker> playerDpsTrackers = new HashMap<>();
    private final Object trackerLock = new Object(); // 同步锁
    private long lastDpsUpdateTime = 0;
    private long lastCleanupTime = 0;

    // 配置常量
    private static final long DPS_UPDATE_INTERVAL = 1000; // 1秒更新一次DPS
    private static final long CLEANUP_INTERVAL = 5000; // 5秒清理一次
    private static final long HISTORY_DURATION = 5000; // 5秒历史数据
    private static final float DEFAULT_MAX_HEALTH = 1000.0f;

    // 配置项
    private boolean autoReset = false;
    private long autoResetTime = 30000; // 30秒后自动重置
    private float displayRange = 10.0f; // 显示范围

    public TrainingPuppet(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoAi(true); // 禁用AI，使其不会移动
        this.setInvulnerable(true); // 免疫所有伤害，但我们会自定义伤害处理
        this.setSilent(true); // 静音
        this.setNoGravity(true); // 无重力
        this.setCustomName(Component.literal("§6训练人偶"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, DEFAULT_MAX_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D) // 免疫击退
                .add(Attributes.MOVEMENT_SPEED, 0.0D); // 无法移动
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTIVE, false);
        this.entityData.define(DATA_START_TIME, 0L);
        this.entityData.define(DATA_CUSTOM_MAX_HEALTH, DEFAULT_MAX_HEALTH);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            long currentTime = System.currentTimeMillis();

            // 定期清理
            if (currentTime - lastCleanupTime >= CLEANUP_INTERVAL) {
                cleanupInactiveTrackers();
                lastCleanupTime = currentTime;
            }

            // DPS更新
            updateDpsDisplay();

            // 检查自动重置
            if (isActive() && autoReset) {
                long elapsedTime = currentTime - getStartTime();
                if (elapsedTime >= autoResetTime) {
                    reset();
                    broadcastMessage("§e训练人偶已自动重置（" + (autoResetTime / 1000) + "秒后）");
                }
            }
        }
    }

    /**
     * 重写hurt方法，记录伤害但不实际扣血
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) {
            return false;
        }

        // 检查伤害来源是否为玩家
        if (source.getEntity() instanceof Player player) {
            // 防止NaN值
            if (Float.isNaN(amount) || Float.isInfinite(amount)) {
                amount = 1.0f; // 设置为默认伤害值
            }
            
            // 确保伤害值有效
            if (amount <= 0) {
                amount = 1.0f; // 确保有最小伤害值
            }

            // 激活训练人偶
            if (!isActive()) {
                activate();
            }

            // 记录伤害
            recordDamage(player, amount);

            // 播放被击效果
            playHurtEffect(amount);

            // 显示单次伤害（可选）
            if (amount >= 100) {
                player.displayClientMessage(
                        Component.literal(String.format("§c💥 暴击: §e%.1f 伤害", amount)),
                        true
                );
            }

            // 立即刷新DPS显示，确保当前伤害能够实时显示
            updateDpsDisplayImmediately();

            return true; // 返回true表示"受到伤害"，但实际上不会扣血
        }

        return false;
    }

    /**
     * 交互方法：右键操作
     */
    @Nonnull
    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, @Nonnull InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            ItemStack item = player.getItemInHand(hand);

            if (item.isEmpty()) {
                // Shift+右键：破坏人偶
                if (player.isShiftKeyDown()) {
                    this.discard();
                    player.displayClientMessage(Component.literal("§c已破坏训练人偶"), false);
                    return InteractionResult.SUCCESS;
                }
                
                // 普通右键：重置数据并隐藏显示
                reset();
                player.displayClientMessage(Component.literal("§c已重置训练数据"), false);
                return InteractionResult.SUCCESS;
            }
        }
        return super.interactAt(player, vec, hand);
    }

    /**
     * 激活训练人偶
     */
    public void activate() {
        this.entityData.set(DATA_ACTIVE, true);
        this.entityData.set(DATA_START_TIME, System.currentTimeMillis());

        synchronized (trackerLock) {
            playerDpsTrackers.clear();
        }

        broadcastMessage("§a训练人偶已激活，开始记录DPS！");
        broadcastMessage("§7空手右键可以重置统计数据");

        // 重置生命值显示
        this.setHealth(this.getMaxHealth());
    }

    /**
     * 重置训练人偶
     */
    public void reset() {
        this.entityData.set(DATA_ACTIVE, false);
        this.entityData.set(DATA_START_TIME, 0L);

        synchronized (trackerLock) {
            playerDpsTrackers.clear();
        }

        // 重置生命值显示
        this.setHealth(this.getMaxHealth());
    }

    /**
     * 设置最大生命值
     */
    public void setMaxHealth(float health) {
        if (health <= 0) health = 1.0f;
        this.entityData.set(DATA_CUSTOM_MAX_HEALTH, health);
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(health);
        this.setHealth(health);
    }

    /**
     * 设置自动重置
     */
    public void setAutoReset(boolean enabled, long resetTimeMs) {
        this.autoReset = enabled;
        this.autoResetTime = Math.max(1000, resetTimeMs); // 最少1秒
    }

    /**
     * 设置显示范围
     */
    public void setDisplayRange(float range) {
        this.displayRange = Math.max(1.0f, Math.min(50.0f, range));
    }

    /**
     * 记录玩家伤害
     */
    private void recordDamage(Player player, float damage) {
        if (damage <= 0) return;

        synchronized (trackerLock) {
            DpsTracker tracker = playerDpsTrackers.computeIfAbsent(
                    player.getUUID(),
                    k -> new DpsTracker()
            );
            tracker.recordDamage(damage);
        }
    }

    /**
     * 立即更新DPS显示 - 每次伤害发生时立即刷新
     */
    private void updateDpsDisplayImmediately() {
        if (isActive()) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - getStartTime();
            float maxHealth = getCustomMaxHealth();

            // 获取范围内玩家
            List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                    Player.class,
                    this.getBoundingBox().inflate(displayRange)
            );

            // 计算总DPS（所有玩家）
            double totalDps;
            double totalDamage;

            synchronized (trackerLock) {
                totalDps = playerDpsTrackers.values().stream()
                        .mapToDouble(DpsTracker::getCurrentDps)
                        .sum();

                totalDamage = playerDpsTrackers.values().stream()
                        .mapToDouble(DpsTracker::getTotalDamage)
                        .sum();
            }

            // 只在有伤害时显示数据
            if (totalDamage > 0) {
                // 为每个玩家显示信息
                for (Player player : nearbyPlayers) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        DpsTracker tracker;
                        synchronized (trackerLock) {
                            tracker = playerDpsTrackers.get(player.getUUID());
                        }

                        // 发送数据包给客户端进行渲染
                        sendDpsDataToClient(serverPlayer, tracker, totalDps, totalDamage, elapsedTime, maxHealth);
                    }
                }
            }
            
            // 更新最后刷新时间，避免重复刷新
            lastDpsUpdateTime = currentTime;
        }
    }

    /**
     * 更新DPS显示 - 只在有伤害时显示
     */
    private void updateDpsDisplay() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastDpsUpdateTime >= DPS_UPDATE_INTERVAL) {
            lastDpsUpdateTime = currentTime;

            if (isActive()) {
                long elapsedTime = currentTime - getStartTime();
                float maxHealth = getCustomMaxHealth();

                // 获取范围内玩家
                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                        Player.class,
                        this.getBoundingBox().inflate(displayRange)
                );

                // 计算总DPS（所有玩家）
                double totalDps;
                double totalDamage;

                synchronized (trackerLock) {
                    totalDps = playerDpsTrackers.values().stream()
                            .mapToDouble(DpsTracker::getCurrentDps)
                            .sum();

                    totalDamage = playerDpsTrackers.values().stream()
                            .mapToDouble(DpsTracker::getTotalDamage)
                            .sum();
                }

                // 只在有伤害时显示数据
                if (totalDamage > 0) {
                    // 为每个玩家显示信息
                    for (Player player : nearbyPlayers) {
                        if (player instanceof ServerPlayer serverPlayer) {
                            DpsTracker tracker;
                            synchronized (trackerLock) {
                                tracker = playerDpsTrackers.get(player.getUUID());
                            }

                            // 发送数据包给客户端进行渲染
                            sendDpsDataToClient(serverPlayer, tracker, totalDps, totalDamage, elapsedTime, maxHealth);
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送DPS数据到客户端进行渲染
     */
    private void sendDpsDataToClient(ServerPlayer player, DpsTracker tracker, 
                                    double totalDps, double totalDamage, 
                                    long elapsedTime, float maxHealth) {
        // 这里可以发送自定义数据包给客户端
        // 目前暂时使用客户端消息作为临时方案
        
        if (tracker != null) {
            double dps = tracker.getCurrentDps();
            double playerDamage = tracker.getTotalDamage();
            double percentage = Math.min((playerDamage / maxHealth) * 100, 100);
            double lastHitDamage = tracker.getLastHitDamage();
            double highestHit = tracker.getHighestHit();
            
            // 发送数据给客户端渲染器
            String data = String.format(
                "DPS:%.1f|总伤害:%.1f|时间:%.1f|当前单次:%.1f|百分比:%.1f|最高伤害:%.1f",
                dps, playerDamage, elapsedTime / 1000.0, 
                lastHitDamage, percentage, highestHit
            );
            
            // 使用自定义数据包或临时使用客户端消息
            player.displayClientMessage(Component.literal("[DPS_DATA]" + data), true);
        }
    }

    /**
     * 清理不活跃的追踪器
     */
    private void cleanupInactiveTrackers() {
        long currentTime = System.currentTimeMillis();
        List<UUID> toRemove = new ArrayList<>();

        synchronized (trackerLock) {
            playerDpsTrackers.forEach((uuid, tracker) -> {
                Player player = level().getPlayerByUUID(uuid);

                // 移除离线或距离过远的玩家
                boolean shouldRemove = player == null ||
                        !player.isAlive() ||
                        player.distanceToSqr(this) > (displayRange * displayRange * 4) ||
                        (currentTime - tracker.getLastDamageTime() > 30000); // 30秒无伤害

                if (shouldRemove) {
                    toRemove.add(uuid);
                }
            });

            toRemove.forEach(playerDpsTrackers::remove);
        }
    }

    /**
     * 播放被击效果
     */
    private void playHurtEffect(float damage) {
        Vec3 pos = this.position();

        if (this.level() instanceof ServerLevel serverLevel) {
            // 根据伤害大小调整粒子数量
            int particleCount = Math.min(20, (int) (damage / 5));

            // 普通暴击粒子
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CRIT,
                    pos.x, pos.y + 1.0, pos.z,
                    particleCount,
                    0.3, 0.3, 0.3,
                    0.1
            );
        }
    }

    /**
     * 广播消息给附近玩家
     */
    private void broadcastMessage(String message) {
        if (!this.level().isClientSide()) {
            for (Player player : this.level().getEntitiesOfClass(
                    Player.class,
                    this.getBoundingBox().inflate(displayRange))
            ) {
                player.displayClientMessage(Component.literal(message), false);
            }
        }
    }

    // ============ Getter方法 ============

    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }

    public long getStartTime() {
        return this.entityData.get(DATA_START_TIME);
    }

    public float getCustomMaxHealth() {
        return this.entityData.get(DATA_CUSTOM_MAX_HEALTH);
    }

    public Map<UUID, DpsTracker> getPlayerTrackers() {
        synchronized (trackerLock) {
            return new HashMap<>(playerDpsTrackers);
        }
    }

    // ============ NBT数据持久化 ============

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Active", isActive());
        compound.putLong("StartTime", getStartTime());
        compound.putFloat("CustomMaxHealth", getCustomMaxHealth());
        compound.putBoolean("AutoReset", autoReset);
        compound.putLong("AutoResetTime", autoResetTime);
        compound.putFloat("DisplayRange", displayRange);

        // 保存玩家伤害数据
        CompoundTag trackersTag = new CompoundTag();
        synchronized (trackerLock) {
            playerDpsTrackers.forEach((uuid, tracker) -> {
                trackersTag.put(uuid.toString(), tracker.serialize());
            });
        }
        compound.put("DpsTrackers", trackersTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(DATA_ACTIVE, compound.getBoolean("Active"));
        this.entityData.set(DATA_START_TIME, compound.getLong("StartTime"));

        if (compound.contains("CustomMaxHealth")) {
            setMaxHealth(compound.getFloat("CustomMaxHealth"));
        }

        if (compound.contains("AutoReset")) {
            autoReset = compound.getBoolean("AutoReset");
        }

        if (compound.contains("AutoResetTime")) {
            autoResetTime = compound.getLong("AutoResetTime");
        }

        if (compound.contains("DisplayRange")) {
            displayRange = compound.getFloat("DisplayRange");
        }

        // 加载追踪器数据
        if (compound.contains("DpsTrackers")) {
            CompoundTag trackersTag = compound.getCompound("DpsTrackers");
            synchronized (trackerLock) {
                trackersTag.getAllKeys().forEach(uuidStr -> {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        DpsTracker tracker = DpsTracker.deserialize(trackersTag.getCompound(uuidStr));
                        playerDpsTrackers.put(uuid, tracker);
                    } catch (IllegalArgumentException e) {
                        // 忽略无效UUID
                    }
                });
            }
        }
    }

    // ============ 实体特性重写 ============

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // 禁用坠落伤害检查
    }

    @Override
    public boolean isPushable() {
        return false; // 不可被推动
    }

    @Override
    public boolean isPushedByFluid() {
        return false; // 不会被流体推动
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // 远离玩家时不移除
    }

    // ============ DPS追踪器内部类 ============

    /**
     * DPS追踪器 - 使用循环数组优化性能
     */
    public static class DpsTracker {
        private double totalDamage = 0;
        private double highestHit = 0;
        private double lastHitDamage = 0;
        private long lastDamageTime = 0;

        // 使用简单的时间戳-伤害对列表
        private final List<DamageRecord> damageRecords = new ArrayList<>();

        private static class DamageRecord {
            final long timestamp;
            final double damage;

            DamageRecord(long timestamp, double damage) {
                this.timestamp = timestamp;
                this.damage = damage;
            }
        }

        public void recordDamage(float damage) {
            long currentTime = System.currentTimeMillis();
            double damageValue = damage;

            totalDamage += damageValue;
            lastDamageTime = currentTime;
            lastHitDamage = damageValue;

            if (damageValue > highestHit) {
                highestHit = damageValue;
            }

            // 添加新记录
            damageRecords.add(new DamageRecord(currentTime, damageValue));

            // 清理过期数据
            cleanupOldData(currentTime);
        }

        public double getLastHitDamage() {
            return lastHitDamage;
        }

        public double getCurrentDps() {
            long currentTime = System.currentTimeMillis();
            cleanupOldData(currentTime);

            if (damageRecords.isEmpty()) {
                return 0;
            }

            // 计算最近HISTORY_DURATION内的总伤害
            double recentDamage = 0;
            for (DamageRecord record : damageRecords) {
                recentDamage += record.damage;
            }

            // 计算DPS（每秒伤害）
            return recentDamage / (HISTORY_DURATION / 1000.0);
        }

        public double getTotalDamage() {
            return totalDamage;
        }

        public double getHighestHit() {
            return highestHit;
        }

        public long getLastDamageTime() {
            return lastDamageTime;
        }

        private void cleanupOldData(long currentTime) {
            damageRecords.removeIf(record ->
                    currentTime - record.timestamp > HISTORY_DURATION
            );
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("TotalDamage", totalDamage);
            tag.putDouble("HighestHit", highestHit);
            tag.putLong("LastDamageTime", lastDamageTime);
            return tag;
        }

        public static DpsTracker deserialize(CompoundTag tag) {
            DpsTracker tracker = new DpsTracker();
            tracker.totalDamage = tag.getDouble("TotalDamage");
            tracker.highestHit = tag.getDouble("HighestHit");
            tracker.lastDamageTime = tag.getLong("LastDamageTime");
            return tracker;
        }
    }

    // ============ 工具方法 ============

    /**
     * 获取格式化时间字符串
     */
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds);
        } else {
            return String.format("%.1f秒", milliseconds / 1000.0);
        }
    }

    /**
     * 获取状态信息（用于调试）
     */
    public String getStatusInfo() {
        return String.format(
                "TrainingPuppet{active=%s, time=%s, trackers=%d, maxHealth=%.1f}",
                isActive(),
                formatTime(System.currentTimeMillis() - getStartTime()),
                playerDpsTrackers.size(),
                getCustomMaxHealth()
        );
    }
}