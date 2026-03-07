package cn.dawnstring.fatality.items;

import cn.dawnstring.fatality.items.AccessoryItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 基础盾牌类，提供冲刺功能的通用实现
 * 子类可以通过重写方法来定制冲刺行为
 */
@Mod.EventBusSubscriber
public abstract class BaseShield extends AccessoryItem {

    // 冲刺状态跟踪
    protected static final Map<UUID, DashData> dashDataMap = new HashMap<>();

    // 双击检测跟踪
    protected static final Map<UUID, KeyTapData> keyTapDataMap = new HashMap<>();

    // 冲刺参数 - 子类可以重写这些方法来自定义参数
    protected float getDashSpeed() {
        return 3.0f;
    }

    protected float getDashDuration() {
        return 1.0f;
    }

    protected float getDashCooldown() {
        return 2.0f;
    }

    protected float getDashDamage() {
        return 20.0f;
    }

    public BaseShield(Properties properties) {
        super(properties);
    }

    @Override
    public void applyEffects(Player player, ItemStack stack) {
        super.applyEffects(player, stack);

        // 初始化冲刺数据
        UUID playerId = player.getUUID();
        if (!dashDataMap.containsKey(playerId)) {
            dashDataMap.put(playerId, new DashData(getDashDuration(), getDashCooldown(), getDashSpeed()));
        }
        if (!keyTapDataMap.containsKey(playerId)) {
            keyTapDataMap.put(playerId, new KeyTapData());
        }
    }

    @Override
    public void removeEffects(Player player, ItemStack stack) {
        super.removeEffects(player, stack);

        // 移除冲刺数据
        UUID playerId = player.getUUID();
        dashDataMap.remove(playerId);
        keyTapDataMap.remove(playerId);
    }

