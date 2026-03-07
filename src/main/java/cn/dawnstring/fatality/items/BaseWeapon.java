package cn.dawnstring.fatality.items;

import cn.dawnstring.fatality.inventory.AccessoryInventory;
import cn.dawnstring.fatality.network.DamageIndicatorPacket;
import cn.dawnstring.fatality.util.TooltipHelper;
import cn.dawnstring.fatality.network.NetworkManager;
import net.minecraft.client.Minecraft;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 武器基类 - 提供复杂的伤害计算系统
 * 伤害计算公式：
 * 未暴击伤害 = 面板伤害 * 基础伤害加成(基于饰品) * 其他伤害加成(饰品，药水等) * 0.9 * 浮动值（根据武器设置）
 * 暴击伤害 = 面板伤害 * 基础伤害加成(基于饰品) * 其他伤害加成(饰品，药水等) * 0.8 * 爆伤加成 * 浮动值（根据武器设置）
 */
public class BaseWeapon extends SwordItem {

    protected final Random random = new Random();

    // 武器配置参数
    protected final float baseDamageMultiplier;        // 基础伤害倍率
    protected final float criticalChance;               // 基础暴击率
    protected final float criticalDamageMultiplier;    // 暴击伤害倍率
    protected final float damageFluctuation;           // 伤害浮动范围
    protected final int baseAttackDamage;
    protected final WeaponEnum weaponType;             // 武器类型

    // 描述信息
    protected String story;

    /**
     * 构造函数 - 初始化武器属性
     *
     * @param tier                     武器等级
     * @param properties               物品属性
     * @param attackDamage             基础攻击伤害
     * @param attackSpeedInSeconds              攻击速度（秒为单位，例如：1.5表示1.5秒攻击一次）
     * @param baseDamageMultiplier     基础伤害倍率
     * @param criticalChance           基础暴击率
     * @param criticalDamageMultiplier 暴击伤害倍率
     * @param damageFluctuation        伤害浮动范围
     * @param weaponType               武器类型
     */
    public BaseWeapon(Tier tier, Properties properties, int attackDamage, float attackSpeedInSeconds,
                      float baseDamageMultiplier, float criticalChance,
                      float criticalDamageMultiplier, float damageFluctuation, WeaponEnum weaponType)
    {
        // 直接在super调用中计算Minecraft攻击速度
        super(tier, attackDamage, (1.0f / attackSpeedInSeconds), properties);
        this.baseDamageMultiplier = baseDamageMultiplier;
        this.baseAttackDamage = attackDamage;
        this.criticalChance = criticalChance;
        this.criticalDamageMultiplier = criticalDamageMultiplier;
        this.damageFluctuation = damageFluctuation;
        this.weaponType = weaponType;
    }

    /**
     * 设置物品故事
     */
    public void setStory(String story) {
        this.story = story;
    }

