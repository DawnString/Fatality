package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.NaturalWillProjectile;
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
import net.minecraft.world.phys.Vec3;

/**
 * 自然意志 - 近战矛类武器
 * 特性：右键投掷，投掷后飞出，受重力影响，命中后生成"生命之环"，持续治疗范围内的玩家，或者持续伤害目标
 * 伤害2375 暴击率29 暴击伤害38 浮动0.3 攻击速度1s
 */
public class NaturalWill extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float PROJECTILE_SPEED = 2.5f; // 投射物速度
    
    public NaturalWill() {
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
        }, new Properties().fireResistant(), 2375, 1.0f, 1f, 0.29f, 1.38f, 0.3f, WeaponEnum.MELEE);
        
        setStory("自然意志 - 蕴含自然生命力的神圣长矛\n" +
                "右键投掷：直线飞行，命中目标生成生命之环\n" +
                "生命之环：治疗范围内的友方单位，伤害敌方单位\n" +
                "自然平衡：根据目标阵营决定治疗效果或伤害效果");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算投射物伤害
            float projectileDamage = calculateProjectileDamage(player, itemstack);
            
            // 创建自然意志投射物
            NaturalWillProjectile projectile = new NaturalWillProjectile(level, player, itemstack, projectileDamage);
            
            // 设置投射物的位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();
            
            projectile.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            projectile.shoot(lookVec.x, lookVec.y, lookVec.z, PROJECTILE_SPEED, 0.1F);
            
            // 添加到世界
            level.addFreshEntity(projectile);
            
            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    /**
     * 计算投射物伤害（使用BaseWeapon的伤害计算逻辑）
     */
    public float calculateProjectileDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于基础伤害2375
        float baseDamage = 2375.0f;

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
            // 暴击伤害公式（与BaseWeapon保持一致）
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            // 普通伤害公式（与BaseWeapon保持一致）
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }

        return Math.max(0, finalDamage);
    }
}