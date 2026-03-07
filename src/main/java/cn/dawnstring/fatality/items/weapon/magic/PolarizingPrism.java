package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.PolarizingPrismLaserProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEntities;
import cn.dawnstring.fatality.system.ManaSystem;
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
 * 偏光棱镜 - 魔法武器
 * 特性：每秒消耗5魔法，玩家长按右键能够一直发射笔直的激光，触碰到激光的实体持续受伤
 * 伤害78 暴击率24 暴击伤害30 浮动0.4 攻击速度0.05s
 */
public class PolarizingPrism extends BaseWeapon
{
    private static final float MANA_COST_PER_SECOND = 5.0f; // 每秒消耗5点魔法值
    private static final float LASER_DAMAGE_PER_TICK = 78.0f; // 每tick激光伤害78
    private static final int MANA_CHECK_INTERVAL = 20; // 每20tick检查一次魔法值（1秒）
    private static final float LASER_SPEED = 10.0f; // 激光速度
    private static final double LASER_RANGE = 50.0; // 激光射程50格
    
    private int lastManaCheckTick = 0; // 上次检查魔法值的tick
    private boolean isLaserActive = false; // 激光是否激活
    private PolarizingPrismLaserProjectile activeLaser = null; // 当前激活的激光

    public PolarizingPrism()
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
        }, new Properties(), 78, 0.05f, 1f, 0.24f, 0.30f, 0.4f, WeaponEnum.MAGIC);
        
        // 设置武器故事
        setStory("偏光棱镜，能够将光线聚焦成强大的激光束，持续灼烧敌人。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查玩家是否在持续使用（长按右键）
        if (player.isUsingItem()) {
            // 检查魔法值消耗
            if (canConsumeMana(player)) {
                // 激活激光
                activateLaser(level, player, itemstack);
                return InteractionResultHolder.success(itemstack);
            } else {
                // 魔法值不足，停止激光
                deactivateLaser();
                if (level.isClientSide()) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST_PER_SECOND + "点魔法值/秒"), true);
                }
                return InteractionResultHolder.fail(itemstack);
            }
        } else {
            // 开始使用，设置持续使用状态
            player.startUsingItem(hand);
            return InteractionResultHolder.success(itemstack);
        }
    }

    /**
     * 检查是否可以消耗魔法值
     */
    private boolean canConsumeMana(Player player) {
        int currentTick = (int) player.level().getGameTime();
        
        // 每1秒检查一次魔法值消耗
        if (currentTick - lastManaCheckTick >= MANA_CHECK_INTERVAL) {
            lastManaCheckTick = currentTick;
            
            // 检查玩家是否有足够的魔法值
            if (ManaSystem.safeConsumeMana(player, MANA_COST_PER_SECOND)) {
                return true;
            } else {
                return false;
            }
        }
        
        return true; // 在检查间隔内，默认允许继续使用
    }

    /**
     * 激活激光
     */
    private void activateLaser(Level level, Player player, ItemStack itemstack) {
        if (!level.isClientSide()) {
            if (!isLaserActive || activeLaser == null || !activeLaser.isAlive()) {
                // 计算激光伤害
                float laserDamage = calculateLaserDamage(player, itemstack);

                // 创建激光投射物
                activeLaser = new PolarizingPrismLaserProjectile(ModEntities.POLARIZING_PRISM_LASER_PROJECTILE.get(), level);
                activeLaser.setShooter(player, itemstack, laserDamage);

                // 设置激光位置和方向
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();

                activeLaser.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                activeLaser.shoot(lookVec.x, lookVec.y, lookVec.z, LASER_SPEED, 0.0f);

                // 添加到世界
                level.addFreshEntity(activeLaser);

                // 播放激光发射音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.2F);
            } else {
                // 更新现有激光的方向和位置
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();
                
                activeLaser.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                activeLaser.setDeltaMovement(lookVec.scale(LASER_SPEED));
            }
            
            isLaserActive = true;
        }
    }

    /**
     * 停用激光
     */
    private void deactivateLaser() {
        if (activeLaser != null && activeLaser.isAlive()) {
            activeLaser.discard();
        }
        isLaserActive = false;
        activeLaser = null;
    }

    /**
     * 计算激光伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateLaserDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于激光伤害
        float baseDamage = LASER_DAMAGE_PER_TICK;

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
     * 当玩家停止使用时调用
     */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            // 停止激光
            deactivateLaser();
        }
        super.releaseUsing(stack, level, entity, timeCharged);
    }

    /**
     * 获取攻击距离（重写为激光的射程）
     */
    @Override
    public double getAttackRange(Player player) {
        return LASER_RANGE;
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "激光伤害: " + String.format("%.1f", LASER_DAMAGE_PER_TICK) +
                " | 魔法消耗: " + MANA_COST_PER_SECOND + "点/秒" +
                " | 射程: " + LASER_RANGE + "格";
    }
}