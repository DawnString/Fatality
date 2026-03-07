package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.AshesDeathProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

import java.util.List;

/**
 * 灰烬死亡 - 投掷长矛
 * 特性：投掷后对目标半径5格内所有实体造成伤害，并施加护甲粉碎效果
 */
public class AshesDeath extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间1秒
    private static final float BASE_DAMAGE = 790.0f; // 基础伤害
    private static final float EXPLOSION_RADIUS = 5.0f; // 爆炸半径
    private static final int ARMOR_BREAK_DURATION = 100; // 护甲粉碎持续时间（5秒）

    public AshesDeath()
    {
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
                return 0;
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
        }, new Properties(), 0, 1.0f, 1f, 0.12f, 0.15f, 0.2f, WeaponEnum.MELEE);
        
        setStory("一把致命的投掷长矛，击中目标后会产生爆炸，对周围敌人造成伤害并粉碎他们的护甲。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 创建长矛投射物
            AshesDeathProjectile spear = new AshesDeathProjectile(level, player, itemstack, calculateSpearDamage(player, itemstack));
            
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();
            
            spear.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            spear.shoot(lookVec.x, lookVec.y, lookVec.z, 3.0F, 0.1F); // 中等速度，小散布
            
            level.addFreshEntity(spear);
            
            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 在客户端生成投掷粒子效果（删除剑气粒子）
        if (level.isClientSide()) {
            spawnThrowParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成投掷粒子效果
     */
    private void spawnThrowParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();
        
        // 投掷起点位置
        Vec3 throwPos = eyePos.add(lookVec.scale(1.0));
        
        // 生成简单的投掷粒子效果（删除剑气粒子）
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;
            
            level.addParticle(ParticleTypes.CRIT,
                    throwPos.x + offsetX,
                    throwPos.y + offsetY,
                    throwPos.z + offsetZ,
                    lookVec.x * 0.1, lookVec.y * 0.1, lookVec.z * 0.1);
        }
    }

    /**
     * 计算长矛伤害
     */
    public float calculateSpearDamage(Player player, ItemStack stack) {
        float baseDamage = BASE_DAMAGE;
        float accessoryBaseBonus = calculateAccessoryBaseBonus(player);
        float otherBonus = calculateOtherBonus(player);
        float fluctuation = calculateDamageFluctuation();
        boolean isCritical = isCriticalHit(player);

        float finalDamage;
        if (isCritical) {
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }

        return Math.max(0, finalDamage);
    }

    /**
     * 长矛击中目标后的爆炸效果
     */
    public static void onSpearHit(LivingEntity target, Vec3 hitPos, Level level, Player player, float baseDamage) {
        if (!level.isClientSide()) {
            // 对半径5格内的所有实体造成伤害
            List<Entity> entitiesInRadius = level.getEntities(target, 
                    new AABB(hitPos.x - EXPLOSION_RADIUS, hitPos.y - EXPLOSION_RADIUS, hitPos.z - EXPLOSION_RADIUS,
                            hitPos.x + EXPLOSION_RADIUS, hitPos.y + EXPLOSION_RADIUS, hitPos.z + EXPLOSION_RADIUS));
            
            for (Entity entity : entitiesInRadius) {
                if (entity instanceof LivingEntity livingEntity) {
                    // 计算距离衰减伤害
                    double distance = hitPos.distanceTo(entity.position());
                    float distanceMultiplier = (float) Math.max(0.3, 1.0 - (distance / EXPLOSION_RADIUS));
                    float explosionDamage = baseDamage * 0.5f * distanceMultiplier; // 爆炸伤害为基础伤害的50%
                    
                    livingEntity.hurt(livingEntity.damageSources().playerAttack(player), explosionDamage);
                    
                    // 对目标施加护甲粉碎效果（使用虚弱效果代替）
                    if (entity == target) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ARMOR_BREAK_DURATION, 1));
                    }
                }
            }
            
            // 生成爆炸粒子效果
            spawnExplosionParticles(level, hitPos);
            
            // 播放爆炸音效
            level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 0.9F);
        }
    }

    /**
     * 生成爆炸粒子效果
     */
    private static void spawnExplosionParticles(Level level, Vec3 centerPos) {
        if (level.isClientSide()) {
            // 生成爆炸粒子（增强特效）
            for (int i = 0; i < 50; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double radius = Math.random() * EXPLOSION_RADIUS;
                double height = (Math.random() - 0.5) * EXPLOSION_RADIUS;
                
                double x = centerPos.x + radius * Math.cos(angle);
                double y = centerPos.y + height;
                double z = centerPos.z + radius * Math.sin(angle);
                
                level.addParticle(ParticleTypes.EXPLOSION_EMITTER,
                        x, y, z,
                        (Math.random() - 0.5) * 0.3,
                        Math.random() * 0.2,
                        (Math.random() - 0.5) * 0.3);
            }
            
            // 生成护甲粉碎特效粒子（增强特效）
            for (int i = 0; i < 30; i++) {
                double offsetX = (Math.random() - 0.5) * 3.0;
                double offsetY = (Math.random() - 0.5) * 3.0;
                double offsetZ = (Math.random() - 0.5) * 3.0;
                
                level.addParticle(ParticleTypes.CRIT,
                        centerPos.x + offsetX,
                        centerPos.y + offsetY,
                        centerPos.z + offsetZ,
                        (Math.random() - 0.5) * 0.2,
                        Math.random() * 0.2,
                        (Math.random() - 0.5) * 0.2);
            }
            
            // 添加额外的火焰粒子效果
            for (int i = 0; i < 20; i++) {
                double offsetX = (Math.random() - 0.5) * 2.0;
                double offsetY = (Math.random() - 0.5) * 2.0;
                double offsetZ = (Math.random() - 0.5) * 2.0;
                
                level.addParticle(ParticleTypes.FLAME,
                        centerPos.x + offsetX,
                        centerPos.y + offsetY,
                        centerPos.z + offsetZ,
                        (Math.random() - 0.5) * 0.1,
                        Math.random() * 0.1,
                        (Math.random() - 0.5) * 0.1);
            }
        }
    }
}