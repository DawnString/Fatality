package cn.dawnstring.fatality.registry;

import cn.dawnstring.fatality.entity.*;
import cn.dawnstring.fatality.entity.projectile.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities
{
    public static final RegistryObject<EntityType<WaveBreakerProjectile>> WAVE_BREAKER_PROJECTILE =
            ModRegistry.ENTITIES.register("wave_breaker_projectile",
                    () -> EntityType.Builder.<WaveBreakerProjectile>of(WaveBreakerProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("wave_breaker_projectile"));
    public static final RegistryObject<EntityType<FireballProjectile>> FIREBALL_PROJECTILE = ModRegistry.ENTITIES.register("fireball_projectile",
            () -> EntityType.Builder.<FireballProjectile>of(FireballProjectile::new, MobCategory.MISC)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("fireball_projectile"));
    public static final RegistryObject<EntityType<WaterArrowProjectile>> WATER_ARROW_PROJECTILE = ModRegistry.ENTITIES.register("water_arrow_projectile",
            () -> EntityType.Builder.<WaterArrowProjectile>of(WaterArrowProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("water_arrow_projectile"));
    public static final RegistryObject<EntityType<CrystalProjectile>> CRYSTAL_PROJECTILE =
            ModRegistry.ENTITIES.register("crystal_projectile",
                    () -> EntityType.Builder.<CrystalProjectile>of(CrystalProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 水晶尺寸较小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("crystal_projectile"));
    public static final RegistryObject<EntityType<DartProjectile>> DART_PROJECTILE =
            ModRegistry.ENTITIES.register("dart_projectile",
                    () -> EntityType.Builder.<DartProjectile>of(DartProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 飞镖尺寸更小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("dart_projectile"));
    public static final RegistryObject<EntityType<BulletProjectile>> BULLET_PROJECTILE =
            ModRegistry.ENTITIES.register("bullet_projectile",
                    () -> EntityType.Builder.<BulletProjectile>of(BulletProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 子弹尺寸更小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("bullet_projectile"));
    public static final RegistryObject<EntityType<FireProjectile>> FIRE_PROJECTILE =
            ModRegistry.ENTITIES.register("fire_projectile",
                    () -> EntityType.Builder.<FireProjectile>of(FireProjectile::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("fire_projectile"));
    public static final RegistryObject<EntityType<TornadoEffect>> TORNADO_EFFECT =
            ModRegistry.ENTITIES.register("tornado_effect",
                    () -> EntityType.Builder.<TornadoEffect>of(TornadoEffect::new, MobCategory.MISC)
                            .sized(4.0f, 4.0f)  // 龙卷风效果尺寸
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("tornado_effect"));
    public static final RegistryObject<EntityType<CommandersGreataxeProjectile>> COMMANDERS_GREATAXE_PROJECTILE =
            ModRegistry.ENTITIES.register("commanders_greataxe_projectile",
                    () -> EntityType.Builder.<CommandersGreataxeProjectile>of(CommandersGreataxeProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 设置合适的大小
                            .clientTrackingRange(4)
                            .updateInterval(1)  // 更频繁的更新
                            .build("commanders_greataxe_projectile"));
    public static final RegistryObject<EntityType<GhostProjectile>> GHOST_PROJECTILE = ModRegistry.ENTITIES.register("ghost_projectile",
            () -> EntityType.Builder.<GhostProjectile>of(GhostProjectile::new, MobCategory.MISC)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("ghost_projectile"));
    public static final RegistryObject<EntityType<VileDarknessEffect>> VILE_DARKNESS_EFFECT =
            ModRegistry.ENTITIES.register("vile_darkness_effect",
                    () -> EntityType.Builder.<VileDarknessEffect>of(VileDarknessEffect::new, MobCategory.MISC)
                            .sized(4.0f, 4.0f)  // 范围效果尺寸
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("vile_darkness_effect"));
    public static final RegistryObject<EntityType<DisasterFlyingAxeProjectile>> DISASTER_FLYING_AXE_PROJECTILE =
            ModRegistry.ENTITIES.register("disaster_flying_axe_projectile",
                    () -> EntityType.Builder.<DisasterFlyingAxeProjectile>of(DisasterFlyingAxeProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 设置合适的大小
                            .clientTrackingRange(4)
                            .updateInterval(1)  // 更频繁的更新
                            .build("disaster_flying_axe_projectile"));
    public static final RegistryObject<EntityType<IcicleProjectile>> ICICLE_PROJECTILE = ModRegistry.ENTITIES.register("icicle_projectile",
            () -> EntityType.Builder.<IcicleProjectile>of(IcicleProjectile::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("icicle_projectile"));
    public static final RegistryObject<EntityType<ShadowProjectile>> SHADOW_PROJECTILE =
            ModRegistry.ENTITIES.register("shadow_projectile",
                    () -> EntityType.Builder.<ShadowProjectile>of(ShadowProjectile::new, MobCategory.MISC)
                            .sized(0.8f, 0.8f)  // 暗影投射物尺寸较大
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("shadow_projectile"));
    public static final RegistryObject<EntityType<ScytheOfTheEndProjectile>> SCYTHE_OF_THE_END_PROJECTILE =
            ModRegistry.ENTITIES.register("scythe_of_the_end_projectile",
                    () -> EntityType.Builder.<ScytheOfTheEndProjectile>of(ScytheOfTheEndProjectile::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)  // 镰刀投射物尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("scythe_of_the_end_projectile"));
    
    public static final RegistryObject<EntityType<SpectersScytheProjectile>> SPECTERS_SCYTHE_PROJECTILE =
            ModRegistry.ENTITIES.register("specters_scythe_projectile",
                    () -> EntityType.Builder.<SpectersScytheProjectile>of(SpectersScytheProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 幽灵镰刀投射物尺寸稍小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("specters_scythe_projectile"));
    
    // SteelFeather钢羽投射物实体
    public static final RegistryObject<EntityType<SteelFeatherProjectile>> STEEL_FEATHER_PROJECTILE =
            ModRegistry.ENTITIES.register("steel_feather_projectile",
                    () -> EntityType.Builder.<SteelFeatherProjectile>of(SteelFeatherProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 钢羽尺寸较小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("steel_feather_projectile"));
    
    // GhostlyGrimoire幽灵粒子投射物实体
    public static final RegistryObject<EntityType<GhostlyParticleProjectile>> GHOSTLY_PARTICLE_PROJECTILE =
            ModRegistry.ENTITIES.register("ghostly_particle_projectile",
                    () -> EntityType.Builder.<GhostlyParticleProjectile>of(GhostlyParticleProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 幽灵粒子尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("ghostly_particle_projectile"));
    
    // HighPressureWaterGun水柱投射物实体
    public static final RegistryObject<EntityType<WaterStreamProjectile>> WATER_STREAM_PROJECTILE =
            ModRegistry.ENTITIES.register("water_stream_projectile",
                    () -> EntityType.Builder.<WaterStreamProjectile>of(WaterStreamProjectile::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)  // 水柱尺寸较小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("water_stream_projectile"));

    public static final RegistryObject<EntityType<TrackingArrow>> TRACKING_ARROW =
            ModRegistry.ENTITIES.register("tracking_arrow",
                    () -> EntityType.Builder.<TrackingArrow>of(TrackingArrow::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("tracking_arrow_projectile"));
    
    public static final RegistryObject<EntityType<ElementalMissileProjectile>> ELEMENTAL_MISSILE_PROJECTILE =
            ModRegistry.ENTITIES.register("elemental_missile_projectile", () ->
                    EntityType.Builder.<ElementalMissileProjectile>of(ElementalMissileProjectile::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("elemental_missile_projectile"));

    public static final RegistryObject<EntityType<ElementalSpearProjectile>> ELEMENTAL_SPEAR_PROJECTILE =
            ModRegistry.ENTITIES.register("elemental_spear_projectile", () ->
                    EntityType.Builder.<ElementalSpearProjectile>of(ElementalSpearProjectile::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("elemental_spear_projectile"));
    public static final RegistryObject<EntityType<GaussLaserProjectile>> GAUSS_LASER_PROJECTILE =
            ModRegistry.ENTITIES.register("gauss_laser_projectile", () ->
                    EntityType.Builder.<GaussLaserProjectile>of(GaussLaserProjectile::new, MobCategory.MISC)
                            .sized(0.3F, 0.3F)  // 激光尺寸较小
                            .clientTrackingRange(8)  // 激光需要更远的追踪范围
                            .updateInterval(1)  // 更频繁的更新
                            .build("gauss_laser_projectile"));
    public static final RegistryObject<EntityType<HighEnergyElementBallProjectile>> HIGH_ENERGY_ELEMENT_BALL_PROJECTILE =
            ModRegistry.ENTITIES.register("high_energy_element_ball_projectile", () ->
                    EntityType.Builder.<HighEnergyElementBallProjectile>of(HighEnergyElementBallProjectile::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)  // 元素球尺寸较大
                            .clientTrackingRange(6)
                            .updateInterval(10)
                            .build("high_energy_element_ball_projectile"));
    public static final RegistryObject<EntityType<MagneticBurstProjectile>> MAGNETIC_BURST_PROJECTILE =
            ModRegistry.ENTITIES.register("magnetic_burst_projectile", () ->
                    EntityType.Builder.<MagneticBurstProjectile>of(MagneticBurstProjectile::new, MobCategory.MISC)
                            .sized(0.6F, 0.6F)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("magnetic_burst_projectile"));
    public static final RegistryObject<EntityType<ElementalTornadoProjectile>> ELEMENTAL_TORNADO_PROJECTILE =
            ModRegistry.ENTITIES.register("elemental_tornado_projectile", () ->
                    EntityType.Builder.<ElementalTornadoProjectile>of(ElementalTornadoProjectile::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)  // 龙卷风尺寸较大
                            .clientTrackingRange(6)
                            .updateInterval(10)
                            .build("elemental_tornado_projectile"));

    // IceSpikeSpear冰刺投射物实体
    public static final RegistryObject<EntityType<IceSpikeProjectile>> ICE_SPIKE_PROJECTILE =
            ModRegistry.ENTITIES.register("ice_spike_projectile", () ->
                    EntityType.Builder.<IceSpikeProjectile>of(IceSpikeProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 冰刺尺寸适中
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("ice_spike_projectile"));
    public static final RegistryObject<EntityType<OriginLaserProjectile>> ORIGIN_LASER_PROJECTILE =
            ModRegistry.ENTITIES.register("origin_laser_projectile",
                    () -> EntityType.Builder.<OriginLaserProjectile>of(OriginLaserProjectile::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build("origin_laser_projectile"));

    // BloodSpiritFlowingLight追踪子弹实体
    public static final RegistryObject<EntityType<TrackingBulletProjectile>> TRACKING_BULLET =
            ModRegistry.ENTITIES.register("tracking_bullet",
                    () -> EntityType.Builder.<TrackingBulletProjectile>of(TrackingBulletProjectile::new, MobCategory.MISC)
                            .sized(0.3F, 0.3F) // 子弹尺寸较小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("tracking_bullet"));

    // BookOfEnder的末影球体实体
    public static final RegistryObject<EntityType<EnderSphere>> ENDER_SPHERE =
            ModRegistry.ENTITIES.register("ender_sphere",
                    () -> EntityType.Builder.<EnderSphere>of(EnderSphere::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F) // 球体尺寸为1x1
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("ender_sphere"));

    // BookOfSacredLight的神圣光芒光束实体
    public static final RegistryObject<EntityType<SacredLightBeam>> SACRED_LIGHT_BEAM =
            ModRegistry.ENTITIES.register("sacred_light_beam",
                    () -> EntityType.Builder.<SacredLightBeam>of(SacredLightBeam::new, MobCategory.MISC)
                            .sized(4.0f, 4.0f)  // 光束效果尺寸
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("sacred_light_beam"));

    // SacredFlyingAxe的神圣飞斧投射物实体
    public static final RegistryObject<EntityType<SacredFlyingAxeProjectile>> SACRED_FLYING_AXE_PROJECTILE =
            ModRegistry.ENTITIES.register("sacred_flying_axe_projectile",
                    () -> EntityType.Builder.<SacredFlyingAxeProjectile>of(SacredFlyingAxeProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 飞斧尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("sacred_flying_axe_projectile"));

    // IncompleteHolySpellBook的不完整神圣光束实体
    public static final RegistryObject<EntityType<IncompleteHolyBeam>> INCOMPLETE_HOLY_BEAM =
            ModRegistry.ENTITIES.register("incomplete_holy_beam",
                    () -> EntityType.Builder.<IncompleteHolyBeam>of(IncompleteHolyBeam::new, MobCategory.MISC)
                            .sized(4.0f, 4.0f)  // 光束效果尺寸
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("incomplete_holy_beam"));

    // BookOfLightAndDarkness的光暗追踪投射物实体
    public static final RegistryObject<EntityType<LightDarkTrackingProjectile>> LIGHT_DARK_TRACKING_PROJECTILE =
            ModRegistry.ENTITIES.register("light_dark_tracking_projectile",
                    () -> EntityType.Builder.<LightDarkTrackingProjectile>of(LightDarkTrackingProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 光暗追踪投射物尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("light_dark_tracking_projectile"));

    // SpearOfLight的光之长矛投射物实体
    public static final RegistryObject<EntityType<SpearOfLightProjectile>> SPEAR_OF_LIGHT_PROJECTILE =
            ModRegistry.ENTITIES.register("spear_of_light_projectile",
                    () -> EntityType.Builder.<SpearOfLightProjectile>of(SpearOfLightProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 长矛尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("spear_of_light_projectile"));

    // SpearOfDarkness的暗黑长矛投射物实体
    public static final RegistryObject<EntityType<SpearOfDarknessProjectile>> SPEAR_OF_DARKNESS_PROJECTILE =
            ModRegistry.ENTITIES.register("spear_of_darkness_projectile",
                    () -> EntityType.Builder.<SpearOfDarknessProjectile>of(SpearOfDarknessProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 长矛尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("spear_of_darkness_projectile"));

    // Revenge的复仇狙击子弹投射物实体
    public static final RegistryObject<EntityType<RevengeProjectile>> REVENGE_PROJECTILE =
            ModRegistry.ENTITIES.register("revenge_projectile",
                    () -> EntityType.Builder.<RevengeProjectile>of(RevengeProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 狙击子弹尺寸较小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("revenge_projectile"));

    // FlowingLight的流动之光追踪子弹投射物实体
    public static final RegistryObject<EntityType<FlowingLightProjectile>> FLOWING_LIGHT_PROJECTILE =
            ModRegistry.ENTITIES.register("flowing_light_projectile",
                    () -> EntityType.Builder.<FlowingLightProjectile>of(FlowingLightProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 流动之光子弹尺寸较小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("flowing_light_projectile"));

    public static final RegistryObject<EntityType<FurnaceSpearProjectile>> FURNACE_SPEAR_PROJECTILE =
            ModRegistry.ENTITIES.register("furnace_spear_projectile",
                    () -> EntityType.Builder.<FurnaceSpearProjectile>of(FurnaceSpearProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 熔炉长矛投射物尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("furnace_spear_projectile"));

    // EclipseFurnaceSpear的日蚀熔炉长矛投射物实体
    public static final RegistryObject<EntityType<EclipseFurnaceSpearProjectile>> ECLIPSE_FURNACE_SPEAR_PROJECTILE =
            ModRegistry.ENTITIES.register("eclipse_furnace_spear_projectile",
                    () -> EntityType.Builder.<EclipseFurnaceSpearProjectile>of(EclipseFurnaceSpearProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 日蚀版尺寸稍大
                            .clientTrackingRange(6)  // 追踪范围更大
                            .updateInterval(1)
                            .build("eclipse_furnace_spear_projectile"));

    // EclipseIceSpikeSpear的日蚀冰刺投射物实体
    public static final RegistryObject<EntityType<EclipseIceSpikeProjectile>> ECLIPSE_ICE_SPIKE_PROJECTILE =
            ModRegistry.ENTITIES.register("eclipse_ice_spike_projectile",
                    () -> EntityType.Builder.<EclipseIceSpikeProjectile>of(EclipseIceSpikeProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 日蚀版尺寸更大
                            .clientTrackingRange(6)  // 追踪范围更大
                            .updateInterval(1)
                            .build("eclipse_ice_spike_projectile"));

    // BurningHeaven的燃烧天堂投射物实体
    public static final RegistryObject<EntityType<BurningHeavenProjectile>> BURNING_HEAVEN_PROJECTILE =
            ModRegistry.ENTITIES.register("burning_heaven_projectile",
                    () -> EntityType.Builder.<BurningHeavenProjectile>of(BurningHeavenProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 燃烧天堂投射物尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("burning_heaven_projectile"));

    // CurseFire的咒火投射物实体
    public static final RegistryObject<EntityType<CurseFireProjectile>> CURSE_FIRE_PROJECTILE =
            ModRegistry.ENTITIES.register("curse_fire_projectile",
                    () -> EntityType.Builder.<CurseFireProjectile>of(CurseFireProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 咒火投射物尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("curse_fire_projectile"));

    public static final RegistryObject<EntityType<DragonSlayerSwordWaveProjectile>> DRAGON_SLAYER_SWORD_WAVE_PROJECTILE =
            ModRegistry.ENTITIES.register("dragon_slayer_sword_wave_projectile",
                    () -> EntityType.Builder.<DragonSlayerSwordWaveProjectile>of(DragonSlayerSwordWaveProjectile::new, MobCategory.MISC)
                            .sized(0.8f, 2.0f)  // 竖着的剑气，宽0.8，高2.0
                            .clientTrackingRange(6)
                            .updateInterval(2)
                            .build("dragon_slayer_sword_wave_projectile"));

    // JungleScepter的丛林剑气投射物实体
    public static final RegistryObject<EntityType<JungleSwordWaveProjectile>> JUNGLE_SWORD_WAVE_PROJECTILE =
            ModRegistry.ENTITIES.register("jungle_sword_wave_projectile",
                    () -> EntityType.Builder.<JungleSwordWaveProjectile>of(JungleSwordWaveProjectile::new, MobCategory.MISC)
                            .sized(0.8f, 2.0f)  // 竖着的绿色剑气，宽0.8，高2.0
                            .clientTrackingRange(6)
                            .updateInterval(2)
                            .build("jungle_sword_wave_projectile"));

    // DragonFlameBurningHeaven的龙炎燃烧天堂投射物实体
    public static final RegistryObject<EntityType<DragonFlameBurningHeavenProjectile>> DRAGON_FLAME_BURNING_HEAVEN_PROJECTILE =
            ModRegistry.ENTITIES.register("dragon_flame_burning_heaven_projectile",
                    () -> EntityType.Builder.<DragonFlameBurningHeavenProjectile>of(DragonFlameBurningHeavenProjectile::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)  // 龙炎燃烧天堂投射物尺寸
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("dragon_flame_burning_heaven_projectile"));

    // DragonFlameCurseFire的龙炎诅咒火焰投射物实体
    public static final RegistryObject<EntityType<DragonFlameCurseFireProjectile>> DRAGON_FLAME_CURSE_FIRE_PROJECTILE =
            ModRegistry.ENTITIES.register("dragon_flame_curse_fire_projectile",
                    () -> EntityType.Builder.<DragonFlameCurseFireProjectile>of(DragonFlameCurseFireProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 龙炎诅咒火焰投射物尺寸
                            .clientTrackingRange(8)  // 追踪投射物需要更大的追踪范围
                            .updateInterval(1)
                            .build("dragon_flame_curse_fire_projectile"));

    // DragonFlameAshes的龙炎灰烬投射物实体
    public static final RegistryObject<EntityType<DragonFlameAshesProjectile>> DRAGON_FLAME_ASHES_PROJECTILE =
            ModRegistry.ENTITIES.register("dragon_flame_ashes_projectile",
                    () -> EntityType.Builder.<DragonFlameAshesProjectile>of(DragonFlameAshesProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 龙炎灰烬子弹尺寸较小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("dragon_flame_ashes_projectile"));

    // SpiritFireJudgment的灵魂火审判主子弹投射物实体
    public static final RegistryObject<EntityType<SpiritFireJudgmentProjectile>> SPIRIT_FIRE_JUDGMENT_PROJECTILE =
            ModRegistry.ENTITIES.register("spirit_fire_judgment_projectile",
                    () -> EntityType.Builder.<SpiritFireJudgmentProjectile>of(SpiritFireJudgmentProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 主子弹尺寸较小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("spirit_fire_judgment_projectile"));

    // SpiritFireJudgment的灵魂火审判散射子弹投射物实体
    public static final RegistryObject<EntityType<SpiritFireScatterProjectile>> SPIRIT_FIRE_SCATTER_PROJECTILE =
            ModRegistry.ENTITIES.register("spirit_fire_scatter_projectile",
                    () -> EntityType.Builder.<SpiritFireScatterProjectile>of(SpiritFireScatterProjectile::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)  // 散射子弹尺寸更小
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("spirit_fire_scatter_projectile"));

    // EternalNight的永恒之夜黑暗法球投射物实体
    public static final RegistryObject<EntityType<EternalNightProjectile>> ETERNAL_NIGHT_PROJECTILE =
            ModRegistry.ENTITIES.register("eternal_night_projectile",
                    () -> EntityType.Builder.<EternalNightProjectile>of(EternalNightProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 黑暗法球尺寸
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("eternal_night_projectile"));

    // ShadowSpear的暗影长矛投射物实体
    public static final RegistryObject<EntityType<ShadowSpearProjectile>> SHADOW_SPEAR_PROJECTILE =
            ModRegistry.ENTITIES.register("shadow_spear_projectile",
                    () -> EntityType.Builder.<ShadowSpearProjectile>of(ShadowSpearProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 暗影长矛尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("shadow_spear_projectile"));

    // DeathRay的死光炮激光投射物实体
    public static final RegistryObject<EntityType<DeathRayLaserProjectile>> DEATH_RAY_LASER_PROJECTILE =
            ModRegistry.ENTITIES.register("death_ray_laser_projectile",
                    () -> EntityType.Builder.<DeathRayLaserProjectile>of(DeathRayLaserProjectile::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)  // 激光尺寸较小
                            .clientTrackingRange(16)  // 激光需要更远的追踪范围
                            .updateInterval(1)  // 更频繁的更新
                            .build("death_ray_laser_projectile"));

    // StarryNight的星星弹幕投射物实体
    public static final RegistryObject<EntityType<StarProjectile>> STAR_PROJECTILE =
            ModRegistry.ENTITIES.register("star_projectile",
                    () -> EntityType.Builder.<StarProjectile>of(StarProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 星星弹幕尺寸
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("star_projectile"));

    // StarryNight的追踪星星弹幕投射物实体
    public static final RegistryObject<EntityType<TrackingStarProjectile>> TRACKING_STAR =
            ModRegistry.ENTITIES.register("tracking_star",
                    () -> EntityType.Builder.<TrackingStarProjectile>of(TrackingStarProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 追踪星星弹幕尺寸
                            .clientTrackingRange(8)  // 追踪弹幕需要更大的追踪范围
                            .updateInterval(1)
                            .build("tracking_star"));

    // Star的星辰矛投射物实体
    public static final RegistryObject<EntityType<StarSpearProjectile>> STAR_SPEAR_PROJECTILE =
            ModRegistry.ENTITIES.register("star_spear_projectile",
                    () -> EntityType.Builder.<StarSpearProjectile>of(StarSpearProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 星辰矛投射物尺寸
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("star_spear_projectile"));

    // Tornado的龙卷风投射物实体
    public static final RegistryObject<EntityType<TornadoProjectile>> TORNADO_PROJECTILE =
            ModRegistry.ENTITIES.register("tornado_projectile",
                    () -> EntityType.Builder.<TornadoProjectile>of(TornadoProjectile::new, MobCategory.MISC)
                            .sized(1.0f, 2.0f)  // 龙卷风尺寸（宽1.0，高2.0）
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("tornado_projectile"));

    // NaturalTornado的自然龙卷风投射物实体
    public static final RegistryObject<EntityType<NaturalTornadoProjectile>> NATURAL_TORNADO_PROJECTILE =
            ModRegistry.ENTITIES.register("natural_tornado_projectile",
                    () -> EntityType.Builder.<NaturalTornadoProjectile>of(NaturalTornadoProjectile::new, MobCategory.MISC)
                            .sized(1.0f, 2.0f)  // 自然龙卷风尺寸（宽1.0，高2.0）
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("natural_tornado_projectile"));

    // Tsunami的海啸投射物实体
    public static final RegistryObject<EntityType<TsunamiProjectile>> TSUNAMI_PROJECTILE =
            ModRegistry.ENTITIES.register("tsunami_projectile",
                    () -> EntityType.Builder.<TsunamiProjectile>of(TsunamiProjectile::new, MobCategory.MISC)
                            .sized(0.8f, 0.8f)  // 海啸投射物尺寸（宽0.8，高0.8）
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("tsunami_projectile"));

    // BubbleGun的泡泡投射物实体
    public static final RegistryObject<EntityType<BubbleProjectile>> BUBBLE_PROJECTILE =
            ModRegistry.ENTITIES.register("bubble_projectile",
                    () -> EntityType.Builder.<BubbleProjectile>of(BubbleProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 泡泡投射物尺寸（宽0.5，高0.5）
                            .clientTrackingRange(8)  // 追踪投射物需要更大的追踪范围
                            .updateInterval(1)
                            .build("bubble_projectile"));

    // BloodTornado的血色龙卷风投射物实体
    public static final RegistryObject<EntityType<BloodTornadoProjectile>> BLOOD_TORNADO_PROJECTILE =
            ModRegistry.ENTITIES.register("blood_tornado_projectile",
                    () -> EntityType.Builder.<BloodTornadoProjectile>of(BloodTornadoProjectile::new, MobCategory.MISC)
                            .sized(1.2f, 2.5f)  // 血色龙卷风尺寸（宽1.2，高2.5），比普通龙卷风更大
                            .clientTrackingRange(8)  // 追踪范围更大
                            .updateInterval(1)
                            .build("blood_tornado_projectile"));

    // ErodingWave的侵蚀波浪投射物实体
    public static final RegistryObject<EntityType<ErodingWaveProjectile>> ERODING_WAVE_PROJECTILE =
            ModRegistry.ENTITIES.register("eroding_wave_projectile",
                    () -> EntityType.Builder.<ErodingWaveProjectile>of(ErodingWaveProjectile::new, MobCategory.MISC)
                            .sized(0.8f, 0.8f)  // 侵蚀波浪投射物尺寸（宽0.8，高0.8）
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("eroding_wave_projectile"));

    // 训练人偶实体
    public static final RegistryObject<EntityType<TrainingPuppet>> TRAINING_PUPPET =
            ModRegistry.ENTITIES.register("training_puppet",
                    () -> EntityType.Builder.<TrainingPuppet>of(TrainingPuppet::new, MobCategory.MISC)
                            .sized(0.9f, 2.0f)  // 人形尺寸（宽0.9，高2.0）
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build("training_puppet"));

    // PolarizingPrism的偏光棱镜激光投射物实体
    public static final RegistryObject<EntityType<PolarizingPrismLaserProjectile>> POLARIZING_PRISM_LASER_PROJECTILE =
            ModRegistry.ENTITIES.register("polarizing_prism_laser_projectile",
                    () -> EntityType.Builder.<PolarizingPrismLaserProjectile>of(PolarizingPrismLaserProjectile::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)  // 激光尺寸较小
                            .clientTrackingRange(16)  // 激光需要更远的追踪范围
                            .updateInterval(1)  // 更频繁的更新
                            .build("polarizing_prism_laser_projectile"));

    // BrokenBlade的破碎之刃剑气投射物实体
    public static final RegistryObject<EntityType<BrokenBladeSwordWaveProjectile>> BROKEN_BLADE_SWORD_WAVE_PROJECTILE =
            ModRegistry.ENTITIES.register("broken_blade_sword_wave_projectile",
                    () -> EntityType.Builder.<BrokenBladeSwordWaveProjectile>of(BrokenBladeSwordWaveProjectile::new, MobCategory.MISC)
                            .sized(1.0f, 2.5f)  // 宽大剑气尺寸（宽1.0，高2.5）
                            .clientTrackingRange(6)
                            .updateInterval(2)
                            .build("broken_blade_sword_wave_projectile"));

    // Harpoon的鱼叉投射物实体
    public static final RegistryObject<EntityType<HarpoonSpearProjectile>> HARPOON_SPEAR_PROJECTILE =
            ModRegistry.ENTITIES.register("harpoon_spear_projectile",
                    () -> EntityType.Builder.<HarpoonSpearProjectile>of(HarpoonSpearProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 鱼叉尺寸（宽0.5，高0.5）
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("harpoon_spear_projectile"));

    // Harpoon的鱼投射物实体
    public static final RegistryObject<EntityType<FishProjectile>> FISH_PROJECTILE =
            ModRegistry.ENTITIES.register("fish_projectile",
                    () -> EntityType.Builder.<FishProjectile>of(FishProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 鱼尺寸较小（宽0.3，高0.3）
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("fish_projectile"));

    // Skyfire的天火矛投射物实体
    public static final RegistryObject<EntityType<SkyfireSpearProjectile>> SKYFIRE_SPEAR_PROJECTILE =
            ModRegistry.ENTITIES.register("skyfire_spear_projectile",
                    () -> EntityType.Builder.<SkyfireSpearProjectile>of(SkyfireSpearProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 天火矛尺寸（宽0.5，高0.5）
                            .clientTrackingRange(6)  // 追踪范围稍大
                            .updateInterval(1)
                            .build("skyfire_spear_projectile"));

    // Phoenix的凤凰射线投射物实体
    public static final RegistryObject<EntityType<PhoenixRayProjectile>> PHOENIX_RAY_PROJECTILE =
            ModRegistry.ENTITIES.register("phoenix_ray_projectile",
                    () -> EntityType.Builder.<PhoenixRayProjectile>of(PhoenixRayProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 凤凰射线尺寸较小（宽0.3，高0.3）
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("phoenix_ray_projectile"));

    // CelestialStar的天星投射物实体
    public static final RegistryObject<EntityType<CelestialStarProjectile>> CELESTIAL_STAR_PROJECTILE =
            ModRegistry.ENTITIES.register("celestial_star_projectile",
                    () -> EntityType.Builder.<CelestialStarProjectile>of(CelestialStarProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 天星尺寸（宽0.5，高0.5）
                            .clientTrackingRange(6)  // 追踪范围稍大
                            .updateInterval(1)
                            .build("celestial_star_projectile"));

    // Calamity的灾难长矛投射物实体
    public static final RegistryObject<EntityType<CalamityProjectile>> CALAMITY_PROJECTILE =
            ModRegistry.ENTITIES.register("calamity_projectile",
                    () -> EntityType.Builder.<CalamityProjectile>of(CalamityProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 长矛尺寸（宽0.5，高0.5）
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("calamity_projectile"));

    // GreenLeafFlyingKnife的绿叶飞刀投射物实体
    public static final RegistryObject<EntityType<GreenLeafFlyingKnifeProjectile>> GREEN_LEAF_FLYING_KNIFE_PROJECTILE =
            ModRegistry.ENTITIES.register("green_leaf_flying_knife_projectile",
                    () -> EntityType.Builder.<GreenLeafFlyingKnifeProjectile>of(GreenLeafFlyingKnifeProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 飞刀尺寸较小（宽0.3，高0.3）
                            .clientTrackingRange(8)  // 追踪飞刀需要更大的追踪范围
                            .updateInterval(1)
                            .build("green_leaf_flying_knife_projectile"));

    // DeathScythe的死亡镰刀投射物实体
    public static final RegistryObject<EntityType<DeathScytheProjectile>> DEATH_SCYTHE_PROJECTILE =
            ModRegistry.ENTITIES.register("death_scythe_projectile",
                    () -> EntityType.Builder.<DeathScytheProjectile>of(DeathScytheProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 镰刀尺寸（宽0.5，高0.5）
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("death_scythe_projectile"));

    // DeathScythe的死亡镰刀静止弹幕实体
    public static final RegistryObject<EntityType<DeathScytheStaticProjectile>> DEATH_SCYTHE_STATIC_PROJECTILE =
            ModRegistry.ENTITIES.register("death_scythe_static_projectile",
                    () -> EntityType.Builder.<DeathScytheStaticProjectile>of(DeathScytheStaticProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 静止弹幕尺寸较小（宽0.3，高0.3）
                            .clientTrackingRange(4)
                            .updateInterval(2)
                            .build("death_scythe_static_projectile"));

    // DeathScythe的死亡镰刀剑气投射物实体
    public static final RegistryObject<EntityType<DeathScytheSwordWaveProjectile>> DEATH_SCYTHE_SWORD_WAVE_PROJECTILE =
            ModRegistry.ENTITIES.register("death_scythe_sword_wave_projectile",
                    () -> EntityType.Builder.<DeathScytheSwordWaveProjectile>of(DeathScytheSwordWaveProjectile::new, MobCategory.MISC)
                            .sized(1.0f, 2.5f)  // 剑气尺寸（宽1.0，高2.5）
                            .clientTrackingRange(6)
                            .updateInterval(2)
                            .build("death_scythe_sword_wave_projectile"));

    // PurgatoryStaff的死灵飞弹投射物实体
    public static final RegistryObject<EntityType<PurgatoryMissileProjectile>> PURGATORY_MISSILE_PROJECTILE =
            ModRegistry.ENTITIES.register("purgatory_missile_projectile",
                    () -> EntityType.Builder.<PurgatoryMissileProjectile>of(PurgatoryMissileProjectile::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)  // 死灵飞弹尺寸（宽0.6，高0.6）
                            .clientTrackingRange(8)  // 追踪飞弹需要更大的追踪范围
                            .updateInterval(1)
                            .build("purgatory_missile_projectile"));

    // Chaos的祸乱子弹投射物实体
    public static final RegistryObject<EntityType<ChaosBulletProjectile>> CHAOS_BULLET_PROJECTILE =
            ModRegistry.ENTITIES.register("chaos_bullet_projectile",
                    () -> EntityType.Builder.<ChaosBulletProjectile>of(ChaosBulletProjectile::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)  // 子弹尺寸较小（宽0.2，高0.2）
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("chaos_bullet_projectile"));

    // Chaos的祸乱弹幕投射物实体
    public static final RegistryObject<EntityType<ChaosBarrageProjectile>> CHAOS_BARRAGE_PROJECTILE =
            ModRegistry.ENTITIES.register("chaos_barrage_projectile",
                    () -> EntityType.Builder.<ChaosBarrageProjectile>of(ChaosBarrageProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 弹幕尺寸（宽0.3，高0.3）
                            .clientTrackingRange(8)  // 追踪弹幕需要更大的追踪范围
                            .updateInterval(1)
                            .build("chaos_barrage_projectile"));

    // ElementalStaff的元素弹幕投射物实体
    public static final RegistryObject<EntityType<ElementalBarrageProjectile>> ELEMENTAL_BARRAGE_PROJECTILE =
            ModRegistry.ENTITIES.register("elemental_barrage_projectile",
                    () -> EntityType.Builder.<ElementalBarrageProjectile>of(ElementalBarrageProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 元素弹幕尺寸（宽0.3，高0.3）
                            .clientTrackingRange(8)  // 追踪弹幕需要更大的追踪范围
                            .updateInterval(1)
                            .build("elemental_barrage_projectile"));

    // TracerLight的曳光子弹投射物实体
    public static final RegistryObject<EntityType<TracerLightProjectile>> TRACER_LIGHT_PROJECTILE =
            ModRegistry.ENTITIES.register("tracer_light_projectile",
                    () -> EntityType.Builder.<TracerLightProjectile>of(TracerLightProjectile::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)  // 曳光子弹尺寸较小（宽0.2，高0.2）
                            .clientTrackingRange(8)  // 追踪子弹需要更大的追踪范围
                            .updateInterval(1)
                            .build("tracer_light_projectile"));

    // StarryJudgment的星辰裁决投射物实体
    public static final RegistryObject<EntityType<StarryJudgmentProjectile>> STARRY_JUDGMENT_PROJECTILE =
            ModRegistry.ENTITIES.register("starry_judgment_projectile",
                    () -> EntityType.Builder.<StarryJudgmentProjectile>of(StarryJudgmentProjectile::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)  // 星辰裁决子弹尺寸较小（宽0.2，高0.2）
                            .clientTrackingRange(8)  // 追踪子弹需要更大的追踪范围
                            .updateInterval(1)
                            .build("starry_judgment_projectile"));

    // AnnihilationJudgement的寂灭裁决子弹投射物实体
    public static final RegistryObject<EntityType<AnnihilationBulletProjectile>> ANNIHILATION_BULLET_PROJECTILE =
            ModRegistry.ENTITIES.register("annihilation_bullet_projectile",
                    () -> EntityType.Builder.<AnnihilationBulletProjectile>of(AnnihilationBulletProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 寂灭裁决子弹尺寸（宽0.3，高0.3）
                            .clientTrackingRange(8)  // 狙击子弹需要更大的追踪范围
                            .updateInterval(1)
                            .build("annihilation_bullet_projectile"));

    // AbyssOfCalamity的深渊之灾长矛投射物实体
    public static final RegistryObject<EntityType<AbyssOfCalamityProjectile>> ABYSS_OF_CALAMITY_PROJECTILE =
            ModRegistry.ENTITIES.register("abyss_of_calamity_projectile",
                    () -> EntityType.Builder.<AbyssOfCalamityProjectile>of(AbyssOfCalamityProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 长矛尺寸（宽0.5，高0.5）
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("abyss_of_calamity_projectile"));

    // NaturalWill的自然意志长矛投射物实体
    public static final RegistryObject<EntityType<NaturalWillProjectile>> NATURAL_WILL_PROJECTILE =
            ModRegistry.ENTITIES.register("natural_will_projectile",
                    () -> EntityType.Builder.<NaturalWillProjectile>of(NaturalWillProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 长矛尺寸（宽0.5，高0.5）
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("natural_will_projectile"));

    // DivineSpearDisasterBreaker的神枪-破灾投射物实体
    public static final RegistryObject<EntityType<DivineSpearDisasterBreakerProjectile>> DIVINE_SPEAR_DISASTER_BREAKER_PROJECTILE =
            ModRegistry.ENTITIES.register("divine_spear_disaster_breaker_projectile",
                    () -> EntityType.Builder.<DivineSpearDisasterBreakerProjectile>of(DivineSpearDisasterBreakerProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 神枪尺寸（宽0.5，高0.5）
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("divine_spear_disaster_breaker_projectile"));

    public static final RegistryObject<EntityType<ShatteredRealmArrow>> SHATTERED_REALM_ARROW =
            ModRegistry.ENTITIES.register("shattered_realm_arrow_projectile",
                    () -> EntityType.Builder.<ShatteredRealmArrow>of(ShatteredRealmArrow::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("shattered_realm_arrow_projectile"));

    // UltimateSubmachineGun的动能爆炸弹投射物实体
    public static final RegistryObject<EntityType<KineticExplosionProjectile>> KINETIC_EXPLOSION_PROJECTILE =
            ModRegistry.ENTITIES.register("kinetic_explosion_projectile",
                    () -> EntityType.Builder.<KineticExplosionProjectile>of(KineticExplosionProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 动能爆炸弹尺寸（宽0.3，高0.3）
                            .clientTrackingRange(6)  // 追踪范围稍大
                            .updateInterval(1)
                            .build("kinetic_explosion_projectile"));

    // UltimateSubmachineGun的电磁闪电链投射物实体
    public static final RegistryObject<EntityType<ElectromagneticChainProjectile>> ELECTROMAGNETIC_CHAIN_PROJECTILE =
            ModRegistry.ENTITIES.register("electromagnetic_chain_projectile",
                    () -> EntityType.Builder.<ElectromagneticChainProjectile>of(ElectromagneticChainProjectile::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)  // 电磁闪电链尺寸（宽0.3，高0.3）
                            .clientTrackingRange(8)  // 闪电链需要更大的追踪范围
                            .updateInterval(1)
                            .build("electromagnetic_chain_projectile"));

    // UltimateSubmachineGun的重力黑洞弹投射物实体
    public static final RegistryObject<EntityType<GravityBlackholeProjectile>> GRAVITY_BLACKHOLE_PROJECTILE =
            ModRegistry.ENTITIES.register("gravity_blackhole_projectile",
                    () -> EntityType.Builder.<GravityBlackholeProjectile>of(GravityBlackholeProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)  // 重力黑洞弹尺寸（宽0.4，高0.4）
                            .clientTrackingRange(6)
                            .updateInterval(1)
                            .build("gravity_blackhole_projectile"));

    // UltimateSubmachineGun的奇点弹投射物实体
    public static final RegistryObject<EntityType<SingularityProjectile>> SINGULARITY_PROJECTILE =
            ModRegistry.ENTITIES.register("singularity_projectile",
                    () -> EntityType.Builder.<SingularityProjectile>of(SingularityProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)  // 奇点弹尺寸（宽0.5，高0.5）
                            .clientTrackingRange(8)  // 奇点弹需要更大的追踪范围
                            .updateInterval(1)
                            .build("singularity_projectile"));
}