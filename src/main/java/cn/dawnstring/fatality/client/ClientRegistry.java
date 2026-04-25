package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.client.model.*;
import cn.dawnstring.fatality.client.renderer.*;
import cn.dawnstring.fatality.registry.ModContainers;
import cn.dawnstring.fatality.registry.ModEntities;
import cn.dawnstring.fatality.bosslist.BossListKeyBinding;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import cn.dawnstring.fatality.registry.ModItems;

@Mod.EventBusSubscriber(modid = Fatality.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistry {

    // 伤害数值渲染器实例
    private static DamageIndicatorRenderer damageIndicatorRenderer;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 注册饰品容器界面
            MenuScreens.register(ModContainers.ACCESSORY_CONTAINER.get(), AccessoryScreen::new);

            // 注册WaveBreakerProjectile渲染器
            EntityRenderers.register(ModEntities.WAVE_BREAKER_PROJECTILE.get(), WaveBreakerProjectileRenderer::new);
            // 注册FireballProjectile渲染器
            EntityRenderers.register(ModEntities.FIREBALL_PROJECTILE.get(), FireballProjectileRenderer::new);
            // 注册FireProjectile渲染器
            //EntityRenderers.register(ModEntities.FIRE_PROJECTILE.get(), FireProjectileRenderer::new);
            // 注册WaterArrowProjectile渲染器
            EntityRenderers.register(ModEntities.WATER_ARROW_PROJECTILE.get(), WaterArrowProjectileRenderer::new);
            // 注册CrystalProjectile渲染器
            EntityRenderers.register(ModEntities.CRYSTAL_PROJECTILE.get(), CrystalProjectileRenderer::new);
            // 注册DartProjectile渲染器
            EntityRenderers.register(ModEntities.DART_PROJECTILE.get(), DartProjectileRenderer::new);
            // 注册BulletProjectile渲染器
            EntityRenderers.register(ModEntities.BULLET_PROJECTILE.get(), BulletProjectileRenderer::new);
            // 注册CommandersGreataxeProjectile渲染器
            EntityRenderers.register(ModEntities.COMMANDERS_GREATAXE_PROJECTILE.get(), CommandersGreataxeProjectileRenderer::new);
            // 注册GhostProjectile渲染器
            EntityRenderers.register(ModEntities.GHOST_PROJECTILE.get(), GhostProjectileRenderer::new);
            // 注册VileDarknessEffect渲染器
            EntityRenderers.register(ModEntities.VILE_DARKNESS_EFFECT.get(), VileDarknessEffectRenderer::new);
            // 注册SacredLightBeam渲染器
            EntityRenderers.register(ModEntities.SACRED_LIGHT_BEAM.get(), SacredLightBeamRenderer::new);
            // 注册EnderSphere渲染器
            EntityRenderers.register(ModEntities.ENDER_SPHERE.get(), EnderSphereRenderer::new);
            // 注册DisasterFlyingAxeProjectile渲染器
            EntityRenderers.register(ModEntities.DISASTER_FLYING_AXE_PROJECTILE.get(), DisasterFlyingAxeProjectileRenderer::new);
            // 注册IcicleProjectile渲染器
            EntityRenderers.register(ModEntities.ICICLE_PROJECTILE.get(), IcicleProjectileRenderer::new);
            // 注册ShadowProjectile渲染器
            EntityRenderers.register(ModEntities.SHADOW_PROJECTILE.get(), ShadowProjectileRenderer::new);
            // 注册ScytheOfTheEndProjectile渲染器
            EntityRenderers.register(ModEntities.SCYTHE_OF_THE_END_PROJECTILE.get(), ScytheOfTheEndProjectileRenderer::new);
            // 注册HighEnergyElementBallProjectile渲染器
            EntityRenderers.register(ModEntities.HIGH_ENERGY_ELEMENT_BALL_PROJECTILE.get(), HighEnergyElementBallProjectileRenderer::new);
            // 注册ElementalTornadoProjectile渲染器
            EntityRenderers.register(ModEntities.ELEMENTAL_TORNADO_PROJECTILE.get(), ElementalTornadoProjectileRenderer::new);

            // 注册其他缺少渲染器的实体类型
            // 使用GenericItemProjectileRenderer为投掷物实体注册渲染器
            EntityRenderers.register(ModEntities.CURSE_FIRE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.FIRE_STAFF.get()), 0.6f));
            EntityRenderers.register(ModEntities.DRAGON_FLAME_CURSE_FIRE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.FIRE_STAFF.get()), 0.7f));
            EntityRenderers.register(ModEntities.SPECTERS_SCYTHE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.SPECTERS_SCYTHE.get()), 0.8f));
            EntityRenderers.register(ModEntities.STEEL_FEATHER_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.STEEL_FEATHER.get()), 0.5f));
            EntityRenderers.register(ModEntities.GHOSTLY_PARTICLE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.GHOSTLY_GRIMOIRE.get()), 0.4f));
            EntityRenderers.register(ModEntities.WATER_STREAM_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.HIGH_PRESSURE_WATER_GUN.get()), 0.6f));
            EntityRenderers.register(ModEntities.TRACKING_ARROW.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.BOW_OF_LIGHT.get()), 0.5f));
            EntityRenderers.register(ModEntities.ICE_SPIKE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ICE_SPIKE_SPEAR.get()), 0.6f));
            EntityRenderers.register(ModEntities.TRACKING_BULLET.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.FLOWING_LIGHT.get()), 0.4f));
            EntityRenderers.register(ModEntities.SACRED_FLYING_AXE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.SACRED_FLYING_AXE.get()), 0.8f));
            EntityRenderers.register(ModEntities.INCOMPLETE_HOLY_BEAM.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.INCOMPLETE_HOLY_SPELL_BOOK.get()), 0.5f));
            EntityRenderers.register(ModEntities.LIGHT_DARK_TRACKING_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.BOOK_OF_LIGHT_AND_DARKNESS.get()), 0.6f));
            EntityRenderers.register(ModEntities.SPEAR_OF_LIGHT_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.SPEAR_OF_LIGHT.get()), 0.7f));
            EntityRenderers.register(ModEntities.SPEAR_OF_DARKNESS_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.SPEAR_OF_DARKNESS.get()), 0.7f));
            EntityRenderers.register(ModEntities.REVENGE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.REVENGE.get()), 0.6f));
            EntityRenderers.register(ModEntities.FLOWING_LIGHT_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.FLOWING_LIGHT.get()), 0.5f));
            EntityRenderers.register(ModEntities.FURNACE_SPEAR_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.FURNACE_SPEAR.get()), 0.7f));
            EntityRenderers.register(ModEntities.ECLIPSE_FURNACE_SPEAR_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ECLIPSE_FURNACE_SPEAR.get()), 0.8f));
            EntityRenderers.register(ModEntities.ECLIPSE_ICE_SPIKE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ECLIPSE_ICE_SPIKE_SPEAR.get()), 0.7f));
            EntityRenderers.register(ModEntities.BURNING_HEAVEN_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.BURNING_HEAVEN.get()), 0.6f));
            EntityRenderers.register(ModEntities.DRAGON_SLAYER_SWORD_WAVE_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.DRAGON_SLAYER_GREATSWORD.get()), 0.8f));
            EntityRenderers.register(ModEntities.DRAGON_FLAME_BURNING_HEAVEN_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.DRAGON_FLAME_BURNING_HEAVEN.get()), 0.7f));
            EntityRenderers.register(ModEntities.DRAGON_FLAME_ASHES_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.DRAGON_FLAME_ASHES.get()), 0.5f));
            EntityRenderers.register(ModEntities.SPIRIT_FIRE_JUDGMENT_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.SPIRIT_FIRE_JUDGMENT.get()), 0.6f));
            EntityRenderers.register(ModEntities.SPIRIT_FIRE_SCATTER_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.SPIRIT_FIRE_JUDGMENT.get()), 0.5f));
            EntityRenderers.register(ModEntities.ETERNAL_NIGHT_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ETERNAL_NIGHT.get()), 0.6f));
            EntityRenderers.register(ModEntities.SHADOW_SPEAR_PROJECTILE.get(), 
                context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.SHADOW_SPEAR.get()), 0.7f));
           
            EntityRenderers.register(ModEntities.DEATH_RAY_LASER_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.DEATH_RAY.get()), 0.7f));
            EntityRenderers.register(ModEntities.STAR_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.STAR.get()), 0.7f));
            EntityRenderers.register(ModEntities.TRACKING_STAR.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.STAR.get()), 0.7f));
            EntityRenderers.register(ModEntities.STAR_SPEAR_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.STAR.get()), 0.7f));
            EntityRenderers.register(ModEntities.TORNADO_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.TORNADO.get()), 0.7f));
            EntityRenderers.register(ModEntities.NATURAL_TORNADO_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.NATURAL_TORNADO.get()), 0.7f));
            EntityRenderers.register(ModEntities.TSUNAMI_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.TSUNAMI.get()), 0.7f));
            EntityRenderers.register(ModEntities.BUBBLE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.BUBBLE_GUN.get()), 0.5f));
            EntityRenderers.register(ModEntities.BLOOD_TORNADO_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.BLOOD_TORNADO.get()), 0.9f));
            EntityRenderers.register(ModEntities.ERODING_WAVE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ERODING_WAVE.get()), 0.7f));
            EntityRenderers.register(ModEntities.POLARIZING_PRISM_LASER_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.POLARIZING_PRISM.get()), 0.3f));
            EntityRenderers.register(ModEntities.BROKEN_BLADE_SWORD_WAVE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.BROKEN_BLADE.get()), 0.8f));
            EntityRenderers.register(ModEntities.HARPOON_SPEAR_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.HARPOON.get()), 0.6f));
            EntityRenderers.register(ModEntities.FISH_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.HARPOON.get()), 0.4f));
            EntityRenderers.register(ModEntities.SKYFIRE_SPEAR_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.SKYFIRE.get()), 0.6f));
            EntityRenderers.register(ModEntities.PHOENIX_RAY_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.PHOENIX.get()), 0.7f));
            EntityRenderers.register(ModEntities.CELESTIAL_STAR_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.CELESTIAL_STAR.get()), 0.5f));
            EntityRenderers.register(ModEntities.CALAMITY_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.CALAMITY.get()), 0.8f));
            EntityRenderers.register(ModEntities.GREEN_LEAF_FLYING_KNIFE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.GREEN_LEAF_FLYING_KNIFE.get()), 0.4f));
            EntityRenderers.register(ModEntities.DEATH_SCYTHE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.DEATH_SCYTHE.get()), 0.7f));
            EntityRenderers.register(ModEntities.DEATH_SCYTHE_STATIC_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.DEATH_SCYTHE.get()), 0.6f));
            EntityRenderers.register(ModEntities.DEATH_SCYTHE_SWORD_WAVE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.DEATH_SCYTHE.get()), 0.8f));
            EntityRenderers.register(ModEntities.PURGATORY_MISSILE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.PURGATORY_STAFF.get()), 0.6f));
            EntityRenderers.register(ModEntities.CHAOS_BULLET_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.CHAOS.get()), 0.4f));
            EntityRenderers.register(ModEntities.CHAOS_BARRAGE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.CHAOS.get()), 0.5f));
            EntityRenderers.register(ModEntities.ELEMENTAL_BARRAGE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ELEMENTAL_STAFF.get()), 0.6f));
            EntityRenderers.register(ModEntities.TRACER_LIGHT_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.TRACER_LIGHT.get()), 0.3f));
            EntityRenderers.register(ModEntities.STARRY_JUDGMENT_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.STARRY_JUDGMENT.get()), 0.7f));
            EntityRenderers.register(ModEntities.ANNIHILATION_BULLET_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ANNIHILATION_JUDGMENT.get()), 0.5f));
            EntityRenderers.register(ModEntities.ABYSS_OF_CALAMITY_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ABYSS_OF_CALAMITY.get()), 0.8f));
            EntityRenderers.register(ModEntities.NATURAL_WILL_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.NATURAL_WILL.get()), 0.6f));
            EntityRenderers.register(ModEntities.DIVINE_SPEAR_DISASTER_BREAKER_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.DIVINE_SPEAR_DISASTER_BREAKER.get()), 0.9f));
            EntityRenderers.register(ModEntities.SHATTERED_REALM_ARROW.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.BOW_OF_SHATTERED_REALM.get()), 0.5f));
            EntityRenderers.register(ModEntities.KINETIC_EXPLOSION_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ULTIMATE_SUBMACHINE_GUN.get()), 0.7f));
            EntityRenderers.register(ModEntities.ELECTROMAGNETIC_CHAIN_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ULTIMATE_SUBMACHINE_GUN.get()), 0.6f));
            EntityRenderers.register(ModEntities.GRAVITY_BLACKHOLE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ULTIMATE_SUBMACHINE_GUN.get()), 0.8f));
            EntityRenderers.register(ModEntities.SINGULARITY_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.ULTIMATE_SUBMACHINE_GUN.get()), 0.9f));
            EntityRenderers.register(ModEntities.JUNGLE_SWORD_WAVE_PROJECTILE.get(),
                    context -> new GenericItemProjectileRenderer<>(context, new ItemStack(ModItems.JUNGLE_SCEPTER.get()), 0.9f));
            // 注册伤害数值指示器渲染器
            registerDamageIndicatorRenderer();
        });
    }

    /**
     * 注册伤害数值指示器渲染器
     */
    private static void registerDamageIndicatorRenderer() {
        damageIndicatorRenderer = new DamageIndicatorRenderer();
        MinecraftForge.EVENT_BUS.register(damageIndicatorRenderer);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // 注册属性面板快捷键
        event.register(KeyBindings.OPEN_ATTRIBUTE_PANEL);
        // 注册boss列表快捷键
        event.register(BossListKeyBinding.OPEN_BOSS_LIST);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        // 注册BulletProjectile模型层定义
        event.registerLayerDefinition(ModModelLayers.BULLET_PROJECTILE_LAYER, BulletProjectileModel::createBodyLayer);
        // 注册TrainingPuppet模型层定义
        event.registerLayerDefinition(ModModelLayers.TRAINING_PUPPET_LAYER, TrainingPuppetModel::createBodyLayer);
        // 注册CommanderOfTheUndeadGuard模型层定义
        event.registerLayerDefinition(CommanderOfTheUndeadGuardModel.LAYER_LOCATION, CommanderOfTheUndeadGuardModel::createBodyLayer);
    }
}