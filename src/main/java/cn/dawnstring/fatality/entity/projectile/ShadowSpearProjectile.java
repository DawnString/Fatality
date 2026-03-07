package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

/**
 * 暗影长矛投射物 - 用于ShadowSpear武器的投掷长矛
 * 特性：投掷长矛击中目标后生成黑色粒子触手造成二次伤害（基础伤害的0.5倍）
 */
public class ShadowSpearProjectile extends AbstractArrow
{
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(ShadowSpearProjectile.class, EntityDataSerializers.FLOAT);

    private int lifeTime = 0;
    private final int maxLifeTime = 100; // 5秒生命周期
    private boolean hasHit = false;
    private int tentacleTimer = 0;
    private Vec3 hitPosition;
    private LivingEntity hitTarget;
    private Player shooter;
    private ItemStack weaponStack;
    private boolean isSummonedSpear = false; // 是否为召唤模式（从上方垂直下落）

    public ShadowSpearProjectile(EntityType<? extends ShadowSpearProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // 暗影长矛受重力影响
        this.entityData.set(DATA_DAMAGE, 888.0f); // 默认伤害
        this.shooter = null;
        this.weaponStack = ItemStack.EMPTY;
    }

    public ShadowSpearProjectile(Level level, Player shooter, ItemStack weaponStack, float damage) {
        this(ModEntities.SHADOW_SPEAR_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.setOwner(shooter);
        this.setBaseDamage(damage); // 使用传入的伤害值
        this.entityData.set(DATA_DAMAGE, damage); // 同步伤害数据
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.isSummonedSpear = false; // 默认投掷模式
    }
    
    /**
     * 召唤模式构造函数 - 从上方垂直下落
     */
    public ShadowSpearProjectile(Level level, Player shooter, ItemStack weaponStack, float damage, boolean isSummoned) {
        this(ModEntities.SHADOW_SPEAR_PROJECTILE.get(), level);
        this.shooter = shooter;
        this.weaponStack = weaponStack;
        this.setOwner(shooter);
        this.setBaseDamage(damage);
        this.entityData.set(DATA_DAMAGE, damage);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.isSummonedSpear = isSummoned;
        
        if (isSummonedSpear) {
            this.setNoGravity(true); // 召唤模式不受重力影响，保持垂直下落
        }
    }

    /**
     * 获取暗影长矛伤害（从同步数据中读取）
     */
    public float getShadowSpearDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DAMAGE, 888.0f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;

        if (hasHit) {
            // 触手阶段
            tentacleTimer++;
            handleTentaclePhase();
        } else {
            // 飞行阶段
            handleFlightPhase();
        }

        // 召唤模式特殊处理：检查是否击中地面或目标
        if (isSummonedSpear && !hasHit && !this.level().isClientSide()) {
            checkSummonedSpearHit();
        }

        // 检查生命周期结束
        if (lifeTime >= maxLifeTime) {
            this.discard();
        }
    }

