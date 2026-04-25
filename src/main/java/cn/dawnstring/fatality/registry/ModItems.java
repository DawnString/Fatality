package cn.dawnstring.fatality.registry;

import cn.dawnstring.fatality.items.accessory.*;
import cn.dawnstring.fatality.items.normal.*;
import cn.dawnstring.fatality.items.summon.*;
import cn.dawnstring.fatality.items.weapon.magic.Fantasy;
import cn.dawnstring.fatality.items.weapon.magic.*;
import cn.dawnstring.fatality.items.weapon.melee.*;
import cn.dawnstring.fatality.items.weapon.ranged.*;
import cn.dawnstring.fatality.items.weapon.melee.Calamity;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    // 饰品物品
    public static final RegistryObject<Item> ACID_ETCHED_SAC = ModRegistry.ITEMS.register("acid_etched_sac",
            () -> new AcidEtchedSac(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACTION_BOOTS = ModRegistry.ITEMS.register("action_boots",
            () -> new ActionBoots());
    public static final RegistryObject<Item> ANTISLIP_GLOVES = ModRegistry.ITEMS.register("antislip_gloves",
            () -> new AntislipGloves(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BLOOD_SPIRIT_NECKLACE = ModRegistry.ITEMS.register("blood_spirit_necklace",
            () -> new BloodSpiritNecklace(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BLOODSTAINED_GLOVES = ModRegistry.ITEMS.register("bloodstained_gloves",
            () -> new BloodstainedGloves(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BLOODTOOTH_NECKLACE = ModRegistry.ITEMS.register("bloodtooth_necklace",
            () -> new BloodtoothNecklace(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BLOODY_SCARF = ModRegistry.ITEMS.register("bloody_scarf",
            () -> new BloodyScarf(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BOOTS_OF_ICE_AND_FIRE = ModRegistry.ITEMS.register("boots_of_ice_and_fire",
            () -> new BootsOfIceAndFire());
    public static final RegistryObject<Item> BOOTS_OF_THE_ELEMENTS = ModRegistry.ITEMS.register("boots_of_the_elements",
            () -> new BootsOfTheElements(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BOOTS_OF_THE_JOURNEY = ModRegistry.ITEMS.register("boots_of_the_journey",
            () -> new BootsOfTheJourney(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BOOTS_OF_THE_WATER_STRIDER = ModRegistry.ITEMS.register("boots_of_the_water_strider",
            () -> new BootsOfTheWaterStrider());
    public static final RegistryObject<Item> BOTTLE_OF_STARS = ModRegistry.ITEMS.register("bottle_of_stars",
            () -> new BottleOfStars(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CANNED_HEART = ModRegistry.ITEMS.register("canned_heart",
            () -> new CannedHeart(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CERTIFICATE_OF_VALOR = ModRegistry.ITEMS.register("certificate_of_valor",
            () -> new CertificateOfValor(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COMMANDERS_SHIELD = ModRegistry.ITEMS.register("commanders_shield",
            () -> new CommandersShield());
    public static final RegistryObject<Item> CORRUPT_SCARF = ModRegistry.ITEMS.register("corrupt_scarf",
            () -> new CorruptScarf(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CROWN_OF_THE_SUPREME_DEMIGOD = ModRegistry.ITEMS.register("crown_of_the_supreme_demigod",
            () -> new CrownOfTheSupremeDemigod(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CURSEFIRE_TOTEM = ModRegistry.ITEMS.register("cursefire_totem",
            () -> new CursefireTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CURSEFLAME_BLOSSOM = ModRegistry.ITEMS.register("curseflame_blossom",
            () -> new CurseflameBlossom(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_AMULET = ModRegistry.ITEMS.register("dark_amulet",
            () -> new DarkAmulet(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_SPIRIT_NECKLACE = ModRegistry.ITEMS.register("dark_spirit_necklace",
            () -> new DarkSpiritNecklace(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DEXTEROUS_GLOVE = ModRegistry.ITEMS.register("dexterous_glove",
            () -> new DexterousGlove(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIVINE_CORE = ModRegistry.ITEMS.register("divine_core",
            () -> new DivineCore(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIVINE_SHIELD = ModRegistry.ITEMS.register("divine_shield",
            () -> new DivineShield(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIVING_MASK = ModRegistry.ITEMS.register("diving_mask",
            () -> new DivingMask(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DRAGON_BRACERS = ModRegistry.ITEMS.register("dragon_bracers",
            () -> new DragonBracers(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DRAGON_ENGRAVED_HILT = ModRegistry.ITEMS.register("dragon_engraved_hilt",
            () -> new DragonEngravedHilt(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLAME_FLOWER = ModRegistry.ITEMS.register("flame_flower",
            () -> new FlameFlower(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLAME_SPIRIT_ESSENCE = ModRegistry.ITEMS.register("flame_spirit_essence",
            () -> new FlameSpiritEssence(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLIPPERS = ModRegistry.ITEMS.register("flippers",
            () -> new Flippers(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FRAGRANT_SCARF = ModRegistry.ITEMS.register("fragrant_scarf",
            () -> new FragrantScarf(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FRESH_SACHET = ModRegistry.ITEMS.register("fresh_sachet",
            () -> new FreshSachet(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FUNERAL_FLOWER = ModRegistry.ITEMS.register("funeral_flower",
            () -> new FuneralFlower(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GOBLIN_TOTEM = ModRegistry.ITEMS.register("goblin_totem",
            () -> new GoblinTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GREAT_MAGE_MEDAL = ModRegistry.ITEMS.register("great_mage_medal",
            () -> new GreatMageMedal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HEART_OF_PROTECTION = ModRegistry.ITEMS.register("heart_of_protection",
            () -> new HeartOfProtection(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HOLY_SPIRIT_BOOTS = ModRegistry.ITEMS.register("holy_spirit_boots",
            () -> new HolySpiritBoots(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JET_BOOTS = ModRegistry.ITEMS.register("jet_boots",
            () -> new JetBoots());
    public static final RegistryObject<Item> KNIGHTS_SHIELD = ModRegistry.ITEMS.register("knights_shield",
            () -> new KnightsShield(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LAVA_BOOTS = ModRegistry.ITEMS.register("lava_boots",
            () -> new LavaBoots());
    public static final RegistryObject<Item> LUMINOUS_CLOAK = ModRegistry.ITEMS.register("luminous_cloak",
            () -> new LuminousCloak(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LUMINOUS_TOTEM = ModRegistry.ITEMS.register("luminous_totem",
            () -> new LuminousTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MAGIC_AMULET = ModRegistry.ITEMS.register("magic_amulet",
            () -> new MagicAmulet(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MAGIC_ARROW = ModRegistry.ITEMS.register("magic_arrow",
            () -> new MagicArrow(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MAGIC_CONDENSER = ModRegistry.ITEMS.register("magic_condenser",
            () -> new MagicCondenser(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANA_BLESSING_SPELL = ModRegistry.ITEMS.register("mana_blessing_spell",
            () -> new ManaBlessingSpell(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_BOOTS = ModRegistry.ITEMS.register("mechanical_boots",
            () -> new MechanicalBoots(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_BRACER = ModRegistry.ITEMS.register("mechanical_bracer",
            () -> new MechanicalBracer(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_EYE_OF_INTELLIGENCE = ModRegistry.ITEMS.register("mechanical_eye_of_intelligence",
            () -> new MechanicalEyeOfIntelligence(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_GLOVE = ModRegistry.ITEMS.register("mechanical_glove",
            () -> new MechanicalGlove(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MEDAL_OF_VENGEANCE = ModRegistry.ITEMS.register("medal_of_vengeance",
            () -> new MedalOfVengeance(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MOLTEN_LIGHT_SHIELD = ModRegistry.ITEMS.register("molten_light_shield",
            () -> new MoltenLightShield(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NAMELESS_CROWN = ModRegistry.ITEMS.register("nameless_crown",
            () -> new NamelessCrown(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NECKLACE_OF_LIFE = ModRegistry.ITEMS.register("necklace_of_life",
            () -> new NecklaceOfLife(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OBSIDIAN_BOOTS = ModRegistry.ITEMS.register("obsidian_boots",
            () -> new ObsidianBoots());
    public static final RegistryObject<Item> PHANTOM_CHARM = ModRegistry.ITEMS.register("phantom_charm",
            () -> new PhantomCharm(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> POWER_GLOVE = ModRegistry.ITEMS.register("power_glove",
            () -> new PowerGlove(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RING_OF_VENGEANCE = ModRegistry.ITEMS.register("ring_of_vengeance",
            () -> new RingOfVengeance(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHADOW_WRISTGUARDS = ModRegistry.ITEMS.register("shadow_wristguards",
            () -> new ShadowWristguards(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SNAKE_TOTEM = ModRegistry.ITEMS.register("snake_totem",
            () -> new SnakeTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SOUL_RING = ModRegistry.ITEMS.register("soul_ring",
            () -> new SoulRing(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPELLFIRE_GAUNTLETS = ModRegistry.ITEMS.register("spellfire_gauntlets",
            () -> new SpellfireGauntlets(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPELL_OF_FIRE_MANA_CHARM = ModRegistry.ITEMS.register("spell_of_fire_mana_charm",
            () -> new SpellOfFireManaCharm(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPIRIT_FIRE_SHIELD = ModRegistry.ITEMS.register("spirit_fire_shield",
            () -> new SpiritFireShield(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STAR_CHARM = ModRegistry.ITEMS.register("star_charm",
            () -> new StarCharm(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STAR_CLOAK = ModRegistry.ITEMS.register("star_cloak",
            () -> new StarCloak(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SUN_ECLIPSE_TOTEM = ModRegistry.ITEMS.register("sun_eclipse_totem",
            () -> new SunEclipseTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> THREE_PHASE_TOTEM = ModRegistry.ITEMS.register("three_phase_totem",
            () -> new ThreePhaseTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TORTOISE_SHIELD = ModRegistry.ITEMS.register("tortoise_shield",
            () -> new TortoiseShield(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRAVELERS_BOOTS = ModRegistry.ITEMS.register("travelers_boots",
            () -> new TravelersBoots(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TROLL_GLOVES = ModRegistry.ITEMS.register("troll_gloves",
            () -> new TrollGloves(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> VIOLENCE_GLOVES = ModRegistry.ITEMS.register("violence_gloves",
            () -> new ViolenceGloves(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WIZARD_HAT = ModRegistry.ITEMS.register("wizard_hat",
            () -> new WizardHat(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WRAITH_BRACERS = ModRegistry.ITEMS.register("wraith_bracers",
            () -> new WraithBracers(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EARTH_AMULET = ModRegistry.ITEMS.register("earth_amulet",
            () -> new EarthAmulet(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ECLIPSE_MIRROR = ModRegistry.ITEMS.register("eclipse_mirror",
            () -> new EclipseMirror(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ULTIMATE_YIN_STONE = ModRegistry.ITEMS.register("ultimate_yin_stone",
            () -> new UltimateYinStone(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MECHANICAL_HEART = ModRegistry.ITEMS.register("mechanical_heart",
            () -> new MechanicalHeart(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OCEAN_TOTEM = ModRegistry.ITEMS.register("ocean_totem",
            () -> new OceanTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BRACELET_OF_THE_SEA = ModRegistry.ITEMS.register("bracelet_of_the_sea",
            () -> new BraceletOfTheSea(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WATER_SPIRIT_GLOVES = ModRegistry.ITEMS.register("water_spirit_gloves",
            () -> new WaterSpiritGloves(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TSUNAMI_NECKLACE = ModRegistry.ITEMS.register("tsunami_necklace",
            () -> new TsunamiNecklace(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AZURE_MEDAL = ModRegistry.ITEMS.register("azure_medal",
            () -> new AzureMedal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> AZURE_COASTAL_FLOWER = ModRegistry.ITEMS.register("azure_coastal_flower",
            () -> new AzureCoastalFlower(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HEART_OF_THE_ORIGIN = ModRegistry.ITEMS.register("heart_of_the_origin",
            () -> new HeartOfTheOrigin(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COMPASS_OF_THE_SOUL = ModRegistry.ITEMS.register("compass_of_the_soul",
            () -> new CompassOfTheSoul(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GLOW_ABYSS_STONE = ModRegistry.ITEMS.register("glow_abyss_stone",
            () -> new GlowAbyssStone(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ABYSS_DRAGON_BALL = ModRegistry.ITEMS.register("abyss_dragon_ball",
            () -> new AbyssDragonBall(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ESSENCE_OF_THE_EARTH_SPIRIT = ModRegistry.ITEMS.register("essence_of_the_earth_spirit",
            () -> new EssenceOfTheEarthSpirit(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SACRED_FLAME_TOTEM = ModRegistry.ITEMS.register("sacred_flame_totem",
            () -> new SacredFlameTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SACRED_FLAME_GLOVES = ModRegistry.ITEMS.register("sacred_flame_gloves",
            () -> new SacredFlameGloves(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NECKLACE_OF_CALAMITY = ModRegistry.ITEMS.register("necklace_of_calamity",
            () -> new NecklaceOfCalamity(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLAME_SPIRIT_FLOWER = ModRegistry.ITEMS.register("flame_spirit_flower",
            () -> new FlameSpiritFlower(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OMINOUS_CURSE = ModRegistry.ITEMS.register("ominous_curse",
            () -> new OminousCurse(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STONE_OF_EARTH_ELEMENTAL_SPIRIT = ModRegistry.ITEMS.register("stone_of_earth_elemental_spirit",
            () -> new StoneOfEarthElementalSpirit(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NORMALIZED_COMPASS = ModRegistry.ITEMS.register("normalized_compass",
            () -> new NormalizedCompass(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JUNGLE_TOTEM = ModRegistry.ITEMS.register("jungle_totem",
            () -> new JungleTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPIRIT_NECKLACE = ModRegistry.ITEMS.register("spirit_necklace",
            () -> new SpiritNecklace(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NATURE_MEDAL = ModRegistry.ITEMS.register("nature_medal",
            () -> new NatureMedal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NECROMANCERS_NECKLACE = ModRegistry.ITEMS.register("necromancers_necklace",
            () -> new NecromancersNecklace(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOTEM_OF_THE_UNDEAD = ModRegistry.ITEMS.register("totem_of_the_undead",
            () -> new TotemOfTheUndead(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NECROTIC_BLOSSOM = ModRegistry.ITEMS.register("necrotic_blossom",
            () -> new NecroticBlossom(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SOUL_BRACERS = ModRegistry.ITEMS.register("soul_bracers",
            () -> new SoulBracers(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GRIP_OF_THE_ELEMENTS = ModRegistry.ITEMS.register("grip_of_the_elements",
            () -> new GripOfTheElements(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TALISMAN_OF_THE_END = ModRegistry.ITEMS.register("talisman_of_the_end",
            () -> new TalismanOfTheEnd(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HOLY_SPIRIT_TOTEM = ModRegistry.ITEMS.register("holy_spirit_totem",
            () -> new HolySpiritTotem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPIRIT_WRISTGUARD = ModRegistry.ITEMS.register("spirit_wristguard",
            () -> new SpiritWristguard(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> NIGHTMARE_MEDAL = ModRegistry.ITEMS.register("nightmare_medal",
            () -> new NightmareMedal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ELEMENTAL_BLOOM = ModRegistry.ITEMS.register("elemental_bloom",
            () -> new ElementalBloom(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ELEMENTAL_FORMATION_SHIELD = ModRegistry.ITEMS.register("elemental_formation_shield",
            () -> new ElementalFormationShield(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HEART_OF_THE_ELEMENTS = ModRegistry.ITEMS.register("heart_of_the_elements",
            () -> new HeartOfTheElements(new Item.Properties().stacksTo(1)));


    //翅膀
    public static final RegistryObject<Item> MECHANICAL_WINGS = ModRegistry.ITEMS.register("mechanical_wings",
            () -> new MechanicalWings());
    public static final RegistryObject<Item> PHANTOM_WINGS = ModRegistry.ITEMS.register("phantom_wings",
            () -> new PhantomWings());
    public static final RegistryObject<Item> DEMONS_WINGS = ModRegistry.ITEMS.register("demons_wings",
            () -> new DemonsWings());
    public static final RegistryObject<Item> SPIRIT_FIRE_WINGS = ModRegistry.ITEMS.register("spirit_fire_wings",
            () -> new SpiritFireWings());
    public static final RegistryObject<Item> DRAGON_WINGS = ModRegistry.ITEMS.register("dragon_wings",
            () -> new DragonWings());
    public static final RegistryObject<Item> FROST_FEATHER_WING = ModRegistry.ITEMS.register("frost_feather_wing",
            () -> new FrostFeatherWing());
    public static final RegistryObject<Item> SANDSTORM_WING = ModRegistry.ITEMS.register("sandstorm_wing",
            () -> new SandstormWing());

    //普通物品
    public static final RegistryObject<Item> ABYSSAL_DEMON_INGOT = ModRegistry.ITEMS.register("abyssal_demon_ingot",
            () -> new AbyssalDemonIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPIRIT_OF_FIRE = ModRegistry.ITEMS.register("spirit_of_fire",
            () -> new SpiritOfFire(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STAR_SHARD = ModRegistry.ITEMS.register("star_shard",
            () -> new StarShard(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WISP_OF_BLACK = ModRegistry.ITEMS.register("wisp_of_black",
            () -> new WispOfBlack(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SOUL_INGOT = ModRegistry.ITEMS.register("soul_ingot",
            () -> new SoulIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SOUL_SHARD = ModRegistry.ITEMS.register("soul_shard",
            () -> new SoulShard(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BRIGHT_INGOT = ModRegistry.ITEMS.register("bright_ingot",
            () -> new BrightIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_ABYSS_INGOT = ModRegistry.ITEMS.register("dark_abyss_ingot",
            () -> new DarkAbyssIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_ABYSS_SHARD = ModRegistry.ITEMS.register("dark_abyss_shard",
            () -> new DarkAbyssShard(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ELEMENT_INGOT = ModRegistry.ITEMS.register("element_ingot",
            () -> new ElementIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ELEMENT_SHARD = ModRegistry.ITEMS.register("element_shard",
            () -> new ElementShard(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENDER_INGOT = ModRegistry.ITEMS.register("ender_ingot",
            () -> new EnderIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ENDER_POWDER = ModRegistry.ITEMS.register("ender_powder",
            () -> new EnderPowder(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ESSENCE_OF_HELL = ModRegistry.ITEMS.register("essence_of_hell",
            () -> new EssenceOfHell(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ESSENCE_OF_HELL_INGOT = ModRegistry.ITEMS.register("essence_of_hell_ingot",
            () -> new EssenceOfHellIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FINAL_CRYSTALLIZATION = ModRegistry.ITEMS.register("final_crystallization",
            () -> new FinalCrystallization(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FIRE_INGOT = ModRegistry.ITEMS.register("fire_ingot",
            () -> new FireIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HOLY_FIRE_INGOT = ModRegistry.ITEMS.register("holy_fire_ingot",
            () -> new HolyFireIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HOLY_FLAME_ESSENCE = ModRegistry.ITEMS.register("holy_flame_essence",
            () -> new HolyFlameEssence(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JUNGLE_ALLOY = ModRegistry.ITEMS.register("jungle_alloy",
            () -> new JungleAlloy(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JUNGLE_ESSENCE = ModRegistry.ITEMS.register("jungle_essence",
            () -> new JungleEssence(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> KNIGHT_INGOT = ModRegistry.ITEMS.register("knight_ingot",
            () -> new KnightIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OCEAN_DRAGON_SOUL = ModRegistry.ITEMS.register("ocean_dragon_soul",
            () -> new OceanDragonSoul(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHADOW_INGOT = ModRegistry.ITEMS.register("shadow_ingot",
            () -> new ShadowIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CELESTIAL_RADIANCE_INGOT = ModRegistry.ITEMS.register("celestial_radiance_ingot",
            () -> new CelestialRadianceIngot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MAGIC_STAR = ModRegistry.ITEMS.register("magic_star",
            () -> new MagicStar());
    public static final RegistryObject<Item> HEART_OF_LIFE = ModRegistry.ITEMS.register("heart_of_life",
            () -> new HeartOfLife());

    // 治疗药水
    public static final RegistryObject<Item> HEALING_POTION_BASIC = ModRegistry.ITEMS.register("healing_potion_basic",
            () -> new HealingPotionBasic());
    public static final RegistryObject<Item> HEALING_POTION_INTERMEDIATE = ModRegistry.ITEMS.register("healing_potion_intermediate",
            () -> new HealingPotionIntermediate());
    public static final RegistryObject<Item> HEALING_POTION_ADVANCED = ModRegistry.ITEMS.register("healing_potion_advanced",
            () -> new HealingPotionAdvanced());
    public static final RegistryObject<Item> HEALING_POTION_ULTIMATE = ModRegistry.ITEMS.register("healing_potion_ultimate",
            () -> new HealingPotionUltimate());

    // 魔力药水
    public static final RegistryObject<Item> MANA_POTION_BASIC = ModRegistry.ITEMS.register("mana_potion_basic",
            () -> new ManaPotionBasic());
    public static final RegistryObject<Item> MANA_POTION_INTERMEDIATE = ModRegistry.ITEMS.register("mana_potion_intermediate",
            () -> new ManaPotionIntermediate());
    public static final RegistryObject<Item> MANA_POTION_ADVANCED = ModRegistry.ITEMS.register("mana_potion_advanced",
            () -> new ManaPotionAdvanced());
    public static final RegistryObject<Item> MANA_POTION_ULTIMATE = ModRegistry.ITEMS.register("mana_potion_ultimate",
            () -> new ManaPotionUltimate());

    // 调试物品
    public static final RegistryObject<Item> DEBUG_HEALTH_RESET = ModRegistry.ITEMS.register("debug_health_reset",
            () -> new DebugHealthReset());
    public static final RegistryObject<Item> DEBUG_MANA_RESET = ModRegistry.ITEMS.register("debug_mana_reset",
            () -> new DebugManaReset());

    // 召唤物品
    public static final RegistryObject<Item> TERMINUS_STONE = ModRegistry.ITEMS.register("terminus_stone",
            () -> new TerminusStone());
    public static final RegistryObject<Item> TRAINING_PUPPET_ITEM = ModRegistry.ITEMS.register("training_puppet",
            () -> new TrainingPuppetItem());

    // 武器物品
    public static final RegistryObject<Item> SCYTHE_OF_THE_END = ModRegistry.ITEMS.register("scythe_of_the_end",
            () -> new ScytheOfTheEnd());
    public static final RegistryObject<Item> LORD_SLAYING_CURSED_BLADE = ModRegistry.ITEMS.register("lord_slaying_cursed_blade",
            () -> new LordSlayingCursedBlade());
    public static final RegistryObject<Item> YOUS_WAVE_BREAKER = ModRegistry.ITEMS.register("yous_wave_breaker",
            () -> new YOUSWaveBreaker());
    public static final RegistryObject<Item> DRAGONFANG_DAGGER = ModRegistry.ITEMS.register("dragonfang_dagger",
            () -> new DragonfangDagger());
    public static final RegistryObject<Item> FIREBALL_SPELLBOOK = ModRegistry.ITEMS.register("fireball_spellbook",
            () -> new FireballSpellbook());
    public static final RegistryObject<Item> WATER_ARROW_SPELLBOOK = ModRegistry.ITEMS.register("water_arrow_spellbook",
            () -> new WaterArrowSpellbook());
    public static final RegistryObject<Item> RECURVE_BOW = ModRegistry.ITEMS.register("recurve_bow",
            () -> new RecurveBow());
    public static final RegistryObject<Item> BLOWGUN = ModRegistry.ITEMS.register("blowgun",
            () -> new Blowgun());
    public static final RegistryObject<Item> HUNTERS_SPEAR = ModRegistry.ITEMS.register("hunters_spear",
            () -> new HuntersSpear());
    public static final RegistryObject<Item> GOBLIN_SPEAR = ModRegistry.ITEMS.register("goblin_spear",
            () -> new GoblinSpear());
    public static final RegistryObject<Item> CRYSTAL_STAFF = ModRegistry.ITEMS.register("crystal_staff",
            () -> new CrystalStaff());
    public static final RegistryObject<Item> DART = ModRegistry.ITEMS.register("dart",
            () -> new Dart());
    public static final RegistryObject<Item> PISTOL = ModRegistry.ITEMS.register("pistol",
            () -> new Pistol());
    public static final RegistryObject<Item> FIRE_STAFF = ModRegistry.ITEMS.register("fire_staff",
            () -> new FireStaff());
    public static final RegistryObject<Item> SNOW_STAFF = ModRegistry.ITEMS.register("snow_staff",
            () -> new SnowStaff());
    public static final RegistryObject<Item> ICE_STAFF = ModRegistry.ITEMS.register("ice_staff",
            () -> new IceStaff());
    public static final RegistryObject<Item> WATER_STAFF = ModRegistry.ITEMS.register("water_staff",
            () -> new WaterStaff());
    public static final RegistryObject<Item> WHIP = ModRegistry.ITEMS.register("whip",
            () -> new Whip());
    public static final RegistryObject<Item> CRYSTAL_WHIP = ModRegistry.ITEMS.register("crystal_whip",
            () -> new CrystalWhip());
    public static final RegistryObject<Item> SOUL_WHIP = ModRegistry.ITEMS.register("soul_whip",
            () -> new SoulWhip());
    public static final RegistryObject<Item> CRYSTAL_DART = ModRegistry.ITEMS.register("crystal_dart",
            () -> new CrystalDart());
    public static final RegistryObject<Item> SHOTGUN = ModRegistry.ITEMS.register("shotgun",
            () -> new Shotgun());
    public static final RegistryObject<Item> COMMANDERS_GREATAXE = ModRegistry.ITEMS.register("commanders_greataxe",
            () -> new CommandersGreataxe());
    public static final RegistryObject<Item> COMMANDERS_WHIP = ModRegistry.ITEMS.register("commanders_whip",
            () -> new CommandersWhip());
    public static final RegistryObject<Item> PHANTOM_BOW = ModRegistry.ITEMS.register("phantom_bow",
            () -> new PhantomBow());
    public static final RegistryObject<Item> GHOST_STAFF = ModRegistry.ITEMS.register("ghost_staff",
            () -> new GhostStaff());
    public static final RegistryObject<Item> BOOK_OF_VILE_DARKNESS = ModRegistry.ITEMS.register("book_of_vile_darkness",
            () -> new BookOfVileDarkness());
    public static final RegistryObject<Item> DISASTER_FLYING_AXE = ModRegistry.ITEMS.register("disaster_flying_axe",
            () -> new DisasterFlyingAxe());
    public static final RegistryObject<Item> CALAMITY_PISTOL = ModRegistry.ITEMS.register("calamity_pistol",
            () -> new CalamityPistol());
    public static final RegistryObject<Item> DESERT_SHOTGUN = ModRegistry.ITEMS.register("desert_shotgun",
            () -> new DesertShotgun());
    public static final RegistryObject<Item> BLIZZARD = ModRegistry.ITEMS.register("blizzard",
            () -> new Blizzard());
    public static final RegistryObject<Item> ROTTING_FANG_BOW = ModRegistry.ITEMS.register("rotting_fang_bow",
            () -> new RottingFangBow());
    public static final RegistryObject<Item> IMPACT_FIST = ModRegistry.ITEMS.register("impact_fist",
            () -> new ImpactFist());
    public static final RegistryObject<Item> STONEBREAKER_GREATSWORD = ModRegistry.ITEMS.register("stonebreaker_greatsword",
            () -> new StonebreakerGreatsword());
    public static final RegistryObject<Item> FLESH_AND_BLOOD_GREATSWORD = ModRegistry.ITEMS.register("flesh_and_blood_greatsword",
            () -> new FleshAndBloodGreatsword());
    public static final RegistryObject<Item> FLESH_PISTOL = ModRegistry.ITEMS.register("flesh_pistol",
            () -> new FleshPistol());
    public static final RegistryObject<Item> BLOOD_ARROW_STAFF = ModRegistry.ITEMS.register("blood_arrow_staff",
            () -> new BloodArrowStaff());
    public static final RegistryObject<Item> SHADOW_BLASTER = ModRegistry.ITEMS.register("shadow_blaster",
            () -> new ShadowBlaster());
    public static final RegistryObject<Item> SHADOW_DAGGER = ModRegistry.ITEMS.register("shadow_dagger",
            () -> new ShadowDagger());
    public static final RegistryObject<Item> BOOK_OF_SHADOW_STEALING = ModRegistry.ITEMS.register("book_of_shadow_stealing",
            () -> new BookOfShadowStealing());

    public static final RegistryObject<Item> STEEL_FEATHER = ModRegistry.ITEMS.register("steel_feather",
            () -> new SteelFeather());
    public static final RegistryObject<Item> SPECTERS_SCYTHE = ModRegistry.ITEMS.register("specters_scythe",
            () -> new SpectersScythe());
    public static final RegistryObject<Item> GHOSTLY_GRIMOIRE = ModRegistry.ITEMS.register("ghostly_grimoire",
            () -> new GhostlyGrimoire());
    public static final RegistryObject<Item> HIGH_PRESSURE_WATER_GUN = ModRegistry.ITEMS.register("high_pressure_water_gun",
            () -> new HighPressureWaterGun());
    public static final RegistryObject<Item> SACRED_FLYING_AXE = ModRegistry.ITEMS.register("sacred_flying_axe",
            () -> new SacredFlyingAxe());
    public static final RegistryObject<Item> INCOMPLETE_HOLY_SPELL_BOOK = ModRegistry.ITEMS.register("incomplete_holy_spell_book",
            () -> new IncompleteHolySpellBook());
    public static final RegistryObject<Item> BOOK_OF_SACRED_LIGHT = ModRegistry.ITEMS.register("book_of_sacred_light",
            () -> new BookOfSacredLight());
    public static final RegistryObject<Item> BOW_OF_LIGHT = ModRegistry.ITEMS.register("bow_of_light",
            () -> new BowOfLight());
    public static final RegistryObject<Item> BOOK_OF_LIGHT_AND_DARKNESS = ModRegistry.ITEMS.register("book_of_light_and_darkness",
            () -> new BookOfLightAndDarkness());
    public static final RegistryObject<Item> SPEAR_OF_LIGHT = ModRegistry.ITEMS.register("spear_of_light",
            () -> new SpearOfLight());
    public static final RegistryObject<Item> SPEAR_OF_DARKNESS = ModRegistry.ITEMS.register("spear_of_darkness",
            () -> new SpearOfDarkness());
    public static final RegistryObject<Item> REVENGE = ModRegistry.ITEMS.register("revenge",
            () -> new Revenge());
    public static final RegistryObject<Item> BOOK_OF_CORRUPTION = ModRegistry.ITEMS.register("book_of_corruption",
            () -> new BookOfCorruption());
    public static final RegistryObject<Item> ICE_SPIKE_SPEAR = ModRegistry.ITEMS.register("ice_spike_spear",
            () -> new IceSpikeSpear());
    public static final RegistryObject<Item> FURNACE_SPEAR = ModRegistry.ITEMS.register("furnace_spear",
            () -> new FurnaceSpear());
    public static final RegistryObject<Item> FLOWING_LIGHT = ModRegistry.ITEMS.register("flowing_light",
            () -> new FlowingLight());
    public static final RegistryObject<Item> BLOOD_SPIRIT_BOOK_OF_CORRUPTION = ModRegistry.ITEMS.register("blood_spirit_book_of_corruption",
            () -> new BloodSpiritBookOfCorruption());
    public static final RegistryObject<Item> BLOOD_SPIRIT_FLOWING_LIGHT = ModRegistry.ITEMS.register("blood_spirit_flowing_light",
            () -> new BloodSpiritFlowingLight());
    public static final RegistryObject<Item> ECLIPSE_ICE_SPIKE_SPEAR = ModRegistry.ITEMS.register("eclipse_ice_spike_spear",
            () -> new EclipseIceSpikeSpear());
    public static final RegistryObject<Item> ECLIPSE_FURNACE_SPEAR = ModRegistry.ITEMS.register("eclipse_furnace_spear",
            () -> new EclipseFurnaceSpear());
    public static final RegistryObject<Item> BURNING_HEAVEN = ModRegistry.ITEMS.register("burning_heaven",
            () -> new BurningHeaven());
    public static final RegistryObject<Item> ASHES = ModRegistry.ITEMS.register("ashes",
            () -> new Ashes());
    public static final RegistryObject<Item> CURSE_FIRE = ModRegistry.ITEMS.register("curse_fire",
            () -> new CurseFire());
    public static final RegistryObject<Item> DRAGON_SLAYER_GREATSWORD = ModRegistry.ITEMS.register("dragon_slayer_greatsword",
            () -> new DragonSlayerGreatsword());
    public static final RegistryObject<Item> DRAGON_FLAME_BURNING_HEAVEN = ModRegistry.ITEMS.register("dragon_flame_burning_heaven",
            () -> new DragonFlameBurningHeaven());
    public static final RegistryObject<Item> DRAGON_FLAME_ASHES = ModRegistry.ITEMS.register("dragon_flame_ashes",
            () -> new DragonFlameAshes());
    public static final RegistryObject<Item> DRAGON_FLAME_CURSE_FIRE = ModRegistry.ITEMS.register("dragon_flame_curse_fire",
            () -> new DragonFlameCurseFire());
    public static final RegistryObject<Item> ENDER_LANCE = ModRegistry.ITEMS.register("ender_lance",
            () -> new EnderLance());
    public static final RegistryObject<Item> ENDER_FLYING_KNIFE = ModRegistry.ITEMS.register("ender_flying_knife",
            () -> new EnderFlyingKnife());
    public static final RegistryObject<Item> BOOK_OF_ENDER = ModRegistry.ITEMS.register("book_of_ender",
            () -> new BookOfEnder());
    public static final RegistryObject<Item> DECAY = ModRegistry.ITEMS.register("decay",
            () -> new Decay());
    public static final RegistryObject<Item> SABOTEUR = ModRegistry.ITEMS.register("saboteur",
            () -> new Saboteur());
    public static final RegistryObject<Item> WITHER_SPEAR = ModRegistry.ITEMS.register("wither_spear",
            () -> new WitherSpear());
    public static final RegistryObject<Item> SPIRIT_FIRE_JUDGMENT = ModRegistry.ITEMS.register("spirit_fire_judgment",
            () -> new SpiritFireJudgment());
    public static final RegistryObject<Item> BURN_TO_ASHES = ModRegistry.ITEMS.register("burn_to_ashes",
            () -> new BurnToAshes());
    public static final RegistryObject<Item> ASHES_DEATH = ModRegistry.ITEMS.register("ashes_death",
            () -> new AshesDeath());
    public static final RegistryObject<Item> SHADOW_SPEAR = ModRegistry.ITEMS.register("shadow_spear",
            () -> new ShadowSpear());
    public static final RegistryObject<Item> SHREDDER = ModRegistry.ITEMS.register("shredder",
            () -> new Shredder());
    public static final RegistryObject<Item> ETERNAL_NIGHT = ModRegistry.ITEMS.register("eternal_night",
            () -> new EternalNight());
    public static final RegistryObject<Item> BLOOD_SPIRIT_BLASTER = ModRegistry.ITEMS.register("blood_spirit_blaster",
            () -> new BloodSpiritBlaster());
    public static final RegistryObject<Item> DRAGON_FLAME_BLASTER = ModRegistry.ITEMS.register("dragon_flame_blaster",
            () -> new DragonFlameBlaster());

    public static final RegistryObject<Item> DEATH_RAY = ModRegistry.ITEMS.register("death_ray",
            () -> new DeathRay());
    public static final RegistryObject<Item> STARRY_NIGHT = ModRegistry.ITEMS.register("starry_night",
            () -> new StarryNight());
    public static final RegistryObject<Item> STAR = ModRegistry.ITEMS.register("star",
            () -> new Star());
    public static final RegistryObject<Item> TORNADO = ModRegistry.ITEMS.register("tornado",
            () -> new Tornado());
    public static final RegistryObject<Item> TSUNAMI = ModRegistry.ITEMS.register("tsunami",
            () -> new Tsunami());
    public static final RegistryObject<Item> BUBBLE_GUN = ModRegistry.ITEMS.register("bubble_gun",
            () -> new BubbleGun());
    public static final RegistryObject<Item> BLOOD_TORNADO = ModRegistry.ITEMS.register("blood_tornado",
            () -> new BloodTornado());
    public static final RegistryObject<Item> ERODING_WAVE = ModRegistry.ITEMS.register("eroding_wave",
            () -> new ErodingWave());
    public static final RegistryObject<Item> BUBBLE_GUN_V2 = ModRegistry.ITEMS.register("bubble_gun_v2",
            () -> new BubbleGunV2());
    public static final RegistryObject<Item> VORTEX_MAKER = ModRegistry.ITEMS.register("vortex_maker",
            () -> new VortexMaker());
    public static final RegistryObject<Item> HARPOON = ModRegistry.ITEMS.register("harpoon",
            () -> new Harpoon());
    public static final RegistryObject<Item> POLARIZING_PRISM = ModRegistry.ITEMS.register("polarizing_prism",
            () -> new PolarizingPrism());
    public static final RegistryObject<Item> BROKEN_BLADE = ModRegistry.ITEMS.register("broken_blade",
            () -> new BrokenBlade());
    public static final RegistryObject<Item> SACRE_FLAME_MATRIX = ModRegistry.ITEMS.register("sacre_flame_matrix",
            () -> new SacreFlameMatrix());
    public static final RegistryObject<Item> SKYFIRE = ModRegistry.ITEMS.register("skyfire",
            () -> new Skyfire());
    public static final RegistryObject<Item> PHOENIX = ModRegistry.ITEMS.register("phoenix",
            () -> new Phoenix());
    public static final RegistryObject<Item> CELESTIAL_STAR = ModRegistry.ITEMS.register("celestial_star",
            () -> new CelestialStar());
    public static final RegistryObject<Item> CALAMITY = ModRegistry.ITEMS.register("calamity",
            () -> new Calamity());
    public static final RegistryObject<Item> UNFORTUNATE = ModRegistry.ITEMS.register("unfortunate",
            () -> new Unfortunate());
    public static final RegistryObject<Item> JUNGLE_SCEPTER = ModRegistry.ITEMS.register("jungle_scepter",
            () -> new JungleScepter());
    public static final RegistryObject<Item> JUNGLE_WHIP= ModRegistry.ITEMS.register("jungle_whip",
            () -> new JungleWhip());
    public static final RegistryObject<Item> NATURAL_TORNADO = ModRegistry.ITEMS.register("natural_tornado",
            () -> new NaturalTornado());
    public static final RegistryObject<Item> GREEN_LEAF_FLYING_KNIFE = ModRegistry.ITEMS.register("green_leaf_flying_knife",
            () -> new GreenLeafFlyingKnife());
    public static final RegistryObject<Item> DEATH_SCYTHE = ModRegistry.ITEMS.register("death_scythe",
            () -> new DeathScythe());
    public static final RegistryObject<Item> PURGATORY_STAFF = ModRegistry.ITEMS.register("purgatory_staff",
            () -> new PurgatoryStaff());
    public static final RegistryObject<Item> CHAOS = ModRegistry.ITEMS.register("chaos",
            () -> new Chaos());
    public static final RegistryObject<Item>ELEMENTAL_STAFF = ModRegistry.ITEMS.register("elemental_staff",
            () -> new ElementalStaff());
    public static final RegistryObject<Item> ELEMENTAL_WHIP = ModRegistry.ITEMS.register("elemental_whip",
            () -> new ElementalWhip());
    public static final RegistryObject<Item> TRACER_LIGHT = ModRegistry.ITEMS.register("tracer_light",
            () -> new TracerLight());
    public static final RegistryObject<Item> STARRY_JUDGMENT = ModRegistry.ITEMS.register("starry_judgment",
            () -> new StarryJudgment());
    public static final RegistryObject<Item> HOLY_FLAME_JUDGMENT = ModRegistry.ITEMS.register("holy_flame_judgment",
            () -> new HolyFlameJudgment());
    public static final RegistryObject<Item> ELEMENT_JUDGMENT = ModRegistry.ITEMS.register("element_judgment",
            () -> new ElementJudgment());
    public static final RegistryObject<Item> ANNIHILATION_JUDGMENT = ModRegistry.ITEMS.register("annihilation_judgment",
            () -> new AnnihilationJudgment());
    public static final RegistryObject<Item> FLAME_STAR = ModRegistry.ITEMS.register("flame_star",
            () -> new FlameStar());
    public static final RegistryObject<Item> PUNISHMENT = ModRegistry.ITEMS.register("punishment",
            () -> new Punishment());
    public static final RegistryObject<Item> STARBURST = ModRegistry.ITEMS.register("starburst",
            () -> new Starburst());
    public static final RegistryObject<Item> ABYSS_OF_CALAMITY = ModRegistry.ITEMS.register("abyss_of_calamity",
            () -> new AbyssOfCalamity());
    public static final RegistryObject<Item> NATURAL_WILL = ModRegistry.ITEMS.register("natural_will",
            () -> new NaturalWill());
    public static final RegistryObject<Item> DIVINE_SPEAR_DISASTER_BREAKER = ModRegistry.ITEMS.register("divine_spear_disaster_breaker",
            () -> new DivineSpearDisasterBreaker());
    public static final RegistryObject<Item> FANTASY = ModRegistry.ITEMS.register("fantasy",
            () -> new Fantasy());
    public static final RegistryObject<Item> STAFF_OF_ORIGINS = ModRegistry.ITEMS.register("staff_of_origins",
            () -> new StaffOfOrigins());
    public static final RegistryObject<Item> BOW_OF_SHATTERED_REALM = ModRegistry.ITEMS.register("bow_of_shattered_realm",
            () -> new BowOfShatteredRealm());
    public static final RegistryObject<Item> ULTIMATE_SUBMACHINE_GUN = ModRegistry.ITEMS.register("ultimate_submachine_gun",
            () -> new UltimateSubmachineGun());
    public static final RegistryObject<Item> SHADOW_PIERCING_SPEAR = ModRegistry.ITEMS.register("shadow_piercing_spear",
            () -> new ShadowPiercingSpear());


    // 获取所有饰品物品（包括翅膀）
    public static RegistryObject<Item>[] getAccessoryItems() {
        return new RegistryObject[] {
                ACID_ETCHED_SAC,
                ACTION_BOOTS,
                ANTISLIP_GLOVES,
                BLOOD_SPIRIT_NECKLACE,
                BLOODSTAINED_GLOVES,
                BLOODTOOTH_NECKLACE,
                BLOODY_SCARF,
                BOOTS_OF_ICE_AND_FIRE,
                BOOTS_OF_THE_ELEMENTS,
                BOOTS_OF_THE_JOURNEY,
                BOOTS_OF_THE_WATER_STRIDER,
                BOTTLE_OF_STARS,
                CANNED_HEART,
                CERTIFICATE_OF_VALOR,
                COMMANDERS_SHIELD,
                CORRUPT_SCARF,
                CROWN_OF_THE_SUPREME_DEMIGOD,
                CURSEFIRE_TOTEM,
                CURSEFLAME_BLOSSOM,
                DARK_AMULET,
                DARK_SPIRIT_NECKLACE,
                DEXTEROUS_GLOVE,
                DIVINE_CORE,
                DIVINE_SHIELD,
                DIVING_MASK,
                DRAGON_BRACERS,
                DRAGON_ENGRAVED_HILT,
                FLAME_FLOWER,
                FLAME_SPIRIT_ESSENCE,
                FLIPPERS,
                FRAGRANT_SCARF,
                FRESH_SACHET,
                FUNERAL_FLOWER,
                GOBLIN_TOTEM,
                GREAT_MAGE_MEDAL,
                HEART_OF_PROTECTION,
                HOLY_SPIRIT_BOOTS,
                JET_BOOTS,
                KNIGHTS_SHIELD,
                LAVA_BOOTS,
                LUMINOUS_CLOAK,
                LUMINOUS_TOTEM,
                MAGIC_AMULET,
                MAGIC_ARROW,
                MAGIC_CONDENSER,
                MANA_BLESSING_SPELL,
                MECHANICAL_BOOTS,
                MECHANICAL_BRACER,
                MECHANICAL_EYE_OF_INTELLIGENCE,
                MECHANICAL_GLOVE,
                MEDAL_OF_VENGEANCE,
                MOLTEN_LIGHT_SHIELD,
                NAMELESS_CROWN,
                NECKLACE_OF_LIFE,
                OBSIDIAN_BOOTS,
                PHANTOM_CHARM,
                POWER_GLOVE,
                RING_OF_VENGEANCE,
                SHADOW_WRISTGUARDS,
                SNAKE_TOTEM,
                SOUL_RING,
                SPELLFIRE_GAUNTLETS,
                SPELL_OF_FIRE_MANA_CHARM,
                SPIRIT_FIRE_SHIELD,
                STAR_CHARM,
                STAR_CLOAK,
                SUN_ECLIPSE_TOTEM,
                THREE_PHASE_TOTEM,
                TORTOISE_SHIELD,
                TRAVELERS_BOOTS,
                TROLL_GLOVES,
                VIOLENCE_GLOVES,
                WIZARD_HAT,
                WRAITH_BRACERS,
                EARTH_AMULET,
                ECLIPSE_MIRROR,
                ULTIMATE_YIN_STONE,
                MECHANICAL_HEART,
                OCEAN_TOTEM,
                BRACELET_OF_THE_SEA,
                WATER_SPIRIT_GLOVES,
                TSUNAMI_NECKLACE,
                AZURE_MEDAL,
                AZURE_COASTAL_FLOWER,
                HEART_OF_THE_ORIGIN,
                COMPASS_OF_THE_SOUL,
                GLOW_ABYSS_STONE,
                ABYSS_DRAGON_BALL,
                ESSENCE_OF_THE_EARTH_SPIRIT,
                SACRED_FLAME_GLOVES,
                SACRED_FLAME_TOTEM,
                NECKLACE_OF_CALAMITY,
                FLAME_SPIRIT_FLOWER,
                OMINOUS_CURSE,
                STONE_OF_EARTH_ELEMENTAL_SPIRIT,
                NORMALIZED_COMPASS,
                JUNGLE_TOTEM,
                SPIRIT_NECKLACE,
                NATURE_MEDAL,
                NECROMANCERS_NECKLACE,
                TOTEM_OF_THE_UNDEAD,
                NECROTIC_BLOSSOM,
                SOUL_BRACERS,
                GRIP_OF_THE_ELEMENTS,
                TALISMAN_OF_THE_END,
                HOLY_SPIRIT_TOTEM,
                SPIRIT_WRISTGUARD,
                NIGHTMARE_MEDAL,
                ELEMENTAL_BLOOM,
                ELEMENTAL_FORMATION_SHIELD,
                HEART_OF_THE_ELEMENTS,

                // 翅膀饰品
                MECHANICAL_WINGS,
                PHANTOM_WINGS,
                DEMONS_WINGS,
                SPIRIT_FIRE_WINGS,
                DRAGON_WINGS,
                FROST_FEATHER_WING,
                SANDSTORM_WING
        };
    }

    // 获取所有普通物品
    public static RegistryObject<Item>[] getNormalItems() {
        return new RegistryObject[] {
                ABYSSAL_DEMON_INGOT,
                SPIRIT_OF_FIRE,
                STAR_SHARD,
                WISP_OF_BLACK,
                SOUL_INGOT,
                SOUL_SHARD,
                BRIGHT_INGOT,
                DARK_ABYSS_INGOT,
                DARK_ABYSS_SHARD,
                ELEMENT_INGOT,
                ELEMENT_SHARD,
                ENDER_INGOT,
                ENDER_POWDER,
                ESSENCE_OF_HELL,
                ESSENCE_OF_HELL_INGOT,
                FINAL_CRYSTALLIZATION,
                FIRE_INGOT,
                HOLY_FIRE_INGOT,
                HOLY_FLAME_ESSENCE,
                JUNGLE_ALLOY,
                JUNGLE_ESSENCE,
                KNIGHT_INGOT,
                OCEAN_DRAGON_SOUL,
                SHADOW_INGOT,
                CELESTIAL_RADIANCE_INGOT,
                HEART_OF_LIFE,
                MAGIC_STAR,
                
                // 治疗药水
                HEALING_POTION_BASIC,
                HEALING_POTION_INTERMEDIATE,
                HEALING_POTION_ADVANCED,
                HEALING_POTION_ULTIMATE,
                
                // 魔力药水
                MANA_POTION_BASIC,
                MANA_POTION_INTERMEDIATE,
                MANA_POTION_ADVANCED,
                MANA_POTION_ULTIMATE
        };
    }

    // 获取所有武器物品
    public static RegistryObject<Item>[] getWeaponItems() {
        return new RegistryObject[] {
                SCYTHE_OF_THE_END,
                LORD_SLAYING_CURSED_BLADE,
                YOUS_WAVE_BREAKER,
                DRAGONFANG_DAGGER,
                FIREBALL_SPELLBOOK,
                WATER_ARROW_SPELLBOOK,
                RECURVE_BOW,
                BLOWGUN,
                HUNTERS_SPEAR,
                GOBLIN_SPEAR,
                CRYSTAL_STAFF,
                DART,
                PISTOL,
                FIRE_STAFF,
                ICE_STAFF,
                WATER_STAFF,
                SNOW_STAFF,
                WHIP,
                CRYSTAL_WHIP,
                SOUL_WHIP,
                CRYSTAL_DART,
                SHOTGUN,
                COMMANDERS_GREATAXE,
                COMMANDERS_WHIP,
                PHANTOM_BOW,
                GHOST_STAFF,
                BOOK_OF_VILE_DARKNESS,
                DISASTER_FLYING_AXE,
                CALAMITY_PISTOL,
                CALAMITY,
                DESERT_SHOTGUN,
                BLIZZARD,
                ROTTING_FANG_BOW,
                IMPACT_FIST,
                STONEBREAKER_GREATSWORD,
                FLESH_AND_BLOOD_GREATSWORD,
                FLESH_PISTOL,
                BLOOD_ARROW_STAFF,
                SHADOW_BLASTER,
                SHADOW_DAGGER,
                BOOK_OF_SHADOW_STEALING,

                STEEL_FEATHER,
                SPECTERS_SCYTHE,
                GHOSTLY_GRIMOIRE,
                HIGH_PRESSURE_WATER_GUN,
                SACRED_FLYING_AXE,
                INCOMPLETE_HOLY_SPELL_BOOK,
                BOOK_OF_SACRED_LIGHT,
                BOW_OF_LIGHT,
                BOOK_OF_LIGHT_AND_DARKNESS,
                SPEAR_OF_LIGHT,
                SPEAR_OF_DARKNESS,
                REVENGE,
                BOOK_OF_CORRUPTION,
                ICE_SPIKE_SPEAR,
                FURNACE_SPEAR,
                FLOWING_LIGHT,
                BLOOD_SPIRIT_BOOK_OF_CORRUPTION,
                BLOOD_SPIRIT_FLOWING_LIGHT,
                ECLIPSE_ICE_SPIKE_SPEAR,
                ECLIPSE_FURNACE_SPEAR,
                BURNING_HEAVEN,
                ASHES,
                CURSE_FIRE,
                DRAGON_SLAYER_GREATSWORD,
                DRAGON_FLAME_BURNING_HEAVEN,
                DRAGON_FLAME_ASHES,
                DRAGON_FLAME_CURSE_FIRE,
                ENDER_LANCE,
                ENDER_FLYING_KNIFE,
                BOOK_OF_ENDER,
                DECAY,
                SABOTEUR,
                WITHER_SPEAR,
                SPIRIT_FIRE_JUDGMENT,
                BURN_TO_ASHES,
                ASHES_DEATH,
                SHADOW_SPEAR,
                SHREDDER,
                ETERNAL_NIGHT,
                BLOOD_SPIRIT_BLASTER,
                DRAGON_FLAME_BLASTER,
                DEATH_RAY,
                STARRY_NIGHT,
                STAR,
                TORNADO,
                TSUNAMI,
                BUBBLE_GUN,
                BLOOD_TORNADO,
                ERODING_WAVE,
                BUBBLE_GUN_V2,
                VORTEX_MAKER,
                HARPOON,
                POLARIZING_PRISM,
                BROKEN_BLADE,
                SACRE_FLAME_MATRIX,
                SKYFIRE,
                PHOENIX,
                CELESTIAL_STAR,
                CALAMITY,
                UNFORTUNATE,
                JUNGLE_SCEPTER,
                JUNGLE_WHIP,
                NATURAL_TORNADO,
                GREEN_LEAF_FLYING_KNIFE,
                DEATH_SCYTHE,
                PURGATORY_STAFF,
                CHAOS,
                ELEMENTAL_STAFF,
                ELEMENTAL_WHIP,
                TRACER_LIGHT,
                STARRY_JUDGMENT,
                HOLY_FLAME_JUDGMENT,
                ELEMENT_JUDGMENT,
                ANNIHILATION_JUDGMENT,
                FLAME_STAR,
                PUNISHMENT,
                STARBURST,
                ABYSS_OF_CALAMITY,
                NATURAL_WILL,
                DIVINE_SPEAR_DISASTER_BREAKER,
                FANTASY,
                STAFF_OF_ORIGINS,
                BOW_OF_SHATTERED_REALM,
                ULTIMATE_SUBMACHINE_GUN,
                SHADOW_PIERCING_SPEAR
        };
    }

    /**
     * 获取所有召唤物品
     */
    public static RegistryObject<Item>[] getSummonItems()
    {
        return new RegistryObject[]{
                TERMINUS_STONE,
                TRAINING_PUPPET_ITEM
        };
    }

    /**
     * 获取所有方块物品
     */
    public static RegistryObject<Item>[] getBlockItems()
    {
        return new RegistryObject[]{
                ModBlocks.SPOTLIGHT_ALTAR_ITEM
        };
    }

    // 获取所有物品（包含所有类型）
    public static RegistryObject<Item>[] getItems() {
        RegistryObject<Item>[] accessories = getAccessoryItems();
        RegistryObject<Item>[] normalItems = getNormalItems();
        RegistryObject<Item>[] weapons = getWeaponItems();
        RegistryObject<Item>[] summonItems = getSummonItems();

        RegistryObject<Item>[] allItems = new RegistryObject[accessories.length + normalItems.length + weapons.length + summonItems.length];

        System.arraycopy(accessories, 0, allItems, 0, accessories.length);
        System.arraycopy(normalItems, 0, allItems, accessories.length, normalItems.length);
        System.arraycopy(weapons, 0, allItems, accessories.length + normalItems.length, weapons.length);
        System.arraycopy(summonItems, 0, allItems, accessories.length + normalItems.length + weapons.length, summonItems.length);

        return allItems;
    }
}