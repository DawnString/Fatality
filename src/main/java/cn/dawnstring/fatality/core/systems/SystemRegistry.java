package cn.dawnstring.fatality.core.systems;

import cn.dawnstring.fatality.Fatality;
import cn.dawnstring.fatality.api.systems.IModSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统注册表
 * 统一管理所有核心系统，按优先级和依赖关系初始化
 */
public class SystemRegistry {
    
    private static final Map<String, IModSystem> systems = new LinkedHashMap<>();
    private static final Map<String, String[]> dependencies = new HashMap<>();
    private static final Map<String, Integer> priorities = new HashMap<>();
    private static boolean initialized = false;
    
    /**
     * 注册系统
     * @param system 要注册的系统
     */
    public static void register(IModSystem system) {
        if (initialized) {
            throw new IllegalStateException("Cannot register system after initialization");
        }
        
        String systemId = system.getSystemId();
        if (systems.containsKey(systemId)) {
            throw new IllegalArgumentException("System already registered: " + systemId);
        }
        
        systems.put(systemId, system);
        Fatality.LOGGER.info("System registered: {}", systemId);
    }
    
    /**
     * 注册系统（带依赖）
     * @param system 要注册的系统
     * @param dependsOn 依赖的系统ID数组
     */
    public static void register(IModSystem system, String[] dependsOn) {
        register(system);
        dependencies.put(system.getSystemId(), dependsOn);
    }
    
    /**
     * 注册系统（带优先级）
     * @param system 要注册的系统
     * @param priority 优先级（数值越小优先级越高）
     */
    public static void register(IModSystem system, int priority) {
        register(system);
        priorities.put(system.getSystemId(), priority);
    }
    
    /**
     * 注册系统（带依赖和优先级）
     * @param system 要注册的系统
     * @param dependsOn 依赖的系统ID数组
     * @param priority 优先级（数值越小优先级越高）
     */
    public static void register(IModSystem system, String[] dependsOn, int priority) {
        register(system, dependsOn);
        priorities.put(system.getSystemId(), priority);
    }
    
    /**
     * 初始化所有系统
     */
    public static void initializeAll() {
        if (initialized) {
            return;
        }
        
        Fatality.LOGGER.info("Initializing {} systems...", systems.size());
        
        List<String> initOrder = resolveDependencies();
        
        for (String systemId : initOrder) {
            IModSystem system = systems.get(systemId);
            try {
                system.initialize();
                Fatality.LOGGER.info("System initialized: {}", systemId);
            } catch (Exception e) {
                Fatality.LOGGER.error("Failed to initialize system: " + systemId, e);
            }
        }
        
        initialized = true;
    }
    
    /**
     * 关闭所有系统
     */
    public static void shutdownAll() {
        Fatality.LOGGER.info("Shutting down {} systems...", systems.size());
        
        List<String> shutdownOrder = new ArrayList<>(systems.keySet());
        Collections.reverse(shutdownOrder);
        
        for (String systemId : shutdownOrder) {
            IModSystem system = systems.get(systemId);
            try {
                system.shutdown();
                Fatality.LOGGER.info("System shutdown: {}", systemId);
            } catch (Exception e) {
                Fatality.LOGGER.error("Failed to shutdown system: " + systemId, e);
            }
        }
    }
    
