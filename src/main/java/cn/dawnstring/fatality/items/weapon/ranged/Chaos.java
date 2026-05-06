package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.ChaosBulletProjectile;
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
 * 祸乱 - 高伤害远程狙击枪
 * 特性：右键发射子弹，击中目标后，目标四周出现4-8红色弹幕，弹幕会攻击最近的生物（除玩家外），伤害为基础伤害的0.5倍
 * 伤害2820 暴击率26 暴击伤害38 浮动0.1 攻击速度1s
 */
public class Chaos extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_BULLET_DAMAGE = 2820.0f; // 基础子弹伤害2820
    
    public Chaos() {
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
        }, new Properties().fireResistant(), 2820, 1.0f, 1f, 0.26f, 1.38f, 0.1f, WeaponEnum.RANGED);
        
        setStory("祸乱，一把能够引发混乱的狙击枪。\n" +
                "射出的子弹在击中目标后会分裂成4-8个红色弹幕，\n" +
                "这些弹幕会自动追踪并攻击周围的其他生物，造成连锁伤害。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算子弹伤害（使用BaseWeapon的伤害计算逻辑）
            float bulletDamage = calculateBulletDamage(player, itemstack);
            
            // 创建祸乱子弹投射物
            ChaosBulletProjectile bullet = new ChaosBulletProjectile(level, player, itemstack, bulletDamage);
            
            // 设置子弹位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();
            
            bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            bullet.shoot(lookVec.x, lookVec.y, lookVec.z, 8.0F, 0.05F); // 8.0速度，极小散布（高精度）
            
            // 添加到世界
            level.addFreshEntity(bullet);
            
            // 播放狙击枪射击音效（使用弓箭射击音效，更高音调）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.8F, 0.5F);
        }
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.success(itemstack);
    }
    
    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    /**
     * 重写暴击特效，添加祸乱特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);
        
        // 添加额外的祸乱暴击特效
        if (player.level().isClientSide()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§4祸乱暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }
    
    /**
     * 重写击中敌人时的处理，添加弹幕生成逻辑
     */
    @Override
    protected void onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage) {
        super.onHitEnemy(player, target, stack, damage);
        
        // 播放特殊击中音效
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.0F, 0.8F);
    }
    
    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "祸乱狙击枪 | 基础伤害: " + String.format("%.0f", BASE_BULLET_DAMAGE) +
                " | 暴击率: " + String.format("%.1f%%", 0.26f * 100) +
                " | 暴击伤害: " + String.format("%.1f", 0.38f) + "倍" +
                " | 弹幕效果: 击中后生成4-8个追踪弹幕，伤害为50%" +
                " | 攻击速度: 1.0秒";
    }
}