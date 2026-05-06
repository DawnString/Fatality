package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.projectile.WaterArrowProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.system.ManaSystem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WaterArrowSpellbook extends BaseWeapon
{
    private static final float MANA_COST = 4.0f; // 每次施法消耗4点魔法值（比火球少）
    private static final int COOLDOWN_TICKS = 12; // 冷却时间12tick（0.6秒，比火球快）
    private static final float BASE_MAGIC_DAMAGE = 8.0f; // 基础魔法伤害（比火球低但更快）

    public WaterArrowSpellbook() {
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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 1, 1, 0.05f, 1.0f, 0.2f, WeaponEnum.MAGIC);
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
            // 计算水箭伤害
            float waterArrowDamage = calculateFinalDamage(player, itemstack, null);

            // 创建水箭投射物，传递伤害信息
            WaterArrowProjectile projectile = new WaterArrowProjectile(level, player, itemstack, waterArrowDamage);

            // 设置水箭位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            projectile.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            projectile.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5F, 1.0F); // 水箭速度比火球快

            // 添加到世界
            level.addFreshEntity(projectile);

            // 播放施法音效（使用水相关音效）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 0.8F, 1.2F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算水箭伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateWaterArrowDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}