package cn.dawnstring.fatality.gamestage;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 游戏阶段事件处理器 - 基于世界阶段
 */
@Mod.EventBusSubscriber(modid = Fatality.MODID, value = Dist.DEDICATED_SERVER)
public class GameStageEvents {

    /**
     * 实体生成时应用世界阶段增益
     */
    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity && !(livingEntity instanceof net.minecraft.world.entity.player.Player)) {
            // 对敌对生物和动物应用当前世界阶段的增益
            if (isHostileOrNeutralEntity(livingEntity)) {
                GameStageManager.applyStageModifiers(livingEntity);
            }
        }
    }

    /**
     * 判断实体是否为敌对或中立生物
     */
    private static boolean isHostileOrNeutralEntity(LivingEntity entity) {
        // 敌对生物
        if (entity instanceof Monster) {
            return true;
        }
        // 动物（中立生物）
        if (entity instanceof Animal) {
            return true;
        }
        // Mob类型的其他实体
        if (entity instanceof Mob) {
            return true;
        }
        // 其他可能具有攻击性的实体
        return entity.getType().getCategory().isFriendly() == false;
    }
}