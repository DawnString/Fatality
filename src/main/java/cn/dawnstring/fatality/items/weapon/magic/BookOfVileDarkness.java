package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.VileDarknessEffect;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
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

/**
 * 邪恶黑暗之书 - 范围持续伤害魔法武器
 * 特性：按住右键时对玩家周围8格半径内的所有生物造成持续伤害
 * 伤害：57点基础伤害，8%暴击率，10%暴击伤害，0.3伤害浮动
 * 消耗：按住右键时每秒消耗1点魔法值
 */
public class BookOfVileDarkness extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 57.0f; // 基础魔法伤害57
    private static final float MANA_COST_PER_SECOND = 1.0f; // 每秒消耗1点魔法值
    private VileDarknessEffect activeEffect; // 当前激活的效果实体
    private long lastManaConsumeTime = 0; // 上次消耗魔法值的时间

    public BookOfVileDarkness() {
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
                return 0; // 书本本身没有攻击伤害加成
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
        }, new Properties(), 0, 1.0f, 1f, 0.08f, 0.10f, 0.3f, WeaponEnum.MAGIC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 检查魔法值是否小于消耗值的2倍（释放失败条件）
            if (ManaSystem.getCurrentMana(player) < MANA_COST_PER_SECOND * 2) {
                // 如果魔法值不足消耗值的2倍，释放失败
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要至少" + (MANA_COST_PER_SECOND * 2) + "点魔法值才能释放"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 检查玩家是否有足够的魔法值（至少1秒的消耗）
            if (!ManaSystem.hasEnoughMana(player, MANA_COST_PER_SECOND)) {
                // 如果魔法值不足，提示玩家
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST_PER_SECOND + "点魔法值/秒"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 开始使用物品（这会触发减速效果，但我们会立即处理）
            player.startUsingItem(hand);

            // 计算邪恶黑暗伤害
            float vileDamage = calculateVileDamage(player, itemstack);

            // 如果效果不存在，创建新的效果
            if (activeEffect == null || activeEffect.isRemoved()) {
                activeEffect = new VileDarknessEffect(level, player, vileDamage);
                level.addFreshEntity(activeEffect);

                // 播放邪恶黑暗施法音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.7f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8f, 0.9f);

                // 魔法释放成功后才消耗魔法值（第一次消耗）
                ManaSystem.consumeMana(player, MANA_COST_PER_SECOND);
            } else {
                // 更新现有效果的伤害
                activeEffect.updateDamage(vileDamage);
            }

            lastManaConsumeTime = System.currentTimeMillis();
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            // 停止效果
            stopEffect();
        }
        super.onStopUsing(stack, entity, count);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        // 返回一个很大的值，让玩家可以持续按住右键
        return 72000; // 2分钟的最大使用时间
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide() && entity instanceof Player player) {
            long currentTime = System.currentTimeMillis();

            // 如果玩家正在使用物品，检查魔法值消耗
            if (player.isUsingItem() && player.getUseItem().getItem() == this) {
                // 每秒检查一次魔法值消耗
                if (currentTime - lastManaConsumeTime >= 1000) {
                    lastManaConsumeTime = currentTime;

                    // 检查魔法值是否小于消耗值的2倍（持续释放失败条件）
                    if (ManaSystem.getCurrentMana(player) < MANA_COST_PER_SECOND * 2) {
                        // 魔法值不足消耗值的2倍，停止使用物品
                        player.stopUsingItem();
                        stopEffect();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要至少" + (MANA_COST_PER_SECOND * 2) + "点魔法值才能持续释放"), true);
                        return;
                    }

                    // 检查魔法值是否足够
                    if (!ManaSystem.hasEnoughMana(player, MANA_COST_PER_SECOND)) {
                        // 魔法值不足，停止使用物品
                        player.stopUsingItem();
                        stopEffect();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！效果已停止"), true);
                        return;
                    }

                    // 魔法值足够，消耗魔法值并更新伤害
                    ManaSystem.consumeMana(player, MANA_COST_PER_SECOND);

                    // 更新伤害
                    if (activeEffect != null && !activeEffect.isRemoved()) {
                        float vileDamage = calculateVileDamage(player, stack);
                        activeEffect.updateDamage(vileDamage);
                    }
                }
            } else if (activeEffect != null && !activeEffect.isRemoved()) {
                // 如果玩家没有使用物品但效果还在，停止效果
                stopEffect();
            }
        }
    }

    /**
     * 停止效果
     */
    private void stopEffect() {
        if (activeEffect != null && !activeEffect.isRemoved()) {
            activeEffect.discard();
            activeEffect = null;
        }
    }

    /**
     * 计算邪恶黑暗伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateVileDamage(Player player, ItemStack stack) {
        // 使用BaseWeapon的伤害计算逻辑，但基于魔法伤害
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
}