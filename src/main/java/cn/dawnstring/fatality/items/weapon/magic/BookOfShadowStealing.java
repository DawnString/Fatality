package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.ShadowProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 暗影窃取之书 - 发射暗影粒子轨迹攻击目标的魔法武器
 * 特性：极高伤害、暗影粒子轨迹、追踪攻击方式
 */
public class BookOfShadowStealing extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 210.0f; // 基础魔法伤害210
    private static final double ATTACK_RANGE = 20.0; // 攻击范围20格

    // 单次攻击相关参数
    private static final float SINGLE_ATTACK_MANA_COST = 8.0f; // 单次攻击消耗8点魔法值
    private static final int ATTACK_COOLDOWN_TICKS = 20; // 攻击间隔20tick（1秒）

    public BookOfShadowStealing() {
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
                return 0; // 魔法书本身没有攻击伤害加成
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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 1.0f, 1f, 0.10f, 0.10f, 0.4f, WeaponEnum.MAGIC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查攻击冷却时间
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }

        // 检查魔法值是否足够
        if (!ManaSystem.hasEnoughMana(player, SINGLE_ATTACK_MANA_COST)) {
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        // 消耗魔法值
        ManaSystem.consumeMana(player, SINGLE_ATTACK_MANA_COST);

        // 设置攻击冷却时间
        player.getCooldowns().addCooldown(this, ATTACK_COOLDOWN_TICKS);

        // 发射追踪弹幕
        performTrackingProjectileAttack(level, player, itemstack);

        // 播放攻击音效
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8F, 1.0F);
        }

        return InteractionResultHolder.consume(itemstack);
    }
    
    /**
     * 执行追踪弹幕攻击
     */
    private void performTrackingProjectileAttack(Level level, Player player, ItemStack itemstack) {
        // 计算伤害
        float shadowDamage = calculateFinalDamage(player, itemstack, null);
        
        // 获取玩家视线方向
        Vec3 lookVec = player.getLookAngle();
        
        // 计算发射位置（玩家前方1.5格）
        Vec3 startPos = player.getEyePosition().add(lookVec.scale(1.5));
        
        // 计算初始目标位置（玩家前方10格）
        Vec3 initialTargetPos = startPos.add(lookVec.scale(10));
        
        // 创建追踪弹幕
        ShadowProjectile projectile = new ShadowProjectile(
                level, player, startPos, initialTargetPos, shadowDamage, 0f, 0f
        );
        
        // 设置弹幕为追踪模式
        projectile.setTrackingMode(true);
        
        // 添加到世界
        level.addFreshEntity(projectile);
    }
    
    /**
     * 获取半径内的所有目标（排除玩家）
     */
    private List<LivingEntity> getTargetsInRadius(Level level, Player player, double radius) {
        Vec3 playerPos = player.position();
        AABB searchBox = new AABB(playerPos, playerPos).inflate(radius);
        
        List<LivingEntity> targets = new ArrayList<>();
        List<Entity> entities = level.getEntities(player, searchBox);
        
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity &&
                    !(livingEntity instanceof Player) &&
                    livingEntity.isAlive() &&
                    playerPos.distanceTo(livingEntity.position()) <= radius) {
                targets.add(livingEntity);
            }
        }
        
        return targets;
    }
    
    @Override
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        super.onCriticalHit(player, target, damage);

        // 添加额外的暗影窃取暴击特效
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§8暗影窃取暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }

        // 暴击时生成额外的暗影粒子
        if (player.level().isClientSide()) {
            spawnCriticalShadowParticles(player.level(), target);
        }
    }

    /**
     * 暴击时生成额外的暗影粒子
     */
    private void spawnCriticalShadowParticles(Level level, LivingEntity target) {
        // 在目标位置生成暴击粒子效果
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 1.2;
            double offsetY = (Math.random() - 0.5) * 1.2 + 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.2;

            // 生成暴击暗影粒子
            level.addParticle(ParticleTypes.SMOKE,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    0, 0.15, 0);

            // 生成灵魂火焰暴击粒子
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX() + offsetX * 0.9,
                        target.getY() + offsetY * 0.9,
                        target.getZ() + offsetZ * 0.9,
                        0, 0.12, 0);
            }
        }
    }
}