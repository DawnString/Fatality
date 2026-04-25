package cn.dawnstring.fatality.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fatality 事件基类
 * 所有自定义事件都应继承此类
 */
public abstract class FatalityEvent extends Event {
    
    private final Player player;
    private final long timestamp;
    
    public FatalityEvent(Player player) {
        this.player = player;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 事件优先级枚举
     */
    public enum Priority {
        LOWEST,     // 最低优先级
        LOW,        // 低优先级
        NORMAL,     // 正常优先级
        HIGH,       // 高优先级
        HIGHEST,    // 最高优先级
        MONITOR     // 监控优先级（只读）
    }
}