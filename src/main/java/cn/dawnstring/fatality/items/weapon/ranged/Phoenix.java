package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.PhoenixRayProjectile;
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
 * 凤凰 - 远程枪类武器
 * 特性：右键攻击发射2-4发凤凰射线
 * 属性：伤害452，暴击率24，暴击伤害30，浮动0.2，攻击速度0.25s
 */
public class Phoenix extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）
    private static final float BASE_RAY_DAMAGE = 452.0f; // 基础射线伤害

    public Phoenix()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
            }

            @Override
            public float getSpeed() {
                return 0;
            }

            @Override
            public float getAttackDamageBonus() {
                return 0;
            }

            @Override
            public int getLevel() {
                return 4; // 钻石级
            }

            @Override
            public int getEnchantmentValue() {
                return 15;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不可修复
            }
        }, new Properties(), 0, 0.25f, 1f, 0.24f, 0.30f, 0.2f, WeaponEnum.RANGED);
        
        // 设置武器故事
        this.setStory("传说中的凤凰武器，能够发射炽热的凤凰射线，每一发都蕴含着凤凰的火焰之力。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 随机发射2-4发凤凰射线
            int rayCount = 2 + level.random.nextInt(3); // 2-4发
            
            for (int i = 0; i < rayCount; i++) {
                // 计算射线伤害
                float rayDamage = calculateRayDamage(player, itemstack);

                // 创建凤凰射线投射物
                PhoenixRayProjectile ray = new PhoenixRayProjectile(level, player, itemstack, rayDamage);

                // 设置射线位置和方向
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();

                // 添加随机散布，使射线稍微分散
                float spreadX = (level.random.nextFloat() - 0.5f) * 0.1f;
                float spreadY = (level.random.nextFloat() - 0.5f) * 0.1f;
                float spreadZ = (level.random.nextFloat() - 0.5f) * 0.1f;

                ray.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                ray.shoot(lookVec.x + spreadX, lookVec.y + spreadY, lookVec.z + spreadZ, 5.0F, 1.0F);

                // 添加到世界
                level.addFreshEntity(ray);
            }

            // 播放射击音效（使用火焰音效）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.5F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算射线伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateRayDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于射线伤害
        float baseDamage = BASE_RAY_DAMAGE;

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