package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.BloodTornadoProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
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
 * 血色龙卷 - Tornado升级版魔法武器
 * 特性：右键释放直线前进的红色龙卷风，吸引4格内实体并造成伤害
 * 伤害675 暴击率22 暴击伤害28 浮动0.3 攻击速度0.5s
 */
public class BloodTornado extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 675.0f; // 基础魔法伤害675
    private static final float MANA_COST_PER_ATTACK = 18.0f; // 每次攻击消耗18点魔法值
    private static final int ATTACK_COOLDOWN_TICKS = 10; // 攻击冷却时间10tick（0.5秒）

    public BloodTornado() {
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
        }, new Properties(), 0, 0.5f, 1f, 0.22f, 0.28f, 0.3f, WeaponEnum.MAGIC);
        
        setStory("血色龙卷是Tornado的升级版本，释放的龙卷风呈现血红色，具有更强的吸引力和伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 检查冷却时间
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }
        
        // 检查魔法值是否足够
        if (!hasEnoughMana(player)) {
            // 播放魔法不足音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F);
            
            // 显示魔法值不足提示
            if (!level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST_PER_ATTACK + "点魔法值才能释放血色龙卷风"), true);
            }
            
            return InteractionResultHolder.fail(itemstack);
        }
        
        // 消耗魔法值
        consumeMana(player);
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);
        
        // 播放施法音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 0.8F);
        
        // 在服务器端执行攻击逻辑
        if (!level.isClientSide()) {
            performBloodTornadoAttack(level, player, itemstack);
        }
        
        return InteractionResultHolder.success(itemstack);
    }
    
    /**
     * 检查玩家是否有足够的魔法值
     */
    private boolean hasEnoughMana(Player player) {
        return ManaSystem.hasEnoughMana(player, MANA_COST_PER_ATTACK);
    }
    
    /**
     * 消耗魔法值
     */
    private void consumeMana(Player player) {
        ManaSystem.consumeMana(player, MANA_COST_PER_ATTACK);
    }
    
    /**
     * 执行血色龙卷风攻击
     */
    private void performBloodTornadoAttack(Level level, Player player, ItemStack itemstack) {
        // 计算血色龙卷风伤害
        float tornadoDamage = calculateBloodTornadoDamage(player, itemstack);
        
        // 创建血色龙卷风投射物
        BloodTornadoProjectile tornado = new BloodTornadoProjectile(level, player, tornadoDamage);
        
        // 添加到世界
        level.addFreshEntity(tornado);
        
        // 播放血色龙卷风发射音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8F, 0.6F);
    }
    
    /**
     * 计算血色龙卷风伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateBloodTornadoDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于魔法伤害
        float baseDamage = BASE_MAGIC_DAMAGE;

        // 计算基础伤害加成（基于饰品）
        float accessoryBaseBonus = calculateAccessoryBaseBonus(player);

        // 计算其他伤害加成（饰品、药水等）
        float otherBonus = calculateOtherBonus(player);

        // 计算伤害浮动值
        float fluctuation = calculateDamageFluctuation();

        // 判断是否暴击
        boolean isCritical = isCriticalHit(player);

        float finalDamage;
        if (isCritical) {
            // 暴击伤害公式
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            // 普通伤害公式
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }

        return Math.max(0.1f, finalDamage); // 确保至少造成0.1点伤害
    }
}