    /**
     * 右键使用方法 - 子类可以重写此方法来实现特殊功能
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 默认行为：不执行任何操作，子类可以重写
        return InteractionResultHolder.pass(itemstack);
    }

    /**
     * 获取原版附魔信息
     */
    protected String getEnchantmentsDescription(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        if (enchantments.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            // 获取附魔的显示名称
            Component enchantmentName = enchantment.getFullname(level);
            sb.append(enchantmentName.getString()).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 重写工具提示方法，实现按键显示不同描述
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 使用统一的工具提示辅助类
        TooltipHelper.addWeaponTooltip(stack, level, tooltip, flag, story,
                baseDamageMultiplier, criticalChance,
                criticalDamageMultiplier, damageFluctuation);
    }

    /**
     * 重写伤害计算方法，应用复杂的伤害计算逻辑
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // 计算最终伤害
            float finalDamage = calculateFinalDamage(player, stack, target);

            System.out.println("BaseWeapon.hurtEnemy: 计算伤害 = " + finalDamage);
            System.out.println("BaseWeapon.hurtEnemy: 目标 = " + target.getType().getDescription().getString());
            System.out.println("BaseWeapon.hurtEnemy: 攻击者 = " + player.getName().getString());
            System.out.println("BaseWeapon.hurtEnemy: 是否客户端 = " + target.level().isClientSide());

            // 应用伤害
            if (finalDamage > 0) {
                // 确保伤害值足够大，避免被游戏忽略
                float effectiveDamage = Math.max(0.5f, finalDamage);

                // 使用正确的伤害源
                boolean damageApplied = target.hurt(target.damageSources().playerAttack(player), effectiveDamage);
                System.out.println("BaseWeapon.hurtEnemy: 伤害是否应用成功 = " + damageApplied);

                // 如果伤害应用失败，尝试使用不同的伤害源
                if (!damageApplied) {
                    System.out.println("BaseWeapon.hurtEnemy: 尝试使用通用伤害源");
                    damageApplied = target.hurt(target.damageSources().generic(), effectiveDamage);
                    System.out.println("BaseWeapon.hurtEnemy: 通用伤害源是否成功 = " + damageApplied);
                }

                // 无论伤害是否应用成功，都发送伤害显示数据包
                // 这样即使伤害被阻挡，玩家也能看到伤害数值
                if (!target.level().isClientSide()) {
                    System.out.println("BaseWeapon.hurtEnemy: 发送伤害网络包: " + finalDamage);
                    NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> target),
                            new DamageIndicatorPacket(
                                    target.getId(),
                                    finalDamage,
                                    player.getId()
                            ));
                } else {
                    System.out.println("BaseWeapon.hurtEnemy: 在客户端，不发送网络包");
                }

                // 如果伤害应用成功，触发特效
                if (damageApplied) {
                    // 触发暴击特效
                    if (isCriticalHit(player)) {
                        onCriticalHit(player, target, finalDamage);
                    }

                    // 触发武器特效
                    onHitEnemy(player, target, stack, finalDamage);
                }

                return damageApplied;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    /**
     * 计算最终伤害
     */
    protected float calculateFinalDamage(Player player, ItemStack stack, LivingEntity target) {
        // 获取基础面板伤害
        float baseDamage = getBaseDamage(player, stack);
        
        // 确保基础伤害不为0或负数
        if (baseDamage <= 0) {
            baseDamage = 1.0f;
        }

        // 计算基础伤害加成（基于饰品）
        float accessoryBaseBonus = calculateAccessoryBaseBonus(player);
        
        // 确保基础伤害加成有效
        if (accessoryBaseBonus <= 0) {
            accessoryBaseBonus = 0.1f;
        }

        // 计算其他伤害加成（饰品、药水等）
        float otherBonus = calculateOtherBonus(player);
        
        // 确保其他伤害加成有效
        if (otherBonus <= 0) {
            otherBonus = 0.1f;
        }

        // 计算伤害浮动值
        float fluctuation = calculateDamageFluctuation();
        
        // 确保伤害浮动值有效
        if (fluctuation <= 0) {
            fluctuation = 0.1f;
        }

        // 判断是否暴击
        boolean isCritical = isCriticalHit(player);

        float finalDamage;
        if (isCritical) {
            // 暴击伤害公式
            float criticalBonus = getCriticalDamageMultiplier(player);
            
            // 确保暴击伤害倍率有效
            if (criticalBonus <= 0) {
                criticalBonus = 1.0f;
            }
            
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.8f * criticalBonus * fluctuation;
        } else {
            // 普通伤害公式
            finalDamage = baseDamage * accessoryBaseBonus * otherBonus * 0.9f * fluctuation;
        }
        
        // 防止NaN值
        if (Float.isNaN(finalDamage) || Float.isInfinite(finalDamage)) {
            finalDamage = 1.0f;
        }

        return Math.max(0.1f, finalDamage); // 确保至少造成0.1点伤害
    }

    /**
     * 获取基础面板伤害
     */
    protected float getBaseDamage(Player player, ItemStack stack) {
        // 获取武器的攻击伤害属性
        float weaponDamage = baseAttackDamage;

        // 考虑玩家攻击力属性
        var attackDamageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            weaponDamage += (float) attackDamageAttr.getValue();
        }

        return weaponDamage * baseDamageMultiplier;
    }

