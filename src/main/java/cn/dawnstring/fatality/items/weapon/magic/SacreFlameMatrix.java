package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 圣炎矩阵 - 魔法武器
 * 特性：长按右键攻击时能够释放圣炎矩阵
 * 以玩家为中心，边框为正20面体，对其中的敌人造成伤害。边框由火焰粒子构成，而且20面体会旋转
 * 伤害92 暴击率24 暴击伤害30 浮动0.4 攻击速度0.05s
 */
public class SacreFlameMatrix extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 92.0f; // 基础魔法伤害92
    private static final float MANA_COST_PER_SECOND = 8.0f; // 每秒消耗8点魔法值
    private static final int ATTACK_COOLDOWN_TICKS = 1; // 攻击冷却时间1tick（0.05秒）
    private static final double MATRIX_RADIUS = 5.0; // 矩阵半径5格
    private static final int MATRIX_DURATION_TICKS = 20; // 矩阵持续时间20tick（1秒）
    
    private long lastManaConsumeTime = 0; // 上次消耗魔法值的时间
    private int matrixActiveTicks = 0; // 矩阵激活时间
    private boolean isMatrixActive = false; // 矩阵是否激活

    public SacreFlameMatrix() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
            }

            @Override
            public float getSpeed() {
                return 0; // 挖掘速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 0; // 武器本身没有攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 4; // 材料等级（钻石级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不可附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不可修复
            }
        }, new Properties().stacksTo(1), (int)BASE_MAGIC_DAMAGE, 0.05f, 1.0f, 0.24f, 30.0f, 0.4f, WeaponEnum.MAGIC);
        
        setStory("圣炎矩阵，蕴含着神圣火焰力量的魔法武器。\n" +
                "长按右键释放圣炎矩阵，以玩家为中心形成正20面体边框，\n" +
                "对范围内的敌人造成持续伤害，边框由神圣火焰粒子构成并不断旋转。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (!ManaSystem.safeConsumeMana(player, MANA_COST_PER_SECOND)) {
                player.displayClientMessage(Component.literal("§c魔法值不足！需要" + MANA_COST_PER_SECOND + "点魔法值/秒"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 开始使用物品
            player.startUsingItem(hand);
            isMatrixActive = true;
            matrixActiveTicks = 0;
            lastManaConsumeTime = System.currentTimeMillis();

            // 播放圣炎矩阵启动音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.2f);
        }

        return InteractionResultHolder.success(itemstack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        // 返回一个很大的值，让玩家可以持续按住右键
        return 72000; // 2分钟的最大使用时间
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide() && entity instanceof Player player) {
            long currentTime = System.currentTimeMillis();

            // 如果矩阵激活，处理矩阵效果
            if (isMatrixActive) {
                matrixActiveTicks++;

                // 每秒检查一次魔法值消耗
                if (currentTime - lastManaConsumeTime >= 1000) {
                    lastManaConsumeTime = currentTime;

                    if (!ManaSystem.safeConsumeMana(player, MANA_COST_PER_SECOND)) {
                        stopMatrix(player);
                        player.displayClientMessage(Component.literal("§c魔法值不足！圣炎矩阵已停止"), true);
                        return;
                    }
                }

                // 处理矩阵伤害和粒子效果
                processMatrixEffect(level, player);

                // 检查矩阵持续时间
                if (matrixActiveTicks >= MATRIX_DURATION_TICKS) {
                    // 矩阵持续时间结束，重新激活（保持连续效果）
                    matrixActiveTicks = 0;
                }
            } else if (player.isUsingItem() && player.getUseItem().getItem() == this) {
                // 如果玩家正在使用物品但矩阵未激活，激活矩阵
                isMatrixActive = true;
                matrixActiveTicks = 0;
                lastManaConsumeTime = currentTime;
            }
        }
    }

    /**
     * 处理圣炎矩阵效果（伤害和粒子）
     */
    private void processMatrixEffect(Level level, Player player) {
        // 生成正20面体粒子边框
        spawnIcosahedronParticles(level, player);

        // 对矩阵范围内的敌人造成伤害（每5tick造成一次伤害）
        if (matrixActiveTicks % 5 == 0) {
            dealMatrixDamage(level, player);
        }
    }

    /**
     * 生成正20面体粒子边框
     */
    private void spawnIcosahedronParticles(Level level, Player player) {
        if (level.isClientSide()) return;

        Vec3 playerPos = player.position().add(0, player.getEyeHeight() / 2, 0);
        double radius = MATRIX_RADIUS;
        
        // 计算旋转角度（基于时间）
        double rotationAngle = (matrixActiveTicks * 0.1) % (2 * Math.PI);
        
        // 正20面体的顶点坐标（黄金比例）
        double phi = (1 + Math.sqrt(5)) / 2; // 黄金比例
        
        // 20面体的12个顶点
        Vec3[] vertices = {
            new Vec3(0, 1, phi), new Vec3(0, -1, phi), new Vec3(0, 1, -phi), new Vec3(0, -1, -phi),
            new Vec3(1, phi, 0), new Vec3(-1, phi, 0), new Vec3(1, -phi, 0), new Vec3(-1, -phi, 0),
            new Vec3(phi, 0, 1), new Vec3(phi, 0, -1), new Vec3(-phi, 0, 1), new Vec3(-phi, 0, -1)
        };

        // 生成顶点粒子
        for (Vec3 vertex : vertices) {
            // 归一化并缩放
            Vec3 normalized = vertex.normalize().scale(radius);
            
            // 应用旋转
            Vec3 rotated = rotateY(normalized, rotationAngle);
            
            // 计算世界坐标
            Vec3 worldPos = playerPos.add(rotated);
            
            // 生成火焰粒子
            level.addParticle(ParticleTypes.FLAME,
                    worldPos.x, worldPos.y, worldPos.z,
                    0, 0.05, 0);
            
            // 生成灵魂火焰粒子增强效果
            if (matrixActiveTicks % 3 == 0) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        worldPos.x, worldPos.y, worldPos.z,
                        0, 0.03, 0);
            }
        }

        // 生成边线粒子（连接顶点）
        int[][] edges = {
            {0, 1}, {0, 4}, {0, 8}, {1, 6}, {1, 8}, {4, 5}, {4, 9}, {5, 2}, {5, 10},
            {2, 3}, {2, 9}, {3, 7}, {3, 10}, {6, 7}, {6, 11}, {7, 11}, {8, 11}, {9, 11}, {10, 11}
        };

        for (int[] edge : edges) {
            Vec3 start = vertices[edge[0]].normalize().scale(radius);
            Vec3 end = vertices[edge[1]].normalize().scale(radius);
            
            // 应用旋转
            Vec3 rotatedStart = rotateY(start, rotationAngle);
            Vec3 rotatedEnd = rotateY(end, rotationAngle);
            
            // 在边上生成多个粒子
            int points = 8;
            for (int i = 0; i <= points; i++) {
                double t = (double) i / points;
                Vec3 point = rotatedStart.lerp(rotatedEnd, t);
                Vec3 worldPoint = playerPos.add(point);
                
                level.addParticle(ParticleTypes.FLAME,
                        worldPoint.x, worldPoint.y, worldPoint.z,
                        0, 0.02, 0);
            }
        }
    }

    /**
     * 绕Y轴旋转向量
     */
    private Vec3 rotateY(Vec3 vec, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                vec.x * cos - vec.z * sin,
                vec.y,
                vec.x * sin + vec.z * cos
        );
    }

    /**
     * 对矩阵范围内的敌人造成伤害
     */
    private void dealMatrixDamage(Level level, Player player) {
        Vec3 playerPos = player.position().add(0, player.getEyeHeight() / 2, 0);
        
        // 创建矩阵范围的AABB
        AABB matrixAABB = new AABB(
                playerPos.x - MATRIX_RADIUS, playerPos.y - MATRIX_RADIUS, playerPos.z - MATRIX_RADIUS,
                playerPos.x + MATRIX_RADIUS, playerPos.y + MATRIX_RADIUS, playerPos.z + MATRIX_RADIUS
        );

        // 获取范围内的实体
        List<LivingEntity> entitiesInRange = level.getEntitiesOfClass(LivingEntity.class, matrixAABB);

        // 对范围内的实体造成伤害
        for (LivingEntity entity : entitiesInRange) {
            if (entity != player && entity.isAlive()) {
                // 计算伤害（使用BaseWeapon的伤害计算逻辑）
                float damage = calculateFinalDamage(player, null, null);
                
                // 应用伤害
                entity.hurt(entity.damageSources().playerAttack(player), damage);

                // 生成击中粒子效果
                if (level.isClientSide()) {
                    spawnHitParticles(level, entity);
                }
            }
        }
    }

    /**
     * 计算圣炎矩阵伤害
     */
    private void spawnHitParticles(Level level, LivingEntity target) {
        // 在目标位置生成火焰粒子效果
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 1.0;

            level.addParticle(ParticleTypes.FLAME,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.1, 0);
        }
    }

    /**
     * 停止圣炎矩阵
     */
    private void stopMatrix(Player player) {
        isMatrixActive = false;
        matrixActiveTicks = 0;
        player.stopUsingItem();

        // 播放矩阵停止音效
        if (!player.level().isClientSide()) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8f, 1.0f);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!level.isClientSide() && entity instanceof Player player) {
            stopMatrix(player);
        }
        super.releaseUsing(stack, level, entity, timeCharged);
    }

    /**
     * 获取攻击距离（圣炎矩阵有较大的攻击范围）
     */
    @Override
    public double getAttackRange(Player player) {
        return MATRIX_RADIUS; // 矩阵半径作为攻击距离
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "圣炎矩阵 - 长按右键释放圣炎矩阵，以玩家为中心形成正20面体边框，对范围内的敌人造成持续伤害，边框由神圣火焰粒子构成并不断旋转";
    }
}