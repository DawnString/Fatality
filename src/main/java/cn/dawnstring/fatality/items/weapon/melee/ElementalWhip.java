package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.ElementalBarrageProjectile;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 元素鞭 - 近战武器
 * 特性：右键时甩出鞭子，鞭子路径生成元素弹幕，弹幕能够追踪被鞭子打中的目标
 * 伤害750 暴击率30 暴击伤害40 浮动0.3 攻击速度0.25s
 */
public class ElementalWhip extends BaseWeapon {

    // 元素鞭参数
    private static final float WHIP_RANGE = 7.0f; // 鞭子抽打范围
    private static final float WHIP_ANGLE = 70.0f; // 扇形角度（度）
    private static final int PARTICLE_COUNT = 25; // 粒子数量
    private static final float BASE_DAMAGE = 750.0f; // 基础伤害
    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）
    
    // 弹幕生成参数
    private static final int BARRAGE_COUNT_PER_HIT = 2; // 每个被击中的目标生成2个弹幕
    private static final float BARRAGE_SPAWN_DISTANCE = 1.5f; // 弹幕生成距离（从目标位置偏移）

    public ElementalWhip() {
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
                return 0; // 基础攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 4; // 材料等级（钻石级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties().fireResistant(),
              (int)BASE_DAMAGE, // 基础攻击伤害
              0.25f, // 攻击速度（0.25秒）
              1.0f, // 基础伤害倍率
              0.30f, // 暴击率
              0.40f, // 暴击伤害倍率
              0.3f, // 伤害浮动
              WeaponEnum.MELEE // 武器类型：近战
        );
        
