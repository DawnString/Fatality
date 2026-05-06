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
 * 丛林鞭 - 右键甩出鞭子，对前方扇形范围内的生物造成伤害
 * 伤害590 暴击率30 暴击伤害40 浮动0.3 攻击速度0.25s
 */
public class JungleWhip extends BaseWeapon {

    // 鞭子抽打参数
    private static final float WHIP_RANGE = 10.0f; // 抽打范围10格
    private static final float WHIP_ANGLE = 90.0f; // 扇形角度90度
    private static final float BASE_DAMAGE = 590.0f; // 基础伤害
    private static final int COOLDOWN = 15; // 冷却时间（秒）

    public JungleWhip() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 1561; // 耐久度
            }

            @Override
            public float getSpeed() {
                return 8.0f; // 挖掘速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 3.0f; // 攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 3; // 工具等级
            }

            @Override
            public int getEnchantmentValue() {
                return 15; // 附魔能力
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(net.minecraft.world.item.Items.VINE); // 修复材料：藤蔓
            }
        }, new Properties(), (int)BASE_DAMAGE, 0.25f, 1f, 0.3f, 4.0f, 0.3f, WeaponEnum.MELEE);
        
        // 设置武器故事
        this.story = "一把来自丛林深处的神秘鞭子，由古老的藤蔓编织而成。" +
                "传说中，丛林守护者使用这把鞭子来保护森林中的生灵。" +
                "右键甩出鞭子，可以对前方扇形范围内的所有敌人造成毁灭性打击。";
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
                    SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 1.2F, 0.6F);

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
        return calculateFinalDamage(player, player.getMainHandItem(), null);
    }

    /**
     * 生成鞭子轨迹粒子效果（丛林主题）
     */
    private void spawnWhipParticles(Level level, Vec3 startPos, Vec3 direction) {
        // 计算垂直方向向量
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();

        // 鞭子弯曲轨迹参数
        int curveSegments = 30; // 曲线段数
        float maxCurveHeight = 2.0f; // 最大弯曲高度
        float rotationSpeed = 1.2f; // 旋转速度

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

            // 使用绿色粒子（丛林主题）
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    particlePos.x, particlePos.y, particlePos.z,
                    (Math.random() - 0.5) * 0.05, // 轻微随机速度
                    (Math.random() - 0.5) * 0.05,
                    (Math.random() - 0.5) * 0.05);

            // 添加树叶粒子增强丛林效果
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.CHERRY_LEAVES,
                        particlePos.x, particlePos.y, particlePos.z,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1);
            }

            // 在曲线关键点添加更密集的粒子
            if (i % 6 == 0) {
                // 在关键点周围生成小型粒子簇
                for (int j = 0; j < 4; j++) {
                    double clusterOffsetX = (Math.random() - 0.5) * 0.4;
                    double clusterOffsetY = (Math.random() - 0.5) * 0.4;
                    double clusterOffsetZ = (Math.random() - 0.5) * 0.4;

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
        for (int i = 0; i < 10; i++) {
            double endOffsetX = (Math.random() - 0.5) * 1.0;
            double endOffsetY = (Math.random() - 0.5) * 1.0;
            double endOffsetZ = (Math.random() - 0.5) * 1.0;

            level.addParticle(ParticleTypes.SWEEP_ATTACK,
                    endPos.x + endOffsetX, endPos.y + endOffsetY, endPos.z + endOffsetZ,
                    0, 0.15, 0);
        }
    }

    /**
     * 生成击中实体时的粒子效果（丛林主题）
     */
    private void spawnHitParticles(Level level, Vec3 hitPos) {
        // 使用绿色粒子创建击中效果
        for (int i = 0; i < 15; i++) {
            // 小型随机偏移
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetY = (Math.random() - 0.5) * 0.8;
            double offsetZ = (Math.random() - 0.5) * 0.8;

            // 向外扩散的小速度
            double velocityScale = 0.2;
            double velocityX = offsetX * velocityScale;
            double velocityY = offsetY * velocityScale + 0.1;
            double velocityZ = offsetZ * velocityScale;

            // 使用绿色粒子（丛林主题）
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    hitPos.x + offsetX, hitPos.y + offsetY, hitPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);
        }

        // 生成中心冲击波效果（树叶粒子）
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double radius = 0.4;

            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.addParticle(ParticleTypes.CHERRY_LEAVES,
                    hitPos.x + offsetX, hitPos.y, hitPos.z + offsetZ,
                    offsetX * 0.3, 0.08, offsetZ * 0.3);
        }
    }
}