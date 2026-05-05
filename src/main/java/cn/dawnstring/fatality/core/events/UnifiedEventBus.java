package cn.dawnstring.fatality.core.events;

import cn.dawnstring.fatality.Fatality;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 统一事件总线
 * 管理所有Fatality事件的注册和分发
 */
public class UnifiedEventBus {
    
    private static final UnifiedEventBus INSTANCE = new UnifiedEventBus();
    
    private final Map<Class<?>, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<Object, List<Method>>> listenerMethods = new ConcurrentHashMap<>();
    
    private UnifiedEventBus() {
        // 私有构造函数
    }
    
    public static UnifiedEventBus getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册事件监听器
     * @param eventClass 事件类型
     * @param listener 监听器对象
     * @param <T> 事件类型
     */
    public <T> void registerListener(Class<T> eventClass, Object listener) {
        List<EventListener> eventListeners = listeners.computeIfAbsent(eventClass, k -> new ArrayList<>());
        eventListeners.add(new EventListener(listener, eventClass));
        
        Fatality.LOGGER.debug("Event listener registered for: {}", eventClass.getSimpleName());
    }
    
    /**
     * 注册事件监听器（函数式）
     * @param eventClass 事件类型
     * @param consumer 事件处理器
     * @param <T> 事件类型
     */
    public <T> void registerListener(Class<T> eventClass, Consumer<T> consumer) {
        List<EventListener> eventListeners = listeners.computeIfAbsent(eventClass, k -> new ArrayList<>());
        eventListeners.add(new EventListener(consumer, eventClass));
        
        Fatality.LOGGER.debug("Event listener registered for: {}", eventClass.getSimpleName());
    }
    
    /**
     * 注销事件监听器
     * @param eventClass 事件类型
     * @param listener 监听器对象
     * @param <T> 事件类型
     */
    public <T> void unregisterListener(Class<T> eventClass, Object listener) {
        List<EventListener> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.removeIf(el -> el.listener.equals(listener));
        }
    }
    
    /**
     * 发布事件
     * @param event 事件对象
     * @param <T> 事件类型
     */
    public <T> void post(T event) {
        Class<?> eventClass = event.getClass();
        List<EventListener> eventListeners = listeners.get(eventClass);
        
        if (eventListeners != null) {
            for (EventListener eventListener : eventListeners) {
                try {
                    eventListener.handle(event);
                } catch (Exception e) {
                    Fatality.LOGGER.error("Error handling event: " + eventClass.getSimpleName(), e);
                }
            }
        }
    }
    
    /**
     * 获取事件监听器数量
     * @param eventClass 事件类型
     * @return 监听器数量
     */
    public int getListenerCount(Class<?> eventClass) {
        List<EventListener> eventListeners = listeners.get(eventClass);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    /**
     * 清除所有监听器
     */
    public void clearAllListeners() {
        listeners.clear();
        listenerMethods.clear();
    }
    
    /**
     * 事件监听器包装类
     */
    private static class EventListener {
        private final Object listener;
        private final Class<?> eventClass;
        
        public EventListener(Object listener, Class<?> eventClass) {
            this.listener = listener;
            this.eventClass = eventClass;
        }
        
        @SuppressWarnings("unchecked")
        public void handle(Object event) {
            if (listener instanceof Consumer) {
                ((Consumer<Object>) listener).accept(event);
            } else {
                invokeMethod(event);
            }
        }
        
        private void invokeMethod(Object event) {
            try {
                Method[] methods = listener.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getParameterCount() == 1 && 
                        method.getParameterTypes()[0].isAssignableFrom(eventClass)) {
                        method.setAccessible(true);
                        method.invoke(listener, event);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke event handler", e);
            }
        }
    }
}