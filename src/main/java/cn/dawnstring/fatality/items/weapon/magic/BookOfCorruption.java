package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.core.particles.ParticleTypes;
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

/**
 * 腐化之书 - 持续伤害魔法书
 * 特性：长按右键攻击，用粒子链接玩家与目标，使目标持续受到伤害
 */
public class BookOfCorruption extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 5;
    private static final float BASE_DAMAGE_PER_TICK = 19.0f;
    private static final float MANA_COST = 2.0f;
    private static final float MAX_RANGE = 12.0f;

    public BookOfCorruption()
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
        }, new Properties(), (int)BASE_DAMAGE_PER_TICK, 0.05f, 1.0f, 0.0f, 0.0f, 0.0f, WeaponEnum.MAGIC);
        
        setStory("一本蕴含腐化力量的魔法书，能够通过魔法链接持续侵蚀敌人的生命力。每秒消耗2点魔法值。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (!ManaSystem.safeConsumeMana(player, MANA_COST)) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c魔力不足！"),
                        true
                );
                return InteractionResultHolder.fail(itemstack);
            }

            LivingEntity target = findNearestTarget(level, player);
            
            if (target != null) {
                createCorruptionLink(level, player, target);
                
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 0.6F);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
        }

        if (level.isClientSide()) {
            spawnMagicParticles(level, player);
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 寻找最近的敌人
     */
    private LivingEntity findNearestTarget(Level level, Player player) {
        LivingEntity nearestTarget = null;
        double nearestDistance = MAX_RANGE;
        
        // 获取玩家周围的所有实体
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(MAX_RANGE))) {
            if (entity != player && entity.isAlive() && !entity.isInvulnerable()) {
                double distance = player.distanceTo(entity);
                if (distance < nearestDistance) {
                    nearestTarget = entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearestTarget;
    }

    /**
     * 创建腐化链接
     */
    private void createCorruptionLink(Level level, Player player, LivingEntity target) {
        // 计算链接伤害
        float damagePerTick = calculateFinalDamage(player, null, null);
        
        // 开始链接效果
        startLinkEffect(level, player, target, damagePerTick);
    }

    private void startLinkEffect(Level level, Player player, LivingEntity target, float damagePerTick) {
        target.hurt(target.damageSources().magic(), damagePerTick);
    }

    /**
     * 生成魔法粒子效果
     */
    private void spawnMagicParticles(Level level, Player player) {
        Vec3 playerPos = player.position();
        
        // 在玩家周围生成魔法粒子
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 2.0;
            double offsetY = Math.random() * 2.0;
            double offsetZ = (Math.random() - 0.5) * 2.0;
            
            // 生成紫色魔法粒子
            level.addParticle(ParticleTypes.ENCHANT,
                    playerPos.x + offsetX,
                    playerPos.y + offsetY,
                    playerPos.z + offsetZ,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.1,
                    (Math.random() - 0.5) * 0.1);
            
            // 生成绿色腐化粒子
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
                        playerPos.x + offsetX * 0.8,
                        playerPos.y + offsetY * 0.8,
                        playerPos.z + offsetZ * 0.8,
                        (Math.random() - 0.5) * 0.05,
                        Math.random() * 0.05,
                        (Math.random() - 0.5) * 0.05);
            }
        }
    }
}