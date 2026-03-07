package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.DragonFlameCurseFireProjectile;
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
 * 龙炎诅咒火焰 - CurseFire的升级版
 * 特性：右键生成追踪火球，施加龙炎燃烧效果
 */
public class DragonFlameCurseFire extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20; // 冷却时间1秒
    
    public DragonFlameCurseFire() {
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
        }, new Properties().fireResistant(), 582, 1.0f, 1f, 0.16f, 1.18f, 0.4f, WeaponEnum.MAGIC);
        
        setStory("龙炎诅咒火焰法杖，蕴含着诅咒的龙炎之力。\n" +
                "释放的龙炎火球会追踪最近的敌人，\n" +
                "被命中的目标将遭受龙炎燃烧之苦。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算咒火伤害
            float curseFireDamage = calculateCurseFireDamage(player, itemstack);
            
            // 生成龙炎诅咒火焰投射物
            DragonFlameCurseFireProjectile projectile = new DragonFlameCurseFireProjectile(
                    level, player, curseFireDamage);
            
            // 设置投射物位置和方向
            projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, 1.0F);
            
            // 添加到世界
            level.addFreshEntity(projectile);
            
            // 播放施法音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 0.6F);
            
            // 统计使用次数
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    /**
     * 计算咒火伤害
     */
    public float calculateCurseFireDamage(Player player, ItemStack stack) {
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
                    SoundEvents.BLAZE_HURT, SoundSource.NEUTRAL, 0.8F, 0.9F);
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
}