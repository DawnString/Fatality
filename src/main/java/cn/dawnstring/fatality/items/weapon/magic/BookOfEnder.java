package cn.dawnstring.fatality.items.weapon.magic;

import cn.dawnstring.fatality.entity.EnderSphere;
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

import java.util.ArrayList;
import java.util.List;

public class BookOfEnder extends BaseWeapon
{
    private static final float MANA_COST = 15.0f; // 每次施法消耗15点魔法值
    private static final int COOLDOWN_TICKS = 40; // 冷却时间40tick（2秒）
    private static final float BASE_MAGIC_DAMAGE = 8.0f; // 基础魔法伤害
    private static final int MAX_SPHERES = 5; // 最多存在5个球体
    private static final int SPHERE_LIFETIME = 200; // 球体存在时间200tick（10秒）
    
    // 存储当前存在的球体列表
    private final List<EnderSphere> activeSpheres = new ArrayList<>();

    public BookOfEnder() {
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
        }, new Properties(), (int)BASE_MAGIC_DAMAGE, 1, 1, 0.05f, 1.0f, 0.3f, WeaponEnum.MAGIC);
        
        this.setStory("末影之书，神秘的魔法武器。右键生成黑色球体，球体会自动发射黑色闪电攻击周围的敌对实体。球体存在10秒，最多可同时存在5个。");
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
            // 清理已消失的球体
            cleanupSpheres();
            
            // 检查球体数量限制
            if (activeSpheres.size() >= MAX_SPHERES) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c已达到最大球体数量限制（5个）"), true);
                return InteractionResultHolder.fail(itemstack);
            }

            // 计算闪电伤害
            float lightningDamage = calculateFinalDamage(player, itemstack, null);

            // 获取玩家视线方向
            Vec3 lookVec = player.getLookAngle();
            Vec3 spawnPos = player.getEyePosition().add(lookVec.scale(2.0)); // 在玩家前方2格生成

            // 创建末影球体
            EnderSphere sphere = new EnderSphere(level, player, lightningDamage, SPHERE_LIFETIME);
            sphere.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            
            // 添加到世界
            level.addFreshEntity(sphere);
            activeSpheres.add(sphere);

            // 播放施法音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }

    private void cleanupSpheres() {
        activeSpheres.removeIf(sphere -> sphere == null || sphere.isRemoved());
    }

    /**
     * 获取当前活跃球体数量
     */
    public int getActiveSphereCount() {
        cleanupSpheres();
        return activeSpheres.size();
    }
}