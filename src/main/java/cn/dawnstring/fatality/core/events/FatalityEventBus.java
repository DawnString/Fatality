package cn.dawnstring.fatality.core.events;

import cn.dawnstring.fatality.api.events.FatalityEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Fatality 事件总线管理器
 * 提供统一的事件注册和分发功能
 */
public class FatalityEventBus {
    
    private static final FatalityEventBus INSTANCE = new FatalityEventBus();
    
    private final Map<Class<? extends FatalityEvent>, EventHandlerRegistry<?>> registries = new HashMap<>();
    
    private FatalityEventBus() {
        // 私有构造函数
    }
    
    public static FatalityEventBus getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册事件监听器
     * @param eventClass 事件类型
     * @param handler 事件处理器
     * @param priority 事件优先级
     * @param <T> 事件类型
     */
    public <T extends FatalityEvent> void registerListener(Class<T> eventClass, Consumer<T> handler, EventPriority priority) {
        @SuppressWarnings("unchecked")
        EventHandlerRegistry<T> registry = (EventHandlerRegistry<T>) registries.computeIfAbsent(
            eventClass, k -> new EventHandlerRegistry<>()
        );
        
        registry.register(handler, priority);
    }
    
    /**
     * 注册事件监听器（默认优先级）
     */
    public <T extends FatalityEvent> void registerListener(Class<T> eventClass, Consumer<T> handler) {
        registerListener(eventClass, handler, EventPriority.NORMAL);
    }
    
    /**
     * 发布事件
     * @param event 事件实例
     * @param <T> 事件类型
     */
    public <T extends FatalityEvent> void post(T event) {
        @SuppressWarnings("unchecked")
        EventHandlerRegistry<T> registry = (EventHandlerRegistry<T>) registries.get(event.getClass());
        
        if (registry != null) {
            registry.handle(event);
        }
        
        // 同时发布到Forge事件总线
        MinecraftForge.EVENT_BUS.post(event);
    }
    
    /**
     * 移除事件监听器
     * @param eventClass 事件类型
     * @param handler 事件处理器
     * @param <T> 事件类型
     */
    public <T extends FatalityEvent> void unregisterListener(Class<T> eventClass, Consumer<T> handler) {
        @SuppressWarnings("unchecked")
        EventHandlerRegistry<T> registry = (EventHandlerRegistry<T>) registries.get(eventClass);
        
        if (registry != null) {
            registry.unregister(handler);
        }
    }
    
    /**
     * 事件处理器注册表
     */
    private static class EventHandlerRegistry<T extends FatalityEvent> {
        
        private final Map<EventPriority, List<Consumer<T>>> handlersByPriority = new HashMap<>();
        
        public void register(Consumer<T> handler, EventPriority priority) {
            handlersByPriority.computeIfAbsent(priority, k -> new CopyOnWriteArrayList<>()).add(handler);
        }
        
        public void unregister(Consumer<T> handler) {
            for (List<Consumer<T>> handlers : handlersByPriority.values()) {
                handlers.remove(handler);
            }
        }
        
        public void handle(T event) {
            // 按优先级顺序处理事件
            for (EventPriority priority : EventPriority.values()) {
                List<Consumer<T>> handlers = handlersByPriority.get(priority);
                if (handlers != null) {
                    for (Consumer<T> handler : handlers) {
                        try {
                            handler.accept(event);
                        } catch (Exception e) {
                            // 记录错误但不中断事件处理
                            System.err.println("Error handling event: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}