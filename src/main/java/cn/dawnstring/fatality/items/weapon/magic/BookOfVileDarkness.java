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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 1.0f, 1f, 0.08f, 0.10f, 0.3f, WeaponEnum.MAGIC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (!ManaSystem.safeConsumeMana(player, MANA_COST_PER_SECOND)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！需要" + MANA_COST_PER_SECOND + "点魔法值/秒"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 开始使用物品（这会触发减速效果，但我们会立即处理）
            player.startUsingItem(hand);

            // 计算邪恶黑暗伤害
            float vileDamage = calculateFinalDamage(player, itemstack, null);

            // 如果效果不存在，创建新的效果
            if (activeEffect == null || activeEffect.isRemoved()) {
                activeEffect = new VileDarknessEffect(level, player, vileDamage);
                level.addFreshEntity(activeEffect);

                // 播放邪恶黑暗施法音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.7f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8f, 0.9f);
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

                    if (!ManaSystem.safeConsumeMana(player, MANA_COST_PER_SECOND)) {
                        player.stopUsingItem();
                        stopEffect();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！效果已停止"), true);
                        return;
                    }

                    // 更新伤害
                    if (activeEffect != null && !activeEffect.isRemoved()) {
                        float vileDamage = calculateFinalDamage(player, stack, null);
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
}