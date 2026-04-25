package cn.dawnstring.fatality.system.accessories;

import cn.dawnstring.fatality.api.accessories.AccessoryEffectHandler;
import cn.dawnstring.fatality.api.accessories.IAccessorySystem;
import cn.dawnstring.fatality.items.AccessoryItem;
import cn.dawnstring.fatality.system.accessories.DamageEffectHandler;
import cn.dawnstring.fatality.system.accessories.DefenseEffectHandler;
import cn.dawnstring.fatality.system.accessories.HealthRegenerationEffectHandler;
import cn.dawnstring.fatality.system.accessories.ManaRegenerationEffectHandler;
import cn.dawnstring.fatality.system.accessories.MovementSpeedEffectHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 饰品效果处理器管理器
 * 统一管理所有饰品效果处理器
 */
public class AccessoryEffectHandlerManager {
    
    private static final AccessoryEffectHandlerManager INSTANCE = new AccessoryEffectHandlerManager();
    
    private final List<AccessoryEffectHandler> effectHandlers = new ArrayList<>();
    private final IAccessorySystem accessorySystem;
    
    private AccessoryEffectHandlerManager() {
        this.accessorySystem = cn.dawnstring.fatality.system.AccessorySystem.getInstance();
        initializeDefaultHandlers();
    }
    
    public static AccessoryEffectHandlerManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化默认的效果处理器
     */
    private void initializeDefaultHandlers() {
        // 注册生命恢复效果处理器
        registerEffectHandler(new HealthRegenerationEffectHandler(AccessoryItem.class));
        
        // 注册法力恢复效果处理器
        registerEffectHandler(new ManaRegenerationEffectHandler(AccessoryItem.class));
        
        // 注册伤害效果处理器
        registerEffectHandler(new DamageEffectHandler(AccessoryItem.class));
        
        // 注册防御效果处理器
        registerEffectHandler(new DefenseEffectHandler(AccessoryItem.class));
        
        // 注册移动速度效果处理器
        registerEffectHandler(new MovementSpeedEffectHandler(AccessoryItem.class));
    }
    
    /**
     * 注册效果处理器
     */
    public void registerEffectHandler(AccessoryEffectHandler effectHandler) {
        effectHandlers.add(effectHandler);
        effectHandlers.sort((h1, h2) -> Integer.compare(h1.getPriority(), h2.getPriority()));
        
        // 同时注册到饰品系统
        accessorySystem.registerEffectHandler(effectHandler);
    }
    
    /**
     * 移除效果处理器
     */
    public void unregisterEffectHandler(AccessoryEffectHandler effectHandler) {
        effectHandlers.remove(effectHandler);
    }
    
    /**
     * 应用饰品效果
     */
    public void applyAccessoryEffects(Player player, ItemStack accessory, int slot) {
        for (AccessoryEffectHandler handler : effectHandlers) {
            if (handler.supports(accessory)) {
                handler.applyEffect(player, accessory, slot);
            }
        }
    }
    
    /**
     * 移除饰品效果
     */
    public void removeAccessoryEffects(Player player, ItemStack accessory, int slot) {
        for (AccessoryEffectHandler handler : effectHandlers) {
            if (handler.supports(accessory)) {
                handler.removeEffect(player, accessory, slot);
            }
        }
    }
    
    /**
     * 更新饰品效果（每tick调用）
     */
    public void updateAccessoryEffects(Player player) {
        List<ItemStack> accessories = accessorySystem.getEquippedAccessories(player);
        for (int i = 0; i < accessories.size(); i++) {
            ItemStack accessory = accessories.get(i);
            for (AccessoryEffectHandler handler : effectHandlers) {
                if (handler.supports(accessory)) {
                    handler.updateEffect(player, accessory, i);
                }
            }
        }
    }
    
    /**
     * 获取所有已注册的效果处理器
     */
    public List<AccessoryEffectHandler> getEffectHandlers() {
        return new ArrayList<>(effectHandlers);
    }
    
    /**
     * 清理玩家数据
     */
    public void cleanupPlayerData(Player player) {
        // 清理所有效果处理器中的玩家数据
        for (AccessoryEffectHandler handler : effectHandlers) {
            if (handler instanceof HealthRegenerationEffectHandler) {
                ((HealthRegenerationEffectHandler) handler).cleanupPlayerData(player);
            }
            if (handler instanceof ManaRegenerationEffectHandler) {
                ((ManaRegenerationEffectHandler) handler).cleanupPlayerData(player);
            }
            if (handler instanceof DamageEffectHandler) {
                ((DamageEffectHandler) handler).cleanupPlayerData(player);
            }
            if (handler instanceof DefenseEffectHandler) {
                ((DefenseEffectHandler) handler).cleanupPlayerData(player);
            }
            if (handler instanceof MovementSpeedEffectHandler) {
                ((MovementSpeedEffectHandler) handler).cleanupPlayerData(player);
            }
        }
    }
    
    /**
     * 初始化玩家数据（当玩家登录时调用）
     */
    public void initializePlayer(Player player) {
        // 初始化所有效果处理器中的玩家数据
        for (AccessoryEffectHandler handler : effectHandlers) {
            if (handler instanceof HealthRegenerationEffectHandler) {
                ((HealthRegenerationEffectHandler) handler).initializePlayerData(player);
            }
            if (handler instanceof ManaRegenerationEffectHandler) {
                ((ManaRegenerationEffectHandler) handler).initializePlayerData(player);
            }
            if (handler instanceof DamageEffectHandler) {
                ((DamageEffectHandler) handler).initializePlayerData(player);
            }
            if (handler instanceof DefenseEffectHandler) {
                ((DefenseEffectHandler) handler).initializePlayerData(player);
            }
            if (handler instanceof MovementSpeedEffectHandler) {
                ((MovementSpeedEffectHandler) handler).initializePlayerData(player);
            }
        }
    }
}