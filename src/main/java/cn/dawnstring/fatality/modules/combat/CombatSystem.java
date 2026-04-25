package cn.dawnstring.fatality.modules.combat;

import cn.dawnstring.fatality.api.attributes.IAttributeSystem;
import cn.dawnstring.fatality.api.events.FatalityEvent;
import cn.dawnstring.fatality.core.events.FatalityEventBus;
import cn.dawnstring.fatality.core.systems.SystemManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * 战斗系统模块
 * 基于事件驱动的战斗计算系统
 */
public class CombatSystem {
    
    private static final CombatSystem INSTANCE = new CombatSystem();
    
    private CombatSystem() {
        // 注册战斗相关事件监听器
        registerEventListeners();
    }
    
    public static CombatSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化战斗系统
     */
    public void initialize() {
        System.out.println("Combat System initialized");
    }
    
    /**
     * 注册事件监听器
     */
    private void registerEventListeners() {
        // 注册伤害计算事件监听
        FatalityEventBus.getInstance().registerListener(
            DamageCalculationEvent.class,
            this::onDamageCalculation
        );
    }
    
    /**
     * 处理伤害计算事件
     */
    private void onDamageCalculation(DamageCalculationEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player attacker = event.getPlayer();
        Entity target = event.getTarget();
        DamageSource damageSource = event.getDamageSource();
        float baseDamage = event.getBaseDamage();
        
        // 获取属性系统
        IAttributeSystem attributeSystem = SystemManager.getInstance().getAttributeSystem();
        
        // 计算攻击者属性加成
        float attackDamage = attributeSystem.getAttribute(attacker, "attack_damage");
        float criticalChance = attributeSystem.getAttribute(attacker, "critical_chance");
        float criticalDamage = attributeSystem.getAttribute(attacker, "critical_damage");
        
        // 基础伤害计算
        float calculatedDamage = baseDamage + attackDamage;
        
        // 暴击计算
        if (Math.random() < criticalChance) {
            calculatedDamage *= criticalDamage;
            event.setCriticalHit(true);
        }
        
        // 防御计算（如果目标是玩家）
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            float defense = attributeSystem.getAttribute(targetPlayer, "defense");
            calculatedDamage = Math.max(1.0f, calculatedDamage - defense);
        }
        
        event.setFinalDamage(calculatedDamage);
    }
    
    /**
     * 伤害计算事件
     */
    public static class DamageCalculationEvent extends FatalityEvent {
        private final Entity target;
        private final DamageSource damageSource;
        private final float baseDamage;
        
        private float finalDamage;
        private boolean criticalHit = false;
        private boolean cancelled = false;
        
        public DamageCalculationEvent(Player attacker, Entity target, DamageSource damageSource, float baseDamage) {
            super(attacker);
            this.target = target;
            this.damageSource = damageSource;
            this.baseDamage = baseDamage;
            this.finalDamage = baseDamage;
        }
        
        public Entity getTarget() {
            return target;
        }
        
        public DamageSource getDamageSource() {
            return damageSource;
        }
        
        public float getBaseDamage() {
            return baseDamage;
        }
        
        public float getFinalDamage() {
            return finalDamage;
        }
        
        public void setFinalDamage(float finalDamage) {
            this.finalDamage = finalDamage;
        }
        
        public boolean isCriticalHit() {
            return criticalHit;
        }
        
        public void setCriticalHit(boolean criticalHit) {
            this.criticalHit = criticalHit;
        }
        
        public boolean isCancelled() {
            return cancelled;
        }
        
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}