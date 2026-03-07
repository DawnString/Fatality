package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.BurnToAshesProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 焚尽 - 魔法武器
 * 特性：右键发射火球，火球向前飞行，不受重力影响，攻击目标获得灵火灼烧效果(SpiritualFireBurn)
 * 伤害810 暴击率15 暴击伤害20 浮动0.4 攻击速度1s
 */
public class BurnToAshes extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    
    public BurnToAshes() {
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
        }, new Properties().fireResistant(), 810, 1.0f, 1f, 0.15f, 1.20f, 0.4f, WeaponEnum.MAGIC);
        
        setStory("焚尽，蕴含着纯净火焰力量的魔法武器。\n" +
                "右键发射灵火箭矢，命中目标后施加灵火灼烧效果。\n" +
                "灵火灼烧会持续造成伤害，并阻止目标自然恢复生命值。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算火球伤害
            float fireballDamage = calculateFinalDamage(player, itemstack, null);
            
            // 创建焚尽火球投射物
            BurnToAshesProjectile fireball = new BurnToAshesProjectile(level, player, fireballDamage);
            fireball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(fireball);
            
            // 播放发射音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 统计使用次数
            player.awardStat(Stats.ITEM_USED.get(this));
            
            // 设置冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 播放命中音效
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLAZE_HURT, SoundSource.NEUTRAL, 0.6F, 1.0F);
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
}