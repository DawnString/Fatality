package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.effects.DragonSlayerBlessingEffect;
import cn.dawnstring.fatality.entity.projectile.DragonSlayerSwordWaveProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 屠龙大剑 - 近战武器
 * 特性：手持时给予屠龙者祝福效果，左键攻击时释放红色粒子剑气
 * 伤害135 暴击率16 暴击伤害20 浮动0.3 攻击速度0.25s
 */
public class DragonSlayerGreatsword extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）

    public DragonSlayerGreatsword()
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
                // 使用下界合金锭修复
                return Ingredient.of(net.minecraft.world.item.Items.NETHERITE_INGOT);
            }
        }, new Properties(), 135, 0.25f, 1f, 0.16f, 0.20f, 0.3f, WeaponEnum.MELEE);

        setStory("传说中的屠龙大剑，蕴含着屠龙者的荣耀与力量。\n" +
                "手持此剑时获得屠龙者祝福，攻击时释放红色剑气，\n" +
                "能够穿透敌人造成毁灭性伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算剑气伤害
            float swordWaveDamage = calculateSwordWaveDamage(player, itemstack);
            
            // 创建屠龙大剑剑气投射物
            DragonSlayerSwordWaveProjectile swordWave = new DragonSlayerSwordWaveProjectile(level, player, swordWaveDamage);
            
            // 设置投射物位置和方向
            swordWave.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            swordWave.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 0.0F); // 中等飞行速度
            
            level.addFreshEntity(swordWave);
            
            // 播放剑气音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算剑气伤害
     */
    public float calculateSwordWaveDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    @Override
    public void onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage) {
        // 触发屠龙者祝福效果
        applyDragonSlayerBlessing(player);
        
        super.onHitEnemy(player, target, stack, damage);
    }

    /**
     * 应用屠龙者祝福效果
     */
    private void applyDragonSlayerBlessing(Player player) {
        // 给予玩家屠龙者祝福效果：增加攻击力和移动速度
        MobEffectInstance attackBoost = new MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 100, 1); // 5秒力量II效果
        MobEffectInstance speedBoost = new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 100, 0); // 5秒速度I效果
        
        player.addEffect(attackBoost);
        player.addEffect(speedBoost);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        // 当玩家手持此剑时，给予屠龙者祝福效果
        if (entity instanceof Player player && isSelected) {
            MobEffectInstance dragonSlayerBlessing = new MobEffectInstance(ModEffects.DRAGON_SLAYER_BLESSING.get(), 100, 0);
            player.addEffect(dragonSlayerBlessing);
        }
    }
}