    /**
     * 处理飞行阶段
     */
    private void handleFlightPhase() {
        // 生成暗影长矛飞行粒子效果
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }
    }

    /**
     * 处理触手阶段
     */
    private void handleTentaclePhase() {
        if (hitPosition != null) {
            // 生成黑色粒子触手效果
            if (this.level().isClientSide()) {
                spawnTentacleParticles();
            }

            // 触手造成二次伤害
            if (tentacleTimer >= 20 && !this.level().isClientSide()) { // 1秒后触发触手伤害
                dealTentacleDamage();
                this.discard(); // 伤害完成后消失
            }
        }
    }
    
    /**
     * 召唤模式命中检测
     */
    private void checkSummonedSpearHit() {
        // 检查是否击中地面（Y坐标低于0.1）
        if (this.getY() <= 0.1) {
            // 击中地面，触发爆炸
            summonSpearExplode(null);
            return;
        }
        
        // 检查是否击中实体
        AABB hitBox = new AABB(
                this.getX() - 0.5, this.getY() - 0.5, this.getZ() - 0.5,
                this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5
        );
        
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, hitBox);
        
        for (LivingEntity entity : entities) {
            if (entity != this.shooter && entity.isAlive()) {
                // 击中实体，触发爆炸
                summonSpearExplode(entity);
                return;
            }
        }
    }
    
    /**
     * 召唤模式爆炸效果
     */
    private void summonSpearExplode(LivingEntity directHit) {
        if (hasHit) {
            return; // 防止重复爆炸
        }
        
        hasHit = true;
        hitPosition = new Vec3(this.getX(), this.getY(), this.getZ());
        
        // 停止移动
        this.setDeltaMovement(Vec3.ZERO);
        
        // 播放爆炸音效
        this.level().playSound(null, hitPosition.x, hitPosition.y, hitPosition.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        // 生成爆炸粒子效果
        spawnExplosionParticles(hitPosition);
        
        // 对爆炸范围内的实体造成伤害
        applySummonExplosionDamage(hitPosition, 2.0f, directHit);
        
        // 立即移除投射物
        this.discard();
    }
    
    /**
     * 生成爆炸粒子效果
     */
    private void spawnExplosionParticles(Vec3 pos) {
        if (this.level().isClientSide()) {
            // 生成爆炸粒子效果（紫色阴影风格）
            for (int i = 0; i < 30; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2;
                double distance = this.random.nextDouble() * 2.0;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;
                double offsetY = this.random.nextDouble() * 2.0 - 1.0;
                
                // 紫色传送门粒子
                this.level().addParticle(ParticleTypes.PORTAL,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        0, 0.1, 0);
                
                // 烟雾粒子
                this.level().addParticle(ParticleTypes.SMOKE,
                        pos.x + offsetX * 0.5, pos.y + offsetY * 0.5, pos.z + offsetZ * 0.5,
                        0, 0.05, 0);
                
                // 灵魂粒子
                if (i % 3 == 0) {
                    this.level().addParticle(ParticleTypes.SOUL,
                            pos.x + offsetX * 0.3, pos.y + offsetY * 0.3, pos.z + offsetZ * 0.3,
                            0, 0.03, 0);
                }
            }
        }
    }
    
    /**
     * 召唤模式爆炸伤害
     */
    private void applySummonExplosionDamage(Vec3 center, float radius, LivingEntity directHit) {
        // 获取爆炸范围内的所有实体
        AABB explosionArea = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
        
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea);
        float explosionDamage = getShadowSpearDamage() * 0.8f; // 爆炸伤害为基础伤害的80%
        
        for (LivingEntity entity : entities) {
            if (entity == this.shooter) {
                continue; // 不对玩家自己造成伤害
            }
            
            // 计算距离衰减
            double distance = center.distanceTo(entity.position());
            if (distance > radius) {
                continue; // 超出爆炸范围
            }
            
            // 计算衰减后的伤害（距离越远伤害越低）
            float distanceFactor = 1.0f - (float)(distance / radius);
            float finalDamage = explosionDamage * distanceFactor;
            
            // 应用伤害
            if (this.shooter != null) {
                entity.hurt(this.level().damageSources().playerAttack(this.shooter), finalDamage);
            } else {
                entity.hurt(this.level().damageSources().magic(), finalDamage);
            }
            
            // 添加击退效果
            Vec3 knockbackDir = entity.position().subtract(center).normalize();
            entity.setDeltaMovement(knockbackDir.x * 0.5, 0.3, knockbackDir.z * 0.5);
        }
        
        // 如果直接命中实体，对其造成额外伤害
        if (directHit != null && directHit != this.shooter) {
            if (this.shooter != null) {
                directHit.hurt(this.level().damageSources().playerAttack(this.shooter), getShadowSpearDamage());
            } else {
                directHit.hurt(this.level().damageSources().magic(), getShadowSpearDamage());
            }
        }
    }

    /**
     * 生成飞行阶段粒子效果
     */
    private void spawnFlightParticles() {
        // 暗影长矛轨迹粒子
        for (int i = 0; i < 3; i++) {
            // 黑色烟幕粒子
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0.05, 0);

            // 灵魂火焰粒子（暗影特效）
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                    0, 0.03, 0);

            // 暗影粒子（自定义暗影效果）
            if (i % 2 == 0) {
                this.level().addParticle(ParticleTypes.SOUL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.15,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.15,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.15,
                        0, 0.02, 0);
            }
        }
    }

    /**
     * 生成触手粒子效果
     */
    private void spawnTentacleParticles() {
        // 触手挥舞效果
        for (int i = 0; i < 12; i++) {
            double radius = 1.0 + (tentacleTimer / 20.0) * 1.5; // 触手逐渐伸长
            double angle = (i * 30 + tentacleTimer * 10) * Math.PI / 180.0;
            
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = (this.random.nextDouble() - 0.5) * 2.0;

            // 触手主体粒子
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    hitPosition.x + offsetX,
                    hitPosition.y + offsetY,
                    hitPosition.z + offsetZ,
                    0, 0.05, 0);

            // 触手尖端粒子
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        hitPosition.x + offsetX * 1.2,
                        hitPosition.y + offsetY * 1.2,
                        hitPosition.z + offsetZ * 1.2,
                        0, 0.03, 0);
            }

            // 触手挥舞轨迹粒子
            if (i % 4 == 0) {
                this.level().addParticle(ParticleTypes.SOUL,
                        hitPosition.x + offsetX * 0.8,
                        hitPosition.y + offsetY * 0.8,
                        hitPosition.z + offsetZ * 0.8,
                        0, 0.02, 0);
            }
        }

        // 如果命中目标，在目标周围生成触手缠绕粒子
        if (hitTarget != null) {
            for (int i = 0; i < 8; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * hitTarget.getBbWidth() * 1.5;
                double offsetY = (this.random.nextDouble() - 0.5) * hitTarget.getBbHeight();
                double offsetZ = (this.random.nextDouble() - 0.5) * hitTarget.getBbWidth() * 1.5;

                this.level().addParticle(ParticleTypes.SMOKE,
                        hitTarget.getX() + offsetX,
                        hitTarget.getY() + offsetY,
                        hitTarget.getZ() + offsetZ,
                        0, 0.1, 0);

                // 触手缠绕特效
                if (i % 2 == 0) {
                    this.level().addParticle(ParticleTypes.SOUL,
                            hitTarget.getX() + offsetX * 0.7,
                            hitTarget.getY() + offsetY * 0.7,
                            hitTarget.getZ() + offsetZ * 0.7,
                            0, 0.05, 0);
                }
            }
        }
    }

    /**
     * 触手造成二次伤害
     */
    private void dealTentacleDamage() {
        if (hitTarget != null && hitTarget.isAlive()) {
            // 计算触手伤害（基础伤害的0.5倍）
            float tentacleDamage = getShadowSpearDamage() * 0.5f;
            
            // 对目标造成触手伤害
            hitTarget.hurt(this.damageSources().indirectMagic(this, shooter), tentacleDamage);
            
            // 播放触手伤害音效
            this.level().playSound(null, hitPosition.x, hitPosition.y, hitPosition.z,
                    SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.8F, 0.8F);
        }

        // 对周围敌人造成范围触手伤害
        AABB area = new AABB(hitPosition.x - 2.5, hitPosition.y - 2.5, hitPosition.z - 2.5,
                            hitPosition.x + 2.5, hitPosition.y + 2.5, hitPosition.z + 2.5);
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, area);
        
        for (LivingEntity entity : nearbyEntities) {
            if (entity != hitTarget && entity != shooter && entity.isAlive()) {
                // 对周围敌人造成较小触手伤害
                float areaDamage = getShadowSpearDamage() * 0.3f;
                entity.hurt(this.damageSources().indirectMagic(this, shooter), areaDamage);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide() && !hasHit) {
            hasHit = true;
            hitPosition = new Vec3(this.getX(), this.getY(), this.getZ());
            
            // 停止移动
            this.setDeltaMovement(Vec3.ZERO);
            
            // 播放击中音效
            this.level().playSound(null, hitPosition.x, hitPosition.y, hitPosition.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.6F);
            
            // 检查是否命中实体
            if (result.getType() == HitResult.Type.ENTITY) {
                Entity hitEntity = ((net.minecraft.world.phys.EntityHitResult) result).getEntity();
                if (hitEntity instanceof LivingEntity) {
                    hitTarget = (LivingEntity) hitEntity;
                    
                    // 立即造成首次伤害
                    hitTarget.hurt(this.damageSources().indirectMagic(this, shooter), getShadowSpearDamage());
                }
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lifeTime = compound.getInt("LifeTime");
        this.hasHit = compound.getBoolean("HasHit");
        this.tentacleTimer = compound.getInt("TentacleTimer");
        this.isSummonedSpear = compound.getBoolean("IsSummonedSpear");
        if (compound.contains("ShadowSpearDamage")) {
            this.entityData.set(DATA_DAMAGE, compound.getFloat("ShadowSpearDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putBoolean("HasHit", this.hasHit);
        compound.putInt("TentacleTimer", this.tentacleTimer);
        compound.putBoolean("IsSummonedSpear", this.isSummonedSpear);
        compound.putFloat("ShadowSpearDamage", getShadowSpearDamage());
    }
}