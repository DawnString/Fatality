package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
import cn.dawnstring.fatality.entity.projectile.GrenadeProjectile;
import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

/**
 * 不幸者 - 远程枪类武器
 * 特性：右键攻击，每次攻击发射5-8发子弹，同时发射2颗榴弹
 * 伤害512 暴击率24 暴击伤害32 浮动0.2 攻击速度0.25s
 */
public class Unfortunate extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 5; // 冷却时间5tick（0.25秒）
    private static final Random random = new Random();
    
    public Unfortunate() {
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
                return 4; // 材料等级（钻石级）
            }

            @Override
            public int getEnchantmentValue() {
                return 0; // 不能附魔
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null; // 不能修复
            }
        }, new Properties().fireResistant(), 512, 0.25f, 1f, 0.24f, 1.32f, 0.2f, WeaponEnum.RANGED);
        
        setStory("不幸者，一把充满悲剧色彩的强大武器。\n" +
                "每次攻击发射5-8发子弹形成密集火力网，\n" +
                "同时发射2颗榴弹对敌人造成毁灭性打击。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算子弹伤害
            float bulletDamage = calculateBulletDamage(player, itemstack);
            
            // 随机发射5-8发子弹
            int bulletCount = 5 + random.nextInt(4); // 5-8发
            
            // 发射多发子弹
            for (int i = 0; i < bulletCount; i++) {
                // 创建子弹投射物
                BulletProjectile bullet = new BulletProjectile(level, player, itemstack, bulletDamage);
                
                // 设置子弹位置和方向
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();
                
                // 计算锥形散布方向
                Vec3 spreadVec = calculateBulletSpreadDirection(lookVec, i, bulletCount);
                
                bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                bullet.shoot(spreadVec.x, spreadVec.y, spreadVec.z, 3.5F, 0.15F); // 3.5速度，小散布
                
                // 添加到世界
                level.addFreshEntity(bullet);
            }
            
            // 发射2颗榴弹
            for (int i = 0; i < 2; i++) {
                GrenadeProjectile grenade = new GrenadeProjectile(level, player, bulletDamage * 1.5f); // 榴弹伤害为子弹的1.5倍
                grenade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                
                // 为第二颗榴弹添加轻微的角度偏移
                float yawOffset = (i == 1) ? 3.0f : 0.0f;
                grenade.shootFromRotation(player, player.getXRot(), player.getYRot() + yawOffset, 0.0F, 1.0F, 1.0F);
                
                level.addFreshEntity(grenade);
            }
            
            // 播放攻击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 统计使用次数
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        
        // 设置冷却时间
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    /**
     * 计算子弹伤害
     */
    public float calculateBulletDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    /**
     * 计算子弹散布方向
     */
    private Vec3 calculateBulletSpreadDirection(Vec3 baseDirection, int bulletIndex, int totalBullets) {
        Vec3 direction = baseDirection.normalize();

        double spreadAngle = Math.toRadians(12);

        double angleStep = spreadAngle / (totalBullets - 1);
        double angleOffset = (bulletIndex - (totalBullets - 1) / 2.0) * angleStep;

        Vec3 right;
        if (Math.abs(direction.y) > 0.99) {
            right = direction.cross(new Vec3(0, 0, 1)).normalize();
        } else {
            right = direction.cross(new Vec3(0, 1, 0)).normalize();
        }

        double cosAngle = Math.cos(angleOffset);
        double sinAngle = Math.sin(angleOffset);

        Vec3 rotatedDirection = direction.scale(cosAngle)
                .add(right.scale(sinAngle));

        return rotatedDirection.normalize();
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.8F, 0.9F);
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
}