    // 处理玩家受伤事件 - 实现冲刺期间免疫伤害
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否装备了盾牌且正在冲刺
            if (hasAnyShieldEquipped(player) && isDashing(player)) {
                // 如果玩家正在冲刺，则取消本次伤害
                event.setCanceled(true);
                System.out.println("伤害免疫：玩家正在冲刺");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            // 检查玩家是否装备了盾牌
            boolean hasShield = hasAnyShieldEquipped(player);

            if (!hasShield) {
                // 如果没有装备盾牌，确保移除数据并返回
                dashDataMap.remove(playerId);
                keyTapDataMap.remove(playerId);
                return;
            }

            // 获取冲刺数据
            DashData dashData = dashDataMap.get(playerId);
            if (dashData == null) {
                // 获取玩家装备的盾牌实例，并从中获取冲刺参数
                BaseShield equippedShield = getEquippedShield(player);
                if (equippedShield != null) {
                    // 使用盾牌实例的参数创建冲刺数据
                    dashData = new DashData(equippedShield.getDashDuration(),
                            equippedShield.getDashCooldown(),
                            equippedShield.getDashSpeed());
                } else {
                    // 如果没有装备盾牌，使用默认参数
                    dashData = new DashData(1.0f, 2.0f, 3.0f);
                }
                dashDataMap.put(playerId, dashData);
            }

            // 更新冷却时间
            if (!player.level().isClientSide && dashData.cooldownTimer > 0) {
                float oldCooldown = dashData.cooldownTimer;
                updateCooldown(player, dashData);
                if (oldCooldown > 0 && dashData.cooldownTimer <= 0) {
                    System.out.println("SERVER: 冲刺冷却完成，玩家: " + player.getName().getString());
                }
            }

            // 在服务器端处理双击检测
            if (!player.level().isClientSide) {
                handleDoubleTapDetection(player, dashData);
            }

            // 处理冲刺逻辑
            if (dashData.isDashing) {
                handleDashMovement(player, dashData);
            }
        }
    }

    // 处理双击请求
    private static void handleDoubleTapRequest(Player player, DashData dashData, KeyTapData keyTapData) {
        // 检查冷却时间和冲刺状态
        if (dashData.cooldownTimer > 0) {
            System.out.println("SERVER: 冲刺冷却中，剩余: " + dashData.cooldownTimer + "秒");
            return;
        }

        if (dashData.isDashing) {
            System.out.println("SERVER: 已经在冲刺中");
            return;
        }

        // 使用存储的按键信息计算冲刺方向
        Vec3 dashDirection = calculateDashDirectionFromKey(keyTapData.lastKey, player);

        // 开始冲刺
        startDash(player, dashData, dashDirection);
    }

    // 在服务器端处理双击检测（只处理客户端发送的请求）
    private static void handleDoubleTapDetection(Player player, DashData dashData) {
        // 获取按键检测数据
        KeyTapData keyTapData = keyTapDataMap.get(player.getUUID());
        if (keyTapData == null) {
            keyTapData = new KeyTapData();
            keyTapDataMap.put(player.getUUID(), keyTapData);
        }

        // 检查是否有待处理的双击请求
        if (keyTapData.pendingDoubleTap) {
            System.out.println("SERVER: 收到双击请求，按键: " + keyTapData.lastKey +
                    " 玩家: " + player.getName().getString());

            // 处理双击请求
            handleDoubleTapRequest(player, dashData, keyTapData);
            keyTapData.pendingDoubleTap = false;
        }
    }
    
    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEventHandler {

        private static final Minecraft MINECRAFT = Minecraft.getInstance();

        // 按键状态跟踪
        private static final Map<Integer, Boolean> keyHeldState = new HashMap<>();

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Player player = MINECRAFT.player;
            if (player == null) return;

            // 检查玩家是否装备了盾牌
            if (!hasAnyShieldEquipped(player)) return;

            // 获取或创建按键检测数据
            KeyTapData keyTapData = getOrCreateKeyTapData(player.getUUID());

            // 检测WASD按键
            int key = event.getKey();
            boolean isDirectionKey = isDirectionKey(key);

            if (isDirectionKey) {
                handleKeyEvent(player, key, event.getAction(), keyTapData);
            }
        }

        private static KeyTapData getOrCreateKeyTapData(UUID playerId) {
            KeyTapData keyTapData = keyTapDataMap.get(playerId);
            if (keyTapData == null) {
                keyTapData = new KeyTapData();
                keyTapDataMap.put(playerId, keyTapData);
            }
            return keyTapData;
        }

        private static void handleKeyEvent(Player player, int key, int action, KeyTapData keyTapData) {
            long currentTime = System.currentTimeMillis();

            if (action == InputConstants.PRESS) {
                // 检查这个键是否已经被按住（防止重复触发）
                if (Boolean.TRUE.equals(keyHeldState.get(key))) {
                    return; // 键已经被按住，忽略这次按下
                }

                // 标记键为按下状态
                keyHeldState.put(key, true);

                // 检查冷却时间
                if (currentTime - keyTapData.lastDoubleTapTime < KeyTapData.DOUBLE_TAP_COOLDOWN) {
                    long remaining = KeyTapData.DOUBLE_TAP_COOLDOWN - (currentTime - keyTapData.lastDoubleTapTime);
                    System.out.println("冷却中，剩余: " + remaining + "ms，忽略按键");
                    return;
                }

                // 处理按键按下
                handleKeyPress(player, key, keyTapData, currentTime);

            } else if (action == InputConstants.RELEASE) {
                // 标记键为释放状态
                keyHeldState.put(key, false);

                // 检查是否持续按住（超过阈值）
                if (keyTapData.tapCount == 1) {
                    long holdDuration = currentTime - keyTapData.firstTapTime;
                    if (holdDuration > KeyTapData.HOLD_THRESHOLD) {
                        // 持续按住，重置双击检测
                        System.out.println("按键持续按住 " + holdDuration + "ms > " +
                                KeyTapData.HOLD_THRESHOLD + "ms，重置检测");
                        keyTapData.resetDoubleTapState();
                    }
                }
            }
        }

        private static void handleKeyPress(Player player, int key, KeyTapData keyTapData, long currentTime) {
            System.out.println("按键按下: " + getKeyName(key) + " 时间: " + (currentTime % 10000) + "ms");

            // 检查是否按下了不同的方向键
            if (keyTapData.lastKey != -1 && keyTapData.lastKey != key) {
                System.out.println("方向键改变: " + getKeyName(keyTapData.lastKey) + " -> " +
                        getKeyName(key) + "，重置检测");
                keyTapData.resetDoubleTapState();
                keyTapData.lastKey = key;
                keyTapData.firstTapTime = currentTime;
                keyTapData.tapCount = 1;
                return;
            }

            // 同一按键的处理
            keyTapData.lastKey = key;

            if (keyTapData.tapCount == 0) {
                // 第一次按键
                keyTapData.firstTapTime = currentTime;
                keyTapData.tapCount = 1;
                System.out.println("第一次按键，开始检测双击");
            }
            else if (keyTapData.tapCount == 1) {
                // 第二次按键 - 检查时间间隔
                long timeBetweenTaps = currentTime - keyTapData.firstTapTime;

                System.out.println("第二次按键，间隔: " + timeBetweenTaps + "ms (窗口: " +
                        KeyTapData.DOUBLE_TAP_WINDOW + "ms)");

                if (timeBetweenTaps <= KeyTapData.DOUBLE_TAP_WINDOW) {
                    // 在时间窗口内，认为是双击
                    keyTapData.tapCount = 2;
                    keyTapData.secondTapTime = currentTime;

                    // 立即触发双击（不等待释放）
                    triggerDoubleTap(player, key, keyTapData, currentTime);
                } else {
                    // 超时，重新开始
                    System.out.println("超时，重新开始检测");
                    keyTapData.firstTapTime = currentTime;
                    keyTapData.tapCount = 1;
                }
            }
            else {
                // 第三次或更多按键，重置
                System.out.println("超过两次按键，重置检测");
                keyTapData.resetDoubleTapState();
                keyTapData.firstTapTime = currentTime;
                keyTapData.tapCount = 1;
            }
        }

        private static void triggerDoubleTap(Player player, int key, KeyTapData keyTapData, long currentTime) {
            System.out.println(">>> 触发双击! 按键: " + getKeyName(key));

            keyTapData.doubleTapTriggered = true;
            keyTapData.lastDoubleTapTime = currentTime;

            // 在客户端显示预览效果
            DashData dashData = dashDataMap.get(player.getUUID());
            if (dashData != null && !dashData.isDashing && dashData.cooldownTimer <= 0) {
                // 确定冲刺方向
                Vec3 dashDirection = calculateDashDirectionFromKey(key, player);

                // 在客户端显示预览效果
                if (player.level().isClientSide()) {
                    spawnPreviewParticles(player, dashDirection);
                }

                // 设置标记让服务器端处理双击检测
                keyTapData.pendingDoubleTap = true;

                System.out.println(">>> 双击已发送到服务器，方向: " + dashDirection);
            } else {
                if (dashData == null) {
                    System.out.println(">>> 错误: dashData为null");
                } else if (dashData.isDashing) {
                    System.out.println(">>> 错误: 已经在冲刺中");
                } else if (dashData.cooldownTimer > 0) {
                    System.out.println(">>> 错误: 冷却中，剩余: " + dashData.cooldownTimer + "秒");
                }
            }

            // 重置检测状态
            keyTapData.resetDoubleTapState();
        }

        // 检查是否是方向键
        private static boolean isDirectionKey(int key) {
            return key == InputConstants.KEY_W || key == InputConstants.KEY_A ||
                    key == InputConstants.KEY_S || key == InputConstants.KEY_D;
        }

        private static String getKeyName(int key) {
            switch (key) {
                case InputConstants.KEY_W: return "W";
                case InputConstants.KEY_A: return "A";
                case InputConstants.KEY_S: return "S";
                case InputConstants.KEY_D: return "D";
                default: return "UNKNOWN";
            }
        }

        // 根据按键计算冲刺方向
        private static Vec3 calculateDashDirectionFromKey(int key, Player player) {
            return BaseShield.calculateDashDirectionFromKey(key, player);
        }

        // 生成预览粒子效果
        private static void spawnPreviewParticles(Player player, Vec3 dashDirection) {
            if (player.level().isClientSide()) {
                for (int i = 0; i < 6; i++) {
                    double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.8;
                    double offsetY = player.getRandom().nextDouble() * 0.5;
                    double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.8;

                    player.level().addParticle(ParticleTypes.GLOW,
                            player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                            dashDirection.x * 0.2, 0.1, dashDirection.z * 0.2);
                }
            }
        }
    }

    // 开始冲刺
    private static void startDash(Player player, DashData dashData, Vec3 direction) {
        // 确保只在服务器端执行冲刺逻辑
        if (player.level().isClientSide) return;

        System.out.println("SERVER: 开始冲刺 - 玩家: " + player.getName().getString() +
                " 方向: " + direction);

        dashData.isDashing = true;
        dashData.dashTimer = dashData.dashDuration;
        dashData.dashDirection = direction;

        // 应用初始冲刺速度
        Vec3 dashVelocity = direction.scale(dashData.dashSpeed);
        player.setDeltaMovement(dashVelocity);

        // 立即设置冷却时间
        dashData.cooldownTimer = dashData.dashCooldown;

        // 播放冲刺音效和视觉效果
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);

        // 生成冲刺粒子效果
        BaseShield equippedShield = getEquippedShield(player);
        if (equippedShield != null) {
            equippedShield.spawnDashParticles(player);
        }
    }

    // 处理冲刺移动
    private static void handleDashMovement(Player player, DashData dashData) {
        if (dashData.isDashing) {
            // 减少冲刺计时器
            dashData.dashTimer -= 0.05f; // 每tick减少0.05秒

            // 检查玩家生命值是否有效，避免NaN值
            if (!isValidHealth(player)) {
                // 如果生命值无效，结束冲刺并重置状态
                endDash(player, dashData);
                return;
            }

            // 检查碰撞
            checkCollision(player, dashData);

            if (dashData.dashTimer <= 0) {
                // 冲刺结束
                endDash(player, dashData);
            } else {
                // 保持冲刺速度，使用更平滑的插值
                Vec3 currentMotion = player.getDeltaMovement();
                Vec3 targetMotion = dashData.dashDirection.scale(dashData.dashSpeed);

                // 根据冲刺剩余时间调整插值因子，实现更自然的减速
                double lerpFactor = 0.5 * (dashData.dashTimer / dashData.dashDuration);
                lerpFactor = Math.max(lerpFactor, 0.1); // 最小插值因子

                Vec3 newMotion = currentMotion.lerp(targetMotion, lerpFactor);
                player.setDeltaMovement(newMotion);

                // 在冲刺过程中持续生成粒子效果（每2tick生成一次，让轨迹更连续）
                if (player.tickCount % 2 == 0) {
                    BaseShield equippedShield = getEquippedShield(player);
                    if (equippedShield != null) {
                        equippedShield.spawnDashParticles(player);
                    }
                }
            }
        }
    }

    // 检查玩家生命值是否有效
    private static boolean isValidHealth(Player player) {
        float health = player.getHealth();
        return Float.isFinite(health) && health > 0;
    }

    // 检查碰撞并造成伤害
    private static void checkCollision(Player player, DashData dashData) {
        // 只在服务器端处理碰撞
        if (player.level().isClientSide) return;

        // 检查玩家生命值是否有效
        if (!isValidHealth(player)) {
            endDash(player, dashData);
            return;
        }

        // 计算玩家前方的碰撞区域（扩大检测范围）
        Vec3 playerPos = player.position();
        Vec3 lookDirection = dashData.dashDirection;
        AABB playerBoundingBox = player.getBoundingBox();

        // 扩大碰撞检测区域：向前延伸2.5格，横向扩展1格，高度扩展1格
        AABB collisionBox = playerBoundingBox
                .expandTowards(lookDirection.scale(2.5))
                .inflate(1.0, 0.5, 1.0);

        // 获取碰撞区域内的实体
        List<Entity> entities = player.level().getEntities(player, collisionBox);

        boolean hitEntity = false;
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && entity != player) {
                // 对生物造成伤害 - 使用默认伤害值20.0f
                livingEntity.hurt(player.damageSources().playerAttack(player), 20.0f);

                // 击退效果
                Vec3 knockbackDirection = lookDirection.normalize().scale(1.5);
                livingEntity.setDeltaMovement(knockbackDirection.x, 0.5, knockbackDirection.z);

                // 生成碰撞粒子效果
                spawnCollisionParticles(livingEntity);
                hitEntity = true;
            }
        }

        // 如果击中了实体，则停止冲刺
        if (hitEntity) {
            // 停止冲刺
            endDash(player, dashData);
            return;
        }

        // 检查方块碰撞
        if (player.horizontalCollision) {
            // 碰到方块后停止冲刺
            endDash(player, dashData);
        }
    }

    // 生成冲刺粒子效果 - 长条形轨迹效果
    protected void spawnDashParticles(Player player) {
        if (player.level().isClientSide()) {
            Level level = player.level();
            Vec3 pos = player.position();

            // 获取玩家的移动方向
            Vec3 motion = player.getDeltaMovement();
            if (motion.length() < 0.1) return; // 如果玩家基本静止，不生成粒子

            Vec3 direction = motion.normalize();

            // 在冲刺路径上生成长条形粒子轨迹
            // 从玩家当前位置向后延伸2格，向前延伸4格
            double trailLength = 6.0; // 轨迹总长度
            double startOffset = -2.0; // 从玩家身后开始

            // 生成轨迹粒子
            for (int i = 0; i < 20; i++) { // 轨迹上的粒子数量
                // 计算轨迹上的位置
                double progress = (double)i / 19; // 0到1的进度
                double distance = startOffset + progress * trailLength;

                Vec3 trailPos = pos.add(direction.scale(distance));

                // 在轨迹位置周围生成粒子群
                for (int j = 0; j < 3; j++) { // 每个位置生成3个粒子
                    // 横向和纵向的随机偏移
                    double horizontalOffset = (Math.random() - 0.5) * 0.4;
                    double verticalOffset = (Math.random() - 0.5) * 0.6;

                    // 计算垂直于冲刺方向的偏移向量
                    Vec3 perpendicular = new Vec3(-direction.z, 0, direction.x).normalize();
                    Vec3 offset = perpendicular.scale(horizontalOffset).add(0, verticalOffset, 0);

                    Vec3 particlePos = trailPos.add(offset);

                    // 使用GLOW粒子形成长条形轨迹
                    level.addParticle(ParticleTypes.GLOW,
                            particlePos.x, particlePos.y, particlePos.z,
                            0, 0, 0); // 静止的轨迹粒子
                }
            }

            // 在轨迹末端生成更密集的粒子效果
            Vec3 endPos = pos.add(direction.scale(4.0));
            for (int i = 0; i < 8; i++) {
                double offsetX = (Math.random() - 0.5) * 0.8;
                double offsetY = (Math.random() - 0.5) * 0.8;
                double offsetZ = (Math.random() - 0.5) * 0.8;

                level.addParticle(ParticleTypes.WHITE_ASH,
                        endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                        0, 0, 0);
            }

            // 在轨迹上生成一些流动的粒子
            for (int i = 0; i < 5; i++) {
                double flowDistance = Math.random() * trailLength + startOffset;
                Vec3 flowPos = pos.add(direction.scale(flowDistance));

                // 添加一些随机偏移
                double offsetX = (Math.random() - 0.5) * 0.3;
                double offsetY = (Math.random() - 0.5) * 0.3;
                double offsetZ = (Math.random() - 0.5) * 0.3;

                // 流动粒子有轻微的速度
                level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        flowPos.x + offsetX, flowPos.y + offsetY, flowPos.z + offsetZ,
                        direction.x * 0.1, direction.y * 0.1, direction.z * 0.1);
            }
        }
    }

    // 生成冲刺粒子效果结束冲刺
    private static void endDash(Player player, DashData dashData) {
        if (!dashData.isDashing) return; // 防止重复调用

        System.out.println("SERVER: 冲刺结束 - 玩家: " + player.getName().getString());

        dashData.isDashing = false;
        dashData.dashTimer = 0;

        // 应用平滑减速效果
        Vec3 currentMotion = player.getDeltaMovement();
        Vec3 slowedMotion = currentMotion.scale(0.6); // 减速到60%，更平滑的过渡
        player.setDeltaMovement(slowedMotion);

        // 播放冲刺结束音效
        if (!player.level().isClientSide) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 0.5f, 1.2f);
        }
    }

    // 生成碰撞粒子效果
    private static void spawnCollisionParticles(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            for (int i = 0; i < 8; i++) {
                double offsetX = (entity.getRandom().nextDouble() - 0.5) * 1.2;
                double offsetY = entity.getRandom().nextDouble() * 1.5;
                double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 1.2;

                entity.level().addParticle(ParticleTypes.CRIT,
                        entity.getX() + offsetX, entity.getY() + offsetY, entity.getZ() + offsetZ,
                        0, 0.1, 0);
            }
        }
    }

    // 更新冷却时间
    private static void updateCooldown(Player player, DashData dashData) {
        // 确保只在服务器端更新冷却时间
        if (player.level().isClientSide) return;

        if (dashData.cooldownTimer > 0) {
            dashData.cooldownTimer -= 0.05f; // 每tick减少0.05秒（20tick=1秒）
            if (dashData.cooldownTimer < 0) {
                dashData.cooldownTimer = 0;
            }
        }
    }

    // 检查玩家是否装备了盾牌（抽象方法，子类必须实现）
    protected abstract boolean hasShieldEquipped(Player player);

    // 静态辅助方法：检查玩家是否装备了任何BaseShield子类的盾牌
    protected static boolean hasAnyShieldEquipped(Player player) {
        return getEquippedShield(player) != null;
    }

    // 静态辅助方法：获取玩家装备的BaseShield子类盾牌实例
    protected static BaseShield getEquippedShield(Player player) {
        // 检查饰品栏
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (accessory.getItem() instanceof BaseShield shield) {
                    return shield;
                }
            }
        }
        return null;
    }

    // 获取冲刺冷却时间（用于UI显示）
    public static float getDashCooldown(Player player) {
        DashData data = dashDataMap.get(player.getUUID());
        return data != null ? data.cooldownTimer : 0;
    }

    // 获取冲刺状态（用于UI显示）
    public static boolean isDashing(Player player) {
        DashData data = dashDataMap.get(player.getUUID());
        return data != null && data.isDashing;
    }

    // 根据按键计算冲刺方向（公共方法）
    private static Vec3 calculateDashDirectionFromKey(int key, Player player) {
        float yaw = player.getYRot();

        // 计算冲刺方向
        switch (key) {
            case InputConstants.KEY_W: // 向前
                return new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
            case InputConstants.KEY_S: // 向后
                return new Vec3(Math.sin(Math.toRadians(yaw)), 0, -Math.cos(Math.toRadians(yaw)));
            case InputConstants.KEY_A: // 向左
                return new Vec3(-Math.sin(Math.toRadians(yaw - 90)), 0, Math.cos(Math.toRadians(yaw - 90)));
            case InputConstants.KEY_D: // 向右
                return new Vec3(-Math.sin(Math.toRadians(yaw + 90)), 0, Math.cos(Math.toRadians(yaw + 90)));
            default:
                return new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))); // 默认向前
        }
    }

    public static class KeyTapData {
        public int lastKey = -1;           // 最后按下的按键
        public long lastDoubleTapTime = 0; // 最后一次双击时间
        public boolean doubleTapTriggered = false; // 双击已触发标记
        public boolean pendingDoubleTap = false;  // 待处理的双击请求

        // 双击检测参数 - 调整参数使其更容易触发
        public static final int DOUBLE_TAP_WINDOW = 350;  // 双击时间窗口：350毫秒
        public static final int HOLD_THRESHOLD = 150;     // 持续按住阈值：150毫秒
        public static final int DOUBLE_TAP_COOLDOWN = 500; // 双击冷却：500毫秒

        // 简化的时间跟踪
        public long firstTapTime = 0;
        public long secondTapTime = 0;
        public int tapCount = 0;

        // 重置双击状态
        public void resetDoubleTapState() {
            tapCount = 0;
            doubleTapTriggered = false;
            firstTapTime = 0;
            secondTapTime = 0;
            // 注意：不重置lastKey，以保持方向一致性
        }
    }

    // 冲刺数据内部类
    public static class DashData {
        public final float dashDuration;
        public final float dashCooldown;
        public final float dashSpeed;

        public boolean isDashing = false;
        public float dashTimer = 0;
        public float cooldownTimer = 0;
        public Vec3 dashDirection = Vec3.ZERO;
        public boolean wasInvulnerable = false;

        public DashData(float dashDuration, float dashCooldown, float dashSpeed) {
            this.dashDuration = dashDuration;
            this.dashCooldown = dashCooldown;
            this.dashSpeed = dashSpeed;
        }
    }
}