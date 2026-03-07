package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ElementalMissileProjectile extends AbstractArrow {

    private float damage;
    private boolean isLargeMissile;
    private int lifeTime = 0;
    private final int maxLifeTime = 600; // 30秒寿命

    public ElementalMissileProjectile(EntityType<? extends ElementalMissileProjectile> type, Level level)
    {
        super(type, level);
    }

    public ElementalMissileProjectile(Level level, LivingEntity owner, float damage, boolean isLargeMissile) {
        super(ModEntities.ELEMENTAL_MISSILE_PROJECTILE.get(), owner, level); // 修复：使用正确的实体类型
        this.damage = damage;
        this.isLargeMissile = isLargeMissile;
        this.setNoGravity(true);
        this.setBaseDamage(damage);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            if (result.getEntity() instanceof LivingEntity living && living != this.getOwner()) {
                living.hurt(this.damageSources().indirectMagic(this, this.getOwner()), damage);
            }
            if (isLargeMissile) {
                // 大飞弹碰到玩家后消失
                this.discard();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide && isLargeMissile) {
            // 大飞弹碰到墙壁后爆炸并生成小飞弹
            spawnSmallMissiles();
            this.discard();
        }
    }

    private void spawnSmallMissiles() {
        Vec3 center = this.position();
        int smallMissileCount = 16; // 大量小飞弹

        for (int i = 0; i < smallMissileCount; i++) {
            double angle = 2 * Math.PI * i / smallMissileCount;
            double pitch = Math.PI * (this.random.nextDouble() - 0.5);

            Vec3 direction = new Vec3(
                    Math.cos(angle) * Math.cos(pitch),
                    Math.sin(pitch),
                    Math.sin(angle) * Math.cos(pitch)
            ).normalize();

            ElementalMissileProjectile smallMissile = new ElementalMissileProjectile(
                    this.level(), (LivingEntity) this.getOwner(), 280.0f, false
            );
            smallMissile.setPos(center.x, center.y, center.z);
            smallMissile.setDeltaMovement(direction.scale(1.0));
            smallMissile.setNoGravity(true);

            this.level().addFreshEntity(smallMissile);
        }
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        // 性能优化：超过寿命的弹幕及时销毁
        if (lifeTime >= maxLifeTime) {
            this.discard();
            return;
        }

        // 粒子效果
        if (this.level().isClientSide) {
            Vec3 pos = this.position();
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                        pos.x + (this.random.nextDouble() - 0.5) * 0.3,
                        pos.y + (this.random.nextDouble() - 0.5) * 0.3,
                        pos.z + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
            }
            
            // 元素飞弹特有的粒子效果
            if (isLargeMissile) {
                // 大飞弹粒子效果
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.FLAME,
                            pos.x + (this.random.nextDouble() - 0.5) * 0.5,
                            pos.y + (this.random.nextDouble() - 0.5) * 0.5,
                            pos.z + (this.random.nextDouble() - 0.5) * 0.5,
                            0, 0, 0);
                }
            } else {
                // 小飞弹粒子效果
                for (int i = 0; i < 2; i++) {
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                            pos.x + (this.random.nextDouble() - 0.5) * 0.2,
                            pos.y + (this.random.nextDouble() - 0.5) * 0.2,
                            pos.z + (this.random.nextDouble() - 0.5) * 0.2,
                            0, 0, 0);
                }
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}