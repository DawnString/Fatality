package cn.dawnstring.fatality.entity.boss.enderdragon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class DragonFlameBall extends Entity implements GeoEntity {

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private LivingEntity owner;
    private LivingEntity target;
    private float damage;
    private int lifetime;
    private int maxLifetime;
    private boolean isProtective;
    private int immunityTicks = 10;

    public DragonFlameBall(EntityType<? extends DragonFlameBall> type, Level level) {
        super(type, level);
        this.maxLifetime = 400;
        this.lifetime = 0;
        this.isProtective = false;
        this.setNoGravity(true);
    }

    public DragonFlameBall(EntityType<? extends DragonFlameBall> type, Level level, LivingEntity owner, LivingEntity target, float damage, boolean isProtective, int lifetime) {
        super(type, level);
        this.owner = owner;
        this.target = target;
        this.damage = damage;
        this.isProtective = isProtective;
        this.maxLifetime = lifetime;
        this.lifetime = 0;
        
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (immunityTicks > 0) immunityTicks--;
        
        if (this.level().isClientSide()) {
            return;
        }
        
        lifetime++;
        
        if (lifetime >= maxLifetime) {
            this.discard();
            return;
        }
        
        if (isProtective) {
            tickProtective();
        } else {
            tickAttack();
        }
        
        checkCollisions();
    }

    private void tickProtective() {
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        
        double angle = (this.tickCount * 0.1) % (Math.PI * 2);
        double radius = 3.0;
        double yOffset = 2.0;
        
        double x = owner.getX() + Math.cos(angle) * radius;
        double y = owner.getY() + yOffset + Math.sin(angle * 2) * 0.5;
        double z = owner.getZ() + Math.sin(angle) * radius;
        
        this.setPos(x, y, z);
    }

    private void tickAttack() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() < 0.001) {
            return;
        }
        this.move(MoverType.SELF, velocity);
    }

    private void checkCollisions() {
        if (immunityTicks > 0) return;
        
        AABB boundingBox = this.getBoundingBox();
        
        if (!isProtective) {
            if (this.level().getBlockState(this.blockPosition()).isSuffocating(this.level(), this.blockPosition())) {
                this.discard();
                return;
            }
        }
        
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, boundingBox.inflate(0.5));
        
        for (LivingEntity entity : entities) {
            if (entity == owner) continue;
            if (entity instanceof Player player && player.isCreative()) continue;
            if (!entity.isAlive()) continue;
            
            if (isProtective) {
                if (entity != owner) {
                    entity.hurt(this.damageSources().mobAttack(owner), damage);
                }
            } else {
                entity.hurt(this.damageSources().mobAttack(owner), damage);
                this.discard();
                return;
            }
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        if (compound.contains("Owner")) {
            int ownerId = compound.getInt("Owner");
            Entity entity = this.level().getEntity(ownerId);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }
        if (compound.contains("Target")) {
            int targetId = compound.getInt("Target");
            Entity entity = this.level().getEntity(targetId);
            if (entity instanceof LivingEntity) {
                this.target = (LivingEntity) entity;
            }
        }
        this.damage = compound.getFloat("Damage");
        this.lifetime = compound.getInt("Lifetime");
        this.isProtective = compound.getBoolean("IsProtective");
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        if (owner != null) {
            compound.putInt("Owner", owner.getId());
        }
        if (target != null) {
            compound.putInt("Target", target.getId());
        }
        compound.putFloat("Damage", damage);
        compound.putInt("Lifetime", lifetime);
        compound.putBoolean("IsProtective", isProtective);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.normal"));
        }));
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setProtective(boolean isProtective) {
        this.isProtective = isProtective;
    }

    public void setLifetime(int lifetime) {
        this.maxLifetime = lifetime;
        this.lifetime = 0;
    }
}
