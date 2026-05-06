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
 * 水晶鞭 - 具有水晶折射效果的鞭子武器
 * 特点：水晶粒子效果、折射伤害、击退效果
 */
public class CrystalWhip extends BaseWeapon {

    // 水晶鞭参数
    private static final float WHIP_RANGE = 6.5f; // 抽打范围
    private static final float WHIP_ANGLE = 65.0f; // 扇形角度
    private static final float BASE_DAMAGE = 10f; // 基础伤害
    private static final int COOLDOWN = 10; // 冷却时间
    private static final float REFLECT_DAMAGE_RATIO = 0.5f; // 折射伤害比例
    private static final float KNOCKBACK_STRENGTH = 1.5f; // 击退强度

    public CrystalWhip() {
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
                return 3.5f; // 攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 20; // 更高附魔值
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(net.minecraft.world.item.Items.AMETHYST_SHARD); // 紫水晶修复
            }
        }, new Properties(), (int)BASE_DAMAGE, 0.5f, 1f, 0.07f, 0.05f, 0.4f, WeaponEnum.MELEE);
    }

    /**
     * 重写右键使用方法，实现水晶鞭抽打效果
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 服务器端：执行抽打逻辑
            performCrystalWhipAttack(level, player);

            // 添加冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN);
        } else {
            // 客户端：播放水晶音效和生成水晶粒子效果
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.1F);

            // 客户端生成水晶鞭轨迹粒子效果
            Vec3 lookVec = player.getLookAngle();
            Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
            spawnCrystalWhipParticles(level, playerPos, lookVec);
        }

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 执行水晶鞭抽打攻击
     */
    private void performCrystalWhipAttack(Level level, Player player) {
        // 获取玩家视线方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        // 计算扇形区域
        List<LivingEntity> entitiesInRange = findEntitiesInWhipRange(level, player, playerPos, lookVec);

        // 对范围内的实体造成伤害并应用水晶效果
        for (LivingEntity entity : entitiesInRange) {
            if (entity != player && entity.isAlive()) {
                // 计算伤害（基于武器基础伤害）
                float damage = calculateCrystalWhipDamage(player);
                entity.hurt(entity.damageSources().playerAttack(player), damage);

                // 应用水晶效果：击退和发光
                Vec3 knockbackDir = entity.position().subtract(playerPos).normalize();
                entity.knockback(KNOCKBACK_STRENGTH, knockbackDir.x, knockbackDir.z);
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0)); // 发光4秒

                // 折射伤害：对周围其他实体造成额外伤害
                applyReflectDamage(level, player, entity, damage);

                // 服务器端发送水晶击中粒子效果
                if (!level.isClientSide) {
                    spawnCrystalHitParticles(level, entity.position());
                }
            }
        }
    }

    /**
     * 应用折射伤害效果
     */
    private void applyReflectDamage(Level level, Player player, LivingEntity primaryTarget, float primaryDamage) {
        // 查找主目标周围的实体
        AABB reflectBox = new AABB(
                primaryTarget.getX() - 3.0, primaryTarget.getY() - 2.0, primaryTarget.getZ() - 3.0,
                primaryTarget.getX() + 3.0, primaryTarget.getY() + 2.0, primaryTarget.getZ() + 3.0
        );

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, reflectBox);

        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (nearbyEntity != player && nearbyEntity != primaryTarget && nearbyEntity.isAlive()) {
                // 计算折射伤害（基于主伤害的比例）
                float reflectDamage = primaryDamage * REFLECT_DAMAGE_RATIO;
                nearbyEntity.hurt(nearbyEntity.damageSources().indirectMagic(player, primaryTarget), reflectDamage);
            }
        }
    }

    /**
     * 计算水晶鞭伤害
     */
    private float calculateCrystalWhipDamage(Player player) {
        return calculateFinalDamage(player, player.getMainHandItem(), null);
    }

    /**
     * 生成水晶鞭轨迹粒子效果 - 水晶主题
     */
    private void spawnCrystalWhipParticles(Level level, Vec3 startPos, Vec3 direction) {
        // 计算垂直方向向量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();

        // 水晶鞭弯曲轨迹参数
        int curveSegments = 28; // 曲线段数
        float maxCurveHeight = 1.8f; // 弯曲高度
        float rotationSpeed = 1.0f; // 旋转速度

        // 生成水晶主题的弯曲旋转轨迹
        for (int i = 0; i < curveSegments; i++) {
            float progress = (float) i / (curveSegments - 1);

            // 计算当前点在曲线上的位置
            float distance = progress * WHIP_RANGE;

            // 计算弯曲高度
            float curveHeight = 4 * maxCurveHeight * progress * (1 - progress);

            // 计算旋转角度
            double rotationAngle = rotationSpeed * progress * Math.PI * 2;

            // 计算弯曲偏移
            Vec3 curveOffset = right.scale(Math.sin(rotationAngle) * curveHeight)
                    .add(up.scale(Math.cos(rotationAngle) * curveHeight * 0.35));

            // 计算粒子位置
            Vec3 particlePos = startPos.add(direction.scale(distance)).add(curveOffset);

            // 使用水晶粒子（CRIT和ENCHANT）
            level.addParticle(ParticleTypes.CRIT,
                    particlePos.x, particlePos.y, particlePos.z,
                    (Math.random() - 0.5) * 0.06,
                    (Math.random() - 0.5) * 0.06,
                    (Math.random() - 0.5) * 0.06);

            // 添加水晶折射效果
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.ENCHANT,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0.04, 0);
            }

            // 添加水晶碎片效果
            if (i % 5 == 0) {
                for (int j = 0; j < 2; j++) {
                    double fragmentOffsetX = (Math.random() - 0.5) * 0.5;
                    double fragmentOffsetY = (Math.random() - 0.5) * 0.5;
                    double fragmentOffsetZ = (Math.random() - 0.5) * 0.5;

                    level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                            particlePos.x + fragmentOffsetX,
                            particlePos.y + fragmentOffsetY,
                            particlePos.z + fragmentOffsetZ,
                            0, 0.02, 0);
                }
            }
        }

        // 生成水晶鞭末端的水晶爆炸效果
        Vec3 endPos = startPos.add(direction.scale(WHIP_RANGE));
        for (int i = 0; i < 10; i++) {
            double angle = (Math.PI * 2 * i) / 10;
            double radius = 0.5;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.ENCHANT,
                    endPos.x + offsetX, endPos.y, endPos.z + offsetZ,
                    offsetX * 0.12, 0.12, offsetZ * 0.12);
        }
    }

    /**
     * 生成水晶击中粒子效果
     */
    private void spawnCrystalHitParticles(Level level, Vec3 hitPos) {
        // 水晶击中效果：水晶碎片飞散
        for (int i = 0; i < 18; i++) {
            double offsetX = (Math.random() - 0.5) * 0.7;
            double offsetY = (Math.random() - 0.5) * 0.7;
            double offsetZ = (Math.random() - 0.5) * 0.7;

            double velocityX = offsetX * 0.2;
            double velocityY = offsetY * 0.2 + 0.12;
            double velocityZ = offsetZ * 0.2;

            // 使用水晶粒子
            level.addParticle(ParticleTypes.ENCHANT,
                    hitPos.x + offsetX, hitPos.y + offsetY, hitPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);
        }

        // 水晶折射效果
        for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2 * i) / 6;
            double radius = 0.5;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    hitPos.x + offsetX, hitPos.y + 0.3, hitPos.z + offsetZ,
                    offsetX * 0.18, 0.06, offsetZ * 0.18);
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