package cn.dawnstring.fatality.api.events;

import net.minecraft.world.entity.player.Player;

/**
 * 玩家属性事件
 * 当玩家属性发生变化时触发
 */
public class PlayerAttributeEvent extends FatalityEvent {
    
    private final String attributeId;
    private final float oldValue;
    private final float newValue;
    private final AttributeChangeSource source;
    
    public PlayerAttributeEvent(Player player, String attributeId, float oldValue, float newValue, AttributeChangeSource source) {
        super(player);
        this.attributeId = attributeId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.source = source;
    }
    
    public String getAttributeId() {
        return attributeId;
    }
    
    public float getOldValue() {
        return oldValue;
    }
    
    public float getNewValue() {
        return newValue;
    }
    
    public AttributeChangeSource getSource() {
        return source;
    }
    
    public float getChangeAmount() {
        return newValue - oldValue;
    }
    
    /**
     * 属性变化来源枚举
     */
    public enum AttributeChangeSource {
        ACCESSORY_EQUIP,      // 饰品装备
        ACCESSORY_UNEQUIP,    // 饰品卸下
        SKILL_ACTIVATION,     // 技能激活
        BUFF_APPLICATION,     // BUFF应用
        DEBUFF_APPLICATION,   // DEBUFF应用
        LEVEL_UP,            // 等级提升
        ITEM_EQUIP,          // 物品装备
        ITEM_UNEQUIP,        // 物品卸下
        SYSTEM               // 系统调整
    }
}