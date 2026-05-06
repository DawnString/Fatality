package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.IncompleteHolyBeam;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.network.chat.Component;
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

/**
 * 不完整神圣法术书 - 散射光束魔法武器
 * 特性：长按右键时向玩家准星朝向持续发射神圣光束，随使用时间增加光束会变得散射
 * 伤害：15点基础伤害/tick，14%暴击率，15%暴击伤害，0.2伤害浮动
 * 消耗：按住右键时每秒消耗5点魔法值
 */
public class IncompleteHolySpellBook extends BaseWeapon
{
    private static final float BASE_MAGIC_DAMAGE = 15.0f; // 基础魔法伤害15/tick
    private static final float MANA_COST_PER_SECOND = 5.0f; // 每秒消耗5点魔法值
    private IncompleteHolyBeam activeBeam; // 当前激活的光束实体
    private long lastManaConsumeTime = 0; // 上次消耗魔法值的时间
    private int useDuration = 0; // 使用持续时间（用于计算散射程度）

    public IncompleteHolySpellBook() {
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
                return 0; // 法术书本身没有攻击伤害加成
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
        
        // 设置物品故事
        setStory("一本散发着神圣光芒的法术书，但似乎并不完整。\n" +
                "当你使用它时，光束会随着时间逐渐散射，\n" +
                "仿佛在寻找着某种平衡。\n" +
                "也许有一天，它能够展现出真正的力量...");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (!ManaSystem.safeConsumeMana(player, MANA_COST_PER_SECOND)) {
                player.displayClientMessage(Component.literal("§c魔法值不足！需要" + MANA_COST_PER_SECOND + "点魔法值/秒"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 开始使用物品
            player.startUsingItem(hand);
            useDuration = 0; // 重置使用时间

            // 计算神圣光束伤害
            float holyDamage = calculateFinalDamage(player, itemstack, null);

            // 如果光束不存在，创建新的光束
            if (activeBeam == null || activeBeam.isRemoved()) {
                activeBeam = new IncompleteHolyBeam(level, player, holyDamage, useDuration);
                level.addFreshEntity(activeBeam);

                // 播放神圣法术书施法音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.8f, 1.0f);
            } else {
                // 更新现有光束的伤害和散射程度
                activeBeam.updateDamage(holyDamage);
                activeBeam.updateScatter(useDuration);
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
            useDuration = 0; // 重置使用时间
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

            // 如果玩家正在使用物品，检查魔法值消耗和更新使用时间
            if (player.isUsingItem() && player.getUseItem().getItem() == this) {
                useDuration++; // 增加使用时间
                
                // 每秒检查一次魔法值消耗
                if (currentTime - lastManaConsumeTime >= 1000) {
                    lastManaConsumeTime = currentTime;

                    if (!ManaSystem.safeConsumeMana(player, MANA_COST_PER_SECOND)) {
                        player.stopUsingItem();
                        stopBeam();
                        useDuration = 0;
                        player.displayClientMessage(Component.literal("§c魔法值不足！光束已停止"), true);
                        return;
                    }

                    // 更新伤害和散射程度
                    if (activeBeam != null && !activeBeam.isRemoved()) {
                        float holyDamage = calculateFinalDamage(player, stack, null);
                        activeBeam.updateDamage(holyDamage);
                        activeBeam.updateScatter(useDuration);
                    }
                }
            } else if (activeBeam != null && !activeBeam.isRemoved()) {
                // 如果玩家没有使用物品但光束还在，停止光束
                stopBeam();
                useDuration = 0;
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