package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.*;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

/**
 * 终极冲锋枪 - 远程武器
 * 特性：右键射出随机子弹（红色动能爆炸、蓝色电磁闪电链、黄色重力黑洞）
 * shift+右键发射奇点弹（5格半径伤害，10倍基础伤害，5秒冷却）
 * 属性：伤害728、暴击率32、暴击伤害35、浮动0.4、攻击速度0.2s
 */
public class UltimateSubmachineGun extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 4; // 冷却时间4tick（0.2秒）
    private static final int SINGULARITY_COOLDOWN_TICKS = 100; // 奇点弹冷却时间100tick（5秒）
    private static final float BASE_BULLET_DAMAGE = 728.0f; // 基础子弹伤害728
    private static final float SINGULARITY_DAMAGE_MULTIPLIER = 10.0f; // 奇点弹伤害倍率
    private static final float SINGULARITY_RADIUS = 5.0f; // 奇点弹爆炸半径5格
    private static final int BULLET_COUNT = 3; // 每次射击发射3发子弹
    private static final float BULLET_SPEED = 4.0f; // 子弹速度
    
    private final Random random = new Random();

    public UltimateSubmachineGun()
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
        }, new Properties().fireResistant(), 728, 0.2f, 1f, 0.32f, 1.35f, 0.4f, WeaponEnum.RANGED);
        
        setStory("终极冲锋枪，融合了三种元素力量的强大武器。\n" +
                "右键射击时随机发射红色动能爆炸弹、蓝色电磁闪电链或黄色重力黑洞弹，\n" +
                "每种子弹都有独特的特效和伤害机制。\n" +
                "shift+右键可发射强大的奇点弹，造成毁灭性的范围伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 检查是否在冷却中
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(itemstack);
            }

            // 检查是否按下shift键（奇点弹模式）
            if (player.isShiftKeyDown()) {
                // 检查奇点弹冷却
                if (player.getCooldowns().isOnCooldown(this)) {
                    return InteractionResultHolder.fail(itemstack);
                }
                
                // 发射奇点弹
                fireSingularityShot(player, level, itemstack);
                
                // 设置奇点弹冷却时间
                player.getCooldowns().addCooldown(this, SINGULARITY_COOLDOWN_TICKS);
            } else {
                // 发射普通随机子弹
                fireRandomBullets(player, level, itemstack);
                
                // 设置普通射击冷却时间
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            }
        }

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 发射随机类型的子弹
     */
    private void fireRandomBullets(Player player, Level level, ItemStack weapon) {
        // 计算子弹伤害
        float bulletDamage = calculateBulletDamage(player, weapon);
        
        // 发射多发子弹
        for (int i = 0; i < BULLET_COUNT; i++) {
            // 随机选择子弹类型
            int bulletType = random.nextInt(3); // 0:动能爆炸, 1:电磁闪电链, 2:重力黑洞
            
            // 创建对应类型的子弹投射物
            AbstractArrow bullet = createBulletByType(bulletType, level, player, weapon, bulletDamage);
            
            if (bullet != null) {
                // 设置子弹位置和方向
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();
                
                // 计算子弹散布
                Vec3 spreadVec = calculateBulletSpread(lookVec, i, BULLET_COUNT);
                
                bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                bullet.shoot(spreadVec.x, spreadVec.y, spreadVec.z, BULLET_SPEED, 0.15F);
                
                // 添加到世界
                level.addFreshEntity(bullet);
            }
        }
        
        // 播放射击音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.7F, 1.8F);
    }

    /**
     * 发射奇点弹
     */
    private void fireSingularityShot(Player player, Level level, ItemStack weapon) {
        // 计算奇点弹伤害（10倍基础伤害）
        float singularityDamage = calculateBulletDamage(player, weapon) *   SINGULARITY_DAMAGE_MULTIPLIER;
        
        // 创建奇点弹投射物
        SingularityProjectile singularity = new SingularityProjectile(level, player, weapon, singularityDamage);
        
        // 设置位置和方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();
        
        singularity.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
        singularity.shoot(lookVec.x, lookVec.y, lookVec.z, BULLET_SPEED * 0.8f, 0.05F); // 稍慢速度，更精准
        
        // 添加到世界
        level.addFreshEntity(singularity);
        
        // 播放奇点弹射击音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.7F);
    }

    /**
     * 根据子弹类型创建对应的投射物
     */
    private AbstractArrow createBulletByType(int bulletType, Level level, Player player, ItemStack weapon, float damage) {
        switch (bulletType) {
            case 0: // 红色动能爆炸弹
                return new KineticExplosionProjectile(level, player, weapon, damage);
            case 1: // 蓝色电磁闪电链
                return new ElectromagneticChainProjectile(level, player, weapon, damage);
            case 2: // 黄色重力黑洞弹
                return new GravityBlackholeProjectile(level, player, weapon, damage);
            default:
                return new BulletProjectile(level, player, weapon, damage);
        }
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
        float spreadAngle = (bulletIndex - (totalBullets - 1) / 2.0f) * 3.0f; // 每发子弹间隔3度
        
        // 转换为弧度
        double spreadRadians = Math.toRadians(spreadAngle);
        
        // 计算垂直于原方向的向量
        Vec3 perpendicular = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        
        // 应用散布
        return lookVec.add(perpendicular.scale(Math.sin(spreadRadians) * 0.1));
    }

    /**
     * 重写暴击特效，添加终极冲锋枪特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的终极冲锋枪暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6终极暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return "终极冲锋枪 | 基础伤害: " + String.format("%.0f", BASE_BULLET_DAMAGE) +
                " | 暴击率: " + String.format("%.1f%%", 0.32f * 100) +
                " | 暴击伤害: " + String.format("%.1f", 0.35f) + "倍" +
                " | 特殊效果: 随机发射三种元素子弹（动能爆炸/电磁闪电链/重力黑洞）\n" +
                " | 奇点弹: 10倍伤害，5格范围爆炸，5秒冷却";
    }
}