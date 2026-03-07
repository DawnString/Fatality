package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.LightDarkTrackingProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.List;

public class BookOfLightAndDarkness extends BaseWeapon
{
    // 武器参数
    private static final float MANA_COST = 30.0f; // 每次施法消耗30点魔法值
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_MAGIC_DAMAGE = 375.0f; // 基础魔法伤害375
    private static final int SPHERE_LIFETIME = 200; // 球体存在时间200tick（10秒）
    private static final double TARGET_RANGE = 15.0; // 检测目标的最大距离
    private static final double SPHERE_RADIUS = 1.5; // 球体环绕半径
    
    // 存储当前存在的球体
    private final List<LightDarkSphere> activeSpheres = new ArrayList<>();
    
    // 攻击计时器
    private int attackTimer = 0;
    
    public BookOfLightAndDarkness() {
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
                return 0; // 法书本身没有攻击伤害加成
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
        }, new Properties(), 0, 1.0f, 1f, 0.15f, 15.0f, 0.4f, WeaponEnum.MAGIC);
        
        this.setStory("光暗之书，神秘的魔法武器。右键召唤光暗双球，光球与暗球环绕玩家旋转并交替发射追踪粒子攻击敌人。球体存在10秒，消耗30点魔法值。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查玩家是否有足够的魔法值
        if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
            // 如果魔法值不足，提示玩家
            if (level.isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST + "点魔法值"), true);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        if (!level.isClientSide()) {
            // 检查是否已经有球体存在
            if (!activeSpheres.isEmpty()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c光暗双球已经存在！"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 创建光暗双球
            createLightDarkSpheres(level, player);

            // 播放施法音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 创建光暗双球
     */
    private void createLightDarkSpheres(Level level, Player player) {
        // 创建光球（白色）
        LightDarkSphere lightSphere = new LightDarkSphere(level, player, true);
        // 创建暗球（黑色）
        LightDarkSphere darkSphere = new LightDarkSphere(level, player, false);
        
        activeSpheres.add(lightSphere);
        activeSpheres.add(darkSphere);
    }

    /**
     * 每tick更新球体状态
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        if (!level.isClientSide() && entity instanceof Player player) {
            // 更新球体位置和攻击逻辑
            updateSpheres(level, player);
            
            // 更新攻击计时器
            attackTimer++;
            if (attackTimer >= 20) { // 每1秒攻击一次
                attackTimer = 0;
                
                // 交替攻击
                if (!activeSpheres.isEmpty()) {
                    for (int i = 0; i < activeSpheres.size(); i++) {
                        LightDarkSphere sphere = activeSpheres.get(i);
                        if (sphere.canAttack()) {
                            // 交替攻击：偶数索引的球体先攻击，奇数索引的后攻击
                            if ((i % 2 == 0 && attackTimer % 2 == 0) || (i % 2 == 1 && attackTimer % 2 == 1)) {
                                performSphereAttack(level, player, sphere);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 更新球体状态
     */
    private void updateSpheres(Level level, Player player) {
        // 移除过期的球体
        activeSpheres.removeIf(sphere -> sphere.isExpired());
        
        // 更新球体位置
        for (LightDarkSphere sphere : activeSpheres) {
            sphere.updatePosition(player);
            
            // 在球体位置生成粒子效果
            spawnSphereParticles(level, sphere);
        }
    }

    /**
     * 执行球体攻击
     */
    private void performSphereAttack(Level level, Player player, LightDarkSphere sphere) {
        // 寻找最近的敌人
        LivingEntity target = findNearestTarget(level, player, sphere.getPosition());
        
        if (target != null) {
            // 计算伤害
            float damage = calculateSphereDamage(player);
            
            // 发射追踪粒子
            shootTrackingParticle(level, sphere.getPosition(), target, damage, sphere.isLightSphere(), player);
            
            // 播放攻击音效
            level.playSound(null, sphere.getPosition().x, sphere.getPosition().y, sphere.getPosition().z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.5F);
        }
    }

    /**
     * 寻找最近的敌人
     */
    private LivingEntity findNearestTarget(Level level, Player player, Vec3 spherePos) {
        // 搜索范围内的敌人
        AABB searchArea = new AABB(
                spherePos.x - TARGET_RANGE, spherePos.y - TARGET_RANGE, spherePos.z - TARGET_RANGE,
                spherePos.x + TARGET_RANGE, spherePos.y + TARGET_RANGE, spherePos.z + TARGET_RANGE
        );
        
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea, 
                entity -> entity != player && entity.isAlive() && !entity.isInvulnerable());
        
        if (entities.isEmpty()) {
            return null;
        }
        
        // 找到最近的敌人
        LivingEntity nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : entities) {
            double distance = entity.distanceToSqr(spherePos.x, spherePos.y, spherePos.z);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = entity;
            }
        }
        
        return nearest;
    }

    /**
     * 计算球体伤害
     */
    private float calculateSphereDamage(Player player) {
        // 使用BaseWeapon的伤害计算逻辑
        float baseDamage = BASE_MAGIC_DAMAGE;

        // 计算基础伤害加成（基于饰品）
        float accessoryBaseBonus = calculateAccessoryBaseBonus(player);

        // 计算其他伤害加成（饰品、药水等）
        float otherBonus = calculateOtherBonus(player);

        // 计算伤害浮动值
        float fluctuation = calculateDamageFluctuation();

        // 判断是否暴击
        boolean isCritical = isCriticalHit(player);

        float finalDamage;
        if (isCritical) {
            // 暴击伤害公式（与BaseWeapon保持一致）
            float criticalBonus = getCriticalDamageMultiplier(player);
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            // 普通伤害公式（与BaseWeapon保持一致）
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }

        return Math.max(0, finalDamage);
    }

    /**
     * 发射追踪粒子
     */
    private void shootTrackingParticle(Level level, Vec3 startPos, LivingEntity target, float damage, boolean isLight, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        
        // 创建光暗追踪投射物
        LightDarkTrackingProjectile projectile = new LightDarkTrackingProjectile(
                serverLevel, player, target, damage, isLight
        );
        
        // 设置投射物位置和初始速度
        projectile.setPos(startPos.x, startPos.y, startPos.z);
        
        // 计算初始方向（朝向目标）
        Vec3 direction = target.position().subtract(startPos).normalize();
        projectile.setDeltaMovement(direction.scale(0.8)); // 初始速度
        
        // 添加到世界
        serverLevel.addFreshEntity(projectile);
    }

    /**
     * 生成追踪粒子轨迹
     */
    private void spawnTrackingParticleTrail(Level level, Vec3 startPos, Vec3 endPos, boolean isLight) {
        if (level.isClientSide()) {
            // 计算轨迹方向
            Vec3 direction = endPos.subtract(startPos).normalize();
            double distance = startPos.distanceTo(endPos);
            
            // 生成粒子轨迹
            for (int i = 0; i < 10; i++) {
                double progress = (double) i / 9;
                Vec3 particlePos = startPos.add(direction.scale(distance * progress));
                
                // 根据球体类型生成不同颜色的粒子
                if (isLight) {
                    // 光球：白色粒子
                    level.addParticle(ParticleTypes.END_ROD, 
                            particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
                } else {
                    // 暗球：黑色粒子
                    level.addParticle(ParticleTypes.SMOKE, 
                            particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
                }
            }
        }
    }

    /**
     * 生成球体粒子效果
     */
    private void spawnSphereParticles(Level level, LightDarkSphere sphere) {
        if (level.isClientSide()) {
            Vec3 pos = sphere.getPosition();
            
            // 根据球体类型生成不同粒子
            if (sphere.isLightSphere()) {
                // 光球：更明亮、更明显的白色粒子
                for (int i = 0; i < 8; i++) { // 增加粒子数量
                    double offsetX = (Math.random() - 0.5) * 1.0; // 增加粒子散布范围
                    double offsetY = (Math.random() - 0.5) * 1.0;
                    double offsetZ = (Math.random() - 0.5) * 1.0;
                    
                    // 使用更明显的粒子类型
                    level.addParticle(ParticleTypes.GLOW, 
                            pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 
                            0, 0.05, 0); // 轻微向上飘动
                    
                    // 添加额外的闪光粒子
                    if (i % 3 == 0) {
                        level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                                pos.x + offsetX * 0.5, pos.y + offsetY * 0.5, pos.z + offsetZ * 0.5,
                                0, 0.1, 0);
                    }
                }
            } else {
                // 暗球：更明显的黑色粒子
                for (int i = 0; i < 8; i++) { // 增加粒子数量
                    double offsetX = (Math.random() - 0.5) * 1.0; // 增加粒子散布范围
                    double offsetY = (Math.random() - 0.5) * 1.0;
                    double offsetZ = (Math.random() - 0.5) * 1.0;
                    
                    // 使用更明显的粒子类型
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, 
                            pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 
                            0, 0.02, 0); // 轻微向上飘动
                    
                    // 添加额外的烟雾粒子
                    if (i % 3 == 0) {
                        level.addParticle(ParticleTypes.LARGE_SMOKE,
                                pos.x + offsetX * 0.5, pos.y + offsetY * 0.5, pos.z + offsetZ * 0.5,
                                0, 0.05, 0);
                    }
                }
            }
        }
    }

    /**
     * 光暗球体内部类
     */
    private class LightDarkSphere {
        private final Level level;
        private final Player owner;
        private final boolean isLightSphere;
        private final long creationTime;
        private Vec3 position;
        private double angle;
        
        public LightDarkSphere(Level level, Player owner, boolean isLightSphere) {
            this.level = level;
            this.owner = owner;
            this.isLightSphere = isLightSphere;
            this.creationTime = level.getGameTime();
            this.angle = isLightSphere ? 0 : Math.PI; // 光球和暗球初始位置相对
        }
        
        public void updatePosition(Player player) {
            // 更新角度（旋转）
            angle += 0.1; // 旋转速度
            if (angle > 2 * Math.PI) {
                angle -= 2 * Math.PI;
            }
            
            // 计算球体位置（环绕玩家）
            double x = player.getX() + Math.cos(angle) * SPHERE_RADIUS;
            double y = player.getY() + player.getEyeHeight() + Math.sin(angle) * 0.5;
            double z = player.getZ() + Math.sin(angle) * SPHERE_RADIUS;
            
            this.position = new Vec3(x, y, z);
        }
        
        public Vec3 getPosition() {
            return position;
        }
        
        public boolean isLightSphere() {
            return isLightSphere;
        }
        
        public boolean canAttack() {
            return !isExpired();
        }
        
        public boolean isExpired() {
            return level.getGameTime() - creationTime > SPHERE_LIFETIME;
        }
    }

    /**
     * 获取武器特殊效果描述
     */
    @Override
    public String getSpecialEffectDescription() {
        return super.getSpecialEffectDescription() + " | 光暗双球：召唤光球与暗球环绕攻击，375基础伤害，10秒持续时间";
    }

    /**
     * 获取攻击距离（光暗之书有较长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return TARGET_RANGE;
    }
}