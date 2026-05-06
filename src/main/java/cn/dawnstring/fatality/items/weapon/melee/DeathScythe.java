package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.entity.projectile.DeathScytheProjectile;
import cn.dawnstring.fatality.entity.projectile.DeathScytheStaticProjectile;
import cn.dawnstring.fatality.entity.projectile.DeathScytheSwordWaveProjectile;
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

/**
 *死灵镰刀
 *近战武器
 *左键挥舞镰刀，召唤剑气飞出
 *长按右键蓄力，蓄力3s，蓄力完成后玩家周围散发红色粒子提示玩家蓄力完成，松开右键投掷镰刀，并在路径上留下静止的弹幕，生物碰到弹幕会造成伤害（除开玩家）
 *左键镰刀伤害为基础伤害
 *长按右键飞出的镰刀伤害为基础伤害的1.2倍，弹幕伤害为基础伤害的0.8倍
 *伤害2790 暴击率28 暴击伤害36 浮动0.2 攻击速度1s
 **/
public class DeathScythe extends BaseWeapon
{
    private static final int SWORD_WAVE_COOLDOWN_TICKS = 10; // 剑气冷却时间10tick（0.5秒）
    private static final int SCYTHE_THROW_COOLDOWN_TICKS = 60; // 镰刀投掷冷却时间60tick（3秒）
    private static final float BASE_SCYTHE_DAMAGE = 2790.0f; // 基础镰刀伤害
    
    // 右键蓄力相关变量
    private boolean isCharging = false;
    private int chargeTicks = 0;
    private static final int MAX_CHARGE_TICKS = 60; // 最大蓄力时间3秒

    public DeathScythe()
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
        }, new Properties(), 2790, 1f, 1f, 0.28f, 0.36f, 0.2f, WeaponEnum.MELEE);

        // 设置物品故事
        setStory("死亡的象征，收割生命的镰刀。\n" +
                "左键挥舞释放死亡剑气，右键蓄力投掷镰刀，\n" +
                "在战场上留下静止的死亡弹幕。");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 检查是否在冷却中
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }
        
        // 左键攻击（召唤剑气）
        if (hand == InteractionHand.MAIN_HAND && !player.isShiftKeyDown()) {
            return useLeftClick(level, player, hand);
        }
        // 右键蓄力（投掷镰刀）
        else if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
            return useRightClick(level, player, hand);
        }
        
        return InteractionResultHolder.pass(itemstack);
    }
    
    /**
     * 左键攻击 - 召唤剑气
     */
    private InteractionResultHolder<ItemStack> useLeftClick(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 计算剑气伤害
            float swordWaveDamage = calculateSwordWaveDamage(player, itemstack);
            
            // 创建死亡镰刀剑气投射物
            DeathScytheSwordWaveProjectile swordWave = new DeathScytheSwordWaveProjectile(level, player, swordWaveDamage);
            
            // 设置投射物位置和方向
            swordWave.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            swordWave.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F); // 快速飞行速度
            
            level.addFreshEntity(swordWave);
            
            // 播放死亡剑气音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, SWORD_WAVE_COOLDOWN_TICKS);

        return InteractionResultHolder.success(itemstack);
    }
    
    /**
     * 右键蓄力 - 投掷镰刀
     */
    private InteractionResultHolder<ItemStack> useRightClick(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 开始蓄力
        if (!isCharging) {
            isCharging = true;
            chargeTicks = 0;
            
            // 播放蓄力开始音效
            if (!level.isClientSide()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.5F, 0.7F);
            }
            
            return InteractionResultHolder.success(itemstack);
        }
        
        return InteractionResultHolder.pass(itemstack);
    }
    
    /**
     * 计算剑气伤害
     */
    public float calculateSwordWaveDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    /**
     * 计算镰刀投掷伤害
     */
    public float calculateScytheDamage(Player player, ItemStack stack) {
        return calculateFinalDamage(player, stack, null);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        // 处理蓄力逻辑
        if (isCharging && entity instanceof Player player) {
            chargeTicks++;
            
            // 蓄力粒子效果
            if (level.isClientSide() && chargeTicks % 5 == 0) {
                Vec3 pos = player.position();
                for (int i = 0; i < 3; i++) {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                            pos.x + (Math.random() - 0.5) * 2.0,
                            pos.y + 1.0 + (Math.random() - 0.5) * 1.5,
                            pos.z + (Math.random() - 0.5) * 2.0,
                            0, 0.1, 0);
                }
            }
            
            // 蓄力完成，投掷镰刀
            if (chargeTicks >= MAX_CHARGE_TICKS) {
                if (!level.isClientSide()) {
                    // 计算镰刀伤害
                    float scytheDamage = calculateScytheDamage(player, stack);

                    // 创建镰刀投射物
                    DeathScytheProjectile scythe = new DeathScytheProjectile(level, player, stack, scytheDamage);

                    // 设置镰刀位置和方向
                    Vec3 lookVec = player.getLookAngle();
                    Vec3 eyePos = player.getEyePosition();

                    scythe.setPos(eyePos.x + lookVec.x, eyePos.y + lookVec.y, eyePos.z + lookVec.z);
                    scythe.shoot(lookVec.x, lookVec.y, lookVec.z, 2.0F, 0.2F); // 中等速度，较小散布

                    // 添加到世界
                    level.addFreshEntity(scythe);

                    // 播放投掷音效
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.6F);
                }
                
                // 重置蓄力状态
                isCharging = false;
                chargeTicks = 0;
                
                // 设置冷却时间
                player.getCooldowns().addCooldown(this, SCYTHE_THROW_COOLDOWN_TICKS);
            }
        }
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity, int timeLeft) {
        super.releaseUsing(stack, level, entity, timeLeft);
        
        // 如果提前释放蓄力，重置状态
        if (isCharging && entity instanceof Player) {
            isCharging = false;
            chargeTicks = 0;
            
            // 播放取消蓄力音效
            if (!level.isClientSide()) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.3F, 1.0F);
            }
        }
    }
}