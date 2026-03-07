package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.BaseShield;
import cn.dawnstring.fatality.items.AccessoryItem;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
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
 * 神圣护盾 - 神圣主题冲刺类饰品
 * 特点：
 * - 双击WASD键触发神圣冲刺
 * - 冲刺过程中无敌且可以穿透目标
 * - 对路径上的敌人造成神圣伤害并附加虚弱效果
 * - 最大冲刺距离8格，碰到方块自动停止
 * - 华丽的神圣粒子效果和音效
 * - 提供30点防御加成和25点血量加成
 */
@Mod.EventBusSubscriber
public class DivineShield extends BaseShield
{
    public DivineShield()
    {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    public DivineShield(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean hasShieldEquipped(Player player)
    {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (stack.getItem() instanceof DivineShield) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float getDefenseBonus()
    {
        return 30.0f;
    }

    @Override
    public float getHealthBonus()
    {
        return 25.0f;
    }

    @Override
    protected float getDashSpeed() {
        return 4.0f;
    }

    @Override
    protected float getDashDuration() {
        return 1.0f;
    }

    @Override
    protected float getDashCooldown() {
        return 2.5f;
    }

    @Override
    protected float getDashDamage() {
        return 300.0f;
    }

    // 神圣护盾独特的神圣粒子效果
    @Override
    protected void spawnDashParticles(Player player) {
        if (player.level().isClientSide()) return;

        Level level = player.level();
        Vec3 pos = player.position();
        
        // 获取玩家的移动方向
        Vec3 motion = player.getDeltaMovement();
        if (motion.length() < 0.1) return; // 如果玩家基本静止，不生成粒子
        
        Vec3 direction = motion.normalize();

        // 神圣主题粒子效果
        double trailLength = 4.5;
        double startOffset = -1.0;

        // 生成神圣光环轨迹粒子
        for (int i = 0; i < 18; i++) {
            double progress = (double)i / 17;
            double distance = startOffset + progress * trailLength;
            Vec3 trailPos = pos.add(direction.scale(distance));

            // 在轨迹周围生成光环粒子
            for (int j = 0; j < 5; j++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = 0.3 + Math.random() * 0.4;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (Math.random() - 0.5) * 0.3;

                // 使用金色和白色粒子交替
                net.minecraft.core.particles.ParticleOptions particleType = Math.random() > 0.3 ? ParticleTypes.GLOW : ParticleTypes.WHITE_ASH;

                level.addParticle(particleType,
                        trailPos.x + offsetX, trailPos.y + offsetY, trailPos.z + offsetZ,
                        0, 0.08, 0);
            }
        }

        // 轨迹末端生成神圣爆发效果
        Vec3 endPos = pos.add(direction.scale(3.0));
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetY = (Math.random() - 0.5) * 0.8;
            double offsetZ = (Math.random() - 0.5) * 0.8;

            // 神圣爆发粒子
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                    (Math.random() - 0.5) * 0.2, (Math.random() - 0.5) * 0.2, (Math.random() - 0.5) * 0.2);
        }

        // 添加天使光环效果
        for (int i = 0; i < 6; i++) {
            double haloHeight = 1.5 + Math.random() * 0.5;
            double haloRadius = 0.8;
            double haloAngle = Math.random() * Math.PI * 2;

            double haloX = pos.x + Math.cos(haloAngle) * haloRadius;
            double haloY = pos.y + haloHeight;
            double haloZ = pos.z + Math.sin(haloAngle) * haloRadius;

            level.addParticle(ParticleTypes.GLOW,
                    haloX, haloY, haloZ,
                    0, 0.05, 0);
        }

        // 添加神圣流动粒子
        for (int i = 0; i < 12; i++) {
            double flowDistance = Math.random() * trailLength + startOffset;
            Vec3 flowPos = pos.add(direction.scale(flowDistance));

            // 神圣流动粒子
            level.addParticle(ParticleTypes.WHITE_ASH,
                    flowPos.x, flowPos.y + 0.3, flowPos.z,
                    direction.x * 0.15, direction.y * 0.05, direction.z * 0.15);
        }

        // 添加圣光粒子效果
        for (int i = 0; i < 8; i++) {
            double lightDistance = Math.random() * trailLength + startOffset;
            Vec3 lightPos = pos.add(direction.scale(lightDistance));

            // 圣光粒子向上飘动
            level.addParticle(ParticleTypes.GLOW,
                    lightPos.x, lightPos.y, lightPos.z,
                    0, 0.2, 0);
        }
    }
}