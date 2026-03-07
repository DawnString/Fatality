package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.core.particles.ParticleTypes;
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
 * 鞭子武器 - 右键抽打前方扇形范围内的敌人
 */
public class Whip extends BaseWeapon {

    // 鞭子抽打参数
    private static final float WHIP_RANGE = 6.0f; // 抽打范围
    private static final float WHIP_ANGLE = 60.0f; // 扇形角度（度）
    private static final int PARTICLE_COUNT = 30; // 粒子数量
    private static final float BASE_DAMAGE = 4.0f; // 基础伤害
    private static final int COOLDOWN = 10; // 范围伤害倍率

    public Whip() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0;
            }

            @Override
            public float getSpeed() {
                return 0;
            }

            @Override
            public float getAttackDamageBonus() {
                return 3.0f;
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(net.minecraft.world.item.Items.LEATHER); // 修复材料
            }
        }, new Properties(), 0, 0.5f, 1f, 0.07f, 0.03f, 0.4f, WeaponEnum.MELEE);
    }

    /**
     * 重写右键使用方法，实现鞭子抽打效果
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 服务器端：执行抽打逻辑
            performWhipAttack(level, player);

            // 添加冷却时间（秒）
            player.getCooldowns().addCooldown(this, COOLDOWN);
        } else {
            // 客户端：播放音效和生成粒子效果
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 1.0F, 0.8F);

            // 客户端生成鞭子轨迹粒子效果
            Vec3 lookVec = player.getLookAngle();
            Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
            spawnWhipParticles(level, playerPos, lookVec);
        }

        return InteractionResultHolder.success(itemstack);
    }


    /**
     * 执行鞭子抽打攻击
     */
    private void performWhipAttack(Level level, Player player) {
        // 获取玩家视线方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        // 计算扇形区域
        List<LivingEntity> entitiesInRange = findEntitiesInWhipRange(level, player, playerPos, lookVec);

        // 对范围内的实体造成伤害
        for (LivingEntity entity : entitiesInRange) {
            if (entity != player && entity.isAlive()) {
                // 计算伤害（基于武器基础伤害）
                float damage = calculateWhipDamage(player);
                entity.hurt(entity.damageSources().playerAttack(player), damage);

                // 服务器端发送击中粒子效果数据包
                if (!level.isClientSide) {
                    // 这里可以添加网络同步代码来在客户端生成击中粒子
                    spawnHitParticles(level, entity.position());
                }
            }
        }
    }

    /**
     * 查找鞭子扇形范围内的实体
     */
    private List<LivingEntity> findEntitiesInWhipRange(Level level, Player player, Vec3 startPos, Vec3 direction) {
        // 计算扇形区域的边界框
        Vec3 endPos = startPos.add(direction.scale(WHIP_RANGE));
        AABB searchBox = new AABB(
                Math.min(startPos.x, endPos.x) - WHIP_RANGE,
                Math.min(startPos.y, endPos.y) - WHIP_RANGE,
                Math.min(startPos.z, endPos.z) - WHIP_RANGE,
                Math.max(startPos.x, endPos.x) + WHIP_RANGE,
                Math.max(startPos.y, endPos.y) + WHIP_RANGE,
                Math.max(startPos.z, endPos.z) + WHIP_RANGE
        );

        // 获取范围内的所有生物实体
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox);

        // 过滤出在扇形范围内的实体
        return entities.stream()
                .filter(entity -> isInWhipSector(startPos, direction, entity.position(), WHIP_RANGE, WHIP_ANGLE))
                .toList();
    }

    /**
     * 判断实体是否在扇形范围内
     */
    private boolean isInWhipSector(Vec3 center, Vec3 direction, Vec3 targetPos, float range, float angle) {
        Vec3 toTarget = targetPos.subtract(center);
        float distance = (float) toTarget.length();

        // 检查距离
        if (distance > range || distance < 1.0f) {
            return false;
        }

        // 计算角度（使用点积）
        toTarget = toTarget.normalize();
        double dotProduct = direction.dot(toTarget);
        double targetAngle = Math.acos(dotProduct) * (180.0 / Math.PI);

        return targetAngle <= angle / 2.0;
    }

    /**
     * 计算鞭子伤害
     */
    private float calculateWhipDamage(Player player) {
        // 使用基础伤害计算，但应用范围伤害倍率

        // 使用BaseWeapon的伤害计算逻辑，但基于魔法伤害
        float baseDamage = BASE_DAMAGE;

        // 计算基础伤害加成（基于饰品）
        float accessoryBaseBonus = calculateAccessoryBaseBonus(player);

        // 计算其他伤害加成（饰品、药水等）
        float otherBonus = calculateOtherBonus(player);

        // 计算伤害浮动值
        float fluctuation = calculateDamageFluctuation();

        // 判断是否暴击
        boolean isCritical = isCriticalHit(player);

        float finalDamage;
        if (isCritical) {
            // 暴击伤害公式（与BaseWeapon保持一致）
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            // 普通伤害公式（与BaseWeapon保持一致）
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }

        return Math.max(0, finalDamage);
    }

    /**
     * 生成鞭子轨迹粒子效果
     */
    private void spawnWhipParticles(Level level, Vec3 startPos, Vec3 direction) {
        // 计算垂直方向向量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();

        // 鞭子弯曲轨迹参数
        int curveSegments = 25; // 曲线段数
        float maxCurveHeight = 1.5f; // 最大弯曲高度
        float rotationSpeed = 0.8f; // 旋转速度

        // 生成弯曲旋转的鞭子轨迹
        for (int i = 0; i < curveSegments; i++) {
            float progress = (float) i / (curveSegments - 1);

            // 计算当前点在曲线上的位置
            float distance = progress * WHIP_RANGE;

            // 计算弯曲高度（抛物线形状）
            float curveHeight = 4 * maxCurveHeight * progress * (1 - progress);

            // 计算旋转角度（随时间旋转）
            double rotationAngle = rotationSpeed * progress * Math.PI * 2;

            // 计算弯曲偏移（垂直于前进方向）
            Vec3 curveOffset = right.scale(Math.sin(rotationAngle) * curveHeight)
                    .add(up.scale(Math.cos(rotationAngle) * curveHeight * 0.3));

            // 计算粒子位置（沿着方向前进并加上弯曲偏移）
            Vec3 particlePos = startPos.add(direction.scale(distance)).add(curveOffset);

            // 使用小型粒子（CRIT粒子较小且明显）
            level.addParticle(ParticleTypes.CRIT,
                    particlePos.x, particlePos.y, particlePos.z,
                    (Math.random() - 0.5) * 0.05, // 轻微随机速度
                    (Math.random() - 0.5) * 0.05,
                    (Math.random() - 0.5) * 0.05);

            // 添加火焰粒子增强视觉效果
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.FLAME,
                        particlePos.x, particlePos.y, particlePos.z,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1);
            }

            // 在曲线关键点添加更密集的粒子
            if (i % 5 == 0) {
                // 在关键点周围生成小型粒子簇
                for (int j = 0; j < 3; j++) {
                    double clusterOffsetX = (Math.random() - 0.5) * 0.3;
                    double clusterOffsetY = (Math.random() - 0.5) * 0.3;
                    double clusterOffsetZ = (Math.random() - 0.5) * 0.3;

                    level.addParticle(ParticleTypes.ENCHANT,
                            particlePos.x + clusterOffsetX,
                            particlePos.y + clusterOffsetY,
                            particlePos.z + clusterOffsetZ,
                            0, 0.02, 0);
                }
            }
        }

        // 生成鞭子末端的特效粒子
        Vec3 endPos = startPos.add(direction.scale(WHIP_RANGE));
        for (int i = 0; i < 8; i++) {
            double endOffsetX = (Math.random() - 0.5) * 0.8;
            double endOffsetY = (Math.random() - 0.5) * 0.8;
            double endOffsetZ = (Math.random() - 0.5) * 0.8;

            level.addParticle(ParticleTypes.SWEEP_ATTACK,
                    endPos.x + endOffsetX, endPos.y + endOffsetY, endPos.z + endOffsetZ,
                    0, 0.1, 0);
        }
    }

    /**
     * 生成击中实体时的粒子效果
     */
    private void spawnHitParticles(Level level, Vec3 hitPos) {
        // 使用小型粒子创建击中效果
        for (int i = 0; i < 12; i++) {
            // 小型随机偏移
            double offsetX = (Math.random() - 0.5) * 0.6;
            double offsetY = (Math.random() - 0.5) * 0.6;
            double offsetZ = (Math.random() - 0.5) * 0.6;

            // 向外扩散的小速度
            double velocityScale = 0.15;
            double velocityX = offsetX * velocityScale;
            double velocityY = offsetY * velocityScale + 0.08;
            double velocityZ = offsetZ * velocityScale;

            // 使用CRIT粒子（小型且明显）
            level.addParticle(ParticleTypes.CRIT,
                    hitPos.x + offsetX, hitPos.y + offsetY, hitPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);
        }

        // 生成中心冲击波效果（小型粒子）
        for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2 * i) / 6;
            double radius = 0.3;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.ENCHANT,
                    hitPos.x + offsetX, hitPos.y, hitPos.z + offsetZ,
                    offsetX * 0.2, 0.05, offsetZ * 0.2);
        }
    }
}
