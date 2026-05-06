package cn.dawnstring.fatality.items.weapon.ranged;

import cn.dawnstring.fatality.entity.projectile.SaboteurBulletProjectile;
import cn.dawnstring.fatality.entity.projectile.SaboteurGrenadeProjectile;
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
 * 破坏者 - 远程武器
 * 特性：右键攻击，每次攻击发射10-12发子弹，同时发射1颗榴弹，榴弹会追踪最近的目标，产生爆炸，对半径5格内所有实体造成伤害
 * 伤害74 暴击率15 暴击伤害22 浮动0.3 攻击速度0.1s
 */
public class Saboteur extends BaseWeapon {
    
    private static final int COOLDOWN_TICKS = 2; // 冷却时间2tick（0.1秒）
    private static final Random random = new Random();
    
    public Saboteur() {
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
        }, new Properties().fireResistant(), 74, 0.1f, 1f, 0.15f, 1.22f, 0.3f, WeaponEnum.RANGED);
        
        setStory("破坏者，专为战场破坏而设计的强大武器。\n" +
                "每次攻击发射10-12发子弹形成密集火力网，\n" +
                "同时发射追踪榴弹自动寻找最近目标并爆炸。");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // 计算子弹伤害
            float bulletDamage = calculateBulletDamage(player, itemstack);
            
            // 随机发射10-12发子弹
            int bulletCount = 10 + random.nextInt(3); // 10-12发
            
            // 发射多发子弹
            for (int i = 0; i < bulletCount; i++) {
                // 创建子弹投射物
                SaboteurBulletProjectile bullet = new SaboteurBulletProjectile(level, player, bulletDamage);
                
                // 设置子弹位置和方向
                Vec3 lookVec = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();
                
                // 计算锥形散布方向
                Vec3 spreadVec = calculateBulletSpreadDirection(lookVec, i, bulletCount);
                
                bullet.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                bullet.shoot(spreadVec.x, spreadVec.y, spreadVec.z, 4.0F, 0.2F); // 4.0速度，小散布
                
                // 添加到世界
                level.addFreshEntity(bullet);
            }
            
            // 发射1颗追踪榴弹
            SaboteurGrenadeProjectile grenade = new SaboteurGrenadeProjectile(level, player, bulletDamage * 2.0f); // 榴弹伤害为子弹的2倍
            grenade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, 1.0F);
            level.addFreshEntity(grenade);
            
            // 播放攻击音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.2F);
            
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

        double spreadAngle = Math.toRadians(15);

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
        // 播放命中音效
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.6F, 0.8F);
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
}