        setStory("蕴含四大元素力量的魔法鞭子，抽打敌人时会在鞭子轨迹上生成元素弹幕。\n" +
                "弹幕会自动追踪被鞭子击中的目标，造成持续的元素伤害。");
    }
    
    /**
     * 右键使用方法 - 甩出元素鞭
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查攻击冷却时间
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }

        // 设置攻击冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        // 执行元素鞭攻击
        if (!level.isClientSide()) {
            performElementalWhipAttack(level, player, itemstack);
        }

        // 播放元素鞭抽打音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 1.0F, 0.6F);

        // 客户端生成鞭子轨迹粒子效果
        if (level.isClientSide()) {
            Vec3 lookVec = player.getLookAngle();
            Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
            spawnElementalWhipParticles(level, playerPos, lookVec);
        }

        return InteractionResultHolder.consume(itemstack);
    }
    
    /**
     * 执行元素鞭攻击
     */
    private void performElementalWhipAttack(Level level, Player player, ItemStack itemstack) {
        // 获取玩家视线方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        // 计算扇形区域内的实体
        List<LivingEntity> hitEntities = findEntitiesInWhipRange(level, player, playerPos, lookVec);
        
        // 存储被击中的目标，用于弹幕追踪
        List<LivingEntity> trackedTargets = new ArrayList<>();

        // 对范围内的实体造成伤害
        for (LivingEntity entity : hitEntities) {
            if (entity != player && entity.isAlive()) {
                // 计算元素鞭伤害
                float damage = calculateElementalWhipDamage(player, itemstack);
                entity.hurt(entity.damageSources().playerAttack(player), damage);
                
                // 添加到追踪目标列表
                trackedTargets.add(entity);
                
                // 服务器端生成击中粒子效果
                spawnHitParticles(level, entity.position());
            }
        }
        
        // 为每个被击中的目标生成元素弹幕
        if (!trackedTargets.isEmpty()) {
            spawnElementalBarrages(level, player, itemstack, trackedTargets);
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
     * 计算元素鞭伤害
     */
    private float calculateElementalWhipDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    /**
     * 生成元素弹幕
     */
    private void spawnElementalBarrages(Level level, Player player, ItemStack itemstack, List<LivingEntity> trackedTargets) {
        // 计算元素弹幕伤害（使用鞭子伤害的70%）
        float barrageDamage = calculateElementalWhipDamage(player, itemstack) * 0.7f;
        
        for (LivingEntity target : trackedTargets) {
            // 为每个目标生成2个弹幕
            for (int i = 0; i < BARRAGE_COUNT_PER_HIT; i++) {
                // 计算弹幕生成位置（目标周围随机位置）
                double offsetX = (level.random.nextDouble() - 0.5) * BARRAGE_SPAWN_DISTANCE;
                double offsetY = (level.random.nextDouble() - 0.5) * BARRAGE_SPAWN_DISTANCE + 1.0;
                double offsetZ = (level.random.nextDouble() - 0.5) * BARRAGE_SPAWN_DISTANCE;
                
                Vec3 spawnPos = target.position().add(offsetX, offsetY, offsetZ);
                
                // 计算初始方向（略微朝向目标）
                Vec3 toTarget = target.position().subtract(spawnPos).normalize();
                Vec3 initialDirection = toTarget.add(
                    (level.random.nextDouble() - 0.5) * 0.2,
                    (level.random.nextDouble() - 0.5) * 0.2,
                    (level.random.nextDouble() - 0.5) * 0.2
                ).normalize();
                
                // 创建元素弹幕
                ElementalBarrageProjectile projectile = new ElementalBarrageProjectile(level, player, barrageDamage);
                projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                projectile.shoot(initialDirection.x, initialDirection.y, initialDirection.z, 1.2F, 0.0F);
                
                // 设置弹幕追踪目标 - 通过反射设置currentTarget字段
                try {
                    java.lang.reflect.Field targetField = ElementalBarrageProjectile.class.getDeclaredField("currentTarget");
                    targetField.setAccessible(true);
                    targetField.set(projectile, target);
                } catch (Exception e) {
                    // 如果反射失败，使用默认追踪逻辑
                    System.err.println("Failed to set target for ElementalBarrageProjectile: " + e.getMessage());
                }
                
                // 添加到世界
                level.addFreshEntity(projectile);
            }
        }
    }
    
    /**
     * 生成元素鞭轨迹粒子效果
     */
    private void spawnElementalWhipParticles(Level level, Vec3 startPos, Vec3 direction) {
        // 计算垂直方向向量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();

        // 鞭子弯曲轨迹参数
        int curveSegments = 20; // 曲线段数
        float maxCurveHeight = 1.2f; // 最大弯曲高度
        float rotationSpeed = 0.6f; // 旋转速度

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

            // 随机选择元素粒子类型
            int elementType = i % 4;
            net.minecraft.core.particles.ParticleOptions particleType;
            
            switch (elementType) {
                case 0:
                    particleType = ParticleTypes.FLAME; // 火元素
                    break;
                case 1:
                    particleType = ParticleTypes.DRIPPING_WATER; // 水元素
                    break;
                case 2:
                    particleType = ParticleTypes.ELECTRIC_SPARK; // 雷元素
                    break;
                case 3:
                    particleType = ParticleTypes.ENCHANT; // 风元素
                    break;
                default:
                    particleType = ParticleTypes.FLAME;
            }
            
            // 生成粒子
            level.addParticle(particleType,
                    particlePos.x, particlePos.y, particlePos.z,
                    0, 0.05, 0);
        }

        // 生成鞭子末端的特效粒子
        Vec3 endPos = startPos.add(direction.scale(WHIP_RANGE));
        for (int i = 0; i < 6; i++) {
            double endOffsetX = (Math.random() - 0.5) * 0.6;
            double endOffsetY = (Math.random() - 0.5) * 0.6;
            double endOffsetZ = (Math.random() - 0.5) * 0.6;

            level.addParticle(ParticleTypes.SWEEP_ATTACK,
                    endPos.x + endOffsetX, endPos.y + endOffsetY, endPos.z + endOffsetZ,
                    0, 0.08, 0);
        }
    }
    
    /**
     * 生成击中实体时的粒子效果
     */
    private void spawnHitParticles(Level level, Vec3 hitPos) {
        // 使用小型粒子创建击中效果
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 0.4;
            double offsetY = (Math.random() - 0.5) * 0.4;
            double offsetZ = (Math.random() - 0.5) * 0.4;

            level.addParticle(ParticleTypes.CRIT,
                    hitPos.x + offsetX, hitPos.y + offsetY, hitPos.z + offsetZ,
                    0, 0.1, 0);
        }
    }
    
    /**
     * 重写暴击特效，添加元素鞭特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, net.minecraft.world.entity.LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的元素鞭暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6元素鞭暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }
    
    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "元素鞭 | 基础伤害: " + String.format("%.0f", BASE_DAMAGE) +
                " | 暴击率: " + String.format("%.1f%%", criticalChance * 100) +
                " | 暴击伤害: " + String.format("%.1f", criticalDamageMultiplier) + "倍" +
                " | 攻击范围: " + WHIP_RANGE + "格扇形区域" +
                " | 弹幕追踪: 每个被击中的目标生成" + BARRAGE_COUNT_PER_HIT + "个追踪弹幕";
    }
}