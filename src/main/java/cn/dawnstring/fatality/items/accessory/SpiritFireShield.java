package cn.dawnstring.fatality.items.accessory;

import cn.dawnstring.fatality.items.BaseShield;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;

/**
 * 灵火护盾 - 火焰主题冲刺类饰品
 * 特点：
 * - 双击WASD键触发火焰冲刺
 * - 冲刺过程中无敌且可以穿透目标
 * - 对路径上的敌人造成火焰伤害并附加燃烧效果
 * - 最大冲刺距离8格，碰到方块自动停止
 * - 华丽的火焰粒子效果和音效
 * - 提供20点防御加成和10点血量加成
 */
@Mod.EventBusSubscriber
public class SpiritFireShield extends BaseShield
{
    public SpiritFireShield()
    {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    public SpiritFireShield(Properties properties)
    {
        super(properties);
    }

    @Override
    protected boolean hasShieldEquipped(Player player) {
        var accessoryInventory = cn.dawnstring.fatality.inventory.AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack stack = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (stack.getItem() instanceof SpiritFireShield) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float getDefenseBonus()
    {
        return 20.0f;
    }

    @Override
    public float getHealthBonus()
    {
        return 10.0f;
    }

    @Override
    protected float getDashSpeed() {
        return 4.0f;
    }

    @Override
    protected float getDashDuration() {
        return 1.4f;
    }

    @Override
    protected float getDashCooldown() {
        return 2.0f;
    }

    @Override
    protected float getDashDamage() {
        return 700.0f;
    }

    // 灵火护盾独特的火焰粒子效果
    @Override
    protected void spawnDashParticles(Player player) {
        if (player.level().isClientSide()) return;

        Level level = player.level();
        Vec3 pos = player.position();
        
        // 获取玩家的移动方向
        Vec3 motion = player.getDeltaMovement();
        if (motion.length() < 0.1) return; // 如果玩家基本静止，不生成粒子
        
        Vec3 direction = motion.normalize();

        // 火焰主题粒子效果
        double trailLength = 6.0;
        double startOffset = -2.0;

        // 生成火焰轨迹粒子
        for (int i = 0; i < 25; i++) {
            double progress = (double)i / 24;
            double distance = startOffset + progress * trailLength;
            Vec3 trailPos = pos.add(direction.scale(distance));

            for (int j = 0; j < 4; j++) {
                double horizontalOffset = (Math.random() - 0.5) * 0.5;
                double verticalOffset = (Math.random() - 0.5) * 0.7;
                Vec3 perpendicular = new Vec3(-direction.z, 0, direction.x).normalize();
                Vec3 offset = perpendicular.scale(horizontalOffset).add(0, verticalOffset, 0);
                Vec3 particlePos = trailPos.add(offset);

                // 使用火焰粒子
                level.addParticle(ParticleTypes.FLAME,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0.1, 0); // 轻微向上飘动
            }
        }

        // 轨迹末端生成火焰爆发效果
        Vec3 endPos = pos.add(direction.scale(4.0));
        for (int i = 0; i < 12; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = (Math.random() - 0.5) * 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.0;

            level.addParticle(ParticleTypes.LAVA,
                    endPos.x + offsetX, endPos.y + offsetY, endPos.z + offsetZ,
                    0, 0.2, 0);
        }

        // 添加火焰流动粒子
        for (int i = 0; i < 8; i++) {
            double flowDistance = Math.random() * trailLength + startOffset;
            Vec3 flowPos = pos.add(direction.scale(flowDistance));

            double offsetX = (Math.random() - 0.5) * 0.4;
            double offsetY = (Math.random() - 0.5) * 0.4;
            double offsetZ = (Math.random() - 0.5) * 0.4;

            level.addParticle(ParticleTypes.SMOKE,
                    flowPos.x + offsetX, flowPos.y + offsetY, flowPos.z + offsetZ,
                    direction.x * 0.2, direction.y * 0.1, direction.z * 0.2);
        }
    }
}