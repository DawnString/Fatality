package cn.dawnstring.fatality.entity.boss.lordofender;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class FireExplosionBall extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LivingEntity owner;
    private Vec3 direction = Vec3.ZERO;
    private static final double SPEED = 1.2;
    private static final float DAMAGE = 220.0f;
    private static final int MAX_LIFETIME = 200;
    private int lifetime = 0;

    public FireExplosionBall(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    public void shoot(Vec3 dir) {
        this.direction = dir.normalize();
        this.setDeltaMovement(direction.scale(SPEED));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        lifetime++;
        if (lifetime >= MAX_LIFETIME) {
            discard();
            return;
        }

        this.move(MoverType.SELF, getDeltaMovement());
        setFacingFromVelocity();

        if (horizontalCollision || verticalCollision) {
            explode();
            return;
        }

        AABB box = getBoundingBox();
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, box.inflate(0.5));
        for (LivingEntity entity : entities) {
            if (entity == owner) continue;
            if (!entity.isAlive()) continue;
            entity.hurt(damageSources().mobAttack(owner), DAMAGE);
            entity.setSecondsOnFire(5);
            explode();
            return;
        }
    }

    private void explode() {
        if (level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.explode(this, getX(), getY(), getZ(), 3.0f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        }
        discard();
    }

    private void setFacingFromVelocity() {
        Vec3 v = getDeltaMovement();
        if (v.lengthSqr() < 0.001) return;
        float yaw = (float) (Math.atan2(v.x, v.z) * (180.0 / Math.PI));
        setYRot(yaw);
        yRotO = yaw;
        setYHeadRot(yaw);
        setYHeadRot(yaw);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        double dx = tag.getDouble("DirX"), dy = tag.getDouble("DirY"), dz = tag.getDouble("DirZ");
        direction = new Vec3(dx, dy, dz);
        if (tag.hasUUID("Owner")) {
            Entity e = ((net.minecraft.server.level.ServerLevel)level()).getEntity(tag.getUUID("Owner"));
            if (e instanceof LivingEntity le) owner = le;
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putDouble("DirX", direction.x);
        tag.putDouble("DirY", direction.y);
        tag.putDouble("DirZ", direction.z);
        if (owner != null) tag.putUUID("Owner", owner.getUUID());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event ->
                event.setAndContinue(RawAnimation.begin().thenPlay("idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
