package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.BaseBoss;
import cn.dawnstring.fatality.entity.projectile.AbyssOfCalamityProjectile;
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
 * 深渊之灾 - 近战矛类武器
 * 特性：右键投掷直线飞行，命中目标附加祸渊效果，累积苦难印记
 * 伤害2021 暴击率29 暴击伤害38 浮动0.3 攻击速度1s
 */
public class AbyssOfCalamity extends BaseWeapon {
    
    // 武器属性
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final int MANA_COST = 25; // 投掷消耗魔力值
    private static final float PROJECTILE_SPEED = 3.0f; // 投射物速度
    private static final int CALAMITY_DURATION = 120; // 祸渊效果持续时间（6秒，20tick=1秒）
    private static final int CALAMITY_DAMAGE_INTERVAL = 20; // 祸渊伤害间隔（1秒）
    private static final float CALAMITY_DAMAGE = 50.0f; // 祸渊每秒伤害
    private static final int SUFFERING_MARK_MAX_STACKS = 5; // 苦难印记最大层数
    private static final int HEAL_AMOUNT = 20; // 印记清除时恢复生命值
    
    public AbyssOfCalamity() {
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
        }, new Properties().fireResistant(), 2021, 1.0f, 1f, 0.29f, 1.38f, 0.3f, WeaponEnum.MELEE);
        
        setStory("深渊之灾 - 来自深渊的诅咒之矛\n" +
                "右键投掷：直线飞行，命中目标附加祸渊效果\n" +
                "苦难印记：目标死亡时清除印记，为玩家恢复生命值\n");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide())
        {
            // 创建深渊之灾投射物
            AbyssOfCalamityProjectile projectile = new AbyssOfCalamityProjectile(level, player, itemstack, calculateFinalDamage(player, itemstack, null));
            
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
     * 判断是否为Boss
     */
    private boolean isBoss(LivingEntity entity) {
        return entity instanceof BaseBoss;
    }
    
    /**
     * 重写inventoryTick方法，处理祸渊效果的持续伤害
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        if (!level.isClientSide && entity instanceof Player player && isSelected) {
            // 处理玩家周围带有祸渊效果的实体
            processCalamityEffects(level, player);
        }
    }
    
    /**
     * 处理祸渊效果的持续伤害
     */
    private void processCalamityEffects(Level level, Player player) {
        // 获取玩家周围10格范围内的实体
        var entities = level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(10.0));
        
        for (LivingEntity target : entities) {
            // 检查是否有祸渊效果
            int damageTicks = target.getPersistentData().getInt("CalamityDamageTicks");
            int maxTicks = target.getPersistentData().getInt("CalamityMaxTicks");
            
            if (maxTicks > 0) {
                damageTicks++;
                
                // 每20tick（1秒）造成一次伤害
                if (damageTicks % CALAMITY_DAMAGE_INTERVAL == 0 && damageTicks <= maxTicks) {
                    target.hurt(target.damageSources().magic(), CALAMITY_DAMAGE);
                    
                    // 播放伤害音效
                    level.playSound(null, target.getX(), target.getY(), target.getZ(), 
                            SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.3F, 1.0F);
                }
                
                // 更新计时器
                target.getPersistentData().putInt("CalamityDamageTicks", damageTicks);
                
                // 效果结束，清除数据
                if (damageTicks >= maxTicks) {
                    target.getPersistentData().remove("CalamityDamageTicks");
                    target.getPersistentData().remove("CalamityMaxTicks");
                }
            }
        }
    }
    
    /**
     * 重写onHitEnemy方法，处理目标死亡时的印记清除和生命恢复
     */
    @Override
    public void  onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage)
    {
        if (!target.isAlive())
        {
            // 目标死亡，清除苦难印记并恢复生命
            clearSufferingMarkAndHeal(target, player);
        }
    }
    
    /**
     * 清除苦难印记并恢复生命
     */
    private void clearSufferingMarkAndHeal(LivingEntity target, Player player) {
        int stacks = target.getPersistentData().getInt("SufferingMarkStacks");
        if (stacks > 0) {
            // 恢复生命值
            player.heal(HEAL_AMOUNT * stacks);
            
            // 清除印记
            target.getPersistentData().remove("SufferingMarkStacks");
 
            // 播放恢复音效
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }
}