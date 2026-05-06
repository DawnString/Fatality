package cn.dawnstring.fatality.items;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.inventory.AccessoryInventory;
import cn.dawnstring.fatality.network.DamageIndicatorPacket;
import cn.dawnstring.fatality.utils.TooltipHelper;
import cn.dawnstring.fatality.network.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class BaseWeapon extends SwordItem {

    private static final Logger LOGGER = Fatality.LOGGER;

    private static final float NON_CRIT_MULTIPLIER = 0.9f;
    private static final float CRIT_MULTIPLIER = 0.8f;
    private static final float MIN_DAMAGE = 0.1f;
    private static final float MIN_EFFECTIVE_DAMAGE = 0.5f;
    private static final float FALLBACK_DAMAGE = 1.0f;
    private static final float FALLBACK_BONUS = 0.1f;
    private static final float DEFAULT_ACCESSORY_BONUS = 0.05f;
    private static final float STRENGTH_BONUS_PER_LEVEL = 0.13f;
    private static final float WEAKNESS_PENALTY_PER_LEVEL = 0.2f;
    private static final float MIN_OTHER_BONUS = 0.1f;
    private static final double DEFAULT_ATTACK_RANGE = 3.0;

    protected final Random random = new Random();

    protected final float baseDamageMultiplier;
    protected final float criticalChance;
    protected final float criticalDamageMultiplier;
    protected final float damageFluctuation;
    protected final int baseAttackDamage;
    protected final WeaponEnum weaponType;

    protected String story;

    public BaseWeapon(Tier tier, Properties properties, int attackDamage, float attackSpeedInSeconds,
                      float baseDamageMultiplier, float criticalChance,
                      float criticalDamageMultiplier, float damageFluctuation, WeaponEnum weaponType)
    {
        super(tier, attackDamage, (1.0f / attackSpeedInSeconds), properties);
        this.baseDamageMultiplier = baseDamageMultiplier;
        this.baseAttackDamage = attackDamage;
        this.criticalChance = criticalChance;
        this.criticalDamageMultiplier = criticalDamageMultiplier;
        this.damageFluctuation = damageFluctuation;
        this.weaponType = weaponType;
    }

    public void setStory(String story) {
        this.story = story;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        return InteractionResultHolder.pass(itemstack);
    }

    protected String getEnchantmentsDescription(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        if (enchantments.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            Component enchantmentName = enchantment.getFullname(level);
            sb.append(enchantmentName.getString()).append("\n");
        }

        return sb.toString().trim();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TooltipHelper.addWeaponTooltip(stack, level, tooltip, flag, story,
                baseDamageMultiplier, criticalChance,
                criticalDamageMultiplier, damageFluctuation);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            float finalDamage = calculateFinalDamage(player, stack, target);

            LOGGER.info("BaseWeapon.hurtEnemy: 计算伤害 = {}, 目标 = {}, 攻击者 = {}, 是否客户端 = {}",
                    finalDamage,
                    target.getType().getDescription().getString(),
                    player.getName().getString(),
                    target.level().isClientSide());

            if (finalDamage > 0) {
                float effectiveDamage = Math.max(MIN_EFFECTIVE_DAMAGE, finalDamage);

                boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), effectiveDamage);
                LOGGER.info("BaseWeapon.hurtEnemy: 伤害是否应用成功 = {}", damageApplied);

                if (!damageApplied) {
                    LOGGER.info("BaseWeapon.hurtEnemy: 尝试使用通用伤害源");
                    damageApplied = target.hurt(target.damageSources().generic(), effectiveDamage);
                    LOGGER.info("BaseWeapon.hurtEnemy: 通用伤害源是否成功 = {}", damageApplied);
                }

                if (!target.level().isClientSide()) {
                    LOGGER.info("BaseWeapon.hurtEnemy: 发送伤害网络包: {}", finalDamage);
                    NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> target),
                            new DamageIndicatorPacket(target.getId(), finalDamage, player.getId()));
                }

                if (damageApplied) {
                    if (isCriticalHit(player)) {
                        onCriticalHit(player, target, finalDamage);
                    }
                    onHitEnemy(player, target, stack, finalDamage);
                }

                return damageApplied;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    protected float calculateFinalDamage(Player player, ItemStack stack, LivingEntity target) {
        float baseDamage = getBaseDamage(player, stack);
        if (baseDamage <= 0) {
            baseDamage = FALLBACK_DAMAGE;
        }

        AccessoryBonuses accessoryBonuses = collectAccessoryBonuses(player);

        float accessoryBaseBonus = accessoryBonuses.baseDamageBonus;
        if (accessoryBaseBonus <= 0) {
            accessoryBaseBonus = FALLBACK_BONUS;
        }

        float otherBonus = calculateOtherBonus(player);
        if (otherBonus <= 0) {
            otherBonus = FALLBACK_BONUS;
        }

        float fluctuation = calculateDamageFluctuation();
        if (fluctuation <= 0) {
            fluctuation = FALLBACK_BONUS;
        }

        boolean isCritical = isCriticalHit(player, accessoryBonuses.criticalChanceBonus);

        float finalDamage;
        if (isCritical) {
            float criticalBonus = criticalDamageMultiplier + accessoryBonuses.criticalDamageBonus;
            if (criticalBonus <= 0) {
                criticalBonus = FALLBACK_DAMAGE;
            }
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * CRIT_MULTIPLIER * criticalBonus * fluctuation;
        } else {
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * NON_CRIT_MULTIPLIER * fluctuation;
        }

        if (Float.isNaN(finalDamage) || Float.isInfinite(finalDamage)) {
            finalDamage = FALLBACK_DAMAGE;
        }

        return Math.max(MIN_DAMAGE, finalDamage);
    }

    protected float getBaseDamage(Player player, ItemStack stack) {
        float weaponDamage = baseAttackDamage;

        var attackDamageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            weaponDamage += (float) attackDamageAttr.getValue();
        }

        return weaponDamage * baseDamageMultiplier;
    }

    protected float calculateAccessoryBaseBonus(Player player) {
        float bonus = FALLBACK_DAMAGE;

        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (!accessory.isEmpty()) {
                    bonus += getAccessoryBaseDamageBonus(accessory);
                }
            }
        }

        return bonus;
    }

    protected float getAccessoryBaseDamageBonus(ItemStack accessory) {
        if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
            float panelBonus = accessoryItem.getPanelDamageBonus();
            if (panelBonus > 0) {
                return panelBonus;
            }
            return DEFAULT_ACCESSORY_BONUS;
        }
        return DEFAULT_ACCESSORY_BONUS;
    }

    protected float calculateOtherBonus(Player player) {
        float bonus = FALLBACK_DAMAGE;

        if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
            int amplifier = player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier();
            bonus += (amplifier + 1) * STRENGTH_BONUS_PER_LEVEL;
        }

        if (player.hasEffect(MobEffects.WEAKNESS)) {
            int amplifier = player.getEffect(MobEffects.WEAKNESS).getAmplifier();
            bonus -= (amplifier + 1) * WEAKNESS_PENALTY_PER_LEVEL;
        }

        return Math.max(MIN_OTHER_BONUS, bonus);
    }

    protected float calculateDamageFluctuation() {
        float min = 1.0f - damageFluctuation;
        float max = 1.0f + damageFluctuation;
        return min + random.nextFloat() * (max - min);
    }

    public boolean isCriticalHit(Player player) {
        AccessoryBonuses bonuses = collectAccessoryBonuses(player);
        return isCriticalHit(player, bonuses.criticalChanceBonus);
    }

    private boolean isCriticalHit(Player player, float accessoryCriticalChanceBonus) {
        float totalCriticalChance = criticalChance + accessoryCriticalChanceBonus;
        return random.nextFloat() < totalCriticalChance;
    }

    public float calculateAccessoryCriticalChanceBonus(Player player) {
        return collectAccessoryBonuses(player).criticalChanceBonus;
    }

    protected float getCriticalDamageMultiplier(Player player) {
        float accessoryCriticalBonus = calculateAccessoryCriticalBonus(player);
        return criticalDamageMultiplier + accessoryCriticalBonus;
    }

    public float calculateAccessoryCriticalBonus(Player player) {
        return collectAccessoryBonuses(player).criticalDamageBonus;
    }

    protected float getAccessoryCriticalDamageBonus(ItemStack accessory, WeaponEnum weaponType) {
        if (accessory.getItem() instanceof AccessoryItem accessoryItem) {
            return switch (weaponType) {
                case MELEE -> accessoryItem.getMeleeCriticalDamageBonus();
                case RANGED -> accessoryItem.getRangedCriticalDamageBonus();
                case MAGIC -> accessoryItem.getMagicCriticalDamageBonus();
            };
        }
        return 0.0f;
    }

    private AccessoryBonuses collectAccessoryBonuses(Player player) {
        float baseDamageBonus = FALLBACK_DAMAGE;
        float criticalChanceBonus = 0.0f;
        float criticalDamageBonus = 0.0f;

        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            var itemHandler = accessoryInventory.getItemHandler();
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof AccessoryItem accessoryItem) {
                    baseDamageBonus += getAccessoryBaseDamageBonus(stack);
                    criticalChanceBonus += accessoryItem.getCriticalChanceBonus();
                    criticalDamageBonus += switch (this.weaponType) {
                        case MELEE -> accessoryItem.getMeleeCriticalDamageBonus();
                        case RANGED -> accessoryItem.getRangedCriticalDamageBonus();
                        case MAGIC -> accessoryItem.getMagicCriticalDamageBonus();
                    };
                }
            }
        }

        return new AccessoryBonuses(baseDamageBonus, criticalChanceBonus, criticalDamageBonus);
    }

    public WeaponEnum getWeaponType() {
        return weaponType;
    }

    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    Component.literal("§6暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    protected void onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage) {
    }

    public double getAttackRange(Player player) {
        return DEFAULT_ATTACK_RANGE;
    }

    public String getSpecialEffectDescription() {
        return "基础伤害倍率: " + String.format("%.1f", baseDamageMultiplier) +
                " | 暴击率: " + String.format("%.1f%%", criticalChance * 100) +
                " | 暴击伤害: " + String.format("%.1f", criticalDamageMultiplier) + "倍";
    }

    /**
     * 计算锥形散布方向（使用局部坐标系，修复飞行时散布方向错误的问题）
     * @param baseDirection 基础方向（玩家视线方向）
     * @param pelletIndex 弹丸索引
     * @param totalPellets 总弹丸数量
     * @param spreadAngle 散射角度（度）
     * @return 散布后的方向向量
     */
    protected Vec3 calculateConeSpreadDirection(Vec3 baseDirection, int pelletIndex, int totalPellets, float spreadAngle) {
        double spreadRad = Math.toRadians(spreadAngle);
        double angleStep = 2 * Math.PI / totalPellets;
        double angle = pelletIndex * angleStep;
        double radius = Math.random() * spreadRad;

        double horizontalOffset = radius * Math.cos(angle);
        double verticalOffset = radius * Math.sin(angle);

        Vec3 forward = baseDirection.normalize();

        Vec3 right;
        if (Math.abs(forward.y) > 0.99) {
            right = forward.cross(new Vec3(0, 0, 1)).normalize();
        } else {
            right = forward.cross(new Vec3(0, 1, 0)).normalize();
        }
        Vec3 localUp = right.cross(forward).normalize();

        Vec3 spreadVec = forward
                .add(right.scale(horizontalOffset))
                .add(localUp.scale(verticalOffset))
                .normalize();

        return spreadVec;
    }

    private record AccessoryBonuses(float baseDamageBonus, float criticalChanceBonus, float criticalDamageBonus) {}
}
