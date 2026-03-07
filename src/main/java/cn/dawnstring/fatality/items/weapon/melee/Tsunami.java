package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.TsunamiProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 海啸 - 近战武器
 * 特性：矛类，无视重力，每次丢出3支矛
 * 伤害550 暴击率20 暴击伤害28 浮动0.3 攻击速度0.5s
 */
public class Tsunami extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 10; // 冷却时间10tick（0.5秒）
    private static final int SPEAR_COUNT = 3; // 每次投掷3支矛
    private static final float SPREAD_ANGLE = 15.0f; // 矛的散布角度
    
    public Tsunami() {
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
        }, new Properties().fireResistant(), 550, 0.5f, 1f, 0.20f, 1.28f, 0.3f, WeaponEnum.MELEE);
        
        setStory("海啸长矛，蕴含着海洋的狂暴力量。\n" +
                "每次投掷同时发射3支无视重力的长矛，形成密集火力网，\n" +
                "对敌人造成毁灭性的打击。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算长矛伤害
            float spearDamage = calculateSpearDamage(player, itemstack);
            
            // 发射3支长矛
            for (int i = 0; i < SPEAR_COUNT; i++) {
                // 创建海啸长矛投射物
                TsunamiProjectile spear = new TsunamiProjectile(level, player, spearDamage);
                
                // 设置投射物位置
                spear.setPos(player.getEyePosition().x, player.getEyePosition().y, player.getEyePosition().z);
                
                // 计算散布方向
                Vec3 spreadDirection = calculateSpreadDirection(player.getLookAngle(), i);
                
                // 设置投射物方向和速度
                spear.shoot(spreadDirection.x, spreadDirection.y, spreadDirection.z, 2.0F, 0.0F); // 2.0速度，无散布
                
                // 添加到世界
                level.addFreshEntity(spear);
            }
            
            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 0.9F);
            
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
    public float calculateSpearDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    /**
     * 计算散布方向
     */
    private Vec3 calculateSpreadDirection(Vec3 lookDirection, int spearIndex) {
        // 计算散布角度（中间一支直射，两侧两支有角度）
        float angle = 0.0f;
        if (spearIndex == 0) {
            angle = -SPREAD_ANGLE; // 左侧
        } else if (spearIndex == 2) {
            angle = SPREAD_ANGLE; // 右侧
        }
        // spearIndex == 1 为中间，角度为0
        
        // 将角度转换为弧度
        double angleRad = Math.toRadians(angle);
        
        // 计算旋转后的方向向量
        double x = lookDirection.x * Math.cos(angleRad) - lookDirection.z * Math.sin(angleRad);
        double z = lookDirection.x * Math.sin(angleRad) + lookDirection.z * Math.cos(angleRad);
        
        return new Vec3(x, lookDirection.y, z);
    }
}