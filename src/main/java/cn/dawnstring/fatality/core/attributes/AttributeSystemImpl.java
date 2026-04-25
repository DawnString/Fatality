package cn.dawnstring.fatality.core.attributes;

import cn.dawnstring.fatality.api.attributes.*;
import cn.dawnstring.fatality.api.events.PlayerAttributeEvent;
import cn.dawnstring.fatality.core.events.FatalityEventBus;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性系统实现类
 */
public class AttributeSystemImpl implements IAttributeSystem {
    
    private static final AttributeSystemImpl INSTANCE = new AttributeSystemImpl();
    
    private final Map<String, AttributeCalculator> calculators = new ConcurrentHashMap<>();
    private final Map<Player, Map<String, AttributeModifier>> playerModifiers = new ConcurrentHashMap<>();
    
    private AttributeSystemImpl() {
        // 注册基础属性
        registerDefaultAttributes();
    }
    
    public static AttributeSystemImpl getInstance() {
        return INSTANCE;
    }
    
    @Override
    public float getAttribute(Player player, String attributeId) {
        if (!calculators.containsKey(attributeId)) {
            return 0.0f;
        }
        
        float baseValue = calculators.get(attributeId).calculate(player);
        float modifiedValue = applyModifiers(player, attributeId, baseValue);
        
        return modifiedValue;
    }
    
    @Override
    public void registerAttribute(String attributeId, AttributeCalculator calculator) {
        calculators.put(attributeId, calculator);
    }
    
    @Override
    public void addAttributeModifier(Player player, AttributeModifier modifier) {
        Map<String, AttributeModifier> modifiers = playerModifiers.computeIfAbsent(player, k -> new ConcurrentHashMap<>());
        
        // 保存旧值用于事件触发
        float oldValue = getAttribute(player, modifier.getAttributeId());
        
        modifiers.put(modifier.getModifierId(), modifier);
        
        // 触发属性变化事件
        float newValue = getAttribute(player, modifier.getAttributeId());
        if (oldValue != newValue) {
            FatalityEventBus.getInstance().post(new PlayerAttributeEvent(
                player, modifier.getAttributeId(), oldValue, newValue, 
                PlayerAttributeEvent.AttributeChangeSource.SYSTEM
            ));
        }
    }
    
    @Override
    public void removeAttributeModifier(Player player, String modifierId) {
        Map<String, AttributeModifier> modifiers = playerModifiers.get(player);
        if (modifiers != null) {
            AttributeModifier modifier = modifiers.get(modifierId);
            if (modifier != null) {
                // 保存旧值用于事件触发
                float oldValue = getAttribute(player, modifier.getAttributeId());
                
                modifiers.remove(modifierId);
                
                // 触发属性变化事件
                float newValue = getAttribute(player, modifier.getAttributeId());
                if (oldValue != newValue) {
                    FatalityEventBus.getInstance().post(new PlayerAttributeEvent(
                        player, modifier.getAttributeId(), oldValue, newValue,
                        PlayerAttributeEvent.AttributeChangeSource.SYSTEM
                    ));
                }
            }
        }
    }
    
    @Override
    public Iterable<String> getRegisteredAttributes() {
        return calculators.keySet();
    }
    
    @Override
    public boolean isAttributeRegistered(String attributeId) {
        return calculators.containsKey(attributeId);
    }
    
    /**
     * 应用属性修改器
     */
    private float applyModifiers(Player player, String attributeId, float baseValue) {
        Map<String, AttributeModifier> modifiers = playerModifiers.get(player);
        if (modifiers == null) {
            return baseValue;
        }
        
        float result = baseValue;
        
        for (AttributeModifier modifier : modifiers.values()) {
            if (modifier.getAttributeId().equals(attributeId)) {
                switch (modifier.getType()) {
                    case FLAT:
                        result += modifier.getValue();
                        break;
                    case MULTIPLY:
                        result *= modifier.getValue();
                        break;
                    case PERCENTAGE:
                        result *= (1.0f + modifier.getValue() / 100.0f);
                        break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * 注册默认属性
     */
    private void registerDefaultAttributes() {
        // 基础属性
        registerAttribute("health", player -> (float) player.getMaxHealth());
        registerAttribute("max_health", player -> (float) player.getMaxHealth());
        registerAttribute("attack_damage", player -> 1.0f); // 基础攻击力
        registerAttribute("defense", player -> 0.0f); // 基础防御
        
        // 战斗属性
        registerAttribute("critical_chance", player -> 0.05f); // 5%基础暴击率
        registerAttribute("critical_damage", player -> 1.5f); // 150%基础暴击伤害
        registerAttribute("attack_speed", player -> 1.0f); // 基础攻击速度
        
        // 移动属性
        registerAttribute("movement_speed", player -> (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED));
        
        // 特殊属性
        registerAttribute("mana", player -> 100.0f); // 基础法力值
        registerAttribute("max_mana", player -> 100.0f); // 最大法力值
        registerAttribute("mana_regeneration", player -> 1.0f); // 法力恢复速度
        registerAttribute("health_regeneration", player -> 0.1f); // 生命恢复速度
    }
    
    /**
     * 清理玩家数据（当玩家退出时调用）
     */
    public void cleanupPlayerData(Player player) {
        playerModifiers.remove(player);
    }
}