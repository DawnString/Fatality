package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.registry.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 元素裁决 - 三元素形态切换远程武器
 * 冰子弹：对目标造成伤害，并附加冻结FreezeEffect效果
 * 火子弹：对目标造成伤害，并附加灼烧BurnEffect效果
 * 雷子弹：对目标造成伤害，雷电会在目标周围跳跃，以目标为中心，半径3格内的生物都造成0.8倍伤害，被攻击的目标之间用白色粒子链接
 * 伤害3133 暴击率28 暴击伤害35 浮动0.3 攻击速度1s
 */
public class ElementJudgment extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_BULLET_DAMAGE = 3133.0f; // 基础子弹伤害3133
    private static final float LIGHTNING_JUMP_DAMAGE_MULTIPLIER = 0.8f; // 雷电跳跃伤害倍率0.8
    private static final float LIGHTNING_JUMP_RADIUS = 3.0f; // 雷电跳跃半径3格
    private static final int MAX_LIGHTNING_JUMPS = 5; // 最大雷电跳跃次数
    private static final int FREEZE_DURATION = 100; // 冻结效果持续时间5秒（100ticks）
    private static final int BURN_DURATION = 100; // 灼烧效果持续时间5秒（100ticks）
    
    // 元素形态枚举
    private enum ElementMode {
        ICE("冰", ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_BREAK),
        FIRE("火", ParticleTypes.FLAME, SoundEvents.FIRE_AMBIENT),
        LIGHTNING("雷", ParticleTypes.ELECTRIC_SPARK, SoundEvents.LIGHTNING_BOLT_THUNDER);
        
        private final String displayName;
        private final net.minecraft.core.particles.SimpleParticleType particleType;
        private final net.minecraft.sounds.SoundEvent sound;
        
        ElementMode(String displayName, net.minecraft.core.particles.SimpleParticleType particleType, net.minecraft.sounds.SoundEvent sound) {
            this.displayName = displayName;
            this.particleType = particleType;
            this.sound = sound;
        }
        
        public String getDisplayName() { return displayName; }
        public net.minecraft.core.particles.SimpleParticleType getParticleType() { return particleType; }
        public net.minecraft.sounds.SoundEvent getSound() { return sound; }
    }
    
    // 当前元素形态
    private ElementMode currentMode = ElementMode.ICE;
    
    public ElementJudgment() {
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
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.NETHERITE_INGOT); // 修复材料：下界合金锭
            }
        }, new Properties().stacksTo(1).fireResistant(), 
              0, // 基础攻击伤害（由子弹伤害决定）
              1.0f, // 攻击速度
              1.0f, // 基础伤害倍率
              0.28f, // 暴击率28%
              0.35f, // 暴击伤害倍率35%
              0.3f, // 伤害浮动
              WeaponEnum.RANGED // 武器类型：远程
        );
        
        setStory("元素裁决是一把能够切换冰、火、雷三种元素形态的神圣狙击枪。每种形态都有独特的攻击效果，能够应对不同的战斗需求。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 检查是否按下Shift键切换模式
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                // 切换到下一个元素形态
                currentMode = getNextMode();
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("元素裁决已切换至：" + currentMode.getDisplayName() + "形态"));
                
                // 播放模式切换音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        currentMode.getSound(), SoundSource.PLAYERS, 0.5F, 1.2F);
                
                // 生成模式切换粒子效果
                spawnModeSwitchParticles(level, player);
            }
            return InteractionResultHolder.success(itemstack);
        }
        
        if (!level.isClientSide()) {
            // 计算子弹伤害
            float bulletDamage = calculateBulletDamage(player, itemstack);
            
            // 查找射击目标
            LivingEntity target = findTarget(player, level, 50.0f);
            
            if (target != null) {
                // 根据当前元素形态执行不同的攻击效果
                switch (currentMode) {
                    case ICE:
                        performIceShot(level, target, bulletDamage, player, itemstack);
                        break;
                    case FIRE:
                        performFireShot(level, target, bulletDamage, player, itemstack);
                        break;
                    case LIGHTNING:
                        performLightningShot(level, target, bulletDamage, player, itemstack);
                        break;
                }
                
                // 播放射击音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 0.8F);
                
                // 生成射击粒子效果
                spawnShotParticles(level, player, target);
            }
            
            // 设置冷却时间
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
        
        return InteractionResultHolder.success(itemstack);
    }
    
    /**
     * 获取下一个元素形态
     */
    private ElementMode getNextMode() {
        ElementMode[] modes = ElementMode.values();
        int nextIndex = (currentMode.ordinal() + 1) % modes.length;
        return modes[nextIndex];
    }
    
    /**
     * 查找射击目标
     */
    private LivingEntity findTarget(Player player, Level level, double range) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(range));
        
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : entities) {
            // 排除玩家自己和友好生物
            if (entity == player || entity.isAlliedTo(player)) {
                continue;
            }
            
            // 检查视线
            if (player.hasLineOfSight(entity)) {
                double distance = player.distanceToSqr(entity);
                if (distance < nearestDistance && distance <= range * range) {
                    nearestTarget = entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearestTarget;
    }
    
    /**
     * 执行冰子弹攻击
     */
    private void performIceShot(Level level, LivingEntity target, float damage, Player player, ItemStack weapon) {
        // 应用直接伤害
        boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), damage);
        
        if (damageApplied) {
            // 应用冻结效果
            MobEffectInstance freezeEffect = new MobEffectInstance(ModEffects.FREEZE.get(), FREEZE_DURATION, 0);
            target.addEffect(freezeEffect);
            
            // 播放冰霜音效
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.8F, 0.7F);
            
            // 生成冰霜粒子效果
            spawnIceParticles(level, target);
        }
    }
    
    /**
     * 执行火子弹攻击
     */
    private void performFireShot(Level level, LivingEntity target, float damage, Player player, ItemStack weapon) {
        // 应用直接伤害
        boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), damage);
        
        if (damageApplied) {
            // 应用灼烧效果
            MobEffectInstance burnEffect = new MobEffectInstance(ModEffects.BURN.get(), BURN_DURATION, 0);
            target.addEffect(burnEffect);
            
            // 播放火焰音效
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.NEUTRAL, 0.8F, 1.0F);
            
            // 生成火焰粒子效果
            spawnFireParticles(level, target);
        }
    }
    
    /**
     * 执行雷子弹攻击（雷电跳跃效果）
     */
    private void performLightningShot(Level level, LivingEntity target, float damage, Player player, ItemStack weapon) {
        // 应用直接伤害
        boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), damage);
        
        if (damageApplied) {
            // 播放雷电音效
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 1.0F, 0.8F);
            
            // 生成雷电粒子效果
            spawnLightningParticles(level, target);
            
            // 执行雷电跳跃效果
            performLightningJump(level, target, damage * LIGHTNING_JUMP_DAMAGE_MULTIPLIER, player, 0);
        }
    }
    
    /**
     * 执行雷电跳跃效果（递归实现）
     */
    private void performLightningJump(Level level, LivingEntity source, float damage, Player player, int jumpCount) {
        if (jumpCount >= MAX_LIGHTNING_JUMPS) {
            return; // 达到最大跳跃次数
        }
        
        // 获取周围3格内的所有实体
        AABB jumpBox = new AABB(
                source.getX() - LIGHTNING_JUMP_RADIUS, source.getY() - LIGHTNING_JUMP_RADIUS, source.getZ() - LIGHTNING_JUMP_RADIUS,
                source.getX() + LIGHTNING_JUMP_RADIUS, source.getY() + LIGHTNING_JUMP_RADIUS, source.getZ() + LIGHTNING_JUMP_RADIUS
        );
        
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                LivingEntity.class, jumpBox,
                entity -> entity.isAlive() && entity != player && entity != source
        );
        
        if (nearbyEntities.isEmpty()) {
            return; // 没有可跳跃的目标
        }
        
        // 选择最近的实体作为跳跃目标
        LivingEntity nextTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : nearbyEntities) {
            double distance = source.distanceToSqr(entity);
            if (distance < nearestDistance) {
                nextTarget = entity;
                nearestDistance = distance;
            }
        }
        
        if (nextTarget != null) {
            // 对跳跃目标造成伤害
            nextTarget.hurt(nextTarget.damageSources().playerAttack(player), damage);
            
            // 生成雷电跳跃粒子链
            spawnLightningChainParticles(level, source, nextTarget);
            
            // 递归执行下一次跳跃
            performLightningJump(level, nextTarget, damage, player, jumpCount + 1);
        }
    }
    
    /**
     * 生成模式切换粒子效果
     */
    private void spawnModeSwitchParticles(Level level, Player player) {
        if (level.isClientSide()) {
            Vec3 pos = player.getEyePosition();
            
            // 生成当前元素形态的粒子效果
            for (int i = 0; i < 20; i++) {
                level.addParticle(currentMode.getParticleType(),
                        pos.x + (Math.random() - 0.5) * 1.5,
                        pos.y + (Math.random() - 0.5) * 1.5,
                        pos.z + (Math.random() - 0.5) * 1.5,
                        0, 0.1, 0);
            }
        }
    }
    
    /**
     * 生成射击粒子效果
     */
    private void spawnShotParticles(Level level, Player player, LivingEntity target) {
        if (level.isClientSide()) {
            Vec3 startPos = player.getEyePosition();
            Vec3 endPos = target.getEyePosition();
            
            // 生成弹道粒子效果
            for (int i = 0; i < 25; i++) {
                double progress = (double) i / 24;
                double x = startPos.x + (endPos.x - startPos.x) * progress;
                double y = startPos.y + (endPos.y - startPos.y) * progress;
                double z = startPos.z + (endPos.z - startPos.z) * progress;
                
                level.addParticle(currentMode.getParticleType(), x, y, z, 0, 0.02, 0);
            }
        }
    }
    
    /**
     * 生成冰霜粒子效果
     */
    private void spawnIceParticles(Level level, LivingEntity target) {
        if (level.isClientSide()) {
            Vec3 pos = target.getEyePosition();
            
            // 生成冰霜粒子
            for (int i = 0; i < 15; i++) {
                level.addParticle(ParticleTypes.SNOWFLAKE,
                        pos.x + (Math.random() - 0.5) * 1.5,
                        pos.y + (Math.random() - 0.5) * 1.5,
                        pos.z + (Math.random() - 0.5) * 1.5,
                        0, 0.08, 0);
            }
            
            // 生成冰晶粒子
            for (int i = 0; i < 8; i++) {
                level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                        pos.x + (Math.random() - 0.5) * 1.2,
                        pos.y + (Math.random() - 0.5) * 1.2,
                        pos.z + (Math.random() - 0.5) * 1.2,
                        0, 0.05, 0);
            }
        }
    }
    
    /**
     * 生成火焰粒子效果
     */
    private void spawnFireParticles(Level level, LivingEntity target) {
        if (level.isClientSide()) {
            Vec3 pos = target.getEyePosition();
            
            // 生成火焰粒子
            for (int i = 0; i < 15; i++) {
                level.addParticle(ParticleTypes.FLAME,
                        pos.x + (Math.random() - 0.5) * 1.5,
                        pos.y + (Math.random() - 0.5) * 1.5,
                        pos.z + (Math.random() - 0.5) * 1.5,
                        0, 0.1, 0);
            }
            
            // 生成烟雾粒子
            for (int i = 0; i < 5; i++) {
                level.addParticle(ParticleTypes.SMOKE,
                        pos.x + (Math.random() - 0.5) * 1.2,
                        pos.y + (Math.random() - 0.5) * 1.2,
                        pos.z + (Math.random() - 0.5) * 1.2,
                        0, 0.05, 0);
            }
        }
    }
    
    /**
     * 生成雷电粒子效果
     */
    private void spawnLightningParticles(Level level, LivingEntity target) {
        if (level.isClientSide()) {
            Vec3 pos = target.getEyePosition();
            
            // 生成雷电粒子
            for (int i = 0; i < 20; i++) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.x + (Math.random() - 0.5) * 2.0,
                        pos.y + (Math.random() - 0.5) * 2.0,
                        pos.z + (Math.random() - 0.5) * 2.0,
                        0, 0.15, 0);
            }
            
            // 生成白色闪光粒子
            for (int i = 0; i < 10; i++) {
                level.addParticle(ParticleTypes.GLOW,
                        pos.x + (Math.random() - 0.5) * 1.5,
                        pos.y + (Math.random() - 0.5) * 1.5,
                        pos.z + (Math.random() - 0.5) * 1.5,
                        0, 0.1, 0);
            }
        }
    }
    
    /**
     * 生成雷电跳跃粒子链
     */
    private void spawnLightningChainParticles(Level level, LivingEntity source, LivingEntity target) {
        if (level.isClientSide()) {
            Vec3 startPos = source.getEyePosition();
            Vec3 endPos = target.getEyePosition();
            
            // 生成闪电链粒子
            for (int i = 0; i < 15; i++) {
                double progress = (double) i / 14;
                double x = startPos.x + (endPos.x - startPos.x) * progress;
                double y = startPos.y + (endPos.y - startPos.y) * progress;
                double z = startPos.z + (endPos.z - startPos.z) * progress;
                
                // 添加随机抖动，模拟闪电的不规则形状
                double jitterX = (Math.random() - 0.5) * 0.3;
                double jitterY = (Math.random() - 0.5) * 0.3;
                double jitterZ = (Math.random() - 0.5) * 0.3;
                
                level.addParticle(ParticleTypes.GLOW,
                        x + jitterX, y + jitterY, z + jitterZ,
                        0, 0.05, 0);
            }
        }
    }
    
    /**
     * 计算子弹伤害（使用BaseWeapon的伤害计算逻辑）
     */
    private float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    @Override
    public String getSpecialEffectDescription() {
        return "当前形态：" + currentMode.getDisplayName() + 
               " | 冰子弹：冻结5秒" + 
               " | 火子弹：灼烧5秒" + 
               " | 雷子弹：雷电跳跃5次，半径3格";
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 始终显示附魔光效
    }
}