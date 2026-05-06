package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
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

import java.util.List;

/**
 * 灰烬 - 高精度狙击枪
 * 特性：击中后子弹散射，对目标后方的锥形区域造成伤害，伤害随距离递减
 */
public class Ashes extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 1; // 冷却时间0.05秒（1tick）
    private static final float BASE_BULLET_DAMAGE = 24.0f; // 基础子弹伤害
    private static final float CONE_ANGLE = 45.0f; // 锥形散射角度
    private static final float MAX_RANGE = 20.0f; // 最大散射范围

    public Ashes()
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
        }, new Properties(), (int)BASE_BULLET_DAMAGE, 0.05f, 1f, 0.14f, 0.15f, 0.2f, WeaponEnum.RANGED);
        
        setStory("一把精准的狙击枪，击中目标后子弹会散射，对目标后方的敌人造成锥形区域伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 创建主子弹
            BulletProjectile bullet = new BulletProjectile(level, player, itemstack, calculateBulletDamage(player, itemstack));
            
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();
            
            bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            bullet.shoot(lookVec.x, lookVec.y, lookVec.z, 6.0F, 0.01F); // 高速度，低散布
            
            level.addFreshEntity(bullet);
            
            // 播放狙击枪射击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.2F, 0.6F);
        }

        // 在客户端生成粒子效果
        if (level.isClientSide()) {
            spawnSniperParticles(level, player);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 生成狙击枪粒子效果
     */
    private void spawnSniperParticles(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();
        
        // 枪口位置
        Vec3 muzzlePos = eyePos.add(lookVec.scale(1.5));
        
        // 生成狙击枪特有的粒子效果
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;
            
            level.addParticle(ParticleTypes.CRIT,
                    muzzlePos.x + offsetX,
                    muzzlePos.y + offsetY,
                    muzzlePos.z + offsetZ,
                    lookVec.x * 0.5, lookVec.y * 0.5, lookVec.z * 0.5);
        }
    }

    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 子弹击中目标后的散射效果
     */
    public static void onBulletHit(LivingEntity target, Vec3 hitPos, Level level, Player player, float baseDamage) {
        if (!level.isClientSide()) {
            // 计算锥形区域 - 从击中位置向目标后方延伸
            Vec3 playerLookVec = player.getLookAngle();
            Vec3 coneDirection = playerLookVec.scale(-1.0); // 反向，指向目标后方
            Vec3 coneStart = hitPos;
            
            // 对锥形区域内的实体造成伤害
            List<Entity> entitiesInCone = level.getEntities(target, 
                    new AABB(coneStart.x - MAX_RANGE, coneStart.y - MAX_RANGE, coneStart.z - MAX_RANGE,
                            coneStart.x + MAX_RANGE, coneStart.y + MAX_RANGE, coneStart.z + MAX_RANGE));
            
            for (Entity entity : entitiesInCone) {
                if (entity instanceof LivingEntity livingEntity && entity != target) {
                    // 检查是否在锥形区域内
                    if (isInConeArea(hitPos, coneDirection, entity.position(), CONE_ANGLE, MAX_RANGE)) {
                        // 计算距离衰减伤害
                        double distance = hitPos.distanceTo(entity.position());
                        float distanceMultiplier = (float) Math.max(0.1, 1.0 - (distance / MAX_RANGE));
                        float scatterDamage = baseDamage * 0.3f * distanceMultiplier; // 散射伤害为基础伤害的30%
                        
                        livingEntity.hurt(livingEntity.damageSources().playerAttack(player), scatterDamage);
                        
                        // 生成灰色粒子效果
                        spawnGrayParticles(level, hitPos, entity.position());
                    }
                }
            }
            
            // 播放散射音效
            level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    /**
     * 检查实体是否在锥形区域内
     */
    private static boolean isInConeArea(Vec3 coneOrigin, Vec3 coneDirection, Vec3 entityPos, float angle, float maxRange) {
        Vec3 toEntity = entityPos.subtract(coneOrigin);
        double distance = toEntity.length();
        
        if (distance > maxRange) return false;
        
        // 计算角度
        double dotProduct = coneDirection.dot(toEntity.normalize());
        double entityAngle = Math.acos(dotProduct);
        
        return Math.toDegrees(entityAngle) <= angle / 2.0;
    }

    /**
     * 生成灰色粒子效果
     */
    private static void spawnGrayParticles(Level level, Vec3 fromPos, Vec3 toPos) {
        if (level.isClientSide()) {
            // 生成灰色粒子效果（使用SMOKE粒子）
            for (int i = 0; i < 15; i++) {
                double progress = Math.random();
                double x = fromPos.x + (toPos.x - fromPos.x) * progress;
                double y = fromPos.y + (toPos.y - fromPos.y) * progress;
                double z = fromPos.z + (toPos.z - fromPos.z) * progress;
                
                level.addParticle(ParticleTypes.SMOKE,
                        x, y, z,
                        (Math.random() - 0.5) * 0.2,
                        Math.random() * 0.1,
                        (Math.random() - 0.5) * 0.2);
            }
        }
    }
}