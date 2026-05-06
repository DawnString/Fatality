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
 * 泡泡枪 - 远程武器
 * 特性：右键释放追踪目标的泡泡，伤害109、暴击率20、暴击伤害28、浮动0.3、攻击速度0.1s
 */
public class BubbleGun extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 2; // 冷却时间2tick（0.1秒）
    private static final float BASE_BUBBLE_DAMAGE = 109.0f; // 基础泡泡伤害109

    public BubbleGun()
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
                return 0; // 材料等级
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties(), (int)BASE_BUBBLE_DAMAGE, 0.1f, 1f, 0.20f, 0.28f, 0.3f, WeaponEnum.RANGED);
        
        setStory("一把能够发射追踪泡泡的玩具枪，泡泡会缓慢地追踪最近的敌人，击中后产生水花效果。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算泡泡伤害（使用BaseWeapon的伤害计算逻辑）
            float bubbleDamage = calculateBubbleDamage(player, itemstack);

            // 寻找最近的目标，决定是否启用追踪模式
            boolean shouldTrack = findNearestTarget(level, player) != null;

            // 创建泡泡投射物
            BubbleProjectile bubble = new BubbleProjectile(level, player, itemstack, bubbleDamage, shouldTrack);

            // 设置泡泡位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            bubble.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            bubble.shoot(lookVec.x, lookVec.y, lookVec.z, 1.0F, 0.0F); // 低速发射，无散布

            // 添加到世界
            level.addFreshEntity(bubble);

            // 播放泡泡枪射击音效（使用水花音效）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.8F, 1.2F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算泡泡伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateBubbleDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 重写暴击特效，添加泡泡枪特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的泡泡枪暴击特效
        if (player.level().isClientSide) {
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
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "追踪泡泡枪 | 基础伤害: " + String.format("%.0f", BASE_BUBBLE_DAMAGE) +
                " | 暴击率: " + String.format("%.1f%%", 0.20f * 100) +
                " | 暴击伤害: " + String.format("%.1f", 0.28f) + "倍" +
                " | 追踪效果: 泡泡自动追踪16格内最近目标";
    }
}