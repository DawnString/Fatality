package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.DragonFlameAshesProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
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
 * 龙炎灰烬 - Ashes的升级版
 * 特性：狙击枪，右键攻击，击中后子弹散射，对锥形区域造成距离递减伤害，施加龙炎燃烧效果
 */
public class DragonFlameAshes extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 1; // 冷却时间0.05秒（1tick）
    
    public DragonFlameAshes() {
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
                return 0; // 材料等级（铁级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties().fireResistant(), 29, 0.05f, 1f, 0.14f, 1.14f, 0.3f, WeaponEnum.RANGED);
        
        setStory("龙炎灰烬狙击枪，蕴含着龙炎之力的精准武器。\n" +
                "击中目标后子弹会散射，对目标后方的敌人造成锥形区域伤害，\n" +
                "被命中的目标将遭受龙炎燃烧之苦。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算子弹伤害
            float bulletDamage = calculateBulletDamage(player, itemstack);
            
            // 生成龙炎灰烬投射物
            DragonFlameAshesProjectile projectile = new DragonFlameAshesProjectile(
                    level, player, bulletDamage);
            
            // 设置投射物位置和方向
            projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F); // 高速子弹
            
            // 添加到世界
            level.addFreshEntity(projectile);
            
            // 播放射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6F, 1.2F);
            
            // 统计使用次数
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    /**
     * 计算子弹伤害
     */
    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 对攻击目标施加龙炎燃烧效果
        if (ModEffects.DRAGONFIRE_BURN != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    ModEffects.DRAGONFIRE_BURN.get(), 100, 0)); // 5秒龙炎燃烧效果
        }
        
        // 播放命中音效
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLAZE_HURT, SoundSource.NEUTRAL, 0.5F, 1.1F);
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
}