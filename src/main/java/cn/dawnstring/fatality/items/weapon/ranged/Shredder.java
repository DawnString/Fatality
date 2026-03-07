package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
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
import net.minecraft.world.phys.Vec3;

/**
 * 粉碎者 - 高射速霰弹武器
 * 特性：右键发射6-10发子弹，击中目标获得护甲粉碎(ArmorBreak)效果
 * 伤害89 暴击率18 暴击伤害23 浮动0.3 攻击速度0.1s
 */
public class Shredder extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 2; // 冷却时间2tick（0.1秒）
    private static final float BASE_BULLET_DAMAGE = 89.0f; // 单发子弹基础伤害89
    private static final int MIN_PELLET_COUNT = 6; // 最小弹丸数量
    private static final int MAX_PELLET_COUNT = 10; // 最大弹丸数量
    private static final float MAX_SPREAD_ANGLE = 15.0f; // 最大散射角度（度）
    private static final int ARMOR_BREAK_DURATION = 100; // 护甲粉碎持续时间（5秒，100ticks）

    public Shredder()
    {
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
                return 0;
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
                return Ingredient.of(net.minecraft.world.item.Items.NETHERITE_INGOT); // 修复材料：下界合金锭
            }
        }, new Properties(), 0, 0.1f, 1f, 0.18f, 23.0f, 0.3f, WeaponEnum.RANGED);
        
        setStory("一把高射速的霰弹武器，能够快速发射大量弹丸，命中目标后削弱其护甲防御能力。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算子弹数量（随机6-10发）
            int pelletCount = MIN_PELLET_COUNT + (int)(Math.random() * (MAX_PELLET_COUNT - MIN_PELLET_COUNT + 1));
            
            // 计算单发子弹伤害
            float bulletDamage = calculateBulletDamage(player, itemstack);

            // 发射多颗弹丸
            for (int i = 0; i < pelletCount; i++) {
                // 创建子弹投射物
                BulletProjectile bullet = new BulletProjectile(level, player, itemstack, bulletDamage);

                // 设置子弹位置和方向
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();

                bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                
                // 计算散射角度
                float spreadAngle = calculateSpreadAngle(i, pelletCount);
                Vec3 spreadDirection = applySpread(lookVec, spreadAngle);
                
                bullet.shoot(spreadDirection.x, spreadDirection.y, spreadDirection.z, 4.0F, 0.1F); // 4.0速度，极小散布

                // 添加到世界
                level.addFreshEntity(bullet);
            }

            // 播放射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.2F);

            // 在客户端生成射击粒子效果
            if (level.isClientSide()) {
                spawnShredderParticles(level, player, pelletCount);
            }
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算散射角度
     */
    private float calculateSpreadAngle(int pelletIndex, int totalPellets) {
        // 根据弹丸索引计算散射角度，中间弹丸散射角度小，边缘弹丸散射角度大
        float normalizedIndex = (float)pelletIndex / (totalPellets - 1);
        float angle = (normalizedIndex - 0.5f) * 2.0f; // -1到1的范围
        return angle * MAX_SPREAD_ANGLE;
    }

    /**
     * 应用散射到方向向量
     */
    private Vec3 applySpread(Vec3 direction, float angle) {
        // 将角度转换为弧度
        double angleRad = Math.toRadians(angle);
        
        // 计算随机旋转
        double randomYaw = (Math.random() - 0.5) * angleRad * 0.5;
        double randomPitch = (Math.random() - 0.5) * angleRad * 0.5;
        
        // 应用旋转
        double x = direction.x;
        double y = direction.y;
        double z = direction.z;
        
        // 应用偏航旋转（绕Y轴）
        double newX = x * Math.cos(randomYaw) - z * Math.sin(randomYaw);
        double newZ = x * Math.sin(randomYaw) + z * Math.cos(randomYaw);
        
        // 应用俯仰旋转（绕X轴）
        double newY = y * Math.cos(randomPitch) - newZ * Math.sin(randomPitch);
        newZ = y * Math.sin(randomPitch) + newZ * Math.cos(randomPitch);
        
        return new Vec3(newX, newY, newZ);
    }

    /**
     * 生成粉碎者粒子效果（射击轨迹）
     */
    private void spawnShredderParticles(Level level, Player player, int pelletCount) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 射击起点位置
        Vec3 shootPos = eyePos.add(lookVec.scale(0.5));

        // 为每颗弹丸生成粒子轨迹
        for (int pelletIndex = 0; pelletIndex < pelletCount; pelletIndex++) {
            // 计算散射角度
            float spreadAngle = calculateSpreadAngle(pelletIndex, pelletCount);
            Vec3 spreadDirection = applySpread(lookVec, spreadAngle);

            // 生成弹丸轨迹粒子
            for (int i = 0; i < 6; i++) {
                // 沿着散射方向生成粒子
                double offsetX = (Math.random() - 0.5) * 0.2;
                double offsetY = (Math.random() - 0.5) * 0.2;
                double offsetZ = (Math.random() - 0.5) * 0.2;

                // 粒子速度（沿着散射方向）
                double velocityX = spreadDirection.x * 0.3 + (Math.random() - 0.5) * 0.05;
                double velocityY = spreadDirection.y * 0.3 + (Math.random() - 0.5) * 0.05;
                double velocityZ = spreadDirection.z * 0.3 + (Math.random() - 0.5) * 0.05;

                // 生成火焰粒子（表示霰弹效果）
                level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        shootPos.x + offsetX,
                        shootPos.y + offsetY,
                        shootPos.z + offsetZ,
                        velocityX, velocityY, velocityZ);

                // 生成烟幕粒子（添加射击特效）
                if (i % 2 == 0) {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                            shootPos.x + offsetX * 0.8,
                            shootPos.y + offsetY * 0.8,
                            shootPos.z + offsetZ * 0.8,
                            velocityX * 0.8, velocityY * 0.8, velocityZ * 0.8);
                }
            }
        }
    }

    /**
     * 计算子弹伤害
     */
    private float calculateBulletDamage(Player player, ItemStack itemstack) {
        return calculateFinalDamage(player, itemstack, null);
    }

    /**
     * 重写击中敌人方法，应用护甲粉碎效果
     */
    @Override
    protected void onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage) {
        super.onHitEnemy(player, target, stack, damage);
        
        // 应用护甲粉碎效果（降低目标护甲值）
        if (!target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ARMOR_BREAK_DURATION, 1)); // 虚弱效果，相当于护甲粉碎
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ARMOR_BREAK_DURATION / 2, 0)); // 移动减速效果
        }
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "粉碎者 - 右键快速发射6-10发霰弹，命中目标施加护甲粉碎效果";
    }
}