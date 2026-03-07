package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.BaseShield;
import cn.dawnstring.fatality.items.AccessoryItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 元素阵型护盾 - 元素主题冲刺类饰品
 * 特点：
 * - 双击WASD键触发元素冲刺
 * - 冲刺过程中无敌且可以穿透目标
 * - 对路径上的敌人造成元素伤害并附加元素效果
 * - 最大冲刺距离7格，碰到方块自动停止
 * - 华丽的元素粒子效果和音效
 * - 提供25点防御加成和20点血量加成
 */
@Mod.EventBusSubscriber
public class ElementalFormationShield extends BaseShield
{
    public ElementalFormationShield()
    {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    public ElementalFormationShield(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean hasShieldEquipped(Player player) {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (stack.getItem() instanceof ElementalFormationShield) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float getDefenseBonus()
    {
        return 25.0f;
    }

    @Override
    public float getHealthBonus()
    {
        return 20.0f;
    }

    @Override
    protected float getDashSpeed() {
        return 4.2f;
    }

    @Override
    protected float getDashDuration() {
        return 1.5f;
    }

    @Override
    protected float getDashCooldown() {
        return 2.0f;
    }

    @Override
    protected float getDashDamage() {
        return 2500.0f;
    }

    // 元素阵盾独特的元素粒子效果
    @Override
    protected void spawnDashParticles(Player player) {
        if (player.level().isClientSide()) return;

        Level level = player.level();
        Vec3 pos = player.position();
        
        // 获取玩家的移动方向
        Vec3 motion = player.getDeltaMovement();
        if (motion.length() < 0.1) return; // 如果玩家基本静止，不生成粒子
        
        Vec3 direction = motion.normalize();

        // 元素主题粒子效果（火、水、土、风）
        double trailLength = 5.0;
        double startOffset = -1.5;

        // 生成四种元素的轨迹粒子
        for (int i = 0; i < 20; i++) {
            double progress = (double)i / 19;
            double distance = startOffset + progress * trailLength;
            Vec3 trailPos = pos.add(direction.scale(distance));

            // 根据位置选择不同的元素粒子
            net.minecraft.core.particles.ParticleOptions particleType;
            if (progress < 0.25) {
                particleType = ParticleTypes.FLAME; // 火元素
            } else if (progress < 0.5) {
                particleType = ParticleTypes.DRIPPING_WATER; // 水元素
            } else if (progress < 0.75) {
                particleType = ParticleTypes.ASH; // 土元素
            } else {
                particleType = ParticleTypes.CLOUD; // 风元素
            }

            // 在轨迹周围生成元素粒子
            for (int j = 0; j < 3; j++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = Math.random() * 0.6;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (Math.random() - 0.5) * 0.4;

                level.addParticle(particleType,
                        trailPos.x + offsetX, trailPos.y + offsetY, trailPos.z + offsetZ,
                        0, 0.05, 0);
            }
        }

        // 轨迹末端生成元素爆发效果
        Vec3 endPos = pos.add(direction.scale(3.5));
        for (int i = 0; i < 15; i++) {
            double offsetX = (Math.random() - 0.5) * 1.2;
            double offsetY = (Math.random() - 0.5) * 1.2;
            double offsetZ = (Math.random() - 0.5) * 1.2;

            // 随机选择元素粒子
            net.minecraft.core.particles.ParticleOptions[] elementParticles = {
                    ParticleTypes.FLAME,
                    ParticleTypes.DRIPPING_WATER,
                    ParticleTypes.ASH,
                    ParticleTypes.CLOUD
            };
            net.minecraft.core.particles.ParticleOptions selectedParticle = elementParticles[(int)(Math.random() * elementParticles.length)];

            level.addParticle(selectedParticle,
                    endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                    (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.1);
        }

        // 添加元素流动粒子
        for (int i = 0; i < 10; i++) {
            double flowDistance = Math.random() * trailLength + startOffset;
            Vec3 flowPos = pos.add(direction.scale(flowDistance));

            // 随机选择流动粒子类型
            net.minecraft.core.particles.ParticleOptions flowParticle = Math.random() > 0.5 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.GLOW;

            level.addParticle(flowParticle,
                    flowPos.x, flowPos.y + 0.5, flowPos.z,
                    direction.x * 0.3, direction.y * 0.1, direction.z * 0.3);
        }
    }
}