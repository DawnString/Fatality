package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.VortexBombProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 漩涡制造者 - 远程武器
 * 右键发射漩涡爆弹，碰撞到目标或方块就爆开产生漩涡
 * 漩涡能够吸附周围2格的实体（除玩家），并对其中的实体（除玩家）持续造成伤害
 * 漩涡伤害为基础伤害0.5
 * 漩涡爆弹伤害为基础伤害
 * 伤害750 暴击率24 暴击伤害30 浮动0.3 攻击速度0.5s
 */
public class VortexMaker extends BaseWeapon
{
    private final float baseDamage = 750f;
    public VortexMaker()
    {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; // 无限耐久
            }

            @Override
            public float getSpeed() {
                return 0; // 挖掘速度
            }

            @Override
            public float getAttackDamageBonus() {
                return 0; // 基础攻击伤害加成
            }

            @Override
            public int getLevel() {
                return 1; // 材料等级（钻石级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties(), 0, 0.5f, 1f, 0.24f, 0.3f, 0.3f, WeaponEnum.RANGED);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // 计算基础伤害（包含暴击逻辑）
            float vortexDamage = baseDamage * 0.5f; // 漩涡伤害为基础伤害的50%
            
            // 创建漩涡爆弹投射物
            VortexBombProjectile vortexBombProjectile = new VortexBombProjectile(level, player, baseDamage);
            
            // 设置投射物位置和方向
            vortexBombProjectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            vortexBombProjectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            
            // 添加到世界
            level.addFreshEntity(vortexBombProjectile);
        }
        
        // 统计使用次数
        player.awardStat(Stats.ITEM_USED.get(this));
        
        // 设置冷却时间（10 tick = 0.5秒）
        player.getCooldowns().addCooldown(this, 10);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}