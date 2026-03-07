package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * 灵魂鞭 - 具有灵魂吸取效果的鞭子武器
 * 特点：灵魂粒子效果、生命吸取、减速效果
 */
public class SoulWhip extends BaseWeapon {

    // 灵魂鞭参数
    private static final float WHIP_RANGE = 7.0f; // 抽打范围（比普通鞭子更远）
    private static final float WHIP_ANGLE = 70.0f; // 扇形角度（更宽）
    private static final float BASE_DAMAGE = 7.0f; // 基础伤害（更高）
    private static final int COOLDOWN = 10; // 冷却时间（稍长）
    private static final float LIFE_STEAL_RATIO = 0.1f; // 生命吸取比例

    public SoulWhip() {
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
                return 4.0f; // 攻击伤害加成更高
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 15; // 附魔值更高
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(net.minecraft.world.item.Items.SOUL_SAND); // 灵魂沙修复
            }
        }, new Properties(), 0, 0.6f, 1f, 0.07f, 0.06f, 0.4f, WeaponEnum.MELEE);
    }

    /**
     * 重写右键使用方法，实现灵魂鞭抽打效果
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 服务器端：执行抽打逻辑
            performSoulWhipAttack(level, player);

            // 添加冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN);
        } else {
            // 客户端：播放灵魂音效和生成灵魂粒子效果
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 0.9F);

            // 客户端生成灵魂鞭轨迹粒子效果
            Vec3 lookVec = player.getLookAngle();
            Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
            spawnSoulWhipParticles(level, playerPos, lookVec);
        }

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 执行灵魂鞭抽打攻击
     */
    private void performSoulWhipAttack(Level level, Player player) {
        // 获取玩家视线方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        // 计算扇形区域
        List<LivingEntity> entitiesInRange = findEntitiesInWhipRange(level, player, playerPos, lookVec);

        float totalDamage = 0;

        // 对范围内的实体造成伤害并应用灵魂效果
        for (LivingEntity entity : entitiesInRange) {
            if (entity != player && entity.isAlive()) {
                // 计算伤害（基于武器基础伤害）
                float damage = calculateSoulWhipDamage(player);
                entity.hurt(entity.damageSources().playerAttack(player), damage);
                totalDamage += damage;

                // 应用灵魂效果：减速和虚弱
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)); // 2级减速3秒
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0)); // 虚弱2秒

                // 服务器端发送灵魂击中粒子效果
                if (!level.isClientSide) {
                    spawnSoulHitParticles(level, entity.position());
                }
            }
        }

        // 生命吸取效果：根据总伤害恢复生命值
        if (totalDamage > 0) {
            float healAmount = totalDamage * LIFE_STEAL_RATIO;
            player.heal(healAmount);

            // 显示生命吸取效果
            if (level.isClientSide) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§d灵魂吸取: +" + String.format("%.1f", healAmount) + " 生命值"),
                        true
                );
            }
        }
    }

    /**
     * 计算灵魂鞭伤害
     */
    private float calculateSoulWhipDamage(Player player) {
        // 使用基础伤害计算
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
            // 暴击伤害公式
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.85f * criticalBonus * fluctuation;
        } else {
            // 普通伤害公式
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.95f * fluctuation;
        }

        return Math.max(0, finalDamage);
    }

    /**
     * 生成灵魂鞭轨迹粒子效果 - 灵魂主题
     */
    private void spawnSoulWhipParticles(Level level, Vec3 startPos, Vec3 direction) {
        // 计算垂直方向向量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();

        // 灵魂鞭弯曲轨迹参数
        int curveSegments = 30; // 更多曲线段数
        float maxCurveHeight = 2.0f; // 更大弯曲高度
        float rotationSpeed = 1.2f; // 更快旋转速度

        // 生成灵魂主题的弯曲旋转轨迹
        for (int i = 0; i < curveSegments; i++) {
            float progress = (float) i / (curveSegments - 1);

            // 计算当前点在曲线上的位置
            float distance = progress * WHIP_RANGE;

            // 计算弯曲高度（更平滑的抛物线）
            float curveHeight = 4 * maxCurveHeight * progress * (1 - progress);

            // 计算旋转角度（随时间旋转）
            double rotationAngle = rotationSpeed * progress * Math.PI * 2;

            // 计算弯曲偏移（垂直于前进方向）
            Vec3 curveOffset = right.scale(Math.sin(rotationAngle) * curveHeight)
                    .add(up.scale(Math.cos(rotationAngle) * curveHeight * 0.4));

            // 计算粒子位置
            Vec3 particlePos = startPos.add(direction.scale(distance)).add(curveOffset);

            // 使用灵魂粒子（SOUL_FIRE_FLAME）
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    particlePos.x, particlePos.y, particlePos.z,
                    (Math.random() - 0.5) * 0.08,
                    (Math.random() - 0.5) * 0.08,
                    (Math.random() - 0.5) * 0.08);

            // 添加灵魂粒子簇
            if (i % 4 == 0) {
                for (int j = 0; j < 2; j++) {
                    double clusterOffsetX = (Math.random() - 0.5) * 0.4;
                    double clusterOffsetY = (Math.random() - 0.5) * 0.4;
                    double clusterOffsetZ = (Math.random() - 0.5) * 0.4;

                    level.addParticle(ParticleTypes.SOUL,
                            particlePos.x + clusterOffsetX,
                            particlePos.y + clusterOffsetY,
                            particlePos.z + clusterOffsetZ,
                            0, 0.03, 0);
                }
            }

            // 添加幽灵火焰效果
            if (i % 6 == 0) {
                level.addParticle(ParticleTypes.SOUL,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0.05, 0);
            }
        }

        // 生成灵魂鞭末端的灵魂漩涡效果
        Vec3 endPos = startPos.add(direction.scale(WHIP_RANGE));
        for (int i = 0; i < 12; i++) {
            double angle = (Math.PI * 2 * i) / 12;
            double radius = 0.6;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    endPos.x + offsetX, endPos.y, endPos.z + offsetZ,
                    offsetX * 0.1, 0.15, offsetZ * 0.1);
        }
    }

    /**
     * 生成灵魂击中粒子效果
     */
    private void spawnSoulHitParticles(Level level, Vec3 hitPos) {
        // 灵魂击中效果：灵魂飞散
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetY = (Math.random() - 0.5) * 0.8;
            double offsetZ = (Math.random() - 0.5) * 0.8;

            double velocityX = offsetX * 0.25;
            double velocityY = offsetY * 0.25 + 0.1;
            double velocityZ = offsetZ * 0.25;

            // 使用灵魂火焰粒子
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    hitPos.x + offsetX, hitPos.y + offsetY, hitPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);
        }

        // 灵魂漩涡效果
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double radius = 0.4;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.SOUL,
                    hitPos.x + offsetX, hitPos.y + 0.5, hitPos.z + offsetZ,
                    offsetX * 0.15, 0.08, offsetZ * 0.15);
        }
    }

    /**
     * 查找鞭子扇形范围内的实体（复用Whip类的方法）
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
     * 判断实体是否在扇形范围内（复用Whip类的方法）
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
}