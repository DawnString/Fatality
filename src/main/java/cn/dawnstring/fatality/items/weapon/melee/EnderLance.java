package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.EnderLanceProjectile;
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
 * 末影长矛 - 近战武器
 * 特性：右键掷出，不受重力影响，击中目标后可以将该目标半径5格内的实体吸附（不包括boss以及玩家），然后爆炸造成伤害
 * 伤害133 暴击率16 暴击伤害18 浮动0.3 攻击速度0.2s
 */
public class EnderLance extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 4; // 冷却时间4tick（0.2秒）
    
    public EnderLance() {
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
        }, new Properties().fireResistant(), 133, 0.2f, 1f, 0.16f, 1.18f, 0.3f, WeaponEnum.MELEE);
        
        setStory("末影长矛，蕴含着强大的末影吸附之力。\n" +
                "投掷时不受重力影响，击中目标后吸附周围5格内的实体，\n" +
                "随后引发爆炸造成范围伤害。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算长矛伤害
            float lanceDamage = calculateLanceDamage(player, itemstack);
            
            // 生成末影长矛投射物
            EnderLanceProjectile projectile = new EnderLanceProjectile(
                    level, player, lanceDamage);
            
            // 设置投射物位置和方向
            projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            
            // 添加到世界
            level.addFreshEntity(projectile);
            
            // 播放攻击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
            
            // 统计使用次数
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    /**
     * 计算长矛伤害
     */
    public float calculateLanceDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 播放命中音效
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENDERMAN_HURT, SoundSource.NEUTRAL, 0.6F, 1.0F);
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
}