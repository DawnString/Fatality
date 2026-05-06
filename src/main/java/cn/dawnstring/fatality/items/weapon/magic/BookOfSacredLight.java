package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.SacredLightBeam;
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
 * 神圣光芒之书 - 持续发射光束魔法武器
 * 特性：长按右键时向玩家准星朝向持续发射神圣光芒光束
 * 伤害：17点基础伤害，14%暴击率，15%暴击伤害，0.2伤害浮动
 * 消耗：按住右键时每秒消耗4点魔法值
 * 每tick17伤害
 */
public class BookOfSacredLight extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 17.0f; // 基础魔法伤害17
    private static final float MANA_COST_PER_SECOND = 4.0f; // 每秒消耗4点魔法值
    private SacredLightBeam activeBeam; // 当前激活的光束实体
    private long lastManaConsumeTime = 0; // 上次消耗魔法值的时间

    public BookOfSacredLight() {
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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 1.0f, 1f, 0.14f, 0.15f, 0.2f, WeaponEnum.MAGIC);
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

            // 计算神圣光芒伤害
            float sacredDamage = calculateFinalDamage(player, itemstack, null);

            // 如果光束不存在，创建新的光束
            if (activeBeam == null || activeBeam.isRemoved()) {
                activeBeam = new SacredLightBeam(level, player, sacredDamage);
                level.addFreshEntity(activeBeam);

                // 播放神圣光芒施法音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.2f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 0.8f);
            } else {
                // 更新现有光束的伤害
                activeBeam.updateDamage(sacredDamage);
            }

            lastManaConsumeTime = System.currentTimeMillis();
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            // 停止光束
            stopBeam();
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
                        stopBeam();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c魔法值不足！光束已停止"), true);
                        return;
                    }

                    // 更新伤害
                    if (activeBeam != null && !activeBeam.isRemoved()) {
                        float sacredDamage = calculateFinalDamage(player, stack, null);
                        activeBeam.updateDamage(sacredDamage);
                    }
                }
            } else if (activeBeam != null && !activeBeam.isRemoved()) {
                // 如果玩家没有使用物品但光束还在，停止光束
                stopBeam();
            }
        }
    }

    /**
     * 停止光束
     */
    private void stopBeam() {
        if (activeBeam != null && !activeBeam.isRemoved()) {
            activeBeam.discard();
            activeBeam = null;
        }
    }
}