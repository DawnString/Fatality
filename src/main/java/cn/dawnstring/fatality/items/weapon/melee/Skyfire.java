package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.SkyfireSpearProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
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

import java.util.List;
import java.util.Random;

/**
 * 天火 - 近战矛类武器
 * 特性：右键攻击，以玩家为中心，半径10格内的所有生物（除玩家），从其头顶上方召唤3-6道天火矛射向目标
 * 伤害1790 暴击率26 暴击伤害34 浮动0.3 攻击速度1s
 */
public class Skyfire extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float ATTACK_RANGE = 10.0f; // 攻击范围半径10格
    private static final int MIN_SPEAR_COUNT = 3; // 最少召唤3道天火矛
    private static final int MAX_SPEAR_COUNT = 6; // 最多召唤6道天火矛
    
    private final Random random = new Random();

    public Skyfire()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
            }

            @Override
            public float getSpeed() {
                return 0; // 挖掘速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 0; // 基础攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 4; // 材料等级（钻石级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties().fireResistant(), 1790, 1.0f, 1f, 0.26f, 1.34f, 0.3f, WeaponEnum.MELEE);
        
        setStory("天火长矛，蕴含着天界的神圣火焰之力。\n" +
                "右键攻击时，会在周围敌人头顶召唤天火矛从天而降，\n" +
                "对敌人造成毁灭性的神圣火焰伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算天火矛伤害（使用BaseWeapon的伤害计算逻辑）
            float spearDamage = calculateSpearDamage(player, itemstack);

            // 查找玩家周围半径10格内的所有生物（除玩家）
            AABB searchArea = new AABB(
                    player.getX() - ATTACK_RANGE, player.getY() - ATTACK_RANGE, player.getZ() - ATTACK_RANGE,
                    player.getX() + ATTACK_RANGE, player.getY() + ATTACK_RANGE, player.getZ() + ATTACK_RANGE
            );
            
            List<Entity> entitiesInRange = level.getEntities(player, searchArea);
            
            // 过滤出活体生物（除玩家）
            List<LivingEntity> targets = entitiesInRange.stream()
                    .filter(entity -> entity instanceof LivingEntity && entity != player)
                    .map(entity -> (LivingEntity) entity)
                    .toList();

            if (!targets.isEmpty()) {
                // 随机决定召唤的天火矛数量（3-6道）
                int spearCount = MIN_SPEAR_COUNT + random.nextInt(MAX_SPEAR_COUNT - MIN_SPEAR_COUNT + 1);
                
                // 为每个目标随机分配天火矛（确保每个目标至少被一道天火矛攻击）
                for (int i = 0; i < spearCount && i < targets.size(); i++) {
                    LivingEntity target = targets.get(i % targets.size());
                    summonSkyfireSpear(level, player, target, spearDamage);
                }
                
                // 如果目标数量少于天火矛数量，为剩余的天火矛随机选择目标
                for (int i = targets.size(); i < spearCount; i++) {
                    LivingEntity target = targets.get(random.nextInt(targets.size()));
                    summonSkyfireSpear(level, player, target, spearDamage);
                }

                // 播放天火召唤音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 召唤天火矛攻击目标
     */
    private void summonSkyfireSpear(Level level, Player player, LivingEntity target, float damage) {
        // 在目标头顶上方20格处生成天火矛
        double spawnX = target.getX() + (random.nextDouble() - 0.5) * 2.0; // 在目标周围随机位置
        double spawnY = target.getY() + 20.0; // 头顶上方20格
        double spawnZ = target.getZ() + (random.nextDouble() - 0.5) * 2.0;

        // 创建天火矛投射物
        SkyfireSpearProjectile spear = new SkyfireSpearProjectile(level, player, damage);
        spear.setPos(spawnX, spawnY, spawnZ);
        
        // 设置垂直下落方向（向下）
        spear.shoot(0, -1.0, 0, 2.0F, 0.0F); // 2.0速度，垂直向下

        // 添加到世界
        level.addFreshEntity(spear);
    }

    /**
     * 计算天火矛伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateSpearDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑
        return calculateFinalDamage(player, stack, null);
    }

    /**
     * 获取攻击距离（天火长矛有较长的攻击距离）
     */
    @Override
    public double getAttackRange(Player player) {
        return 5.0; // 长矛有5格攻击距离
    }
}