package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.BubbleProjectile;
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
 * 泡泡枪 V2升级版 - 远程武器
 * 特性：右键释放追踪目标的小泡泡，并释放一个大泡泡直线向前，速度快，伤害为300
 * 伤害130 暴击率22 暴击伤害28 浮动0.3 攻击速度0.1s
 */
public class BubbleGunV2 extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 2; // 冷却时间2tick（0.1秒）
    private static final float BASE_SMALL_BUBBLE_DAMAGE = 130.0f; // 小泡泡基础伤害130
    private static final float BASE_BIG_BUBBLE_DAMAGE = 300.0f; // 大泡泡基础伤害300
    private static final int SMALL_BUBBLE_COUNT = 3; // 小泡泡数量
    
    public BubbleGunV2()
    {
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
                return 1; // 材料等级（比原版泡泡枪更高）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties().fireResistant(), 0, 0.1f, 1f, 0.22f, 0.28f, 0.3f, WeaponEnum.RANGED);
        
        setStory("泡泡枪的升级版本，融合了追踪小泡泡和高速大泡泡的双重攻击模式。\n" +
                "发射3个追踪小泡泡自动寻找目标，同时发射一个高速直线大泡泡造成高额伤害。\n" +
                "虽然单发伤害略低，但多重攻击模式提供了更强的战术灵活性。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算小泡泡伤害
            float smallBubbleDamage = calculateSmallBubbleDamage(player, itemstack);
            // 计算大泡泡伤害
            float bigBubbleDamage = calculateBigBubbleDamage(player, itemstack);
            
            // 寻找最近的目标，决定是否启用追踪模式
            boolean shouldTrack = findNearestTarget(level, player) != null;

            // 发射3个追踪小泡泡
            for (int i = 0; i < SMALL_BUBBLE_COUNT; i++) {
                // 创建小泡泡投射物
                BubbleProjectile smallBubble = new BubbleProjectile(level, player, itemstack, smallBubbleDamage, shouldTrack);
                
                // 设置小泡泡位置和方向（轻微散布）
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();
                
                // 为小泡泡添加轻微的角度散布
                Vec3 spreadDirection = calculateSpreadDirection(lookVec, i);
                
                smallBubble.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                smallBubble.shoot(spreadDirection.x, spreadDirection.y, spreadDirection.z, 1.2F, 0.0F); // 比原版稍快
                
                // 添加到世界
                level.addFreshEntity(smallBubble);
            }
            
            // 发射1个高速大泡泡（直线向前）
            BubbleProjectile bigBubble = new BubbleProjectile(level, player, itemstack, bigBubbleDamage, false); // 大泡泡不追踪
            
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();
            
            bigBubble.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            bigBubble.shoot(lookVec.x, lookVec.y, lookVec.z, 2.5F, 0.0F); // 高速直线发射
            
            // 添加到世界
            level.addFreshEntity(bigBubble);

            // 播放泡泡枪射击音效（使用水花音效）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0F, 0.9F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }
    
    /**
     * 计算小泡泡伤害
     */
    public float calculateSmallBubbleDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于小泡泡伤害
        float baseDamage = BASE_SMALL_BUBBLE_DAMAGE;

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
    
    /**
     * 计算大泡泡伤害
     */
    public float calculateBigBubbleDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于大泡泡伤害
        float baseDamage = BASE_BIG_BUBBLE_DAMAGE;

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
    
    /**
     * 重写暴击特效，添加泡泡枪V2特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的泡泡枪V2暴击特效
        if (player.level().isClientSide()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§b泡泡暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }
    
    /**
     * 寻找最近的追踪目标
     */
    private LivingEntity findNearestTarget(Level level, Player player) {
        LivingEntity nearestTarget = null;
        double nearestDistance = 16.0; // 最大追踪范围16格
        
        // 获取玩家周围的所有实体
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(16.0))) {
            if (entity != player && entity.isAlive() && !entity.isInvulnerable()) {
                double distance = player.distanceTo(entity);
                if (distance < nearestDistance) {
                    nearestTarget = entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearestTarget;
    }
    
    /**
     * 计算散布方向（为小泡泡添加轻微散布）
     */
    private Vec3 calculateSpreadDirection(Vec3 lookDirection, int bubbleIndex) {
        // 为3个小泡泡添加轻微的角度散布
        float angle = 0.0f;
        if (bubbleIndex == 0) {
            angle = -5.0f; // 左侧小泡泡
        } else if (bubbleIndex == 2) {
            angle = 5.0f; // 右侧小泡泡
        }
        // bubbleIndex == 1 为中间小泡泡，角度为0
        
        // 将角度转换为弧度
        double angleRad = Math.toRadians(angle);
        
        // 计算旋转后的方向向量
        double x = lookDirection.x * Math.cos(angleRad) - lookDirection.z * Math.sin(angleRad);
        double z = lookDirection.x * Math.sin(angleRad) + lookDirection.z * Math.cos(angleRad);
        
        return new Vec3(x, lookDirection.y, z);
    }
    
    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "泡泡枪V2 | 小泡泡伤害: " + String.format("%.0f", BASE_SMALL_BUBBLE_DAMAGE) +
                " | 大泡泡伤害: " + String.format("%.0f", BASE_BIG_BUBBLE_DAMAGE) +
                " | 暴击率: " + String.format("%.1f%%", 0.22f * 100) +
                " | 暴击伤害: " + String.format("%.1f", 0.28f) + "倍" +
                " | 攻击模式: 3个追踪小泡泡 + 1个高速大泡泡";
    }
}