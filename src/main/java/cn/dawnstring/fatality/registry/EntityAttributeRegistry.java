package cn.dawnstring.fatality.registry;

import cn.dawnstring.fatality.entity.TrainingPuppet;
import cn.dawnstring.fatality.entity.boss.lordofender.LordOfEnderEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class EntityAttributeRegistry {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(EntityAttributeRegistry::onEntityAttributeCreation);
    }

    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event)
    {
        // 注册TrainingPuppet的属性
        event.put(ModEntities.TRAINING_PUPPET.get(), TrainingPuppet.createAttributes().build());

        // 注册LordOfEnder的属性
        event.put(ModEntities.LORD_OF_ENDER.get(), LordOfEnderEntity.createAttributes().build());
    }
}