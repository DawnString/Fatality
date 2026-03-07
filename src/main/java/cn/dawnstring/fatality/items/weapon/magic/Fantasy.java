package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * Fantasy - 魔法武器
 * 特性：自动追踪目标，四种粒子组合模式，按Shift键切换
 * 模式1：深蓝火焰+炽白粒子 - 目标禁锢+灼烧效果
 * 模式2：白色粒子+黑色粒子 - 太极图案+伤害提升20%
 * 模式3：红色粒子+绿色粒子 - 双倍伤害
 * 模式4：黄色粒子+紫色粒子 - 爆炸效果
 * 属性：伤害920，暴击率30%，暴击伤害30%，浮动0.3，攻击速度0.25s
 */
public class Fantasy extends BaseWeapon {
    
    // 武器属性
    private static final int BASE_DAMAGE = 920;
    private static final float ATTACK_SPEED = 0.25f; // 0.25秒攻击速度
    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f;
    private static final float CRIT_RATE = 0.30f;
    private static final float CRIT_DAMAGE = 0.30f;
    private static final float DAMAGE_VARIATION = 0.3f;
    
    // 粒子组合类型
    private int currentMode = 0;
    private static final int MODE_COUNT = 4;
    
    // 追踪范围
    private static final double TRACKING_RANGE = 20.0;
    
