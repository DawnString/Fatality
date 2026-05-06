package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.ScytheOfTheEndProjectile;
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

public class ScytheOfTheEnd extends BaseWeapon
{
    private static final int COOLDOWN_TICKS = 20; // 冷却时间20tick（1秒）
    private static final float BASE_SCYTHE_DAMAGE = 3600.0f; // 基础镰刀伤害

    public ScytheOfTheEnd()
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
        }, new Properties(), 3600, 1, 1, 0.25f, 1.5f, 0.3f, WeaponEnum.MELEE);

        // 设置物品故事
        setStory("终焉的力量在镰刀柄上流转\n" +
                "你握着这把漆黑之镰\n" +
                "感受着掌握生死的力量\n");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算镰刀伤害（使用BaseWeapon的伤害计算逻辑）
            float scytheDamage = calculateScytheDamage(player, itemstack);

            // 创建镰刀投射物
            ScytheOfTheEndProjectile scythe = new ScytheOfTheEndProjectile(level, player, itemstack, scytheDamage);

            // 设置镰刀位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            scythe.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            scythe.shoot(lookVec.x, lookVec.y, lookVec.z, 2.5F, 0.3F); // 2.5速度，0.3散布

            // 添加到世界
            level.addFreshEntity(scythe);

            // 播放投掷音效 - 使用灵魂主题音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 0.9F);
        }

        // 设置冷却时间（不消耗物品）
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * 计算镰刀伤害（使用BaseWeapon相同的计算方法）
     */
    public float calculateScytheDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
}