    /**
     * 计算饰品基础伤害加成
     */
    protected float calculateAccessoryBaseBonus(Player player) {
        float bonus = 1.0f;

        // 获取饰品栏
        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory != null) {
            // 遍历饰品栏，计算基础伤害加成
            for (int i = 0; i < accessoryInventory.getItemHandler().getSlots(); i++) {
                ItemStack accessory = accessoryInventory.getItemHandler().getStackInSlot(i);
                if (!accessory.isEmpty()) {
                    bonus += getAccessoryBaseDamageBonus(accessory);
                }
            }
        }

        return bonus; // 确保至少有10%的伤害
    }


    /**
     * 获取单个饰品的基础伤害加成
     */
    protected float getAccessoryBaseDamageBonus(ItemStack accessory) {
        // 根据饰品类型返回不同的加成
        String itemName = accessory.getItem().getDescriptionId();

        if (itemName.contains("attack_amulet")) {
            if (itemName.contains("epic")) return 0.3f;      // 史诗攻击护符：30%
            if (itemName.contains("advanced")) return 0.2f;  // 高级攻击护符：20%
            return 0.1f;                                     // 普通攻击护符：10%
        }

        if (itemName.contains("composite_amulet")) {
            if (itemName.contains("advanced")) return 0.15f; // 高级综合护符：15%
            return 0.08f;                                    // 普通综合护符：8%
        }

        return 0.05f; // 其他饰品：5%
    }

    /**
     * 计算其他伤害加成（药水、状态等）
     */
    protected float calculateOtherBonus(Player player) {
        float bonus = 1.0f;

        // 力量药水效果
        if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
            int amplifier = player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier();
            bonus += (amplifier + 1) * 0.13f; // 每级力量增加13%伤害
        }

        // 虚弱效果
        if (player.hasEffect(MobEffects.WEAKNESS)) {
            int amplifier = player.getEffect(MobEffects.WEAKNESS).getAmplifier();
            bonus -= (amplifier + 1) * 0.2f; // 每级虚弱减少20%伤害
        }

        // 其他状态效果可以在这里添加

        return Math.max(0.1f, bonus); // 确保至少有10%的伤害
    }

    /**
     * 计算伤害浮动值
     */
    protected float calculateDamageFluctuation() {
        // 在 [1 - fluctuation, 1 + fluctuation] 范围内浮动
        float min = 1.0f - damageFluctuation;
        float max = 1.0f + damageFluctuation;
        return min + random.nextFloat() * (max - min);
    }

    /**
     * 判断是否暴击
     */
    public boolean isCriticalHit(Player player) {
        // 基础暴击率
        float totalCriticalChance = criticalChance;

        // 获取饰品提供的暴击率加成
        float accessoryCriticalChanceBonus = calculateAccessoryCriticalChanceBonus();
        totalCriticalChance += accessoryCriticalChanceBonus;

        // 考虑玩家状态（如药水效果等）
        // 可以在这里添加影响暴击率的因素

        return random.nextFloat() < totalCriticalChance;
    }

    /**
     * 计算饰品提供的暴击率加成
     */
    public float calculateAccessoryCriticalChanceBonus() {
        float bonus = 0.0f;

        // 获取玩家饰品栏
        var player = Minecraft.getInstance().player;
        if (player == null) return bonus;

        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory == null) return bonus;

        // 获取饰品栏的ItemHandler并正确遍历
        var itemHandler = accessoryInventory.getItemHandler();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AccessoryItem accessoryItem) {
                bonus += accessoryItem.getCriticalChanceBonus();
            }
        }
        return bonus;
    }

    /**
     * 获取暴击伤害倍率（考虑饰品加成）
     */
    protected float getCriticalDamageMultiplier(Player player) {
        float multiplier = criticalDamageMultiplier;

        // 获取饰品对特定武器类型的暴击伤害加成
        float accessoryCriticalBonus = calculateAccessoryCriticalBonus();
        multiplier += accessoryCriticalBonus;

        return multiplier;
    }

    /**
     * 计算饰品对特定武器类型的暴击伤害加成
     */
    public float calculateAccessoryCriticalBonus() {
        float bonus = 0.0f;

        // 获取玩家饰品栏
        var player = Minecraft.getInstance().player;
        if (player == null) return bonus;

        var accessoryInventory = AccessoryInventory.get(player);
        if (accessoryInventory == null) return bonus;

        // 获取饰品栏的ItemHandler并正确遍历
        var itemHandler = accessoryInventory.getItemHandler();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AccessoryItem accessoryItem) {
                // 根据武器类型获取对应的爆伤加成
                switch (this.weaponType) {
                    case MELEE:
                        bonus += accessoryItem.getMeleeCriticalDamageBonus();
                        break;
                    case RANGED:
                        bonus += accessoryItem.getRangedCriticalDamageBonus();
                        break;
                    case MAGIC:
                        bonus += accessoryItem.getMagicCriticalDamageBonus();
                        break;
                    default:
                        bonus +=0;
                }
            }
        }

        return bonus;
    }

    /**
     * 获取单个饰品对特定武器类型的暴击伤害加成
     */
    protected float getAccessoryCriticalDamageBonus(ItemStack accessory, WeaponEnum weaponType) {
        // 根据饰品类型和武器类型返回不同的加成
        String itemName = accessory.getItem().getDescriptionId();

        // 近战爆伤饰品
        if (weaponType == WeaponEnum.MELEE) {
            if (itemName.contains("melee_critical")) {
                if (itemName.contains("epic")) return 0.5f;      // 史诗近战爆伤饰品：50%
                if (itemName.contains("advanced")) return 0.3f;  // 高级近战爆伤饰品：30%
                return 0.15f;                                   // 普通近战爆伤饰品：15%
            }
        }
        // 远程爆伤饰品
        else if (weaponType == WeaponEnum.RANGED) {
            if (itemName.contains("ranged_critical")) {
                if (itemName.contains("epic")) return 0.5f;      // 史诗远程爆伤饰品：50%
                if (itemName.contains("advanced")) return 0.3f;  // 高级远程爆伤饰品：30%
                return 0.15f;                                   // 普通远程爆伤饰品：15%
            }
        }
        // 魔法爆伤饰品
        else if (weaponType == WeaponEnum.MAGIC) {
            if (itemName.contains("magic_critical")) {
                if (itemName.contains("epic")) return 0.5f;      // 史诗魔法爆伤饰品：50%
                if (itemName.contains("advanced")) return 0.3f;  // 高级魔法爆伤饰品：30%
                return 0.15f;                                   // 普通魔法爆伤饰品：15%
            }
        }

        // 通用爆伤饰品
        if (itemName.contains("critical_amulet")) {
            if (itemName.contains("epic")) return 0.2f;      // 史诗通用爆伤饰品：20%
            if (itemName.contains("advanced")) return 0.1f;  // 高级通用爆伤饰品：10%
            return 0.05f;                                   // 普通通用爆伤饰品：5%
        }

        return 0.0f;
    }

    /**
     * 获取武器类型
     */
    public WeaponEnum getWeaponType() {
        return weaponType;
    }

    /**
     * 暴击命中时的特效
     */
    protected void onCriticalHit(Player player, LivingEntity target, float damage) {
        // 播放暴击音效和粒子效果
        // 可以在这里添加暴击特效

        // 发送暴击消息
        if (player.level().isClientSide) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6暴击！ " + String.format("%.1f", damage) + " 伤害"),
                    true
            );
        }
    }

    /**
     * 命中敌人时的回调（子类可以重写）
     */
    protected void onHitEnemy(Player player, LivingEntity target, ItemStack stack, float damage) {
        // 子类可以重写此方法来实现特殊效果
    }

    /**
     * 获取攻击距离（子类可以重写）
     */
    public double getAttackRange(Player player) {
        return 3.0; // 默认攻击距离
    }

    /**
     * 获取武器特殊效果描述
     */
    public String getSpecialEffectDescription() {
        return "基础伤害倍率: " + String.format("%.1f", baseDamageMultiplier) +
                " | 暴击率: " + String.format("%.1f%%", criticalChance * 100) +
                " | 暴击伤害: " + String.format("%.1f", criticalDamageMultiplier) + "倍";
    }
}