    public Fantasy() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
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
                return 4; // 钻石级
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不可修复
            }
        }, new Properties(), BASE_DAMAGE, ATTACK_SPEED, BASE_DAMAGE_MULTIPLIER, 
           CRIT_RATE, CRIT_DAMAGE, DAMAGE_VARIATION, WeaponEnum.MAGIC);
        
        // 设置武器故事
        setStory("幻 - 神秘的魔法武器，能够操控四种不同的元素力量。\n" +
                "自动追踪目标并释放粒子攻击，按Shift键切换四种不同的攻击模式。\n" +
                "每种模式都有独特的粒子效果和特殊能力。");
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        if (!level.isClientSide() || !(entity instanceof Player player) || !isSelected) {
            return;
        }
        
        // 检测Shift键切换模式
        if (player.isShiftKeyDown()) {
            if (player.getPersistentData().getLong("fantasyModeSwitchCooldown") < level.getGameTime()) {
                currentMode = (currentMode + 1) % MODE_COUNT;
                player.getPersistentData().putLong("fantasyModeSwitchCooldown", level.getGameTime() + 10); // 0.5秒冷却
                
                // 播放切换音效
                level.playSound(player, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.0f);
            }
        }
        
        // 生成环绕粒子
        generateOrbitingParticles(level, player);
        
        // 寻找并追踪目标
        trackAndAttackTarget(level, player);
    }
    
    private void generateOrbitingParticles(Level level, Player player) {
        // 只在客户端生成粒子
        if (!level.isClientSide()) return;
        
        double time = level.getGameTime() * 0.1;
        double radius = 1.5;
        
        // 根据当前模式选择粒子颜色
        ParticleOptions leftParticle = getLeftParticleForMode();
        ParticleOptions rightParticle = getRightParticleForMode();
        
        // 左侧粒子轨道
        double leftX = player.getX() + Math.cos(time) * radius;
        double leftZ = player.getZ() + Math.sin(time) * radius;
        
        // 右侧粒子轨道
        double rightX = player.getX() + Math.cos(time + Math.PI) * radius;
        double rightZ = player.getZ() + Math.sin(time + Math.PI) * radius;
        
        // 生成粒子
        level.addParticle(leftParticle, leftX, player.getY() + 1.5, leftZ, 0, 0, 0);
        level.addParticle(rightParticle, rightX, player.getY() + 1.5, rightZ, 0, 0, 0);
    }
    
    private void trackAndAttackTarget(Level level, Player player) {
        // 寻找最近的敌对目标
        LivingEntity target = findNearestTarget(level, player);
        if (target == null) return;
        
        // 生成追踪粒子
        generateTrackingParticles(level, player, target);
        
        // 每20tick攻击一次（1秒）
        if (level.getGameTime() % 20 == 0) {
            attackTarget(player, target);
        }
    }
    
    private LivingEntity findNearestTarget(Level level, Player player) {
        AABB searchArea = new AABB(
            player.getX() - TRACKING_RANGE, player.getY() - TRACKING_RANGE, player.getZ() - TRACKING_RANGE,
            player.getX() + TRACKING_RANGE, player.getY() + TRACKING_RANGE, player.getZ() + TRACKING_RANGE
        );
        
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea, 
            entity -> entity != player && entity.isAlive() && !entity.isInvulnerable());
        
        if (entities.isEmpty()) return null;
        
        // 返回最近的实体
        return entities.stream()
            .min((e1, e2) -> Double.compare(
                e1.distanceToSqr(player), 
                e2.distanceToSqr(player)
            ))
            .orElse(null);
    }
    
    private void generateTrackingParticles(Level level, Player player, LivingEntity target) {
        // 只在客户端生成粒子
        if (!level.isClientSide()) return;
        
        ParticleOptions leftParticle = getLeftParticleForMode();
        ParticleOptions rightParticle = getRightParticleForMode();
        
        // 从左右轨道位置向目标发射粒子
        double time = level.getGameTime() * 0.1;
        double radius = 1.5;
        
        Vec3 leftOrbit = new Vec3(
            player.getX() + Math.cos(time) * radius,
            player.getY() + 1.5,
            player.getZ() + Math.sin(time) * radius
        );
        
        Vec3 rightOrbit = new Vec3(
            player.getX() + Math.cos(time + Math.PI) * radius,
            player.getY() + 1.5,
            player.getZ() + Math.sin(time + Math.PI) * radius
        );
        
        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);
        
        // 计算粒子方向
        Vec3 leftDir = targetPos.subtract(leftOrbit).normalize().scale(0.1);
        Vec3 rightDir = targetPos.subtract(rightOrbit).normalize().scale(0.1);
        
        // 生成追踪粒子
        level.addParticle(leftParticle, leftOrbit.x, leftOrbit.y, leftOrbit.z, 
            leftDir.x, leftDir.y, leftDir.z);
        level.addParticle(rightParticle, rightOrbit.x, rightOrbit.y, rightOrbit.z, 
            rightDir.x, rightDir.y, rightDir.z);
    }
    
    private void attackTarget(Player player, LivingEntity target) {
        if (player.level().isClientSide()) return;
        
        // 使用BaseWeapon的标准伤害计算方法
        float baseDamage = calculateFinalDamage(player, null, target);
        
        // 根据模式调整伤害
        float finalDamage = applyModeDamageMultiplier(baseDamage);
        
        // 应用模式特效
        applyModeEffect(player, target);
        
        // 造成伤害
        target.hurt(player.damageSources().magic(), finalDamage);
        
        // 播放攻击音效
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
    
    /**
     * 根据当前模式应用伤害倍率
     */
    private float applyModeDamageMultiplier(float baseDamage) {
        switch (currentMode) {
            case 1: // 模式2：伤害提升20%
                return baseDamage * 1.2f;
            case 2: // 模式3：双倍伤害
                return baseDamage * 2.0f;
            default: // 模式1和模式4：基础伤害
                return baseDamage;
        }
    }
    
    private void applyModeEffect(Player player, LivingEntity target) {
        Level level = player.level();
        
        switch (currentMode) {
            case 0: // 模式1：深蓝火焰 + 炽白粒子
                // 无法移动效果（除了玩家与boss）
                if (!(target instanceof Player) && !isBoss(target)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 255)); // 10秒无法移动
                }
                // 灼烧效果
                target.setSecondsOnFire(10);
                break;
                
            case 1: // 模式2：白色粒子 + 黑色粒子
                // 太极图案效果 - 伤害提升20%
                target.getPersistentData().putLong("fantasyTaiChiEffect", level.getGameTime() + 200);
                // 在目标脚下生成太极粒子效果
                generateTaiChiParticles(level, target);
                break;
                
            case 2: // 模式3：红色粒子 + 绿色粒子
                // 双倍伤害已在attackTarget中计算
                break;
                
            case 3: // 模式4：黄色粒子 + 紫色粒子
                // 爆炸效果
                level.explode(player, target.getX(), target.getY(), target.getZ(), 2.0f, 
                    Level.ExplosionInteraction.NONE);
                break;
        }
    }
    
    private boolean isBoss(LivingEntity entity) {
        // 简单的boss判断逻辑
        return entity.getMaxHealth() > 100; // 血量超过100的认为是boss
    }
    
    private void generateTaiChiParticles(Level level, LivingEntity target) {
        // 只在客户端生成粒子
        if (!level.isClientSide()) return;
        
        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();
        
        // 生成太极图案粒子
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = centerX + Math.cos(angle) * 1.0;
            double z = centerZ + Math.sin(angle) * 1.0;
            
            // 黑白交替粒子
            ParticleOptions particle = (i % 20 == 0) ? ParticleTypes.WHITE_ASH : ParticleTypes.SMOKE;
            level.addParticle(particle, x, centerY, z, 0, 0.1, 0);
        }
    }
    
    private ParticleOptions getLeftParticleForMode() {
        switch (currentMode) {
            case 0: return ParticleTypes.SOUL_FIRE_FLAME; // 深蓝色火焰
            case 1: return ParticleTypes.WHITE_ASH; // 白色粒子
            case 2: return new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f); // 红色粒子
            case 3: return new DustParticleOptions(new Vector3f(1.0f, 1.0f, 0.0f), 1.0f); // 黄色粒子
            default: return ParticleTypes.FLAME;
        }
    }
    
    private ParticleOptions getRightParticleForMode() {
        switch (currentMode) {
            case 0: return ParticleTypes.GLOW; // 炽白色粒子
            case 1: return ParticleTypes.SMOKE; // 黑色粒子
            case 2: return new DustParticleOptions(new Vector3f(0.0f, 1.0f, 0.0f), 1.0f); // 绿色粒子
            case 3: return new DustParticleOptions(new Vector3f(1.0f, 0.0f, 1.0f), 1.0f); // 紫色粒子
            default: return ParticleTypes.FLAME;
        }
    }
    
    @Override
    public String getSpecialEffectDescription() {
        String[] modeDescriptions = {
            "深蓝火焰+炽白粒子：目标无法移动+灼烧效果",
            "白色粒子+黑色粒子：太极图案+伤害提升20%",
            "红色粒子+绿色粒子：双倍伤害",
            "黄色粒子+紫色粒子：爆炸效果"
        };
        
        return "幻 - 魔法武器 | 当前模式：" + modeDescriptions[currentMode] + 
               " | 按Shift切换模式 | 右键释放特殊攻击 | 伤害：" + BASE_DAMAGE + " | 暴击率：" + (CRIT_RATE * 100) + "%";
    }
    
    /**
     * 右键攻击功能 - 根据当前模式释放特殊攻击
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 检查冷却时间
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }
        
        // 只在服务器端执行
        if (!level.isClientSide()) {
            // 根据当前模式释放不同的特殊攻击
            switch (currentMode) {
                case 0:
                    releaseFrozenFlameAttack(level, player);
                    break;
                case 1:
                    releaseTaiChiExplosion(level, player);
                    break;
                case 2:
                    releaseBloodThornBarrage(level, player);
                    break;
                case 3:
                    releaseChaosNova(level, player);
                    break;
            }
            
            // 设置冷却时间（3秒）
            player.getCooldowns().addCooldown(this, 60);
            
            // 播放特殊攻击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    /**
     * 模式0特殊攻击：冰封火焰
     * 释放一圈冰封火焰，对周围敌人造成冰冻和灼烧双重效果
     */
    private void releaseFrozenFlameAttack(Level level, Player player) {
        double radius = 8.0;
        int particleCount = 36;
        
        for (int i = 0; i < particleCount; i++) {
            double angle = (i * Math.PI * 2) / particleCount;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY() + 1.0;
            
            // 在服务器端对范围内的敌人造成效果
            if (!level.isClientSide()) {
                AABB effectArea = new AABB(x - 2, y - 2, z - 2, x + 2, y + 2, z + 2);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, effectArea, 
                    entity -> entity != player && entity.isAlive());
                
                for (LivingEntity target : targets) {
                    // 冰冻效果
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    // 灼烧效果
                    target.setSecondsOnFire(5);
                    // 造成伤害
                    target.hurt(player.damageSources().magic(), BASE_DAMAGE * 0.5f);
                }
            }
            
            // 在客户端生成粒子效果
            if (level.isClientSide()) {
                // 冰封火焰粒子
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.1, 0);
                level.addParticle(ParticleTypes.ITEM_SNOWBALL, x, y + 0.5, z, 0, 0.05, 0);
            }
        }
    }
    
    /**
     * 模式1特殊攻击：太极爆炸
     * 在玩家位置生成太极图案，对范围内的敌人造成持续伤害
     */
    private void releaseTaiChiExplosion(Level level, Player player) {
        double radius = 6.0;
        
        // 在服务器端对范围内的敌人造成效果
        if (!level.isClientSide()) {
            AABB effectArea = new AABB(
                player.getX() - radius, player.getY() - 2, player.getZ() - radius,
                player.getX() + radius, player.getY() + 4, player.getZ() + radius
            );
            
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, effectArea, 
                entity -> entity != player && entity.isAlive());
            
            for (LivingEntity target : targets) {
                // 太极标记效果（持续5秒）
                target.getPersistentData().putLong("fantasyTaiChiMark", level.getGameTime() + 100);
                // 造成初始伤害
                target.hurt(player.damageSources().magic(), BASE_DAMAGE * 0.8f);
                // 虚弱效果
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            }
        }
        
        // 在客户端生成太极粒子效果
        if (level.isClientSide()) {
            for (int i = 0; i < 360; i += 15) {
                double angle = Math.toRadians(i);
                double x = player.getX() + Math.cos(angle) * radius;
                double z = player.getZ() + Math.sin(angle) * radius;
                
                // 黑白交替的太极粒子
                ParticleOptions particle = (i % 30 == 0) ? ParticleTypes.WHITE_ASH : ParticleTypes.SMOKE;
                level.addParticle(particle, x, player.getY() + 1.0, z, 0, 0.2, 0);
            }
        }
    }
    
    /**
     * 模式2特殊攻击：血荆棘弹幕
     * 向周围发射大量红色和绿色粒子，追踪多个目标
     */
    private void releaseBloodThornBarrage(Level level, Player player) {
        int barrageCount = 12;
        
        // 寻找多个目标
        List<LivingEntity> targets = findMultipleTargets(level, player, barrageCount);
        
        for (int i = 0; i < barrageCount; i++) {
            LivingEntity target = i < targets.size() ? targets.get(i) : null;
            
            if (target != null && !level.isClientSide()) {
                // 对目标造成双倍伤害
                target.hurt(player.damageSources().magic(), BASE_DAMAGE * 1.5f);
                // 流血效果
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
            }
            
            // 在客户端生成弹幕粒子
            if (level.isClientSide()) {
                double angle = (i * Math.PI * 2) / barrageCount;
                double startX = player.getX() + Math.cos(angle) * 2.0;
                double startZ = player.getZ() + Math.sin(angle) * 2.0;
                double startY = player.getY() + 1.5;
                
                // 红色血荆棘粒子
                level.addParticle(new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f),
                    startX, startY, startZ, 0, 0, 0);
                
                // 绿色荆棘粒子
                if (i % 2 == 0) {
                    level.addParticle(new DustParticleOptions(new Vector3f(0.0f, 1.0f, 0.0f), 1.0f),
                        startX, startY + 0.3, startZ, 0, 0.1, 0);
                }
            }
        }
    }
    
    /**
     * 模式3特殊攻击：混沌新星
     * 释放强大的混沌能量爆炸，对大面积敌人造成巨大伤害
     */
    private void releaseChaosNova(Level level, Player player) {
        double radius = 10.0;
        
        // 在服务器端造成爆炸效果
        if (!level.isClientSide()) {
            AABB explosionArea = new AABB(
                player.getX() - radius, player.getY() - 3, player.getZ() - radius,
                player.getX() + radius, player.getY() + 5, player.getZ() + radius
            );
            
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, explosionArea, 
                entity -> entity != player && entity.isAlive());
            
            for (LivingEntity target : targets) {
                double distance = target.distanceTo(player);
                float damageMultiplier = (float)(1.0 - (distance / radius)); // 距离越近伤害越高
                
                // 造成基于距离的伤害
                target.hurt(player.damageSources().magic(), BASE_DAMAGE * damageMultiplier);
                
                // 击退效果
                Vec3 knockback = target.position().subtract(player.position()).normalize().scale(2.0);
                target.setDeltaMovement(knockback.x, 0.5, knockback.z);
                
                // 混乱效果
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
            
            // 创建爆炸效果（不破坏方块）
            level.explode(player, player.getX(), player.getY(), player.getZ(), 4.0f, 
                Level.ExplosionInteraction.NONE);
        }
        
        // 在客户端生成混沌粒子效果
        if (level.isClientSide()) {
            for (int i = 0; i < 50; i++) {
                double angle = Math.random() * Math.PI * 2;
                double distance = Math.random() * radius;
                double x = player.getX() + Math.cos(angle) * distance;
                double z = player.getZ() + Math.sin(angle) * distance;
                double y = player.getY() + Math.random() * 4.0;
                
                // 黄色和紫色交替的混沌粒子
                ParticleOptions particle = (i % 2 == 0) ? 
                    new DustParticleOptions(new Vector3f(1.0f, 1.0f, 0.0f), 1.0f) : 
                    new DustParticleOptions(new Vector3f(1.0f, 0.0f, 1.0f), 1.0f);
                
                level.addParticle(particle, x, y, z, 
                    (Math.random() - 0.5) * 0.5, 
                    Math.random() * 0.3, 
                    (Math.random() - 0.5) * 0.5);
            }
        }
    }
    
    /**
     * 寻找多个目标用于弹幕攻击
     */
    private List<LivingEntity> findMultipleTargets(Level level, Player player, int maxTargets) {
        AABB searchArea = new AABB(
            player.getX() - TRACKING_RANGE, player.getY() - TRACKING_RANGE, player.getZ() - TRACKING_RANGE,
            player.getX() + TRACKING_RANGE, player.getY() + TRACKING_RANGE, player.getZ() + TRACKING_RANGE
        );
        
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea, 
            entity -> entity != player && entity.isAlive() && !entity.isInvulnerable());
        
        // 按距离排序并返回前maxTargets个目标
        entities.sort((e1, e2) -> Double.compare(e1.distanceToSqr(player), e2.distanceToSqr(player)));
        
        return entities.subList(0, Math.min(maxTargets, entities.size()));
    }
}