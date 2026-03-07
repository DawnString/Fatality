package cn.dawnstring.fatality.items;

/**
 * 武器类型枚举 - 用于区分近战、远程、魔法武器
 */
public enum WeaponEnum {
    MELEE("近战武器", 0xFFAA0000),      // 红色系
    RANGED("远程武器", 0xFF00AA00),    // 绿色系
    MAGIC("魔法武器", 0xFF0000AA);     // 蓝色系

    private final String displayName;
    private final int color;

    WeaponEnum(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }
}