    /**
     * 获取系统
     * @param systemId 系统ID
     * @return 系统实例，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public static <T extends IModSystem> T getSystem(String systemId) {
        return (T) systems.get(systemId);
    }
    
    /**
     * 获取系统（按类型）
     * @param systemClass 系统类型
     * @return 系统实例，如果不存在返回null
     */
    public static <T extends IModSystem> T getSystem(Class<T> systemClass) {
        return systems.values().stream()
            .filter(systemClass::isInstance)
            .map(systemClass::cast)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 检查系统是否已注册
     * @param systemId 系统ID
     * @return 是否已注册
     */
    public static boolean hasSystem(String systemId) {
        return systems.containsKey(systemId);
    }
    
    /**
     * 获取所有已注册的系统ID
     * @return 系统ID集合
     */
    public static Set<String> getSystemIds() {
        return Collections.unmodifiableSet(systems.keySet());
    }
    
    /**
     * 玩家登录时通知所有系统
     * @param player 登录的玩家
     */
    public static void onPlayerJoin(Player player) {
        systems.values().forEach(system -> {
            try {
                system.onPlayerJoin(player);
            } catch (Exception e) {
                Fatality.LOGGER.error("Error in system.onPlayerJoin: " + system.getSystemId(), e);
            }
        });
    }
    
    /**
     * 玩家退出时通知所有系统
     * @param player 退出的玩家
     */
    public static void onPlayerLeave(Player player) {
        systems.values().forEach(system -> {
            try {
                system.onPlayerLeave(player);
            } catch (Exception e) {
                Fatality.LOGGER.error("Error in system.onPlayerLeave: " + system.getSystemId(), e);
            }
        });
    }
    
    /**
     * 玩家tick时通知所有系统
     */
    public static void onPlayerTick(Player player) {
        systems.values().forEach(system -> {
            try {
                system.onPlayerTick(player);
            } catch (Exception e) {
                Fatality.LOGGER.error("Error in system.onPlayerTick: " + system.getSystemId(), e);
            }
        });
    }

    /**
     * 实体死亡时通知所有系统
     */
    public static void onLivingDeath(LivingEntity killed) {
        systems.values().forEach(system -> {
            try {
                system.onLivingDeath(killed);
            } catch (Exception e) {
                Fatality.LOGGER.error("Error in system.onLivingDeath: " + system.getSystemId(), e);
            }
        });
    }

    /**
     * 玩家复活时通知所有系统
     */
    public static void onPlayerRespawn(Player player) {
        systems.values().forEach(system -> {
            try {
                system.onPlayerRespawn(player);
            } catch (Exception e) {
                Fatality.LOGGER.error("Error in system.onPlayerRespawn: " + system.getSystemId(), e);
            }
        });
    }

    /**
     * 世界加载时通知所有系统
     */
    public static void onWorldLoad(ServerLevel level) {
        systems.values().forEach(system -> {
            try {
                system.onWorldLoad(level);
            } catch (Exception e) {
                Fatality.LOGGER.error("Error in system.onWorldLoad: " + system.getSystemId(), e);
            }
        });
    }

    /**
     * 服务器tick时通知所有系统
     */
    public static void onServerTick() {
        systems.values().forEach(system -> {
            try {
                system.onServerTick();
            } catch (Exception e) {
                Fatality.LOGGER.error("Error in system.onServerTick: " + system.getSystemId(), e);
            }
        });
    }
    
    /**
     * 客户端tick时通知所有系统
     */
    public static void onClientTick() {
        systems.values().forEach(system -> {
            try {
                system.onClientTick();
            } catch (Exception e) {
                Fatality.LOGGER.error("Error in system.onClientTick: " + system.getSystemId(), e);
            }
        });
    }
    
    /**
     * 解析系统依赖关系
     * @return 初始化顺序
     */
    private static List<String> resolveDependencies() {
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        
        for (String systemId : systems.keySet()) {
            if (!visited.contains(systemId)) {
                visit(systemId, visited, visiting, result);
            }
        }
        
        return result;
    }
    
    /**
     * 深度优先搜索访问系统
     */
    private static void visit(String systemId, Set<String> visited, Set<String> visiting, List<String> result) {
        if (visiting.contains(systemId)) {
            throw new IllegalStateException("Circular dependency detected: " + systemId);
        }
        
        if (visited.contains(systemId)) {
            return;
        }
        
        visiting.add(systemId);
        
        String[] deps = dependencies.get(systemId);
        if (deps != null) {
            for (String dep : deps) {
                if (!systems.containsKey(dep)) {
                    throw new IllegalStateException("Missing dependency: " + dep + " for system: " + systemId);
                }
                visit(dep, visited, visiting, result);
            }
        }
        
        visiting.remove(systemId);
        visited.add(systemId);
        result.add(systemId);
    }
}