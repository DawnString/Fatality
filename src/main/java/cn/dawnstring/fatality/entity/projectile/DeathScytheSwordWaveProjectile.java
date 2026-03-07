package cn.dawnstring.fatality.entity.projectile;

import cn.dawnstring.fatality.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

/**
 * 死亡镰刀剑气投射物 - 左键挥舞释放的剑气
 * 特性：竖着的绿色粒子剑气，快速飞行，能够穿透敌人造成伤害
 */
public class DeathScytheSwordWaveProjectile extends AbstractArrow {
    
    private static final int MAX_LIFE_TIME = 80; // 最大生命周期80tick（4秒）
    private static final float SWORD_WAVE_DAMAGE = 1.0f; // 剑气伤害倍率
    private static final float PIERCING_DAMAGE_REDUCTION = 0.7f; // 穿透伤害衰减
    private static final int MAX_PIERCED_ENTITIES = 3; // 最大穿透敌人数量
    
    private int lifeTime = 0;
    private float swordWaveDamage;
    private LivingEntity shooter;
    private int piercedEntities = 0;
    
    public DeathScytheSwordWaveProjectile(EntityType<? extends DeathScytheSwordWaveProjectile> type, Level level) {
        super(type, level);
    }
    
    public DeathScytheSwordWaveProjectile(Level level, LivingEntity shooter, float swordWaveDamage) {
        super(ModEntities.DEATH_SCYTHE_SWORD_WAVE_PROJECTILE.get(), shooter, level);
        this.shooter = shooter;
        this.swordWaveDamage = swordWaveDamage;
        
        // 设置投射物属性
        this.setNoGravity(true); // 无视重力
        this.setBaseDamage(0); // 基础伤害设为0，使用自定义伤害计算
        this.setPierceLevel((byte) MAX_PIERCED_ENTITIES); // 穿透等级
        this.setKnockback(0); // 无击退效果
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;
        
        // 在飞行过程中生成剑气粒子
        if (this.level().isClientSide()) {
            spawnFlightParticles();
        }
        
        // 检查生命周期
        if (lifeTime > MAX_LIFE_TIME) {
            if (!this.level().isClientSide()) {
                // 生命周期结束时播放消失音效
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            this.discard();
        }
    }
    
    /**
     * 生成飞行粒子效果（竖着的绿色剑气）
     */
    private void spawnFlightParticles() {
        Vec3 pos = this.position();
        Vec3 motion = this.getDeltaMovement();
        
        // 生成竖着的绿色剑气粒子
        for (int i = 0; i < 5; i++) {
            // 竖着的粒子排列（垂直于运动方向）
            double verticalOffset = (Math.random() - 0.5) * 2.0; // 垂直方向偏移
            double horizontalOffset = (Math.random() - 0.5) * 0.5; // 水平方向偏移
            
            // 计算垂直于运动方向的向量
            Vec3 perpendicular = new Vec3(-motion.z, 0, motion.x).normalize();
            
            double particleX = pos.x + perpendicular.x * horizontalOffset;
            double particleY = pos.y + verticalOffset;
            double particleZ = pos.z + perpendicular.z * horizontalOffset;
            
            // 绿色粒子效果（死亡主题）
            Vector3f color = new Vector3f(0.1f, 0.8f, 0.1f); // 深绿色
            DustParticleOptions particle = new DustParticleOptions(color, 2.0f);
            
            this.level().addParticle(particle,
                    particleX, particleY, particleZ,
                    motion.x * -0.1,
                    motion.y * -0.1,
                    motion.z * -0.1);
        }
        
        // 生成剑气轨迹粒子
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x, pos.y, pos.z,
                    motion.x * -0.2,
                    motion.y * -0.2,
                    motion.z * -0.2);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        
        if (entity instanceof LivingEntity livingEntity && entity != this.shooter) {
            // 计算伤害（考虑穿透衰减）
            float damage = swordWaveDamage * SWORD_WAVE_DAMAGE * (float) Math.pow(PIERCING_DAMAGE_REDUCTION, piercedEntities);
            
            if (damage > 0) {
                // 应用伤害
                livingEntity.hurt(livingEntity.damageSources().indirectMagic(this, this.shooter), damage);
                
                // 播放命中音效
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.5F, 0.8F);
                
                // 生成命中粒子效果
                if (this.level().isClientSide()) {
                    spawnHitParticles(result.getLocation());
                }
                
                piercedEntities++;
                
                // 检查是否达到最大穿透数量
                if (piercedEntities >= MAX_PIERCED_ENTITIES) {
                    this.discard();
                }
            }
        }
    }
    
    /**
     * 生成命中粒子效果
     */
    private void spawnHitParticles(Vec3 hitPos) {
        for (int i = 0; i < 10; i++) {
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetY = (Math.random() - 0.5) * 0.8;
            double offsetZ = (Math.random() - 0.5) * 0.8;
            
            // 绿色粒子效果
            Vector3f color = new Vector3f(0.1f, 0.8f, 0.1f);
            DustParticleOptions particle = new DustParticleOptions(color, 1.5f);
            
            this.level().addParticle(particle,
                    hitPos.x + offsetX,
                    hitPos.y + offsetY,
                    hitPos.z + offsetZ,
                    0, 0.1, 0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // 不调用super.onHit(result)，避免默认的碰撞处理逻辑
        
        if (!this.level().isClientSide()) {
            // 播放碰撞音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.5F, 1.0F);
            
            this.discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 不允许拾取
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.lifeTime = compound.getInt("LifeTime");
        this.swordWaveDamage = compound.getFloat("SwordWaveDamage");
        this.piercedEntities = compound.getInt("PiercedEntities");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LifeTime", this.lifeTime);
        compound.putFloat("SwordWaveDamage", this.swordWaveDamage);
        compound.putInt("PiercedEntities", this.piercedEntities);
    }
}