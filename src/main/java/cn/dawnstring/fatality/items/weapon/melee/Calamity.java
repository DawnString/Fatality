package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.CalamityProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 灾厄 - 近战武器
 * 特性：矛类，右键投掷，击中目标爆炸，对周围目标造成伤害，飞出时有黑色粒子
 * 伤害500 暴击率26 暴击伤害35 浮动0.3 攻击速度0.25s
 */
public class Calamity extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）
    
    public Calamity() {
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
        }, new Properties().fireResistant(), 500, 0.25f, 1f, 0.26f, 1.35f, 0.3f, WeaponEnum.MELEE);
        
        setStory("灾厄长矛，蕴含着毁灭性的力量。\n" +
                "投掷时伴随黑色粒子轨迹，击中目标后引发剧烈爆炸，\n" +
                "对周围敌人造成毁灭性打击。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算长矛伤害
            float spearDamage = calculateSpearDamage(player, itemstack);
            
            // 生成灾厄长矛投射物
            CalamityProjectile projectile = new CalamityProjectile(
                    level, player, itemstack, spearDamage);
            
            // 设置投射物位置和方向
            projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 1.0F);
            
            // 添加到世界
            level.addFreshEntity(projectile);
            
            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 0.8F);
            
            // 统计使用次数
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        }
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.success(itemstack);
    }
    
    /**
     * 计算长矛伤害（使用BaseWeapon的伤害计算逻辑）
     */
    public float calculateSpearDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}