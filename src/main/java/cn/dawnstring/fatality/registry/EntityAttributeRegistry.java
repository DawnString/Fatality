package cn.dawnstring.fatality.registry;

import cn.dawnstring.fatality.entity.TrainingPuppet;
import cn.dawnstring.fatality.entity.basemonster.desertbeetle.DesertBeetle;
import cn.dawnstring.fatality.entity.basemonster.goblin.Goblin;
import cn.dawnstring.fatality.entity.basemonster.littleghost.LittleGhost;
import cn.dawnstring.fatality.entity.basemonster.spirit.Spirit;
import cn.dawnstring.fatality.entity.boss.ExampleBoss;
import cn.dawnstring.fatality.entity.boss.commanderoftheundeadguard.CommanderOfTheUndeadGuard;
import cn.dawnstring.fatality.entity.boss.endofnightmare.EndOfNightmare;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class EntityAttributeRegistry {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(EntityAttributeRegistry::onEntityAttributeCreation);
    }

    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        // 注册ExampleBoss的属性
        event.put(ModEntities.EXAMPLE_BOSS.get(), ExampleBoss.createAttributes().build());
        // 注册EndOfNightmare Boss的属性
        event.put(ModEntities.END_OF_NIGHTMARE.get(), EndOfNightmare.createAttributes().build());
        // 注册TrainingPuppet的属性
        event.put(ModEntities.TRAINING_PUPPET.get(), TrainingPuppet.createAttributes().build());
        // 注册Spirit的属性
        event.put(ModEntities.SPIRIT.get(), Spirit.createAttributes().build());
        // 注册DesertBeetle的属性
        event.put(ModEntities.DESERT_BEETLE.get(), DesertBeetle.createAttributes().build());
        // 注册Goblin的属性
        event.put(ModEntities.GOBLIN.get(), Goblin.createAttributes().build());
        // 注册LittleGhost的属性
        event.put(ModEntities.LITTLE_GHOST.get(), LittleGhost.createAttributes().build());
        // 注册CommanderOftTheUndeadGuard Boss的属性
        event.put(ModEntities.COMMANDER_OF_THE_UNDEAD_GUARD.get(), CommanderOfTheUndeadGuard.createAttributes().build());
    }
}