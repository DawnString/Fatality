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
 * 指挥官鞭子 - 具有指挥官风格的强力鞭子武器
 * 特点：更大范围、更高伤害、指挥官主题粒子效果
 */
public class CommandersWhip extends BaseWeapon {

    // 指挥官鞭子参数
    private static final float WHIP_RANGE = 7.5f; // 更大的抽打范围
    private static final float WHIP_ANGLE = 75.0f; // 更宽的扇形角度
    private static final int PARTICLE_COUNT = 35; // 更多的粒子数量
    private static final float BASE_DAMAGE = 7.5f; // 更高的基础伤害
    private static final int COOLDOWN = 5; // 冷却时间
    private static final float KNOCKBACK_STRENGTH = 1.2f; // 击退强度

    public CommandersWhip() {
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
                return 0; // 更高的攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 更高的附魔值
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT); // 铁锭修复，指挥官风格
            }
        }, new Properties(), 0, 0.25f, 1f, 0.08f, 0.07f, 0.4f, WeaponEnum.MELEE);
    }

    /**
     * 重写右键使用方法，实现指挥官鞭子抽打效果
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 服务器端：执行抽打逻辑
            performCommandersWhipAttack(level, player);

            // 添加冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN);
        } else {
            // 客户端：播放指挥官风格音效和生成粒子效果
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.7F);

            // 客户端生成指挥官鞭子轨迹粒子效果
            Vec3 lookVec = player.getLookAngle();
            Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
            spawnCommandersWhipParticles(level, playerPos, lookVec);
        }

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 执行指挥官鞭子抽打攻击
     */
    private void performCommandersWhipAttack(Level level, Player player) {
        // 获取玩家视线方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        // 计算扇形区域
        List<LivingEntity> entitiesInRange = findEntitiesInWhipRange(level, player, playerPos, lookVec);

        // 对范围内的实体造成伤害
        for (LivingEntity entity : entitiesInRange) {
            if (entity != player && entity.isAlive()) {
                // 计算伤害（基于武器基础伤害）
                float damage = calculateCommandersWhipDamage(player);
                entity.hurt(entity.damageSources().playerAttack(player), damage);

                // 添加击退效果
                Vec3 knockbackDir = entity.position().subtract(playerPos).normalize();
                entity.setDeltaMovement(knockbackDir.scale(KNOCKBACK_STRENGTH));

                // 服务器端发送击中粒子效果数据包
                if (!level.isClientSide) {
                    spawnCommandersHitParticles(level, entity.position());
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
     * 计算指挥官鞭子伤害
     */
    private float calculateCommandersWhipDamage(Player player) {
        // 使用BaseWeapon的伤害计算逻辑
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
     * 生成指挥官鞭子轨迹粒子效果 - 指挥官主题
     */
    private void spawnCommandersWhipParticles(Level level, Vec3 startPos, Vec3 direction) {
        // 计算垂直方向向量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();

        // 简化版鞭子轨迹参数
        int curveSegments = 15; // 减少曲线段数
        float maxCurveHeight = 1.2f; // 减小弯曲高度
        float rotationSpeed = 0.4f; // 更慢的旋转速度

        // 生成简化的弯曲旋转轨迹
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

            // 使用更简洁的粒子效果（CRIT粒子，不遮挡视线）
            level.addParticle(ParticleTypes.CRIT,
                    particlePos.x, particlePos.y, particlePos.z,
                    (Math.random() - 0.5) * 0.02, // 更小的随机速度
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);

            // 减少额外粒子的频率
            if (i % 4 == 0) {
                level.addParticle(ParticleTypes.ENCHANT,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0.01, 0);
            }
        }

        // 简化末端特效粒子
        Vec3 endPos = startPos.add(direction.scale(WHIP_RANGE));
        for (int i = 0; i < 4; i++) {
            double endOffsetX = (Math.random() - 0.5) * 0.5;
            double endOffsetY = (Math.random() - 0.5) * 0.5;
            double endOffsetZ = (Math.random() - 0.5) * 0.5;

            level.addParticle(ParticleTypes.CRIT,
                    endPos.x + endOffsetX, endPos.y + endOffsetY, endPos.z + endOffsetZ,
                    0, 0.05, 0);
        }
    }

    /**
     * 生成击中实体时的指挥官风格粒子效果
     */
    private void spawnCommandersHitParticles(Level level, Vec3 hitPos) {
        // 使用简洁的粒子创建击中效果
        for (int i = 0; i < 8; i++) {
            // 小型随机偏移
            double offsetX = (Math.random() - 0.5) * 0.4;
            double offsetY = (Math.random() - 0.5) * 0.4;
            double offsetZ = (Math.random() - 0.5) * 0.4;

            // 向外扩散的小速度
            double velocityScale = 0.1;
            double velocityX = offsetX * velocityScale;
            double velocityY = offsetY * velocityScale + 0.05;
            double velocityZ = offsetZ * velocityScale;

            // 使用CRIT粒子（简洁且不遮挡视线）
            level.addParticle(ParticleTypes.CRIT,
                    hitPos.x + offsetX, hitPos.y + offsetY, hitPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);
        }

        // 简化冲击波效果
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI * 2 * i) / 4;
            double radius = 0.2;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.ENCHANT,
                    hitPos.x + offsetX, hitPos.y + 0.2, hitPos.z + offsetZ,
                    offsetX * 0.1, 0.03, offsetZ * 0.1);
        }
    }
}