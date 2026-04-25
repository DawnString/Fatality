package cn.dawnstring.fatality.api.attributes;

/**
 * 属性修改器
 * 用于临时或永久修改玩家属性
 */
public class AttributeModifier {
    
    private final String modifierId;
    private final String attributeId;
    private final float value;
    private final ModifierType type;
    private final int duration; // 持续时间（tick），-1表示永久
    private final String source;
    
    public AttributeModifier(String modifierId, String attributeId, float value, 
                           ModifierType type, int duration, String source) {
        this.modifierId = modifierId;
        this.attributeId = attributeId;
        this.value = value;
        this.type = type;
        this.duration = duration;
        this.source = source;
    }
    
    public String getModifierId() {
        return modifierId;
    }
    
    public String getAttributeId() {
        return attributeId;
    }
    
    public float getValue() {
        return value;
    }
    
    public ModifierType getType() {
        return type;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public String getSource() {
        return source;
    }
    
    public boolean isPermanent() {
        return duration == -1;
    }
    
    public boolean isExpired(int currentTick) {
        return duration > 0 && currentTick >= duration;
    }
    
    /**
     * 修改器类型
     */
    public enum ModifierType {
        FLAT,       // 数值加成（直接相加）
        MULTIPLY,   // 乘法加成（乘以系数）
        PERCENTAGE  // 百分比加成（增加百分比）
    }
}