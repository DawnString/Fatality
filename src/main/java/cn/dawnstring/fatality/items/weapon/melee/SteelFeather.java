package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.SteelFeatherProjectile;
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
import net.minecraft.world.phys.Vec3;

/**
 * 钢羽 - 特殊的近战武器，可以投掷并分裂
 * 特性：中等伤害、高暴击率、快速攻击速度、投掷分裂能力
 */
public class SteelFeather extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 4; // 冷却时间4tick（0.2秒）
    private static final float BASE_FEATHER_DAMAGE = 47.0f; // 基础羽毛伤害47
    private static final float PROJECTILE_SPEED = 3.0f; // 投射物速度
    private static final int SPLIT_DISTANCE = 10; // 分裂距离（格）
    private static final int SPLIT_COUNT = 5; // 分裂数量

    public SteelFeather()
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
                return null;
            }
        }, new Properties(), 47, 0.2f, 1f, 0.12f, 0.15f, 0.4f, WeaponEnum.MELEE);
        
        this.setStory("神秘的钢羽武器，看似轻盈却蕴含着强大的力量。右键投掷后会在飞行过程中分裂成多个羽毛，对敌人造成多重打击。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算钢羽伤害（使用BaseWeapon的伤害计算逻辑）
            float featherDamage = calculateFeatherDamage(player, itemstack);

            // 创建钢羽投射物（使用独立的SteelFeatherProjectile类）
            SteelFeatherProjectile feather = new SteelFeatherProjectile(level, player, itemstack, featherDamage);

            // 设置钢羽位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            feather.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            feather.shoot(lookVec.x, lookVec.y, lookVec.z, PROJECTILE_SPEED, 0.1f); // 轻微散布

            // 添加到世界
            level.addFreshEntity(feather);

            // 播放钢羽投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.2F); // 更高的音调

            // 在客户端生成钢羽粒子效果
            if (level.isClientSide()) {
                spawnFeatherParticles(level, player);
            }
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成钢羽粒子效果（羽毛投掷轨迹）
     */
    private void spawnFeatherParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        // 钢羽投掷起点位置
        Vec3 throwPos = eyePos.add(lookVec.scale(0.5));

        // 生成钢羽轨迹粒子
        for (int i = 0; i < 10; i++) {
            // 沿着投掷方向生成粒子
            double offsetX = (Math.random() - 0.5) * 0.4;
            double offsetY = (Math.random() - 0.5) * 0.4;
            double offsetZ = (Math.random() - 0.5) * 0.4;

            // 粒子速度（沿着投掷方向）
            double velocityX = lookVec.x * 0.4 + (Math.random() - 0.5) * 0.08;
            double velocityY = lookVec.y * 0.4 + (Math.random() - 0.5) * 0.08;
            double velocityZ = lookVec.z * 0.4 + (Math.random() - 0.5) * 0.08;

            // 生成钢羽粒子（使用白色粒子）
            level.addParticle(ParticleTypes.CLOUD,
                    throwPos.x + offsetX,
                    throwPos.y + offsetY,
                    throwPos.z + offsetZ,
                    velocityX, velocityY, velocityZ);

            // 生成金属光泽粒子（添加钢羽特效）
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.GLOW,
                        throwPos.x + offsetX * 0.5,
                        throwPos.y + offsetY * 0.5,
                        throwPos.z + offsetZ * 0.5,
                        velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
            }
        }
    }

    /**
     * 计算钢羽伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateFeatherDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 重写暴击特效，添加钢羽特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的钢羽暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7钢羽暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 暴击时生成额外的钢羽粒子
        if (player.level().isClientSide()) {
            spawnCriticalFeatherParticles(player.level(), target);
        }
    }

    /**
     * 暴击时生成额外的钢羽粒子
     */
    private void spawnCriticalFeatherParticles(Level level, LivingEntity target) {
        // 在目标位置生成暴击粒子效果
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 1.2;
            double offsetY = (Math.random() - 0.5) * 1.2 + 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.2;

            // 生成暴击钢羽粒子
            level.addParticle(ParticleTypes.CLOUD,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.12, 0);

            // 生成金属光泽暴击粒子
            if (i % 4 == 0) {
                level.addParticle(ParticleTypes.GLOW,
                        target.getX() + offsetX * 0.8,
                        target.getY() + offsetY * 0.8,
                        target.getZ() + offsetZ * 0.8,
                        0, 0.1, 0);
            }
        }
    }

    /**
     * 获取攻击距离（钢羽有中等距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 3.0; // 钢羽有3.0格攻击距离
    }
}