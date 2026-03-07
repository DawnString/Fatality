package cn.dawnstring.fatality.registry;

import cn.dawnstring.fatality.effects.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects
{
    // 护甲撕裂效果
    public static RegistryObject<MobEffect> ARMOR_EROSION;
    // 灵火灼烧效果
    public static RegistryObject<MobEffect> SPIRITUAL_FIRE_BURN;
    // 治疗饱和效果
    public static RegistryObject<MobEffect> TREATMENT_SATURATION;
    // 魔力衰减效果
    public static RegistryObject<MobEffect> MAGIC_FADE;
    // 冻结效果
    public static RegistryObject<MobEffect> FREEZE;
    // 护甲破碎效果
    public static RegistryObject<MobEffect> ARMOR_BREAK;
    // 灼烧效果
    public static RegistryObject<MobEffect> BURN;
    // 诅咒火焰效果
    public static RegistryObject<MobEffect> CURSE_FIRE_BURNING;
    // 龙炎焚烧效果
    public static RegistryObject<MobEffect> DRAGONFIRE_BURN;
    // 屠龙者祝福效果
    public static RegistryObject<MobEffect> DRAGON_SLAYER_BLESSING;
    // 星光标记效果
    public static RegistryObject<MobEffect> STARLIGHT_MARK;
    // 灼痕印记效果
    public static RegistryObject<MobEffect> BURN_MARK;
    // 罪印标记效果
    public static RegistryObject<MobEffect> SIN_MARK;
    
    public static void registerEffects()
    {
        // 注册护甲撕裂效果
        ARMOR_EROSION = ModRegistry.EFFECTS.register("armor_erosion", ArmorErosionEffect::new);
        // 注册灵火灼烧效果
        SPIRITUAL_FIRE_BURN = ModRegistry.EFFECTS.register("spiritual_fire_burn", SpiritualFireBurnEffect::new);
        // 注册治疗饱和效果
        TREATMENT_SATURATION = ModRegistry.EFFECTS.register("treatment_saturation", TreatmentSaturationEffect::new);
        // 注册魔力衰减效果
        MAGIC_FADE = ModRegistry.EFFECTS.register("magic_fade", MagicFadeEffect::new);
        // 注册冻结效果
        FREEZE = ModRegistry.EFFECTS.register("freeze", FreezeEffect::new);
        // 注册护甲破碎效果
        ARMOR_BREAK = ModRegistry.EFFECTS.register("armor_break", ArmorBreakEffect::new);
        // 注册灼烧效果
        BURN = ModRegistry.EFFECTS.register("burn", BurnEffect::new);
        // 注册诅咒火焰效果
        CURSE_FIRE_BURNING = ModRegistry.EFFECTS.register("curse_fire_burning", CurseFireBurningEffect::new);
        // 注册龙炎焚烧效果
        DRAGONFIRE_BURN = ModRegistry.EFFECTS.register("dragonfire_burn", DragonfireBurnEffect::new);
        // 注册屠龙者祝福效果
        DRAGON_SLAYER_BLESSING = ModRegistry.EFFECTS.register("dragon_slayer_blessing", DragonSlayerBlessingEffect::new);
        // 注册星光标记效果
        STARLIGHT_MARK = ModRegistry.EFFECTS.register("starlight_mark", StarlightMarkEffect::new);
        // 注册灼痕印记效果
        BURN_MARK = ModRegistry.EFFECTS.register("burn_mark", BurnMarkEffect::new);
        // 注册罪印标记效果
        SIN_MARK = ModRegistry.EFFECTS.register("sin_mark", SinMarkEffect::new);
    }
}