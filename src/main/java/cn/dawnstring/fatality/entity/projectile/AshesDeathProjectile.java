package cn.dawnstring.fatality.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 死亡灰烬长矛投射物类
 * 实现长矛投掷功能，击中目标后造成半径5格的范围伤害
 */
public class AshesDeathProjectile extends AbstractArrow {
    private static final float EXPLOSION_RADIUS = 5.0f; // 爆炸半径5格
    private static final float DAMAGE_REDUCTION_PER_BLOCK = 0.05f; // 每格伤害衰减5%
    private final float baseDamage;
    private final Player shooter;

    public AshesDeathProjectile(Level level, Player shooter, ItemStack weaponStack, float damage) {
        super(EntityType.ARROW, level);
        this.shooter = shooter;
        this.baseDamage = damage;

        // 设置投射物属性
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.pickup = AbstractArrow.Pickup.DISALLOWED; // 不允许拾取

        // 设置位置和方向
        Vec3 shooterPos = shooter.getEyePosition();
        Vec3 lookVec = shooter.getLookAngle();

        this.setPos(shooterPos.x, shooterPos.y, shooterPos.z);
        this.shoot(lookVec.x, lookVec.y, lookVec.z, 2.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();

        // 生成飞行粒子效果
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }

        // 检查是否超时（10秒后消失）
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide()) {
            // 处理击中逻辑
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityResult = (EntityHitResult) result;
                Entity target = entityResult.getEntity();

                if (target instanceof LivingEntity) {
                    // 造成范围伤害
                    createExplosionEffect((LivingEntity) target);
                }
            } else {
                // 击中方块或其他物体，也造成范围伤害
                createExplosionEffect(null);
            }

            // 播放击中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.8F);

            // 销毁投射物
            this.discard();
        }
    }

    /**
     * 创建爆炸效果，对半径5格内的所有实体造成伤害
     */
    private void createExplosionEffect(LivingEntity primaryTarget) {
        Vec3 explosionCenter = this.position();

        // 获取爆炸范围内的所有实体
        AABB explosionArea = new AABB(
                explosionCenter.x - EXPLOSION_RADIUS,
                explosionCenter.y - EXPLOSION_RADIUS,
                explosionCenter.z - EXPLOSION_RADIUS,
                explosionCenter.x + EXPLOSION_RADIUS,
                explosionCenter.y + EXPLOSION_RADIUS,
                explosionCenter.z + EXPLOSION_RADIUS
        );

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, explosionArea)) {
            // 跳过施法者
            if (entity == this.shooter) continue;

            // 计算距离衰减伤害
            double distance = entity.distanceTo(this);
            float damageMultiplier = Math.max(0.1f, 1.0f - (float)(distance / EXPLOSION_RADIUS));
            float finalDamage = this.baseDamage * damageMultiplier;

            // 造成伤害
            if (finalDamage > 0) {
                DamageSource damageSource = this.damageSources().playerAttack(this.shooter);
                entity.hurt(damageSource, finalDamage);

                // 对主要目标施加护甲粉碎效果
                if (entity == primaryTarget) {
                    applyArmorBreakEffect(entity);
                }
            }
        }

        // 生成爆炸粒子效果
        spawnExplosionParticles();
    }

    /**
     * 施加护甲粉碎效果（使用虚弱效果模拟）
     */
    private void applyArmorBreakEffect(LivingEntity target) {
        // 施加虚弱效果（模拟护甲粉碎）
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1)); // 5秒虚弱效果

        // 播放效果音效
        this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5F, 0.8F);
    }

    /**
     * 生成飞行粒子效果
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();

        // 生成灰色烟雾粒子
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    pos.x + (Math.random() - 0.5) * 0.2,
                    pos.y + (Math.random() - 0.5) * 0.2,
                    pos.z + (Math.random() - 0.5) * 0.2,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02,
                    (Math.random() - 0.5) * 0.02);
        }

        // 生成火焰粒子
        if (this.tickCount % 5 == 0) {
            this.level().addParticle(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    this.getDeltaMovement().x * -0.1,
                    this.getDeltaMovement().y * -0.1,
                    this.getDeltaMovement().z * -0.1);
        }
    }

    /**
     * 生成爆炸粒子效果
     */
    private void spawnExplosionParticles() {
        Vec3 center = this.position();

        // 生成爆炸粒子
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * EXPLOSION_RADIUS;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (Math.random() - 0.5) * EXPLOSION_RADIUS;

            // 生成火焰粒子
            this.level().addParticle(ParticleTypes.FLAME,
                    x, y, z,
                    (Math.random() - 0.5) * 0.2,
                    Math.random() * 0.1,
                    (Math.random() - 0.5) * 0.2);

            // 生成烟雾粒子
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    x, y, z,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.05,
                    (Math.random() - 0.5) * 0.1);
        }

        // 生成冲击波粒子
        for (int ring = 1; ring <= 3; ring++) {
            for (int i = 0; i < 12; i++) {
                double angle = (double) i / 12.0 * Math.PI * 2;
                double x = center.x + Math.cos(angle) * ring;
                double z = center.z + Math.sin(angle) * ring;
                double y = center.y;

                this.level().addParticle(ParticleTypes.CRIT,
                        x, y, z,
                        Math.cos(angle) * 0.1,
                        0.1,
                        Math.sin(angle) * 0.1);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }
}