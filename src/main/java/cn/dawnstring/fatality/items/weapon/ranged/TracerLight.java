package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.TracerLightProjectile;
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
 * 曳光 - 远程武器
 * 特性：冲锋枪，右键发射曳光子弹，子弹能够追踪最近的目标（除了玩家）
 * 属性包括310伤害、28%暴击率、38暴击伤害、0.3浮动值和0.1s攻击速度
 */
public class TracerLight extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 2; // 冷却时间2tick（0.1秒）
    private static final float BASE_BULLET_DAMAGE = 310.0f; // 基础子弹伤害310
    private static final int BULLET_COUNT = 1; // 每次射击发射1发子弹
    private static final float BULLET_SPEED = 3.5f; // 子弹速度
    private static final double TRACKING_RANGE = 20.0; // 追踪范围20格

    public TracerLight()
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
        }, new Properties().fireResistant(), 310, 0.1f, 1f, 0.28f, 1.38f, 0.3f, WeaponEnum.RANGED);
        
        setStory("一把能够发射曳光子弹的冲锋枪，子弹会自动追踪最近的敌人，在黑暗中划出明亮的轨迹。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算子弹伤害（使用BaseWeapon的伤害计算逻辑）
            float bulletDamage = calculateBulletDamage(player, itemstack);

            // 寻找最近的追踪目标
            LivingEntity nearestTarget = findNearestTarget(level, player);

            // 发射曳光子弹
            for (int i = 0; i < BULLET_COUNT; i++) {
                // 创建曳光子弹投射物
                TracerLightProjectile bullet = new TracerLightProjectile(level, player, bulletDamage);

                // 设置子弹位置和方向
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();

                // 添加轻微散布（如果是多发子弹）
                Vec3 spreadVec = calculateBulletSpread(lookVec, i, BULLET_COUNT);

                bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                bullet.shoot(spreadVec.x, spreadVec.y, spreadVec.z, BULLET_SPEED, 0.1F); // 3.5速度，小散布

                // 如果找到目标，设置追踪
                if (nearestTarget != null) {
                    bullet.setTarget(nearestTarget);
                }

                // 添加到世界
                level.addFreshEntity(bullet);
            }

            // 播放冲锋枪射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.6F, 1.5F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算子弹伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateBulletDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于子弹伤害
        float baseDamage = BASE_BULLET_DAMAGE;

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
     * 计算子弹散布
     */
    private Vec3 calculateBulletSpread(Vec3 lookVec, int bulletIndex, int totalBullets) {
        if (totalBullets <= 1) {
            return lookVec; // 单发子弹，无散布
        }

        // 计算散布角度（基于子弹索引）
        float spreadAngle = (bulletIndex - (totalBullets - 1) / 2.0f) * 2.0f; // 每发子弹间隔2度
        
        // 转换为弧度
        double spreadRadians = Math.toRadians(spreadAngle);
        
        // 计算垂直于原方向的向量
        Vec3 perpendicular = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        
        // 应用散布
        return lookVec.add(perpendicular.scale(Math.sin(spreadRadians) * 0.1));
    }

    /**
     * 寻找最近的追踪目标
     */
    private LivingEntity findNearestTarget(Level level, Player player) {
        LivingEntity nearestTarget = null;
        double nearestDistance = TRACKING_RANGE;
        
        // 获取玩家周围的所有实体
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(TRACKING_RANGE))) {
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
     * 重写暴击特效，添加曳光枪特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的曳光枪暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6曳光暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "曳光冲锋枪 | 基础伤害: " + String.format("%.0f", BASE_BULLET_DAMAGE) +
                " | 暴击率: " + String.format("%.1f%%", 0.28f * 100) +
                " | 暴击伤害: " + String.format("%.1f", 0.38f) + "倍" +
                " | 追踪效果: 子弹自动追踪20格内最近目标";
    }
}