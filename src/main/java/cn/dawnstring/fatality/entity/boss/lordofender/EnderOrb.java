package cn.dawnstring.fatality.entity.boss.lordofender;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EnderOrb extends Entity {

    private LivingEntity owner;
    private LivingEntity target;
    private static final double SPEED = 1.5;
    private static final float DAMAGE = 200.0f;
    private static final int MAX_LIFETIME = 120;
    private int lifetime = 0;

    public EnderOrb(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public void setOwner(LivingEntity owner) { this.owner = owner; }
    public void setTarget(LivingEntity target) { this.target = target; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        lifetime++;
        if (lifetime >= MAX_LIFETIME) {
            discard();
            return;
        }

        if (target != null && target.isAlive()) {
            Vec3 dir = target.getEyePosition().subtract(position()).normalize();
            setDeltaMovement(dir.scale(SPEED));
        }

        this.move(MoverType.SELF, getDeltaMovement());
        setFacingFromVelocity();

        if (horizontalCollision || verticalCollision) {
            discard();
            return;
        }

        AABB box = getBoundingBox();
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, box.inflate(0.4));
        for (LivingEntity entity : entities) {
            if (entity == owner) continue;
            if (!entity.isAlive()) continue;
            entity.hurt(damageSources().mobAttack(owner), DAMAGE);
            discard();
            return;
        }
    }

    private void setFacingFromVelocity() {
        Vec3 v = getDeltaMovement();
        if (v.lengthSqr() < 0.001) return;
        float yaw = (float) (Math.atan2(v.x, v.z) * (180.0 / Math.PI));
        setYRot(yaw);
        yRotO = yaw;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            Entity e = ((net.minecraft.server.level.ServerLevel)level()).getEntity(tag.getUUID("Owner"));
            if (e instanceof LivingEntity le) owner = le;
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        if (owner != null) tag.putUUID("Owner", owner.getUUID());
    }
}
