package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.StarryJudgmentProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
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
 * 星辰裁决 - 高精度远程狙击枪
 * 特性：极高基础伤害、星光标记效果、星爆AOE伤害
 * 命中目标后留下持续8秒的"星光标记"，同一目标被连续标记3次后触发"星爆"造成小范围AOE伤害
 */
public class StarryJudgment extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_BULLET_DAMAGE = 991.0f; // 基础子弹伤害991

    public StarryJudgment()
    {
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
                return 0; // 材料等级
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 修复材料：下界合金锭
            }
        }, new Properties().stacksTo(1).fireResistant(),
              (int)BASE_BULLET_DAMAGE, 1.0f, 1f, 0.2f, 0.28f, 0.3f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算子弹伤害（使用BaseWeapon的伤害计算逻辑）
            float bulletDamage = calculateBulletDamage(player, itemstack);

            // 创建星辰裁决子弹投射物
            StarryJudgmentProjectile bullet = new StarryJudgmentProjectile(level, player, itemstack, bulletDamage);

            // 设置子弹位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            bullet.shoot(lookVec.x, lookVec.y, lookVec.z, 8.0F, 0.05F); // 8.0速度，极小散布（高精度狙击枪）

            // 添加到世界
            level.addFreshEntity(bullet);

            // 播放狙击枪射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.2F, 0.6F);

            // 生成发射粒子效果
            spawnCastParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 重写暴击特效，添加星辰裁决特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);
    }

    /**
     * 生成施法粒子效果
     */
    private void spawnCastParticles(Level level, Player player) {
        if (level.isClientSide()) {
            Vec3 pos = player.getEyePosition();

            // 生成蓝色星光粒子效果
            for (int i = 0; i < 12; i++) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        pos.x + (Math.random() - 0.5) * 1.0,
                        pos.y + (Math.random() - 0.5) * 1.0,
                        pos.z + (Math.random() - 0.5) * 1.0,
                        0, 0.1, 0);
            }

            // 生成金色闪烁粒子效果
            for (int i = 0; i < 8; i++) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,
                        pos.x + (Math.random() - 0.5) * 0.8,
                        pos.y + (Math.random() - 0.5) * 0.8,
                        pos.z + (Math.random() - 0.5) * 0.8,
                        0, 0.05, 0);
            }
        }
    }

    /**
     * 设置物品故事
     */
    @Override
    public void setStory(String story) {
        this.story = "星辰裁决 - 蕴含星辰之力的狙击枪\n" +
                "特性：极高基础伤害、星光标记效果、星爆AOE伤害\n" +
                "命中目标后留下持续8秒的星光标记，同一目标被连续标记3次后触发星爆，造成小范围AOE伤害\n" +
                "伤害: " + String.format("%.0f", BASE_BULLET_DAMAGE) + " 暴击率: " + String.format("%.1f%%", 0.2f * 100) +
                " 暴击伤害: " + String.format("%.1f", 0.28f) + "倍 浮动: " + String.format("%.1f", 0.3f);
    }
}