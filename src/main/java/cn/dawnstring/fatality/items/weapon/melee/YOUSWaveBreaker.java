package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.WaveBreakerProjectile;
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

import static cn.dawnstring.fatality.Fatality.DOT_ITEM_DES;

public class YOUSWaveBreaker extends BaseWeapon
{
    public YOUSWaveBreaker() {
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
        },  new Properties(), 1000,1, 1, 0.25f, 1.5f, 0.2f, WeaponEnum.MELEE);

        setStory("那只巨龙有时在海边游荡\n" +
                "偶尔有剥落的鳞片被海浪带到岸边\n" +
                "一位锻造师收集了这些鳞片\n" +
                "将其打造成了武器\n" +
                "尽管武器很锋利\n" +
                "但是它只是没有灵魂的物品而已\n" +
                "在找到这把武器后\n" +
                "你将巨龙的眼泪滴在武器上\n" +
                "随机武器开始发生了变化\n" +
                "缓缓流转的蓝色光辉告诉着你\n" +
                "这把武器并不一般\n" +
                DOT_ITEM_DES);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 创建投掷物
            WaveBreakerProjectile projectile = new WaveBreakerProjectile(level, player, itemstack);

            // 设置投掷物位置和方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            projectile.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
            projectile.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5F, 1.0F);

            // 添加到世界
            level.addFreshEntity(projectile);

            // 播放投掷音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // 设置冷却时间（20 tick = 1秒）
        player.getCooldowns().addCooldown(this, 20);

        return InteractionResultHolder.success(itemstack);
    }
}
