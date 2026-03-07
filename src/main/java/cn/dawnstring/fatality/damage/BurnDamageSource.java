package cn.dawnstring.fatality.damage;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BurnDamageSource extends DamageSource {

    public BurnDamageSource(Holder<DamageType> damageType, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition) {
        super(damageType, directEntity, causingEntity, damageSourcePosition);
    }

    // 静态工厂方法，方便创建实例
    public static BurnDamageSource burn(@Nullable Entity directEntity, @Nullable Entity causingEntity) {
        // 使用火焰伤害类型作为基础
        Holder<DamageType> damageType = getFireDamageTypeHolder(directEntity);

        return new BurnDamageSource(damageType, directEntity, causingEntity, null);
    }

    public static BurnDamageSource burn() {
        return burn(null, null);
    }

    // 辅助方法：获取火焰伤害类型的Holder
    private static Holder<DamageType> getFireDamageTypeHolder(@Nullable Entity entity) {
        if (entity != null) {
            // 从实体的level中获取火焰伤害类型的Holder
            Level level = entity.level();
            return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.ON_FIRE);
        } else {
            // 如果entity为null，抛出异常
            throw new IllegalStateException("Cannot get damage type holder without entity context");
        }
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity entity) {
        String messageKey = "death.attack.burn";
        Entity killer = entity.getKillCredit();

        if (killer != null) {
            return Component.translatable(messageKey + ".player", entity.getDisplayName(), killer.getDisplayName());
        } else {
            return Component.translatable(messageKey, entity.getDisplayName());
        }
    }
}