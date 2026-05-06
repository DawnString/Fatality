package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.CrystalProjectile;
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

public class CrystalDart extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 10; // 冷却时间10tick（0.5秒），比普通飞镖稍慢
    private static final float BASE_CRYSTAL_DART_DAMAGE = 10.0f; // 基础水晶飞镖伤害，比普通飞镖高

    public CrystalDart()
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
        }, new Properties(), (int)BASE_CRYSTAL_DART_DAMAGE, 0.5f, 1f, 0.05f, 0.05f, 0.4f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算水晶飞镖伤害（使用BaseWeapon的伤害计算逻辑）
            float crystalDartDamage = calculateCrystalDartDamage(player, itemstack);

            // 创建水晶飞镖投射物
            CrystalProjectile crystalDart = new CrystalProjectile(level, player, itemstack, crystalDartDamage);

            // 设置水晶飞镖位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            crystalDart.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            crystalDart.shoot(lookVec.x, lookVec.y, lookVec.z, 3.5F, 0.3F); // 3.5速度，更低散布

            // 添加到世界
            level.addFreshEntity(crystalDart);

            // 播放发射音效（水晶音效）
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.8F); // 水晶音效
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算水晶飞镖伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateCrystalDartDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}