package cn.dawnstring.fatality.entity.boss.enderdragon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private LivingEntity owner;
    private LivingEntity target;
    private float damage;
    private int lifetime;
    private final int maxLifetime;
    private boolean isProtective;

    public DragonFlameBall(EntityType<? extends DragonFlameBall> type, Level level) {
        super(type, level);
        this.maxLifetime = 400;
        this.lifetime = 0;
        this.isProtective = false;
    }

    public DragonFlameBall(Level level, LivingEntity owner, LivingEntity target, float damage, boolean isProtective) {
        this(level, owner, target, damage, isProtective, 400);
    }

    public DragonFlameBall(Level level, LivingEntity owner, LivingEntity target, float damage, boolean isProtective, int lifetime) {
        super(null, level);
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
        if (target == null || !target.isAlive()) {
            this.discard();
            return;
        }
        
        Vec3 currentPos = this.position();
        Vec3 targetPos = target.position();
        
        Vec3 direction = targetPos.subtract(currentPos).normalize();
        double speed = 0.8;
        
        Vec3 velocity = direction.scale(speed);
        this.setDeltaMovement(velocity);
        
        this.setPos(
            this.getX() + velocity.x,
            this.getY() + velocity.y,
            this.getZ() + velocity.z
        );
        
        this.lookAt(target, 30.0f, 30.0f);
    }

    private void checkCollisions() {
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
                if (entity == target || entity == owner) {
                    entity.hurt(this.damageSources().mobAttack(owner), damage);
                    this.discard();
                    return;
                }
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

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public boolean isProtective() {
        return isProtective;
    }

    public void setProtective(boolean protective) {
        isProtective = protective;
    }
}