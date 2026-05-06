package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.CommandersGreataxeProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
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

public class CommandersGreataxe extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_GREATAXE_DAMAGE = 28.0f; // 基础战斧伤害

    public CommandersGreataxe()
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
        }, new Properties(), 28, 1f, 1f, 0.08f, 0.07f, 0.2f, WeaponEnum.MELEE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算战斧伤害（使用BaseWeapon的伤害计算逻辑）
            float greataxeDamage = calculateGreataxeDamage(player, itemstack);

            // 创建战斧投射物
            CommandersGreataxeProjectile greataxe = new CommandersGreataxeProjectile(level, player, itemstack, greataxeDamage);

            // 设置战斧位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            greataxe.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            greataxe.shoot(lookVec.x, lookVec.y, lookVec.z, 2.0F, 0.5F); // 2.0速度，0.5散布

            // 添加到世界
            level.addFreshEntity(greataxe);

            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.9F);
        }

        // 设置冷却时间（不消耗物品）
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算战斧伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateGreataxeDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}