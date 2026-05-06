package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.GhostlyParticleProjectile;
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
import net.minecraft.world.phys.Vec3;

/**
 * 幽灵魔典 - 发射白色幽灵粒子追踪目标的魔法武器
 * 特性：生成2-4个白色粒子，以曲线轨迹追踪最近目标
 * 伤害：313点基础魔法伤害，12%暴击率，15%暴击伤害，0.4伤害浮动
 * 消耗：10点魔法值
 * 冷却：1秒攻击速度
 */
public class GhostlyGrimoire extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 313.0f; // 基础魔法伤害313
    private static final float MANA_COST_PER_ATTACK = 10.0f; // 每次攻击消耗10点魔法值
    private static final int ATTACK_COOLDOWN_TICKS = 20; // 攻击冷却时间20tick（1秒）
    private static final int MIN_PARTICLE_COUNT = 2; // 最少粒子数量
    private static final int MAX_PARTICLE_COUNT = 4; // 最多粒子数量

    public GhostlyGrimoire() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0;
            }

            @Override
            public float getSpeed() {
                return 0;
            }

            @Override
            public float getAttackDamageBonus() {
                return 0; // 魔典本身没有攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null;
            }
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 1.0f, 1f, 0.12f, 0.15f, 0.4f, WeaponEnum.MAGIC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查攻击冷却时间
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }

        // 检查魔法值是否足够
        if (!ManaSystem.hasEnoughMana(player, MANA_COST_PER_ATTACK)) {
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 消耗魔法值
        ManaSystem.consumeMana(player, MANA_COST_PER_ATTACK);

        // 设置攻击冷却时间
        player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);

        // 发射幽灵粒子弹幕
        performGhostlyParticleAttack(level, player, itemstack);

        // 播放幽灵音效
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.6F, 1.2F);
        }

        return InteractionResultHolder.consume(itemstack);
    }
    
    /**
     * 执行幽灵粒子攻击
     */
    private void performGhostlyParticleAttack(Level level, Player player, ItemStack itemstack) {
        // 计算伤害
        float ghostDamage = calculateFinalDamage(player, itemstack, null);
        
        // 获取玩家视线方向
        Vec3 lookVec = player.getLookAngle();
        
        // 计算发射位置（玩家前方1.5格）
        Vec3 startPos = player.getEyePosition().add(lookVec.scale(1.5));
        
        // 随机生成2-4个粒子
        int particleCount = level.random.nextInt(MAX_PARTICLE_COUNT - MIN_PARTICLE_COUNT + 1) + MIN_PARTICLE_COUNT;
        
        for (int i = 0; i < particleCount; i++) {
            // 计算每个粒子的初始目标位置（随机偏移）
            double spreadX = (level.random.nextDouble() - 0.5) * 3.0;
            double spreadY = (level.random.nextDouble() - 0.5) * 2.0;
            double spreadZ = (level.random.nextDouble() - 0.5) * 3.0;
            
            Vec3 initialTargetPos = startPos.add(lookVec.scale(8.0))
                    .add(spreadX, spreadY, spreadZ);
            
            // 创建幽灵粒子弹幕
            GhostlyParticleProjectile projectile = new GhostlyParticleProjectile(
                    level, player, startPos, initialTargetPos, ghostDamage, i * 0.5f
            );
            
            // 设置弹幕为追踪模式
            projectile.setTrackingMode(true);
            
            // 添加到世界
            level.addFreshEntity(projectile);
        }
    }
    
    /**
     * 计算幽灵伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateGhostDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 重写暴击特效，添加幽灵魔典特有的暴击效果
     */
    @Override
    protected void onCriticalHit(Player player, net.minecraft.world.entity.LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的幽灵魔典暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§f幽灵暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    /**
     * 检查是否有足够的魔法值
     */
    public static boolean hasEnoughMana(Player player, float requiredMana) {
        return ManaSystem.getCurrentMana(player) >= requiredMana;
    }
}