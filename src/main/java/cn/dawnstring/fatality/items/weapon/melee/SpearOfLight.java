package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import cn.dawnstring.fatality.entity.projectile.SpearOfLightProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 光之长矛 - 神圣的近战投掷武器
 * 特性：右键投掷光之长矛，受重力影响，造成神圣伤害
 */
public class SpearOfLight extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_THROW_DAMAGE = 73.0f; // 基础投掷伤害73

    public SpearOfLight()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 1561; // 耐久度
            }

            @Override
            public float getSpeed() {
                return 8.0f; // 挖掘速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 3.0f; // 攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 3; // 工具等级
            }

            @Override
            public int getEnchantmentValue() {
                return 15; // 附魔能力
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT); // 修复材料：金锭
            }
        }, new Properties().stacksTo(1), 73, 0.2f, 1.0f, 0.15f, 15.0f, 0.3f, WeaponEnum.MELEE);
        
        setStory("一把充满神圣光芒的长矛，投掷时会发出耀眼的光芒，对邪恶生物造成额外伤害。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算投掷伤害（使用BaseWeapon的伤害计算逻辑）
            float throwDamage = calculateThrowDamage(player, itemstack);

            // 创建光之长矛投射物
            SpearOfLightProjectile spear = new SpearOfLightProjectile(level, player, itemstack, throwDamage);

            // 设置投射物位置和方向
            spear.setPos(player.getEyePosition().x, player.getEyePosition().y, player.getEyePosition().z);
            spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.0f, 1.0f); // 2.0速度，标准散布

            // 添加到世界
            level.addFreshEntity(spear);

            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.2F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算投掷伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateThrowDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}