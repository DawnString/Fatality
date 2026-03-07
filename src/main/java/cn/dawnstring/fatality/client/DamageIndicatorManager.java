package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.items.BaseWeapon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 伤害数值指示器管理器（移除所有限制条件）
 */
public class DamageIndicatorManager {

    private static final List<DamageIndicator> damageIndicators = new ArrayList<>();
    private static final long INDICATOR_DURATION = 4000; // 传奇风格：4秒显示时间
    private static final Random random = new Random();

    /**
     * 伤害数值指示器数据结构
     */
    public static class DamageIndicator {
        public final LivingEntity target;
        public final float damage;
        public final long spawnTime;
        public final Vec3 spawnPosition;
        public final float offsetX;
        public float offsetY = 0;
        public float alpha = 1.0f;
        public  boolean isCritical = false;
        public final float initialYOffset; // 初始Y偏移，避免重叠

        public DamageIndicator(LivingEntity target, float damage) {
            this.target = target;
            this.damage = damage;
            this.spawnTime = System.currentTimeMillis();

            // 在目标实体上方生成
            float entityWidth = target.getBbWidth();
            this.offsetX = (random.nextFloat() - 0.5f) * entityWidth * 1.5f;

            // 计算初始Y偏移，避免重叠
            this.initialYOffset = calculateInitialYOffset(target);

            // 生成高度：实体高度 + 初始偏移
            float baseHeight = target.getBbHeight();
            float spawnHeight = baseHeight + initialYOffset;
            this.spawnPosition = target.position().add(offsetX, spawnHeight, 0);
        }

        /**
         * 计算初始Y偏移，避免指示器重叠
         */
        private float calculateInitialYOffset(LivingEntity target) {
            // 获取当前目标的所有活跃指示器
            List<DamageIndicator> activeIndicators = getActiveIndicatorsForTarget(target);
            
            // 如果没有其他指示器，使用基础偏移
            if (activeIndicators.isEmpty()) {
                return 0.5f;
            }
            
            // 找到最高的Y偏移（包括浮动偏移）
            float maxTotalOffset = 0f;
            for (DamageIndicator indicator : activeIndicators) {
                float totalOffset = indicator.initialYOffset + indicator.offsetY;
                if (totalOffset > maxTotalOffset) {
                    maxTotalOffset = totalOffset;
                }
            }
            
            // 在最高偏移基础上增加一定高度，避免重叠
            return maxTotalOffset + 0.4f;
        }

        /**
         * 获取同一目标的活跃指示器
         */
        private List<DamageIndicator> getActiveIndicatorsForTarget(LivingEntity target) {
            List<DamageIndicator> result = new ArrayList<>();
            for (DamageIndicator indicator : damageIndicators) {
                if (indicator.target == target && !indicator.isExpired()) {
                    result.add(indicator);
                }
            }
            return result;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - spawnTime > INDICATOR_DURATION;
        }

        public void update() {
            long elapsed = System.currentTimeMillis() - spawnTime;
            float progress = (float) elapsed / INDICATOR_DURATION;

            // 传奇风格浮动效果：更明显的浮动
            float floatHeight = isCritical ? 2.0f : 1.5f; // 暴击时浮动更高
            // 使用线性浮动，更符合传奇风格
            offsetY = progress * floatHeight;

            // 传奇风格淡出效果：更晚开始淡出
            if (progress > 0.7f) {
                // 线性淡出，更符合传奇风格
                float fadeProgress = (progress - 0.7f) / 0.3f;
                alpha = 1.0f - fadeProgress;
            } else {
                alpha = 1.0f;
            }
        }

        public Vec3 getCurrentPosition() {
            if (target != null && target.isAlive()) {
                return target.position().add(offsetX, target.getBbHeight() + 0.5f + offsetY, 0);
            }
            return spawnPosition.add(0, offsetY, 0);
        }
    }

    /**
     * 添加伤害数值指示器（移除所有限制条件）
     */
    public static void addDamageIndicator(LivingEntity target, float damage, Player attacker) {
        // 移除所有限制条件，只要伤害大于0就显示
        if (damage <= 0) {
            return;
        }

        System.out.println("DamageIndicatorManager.addDamageIndicator: 伤害=" + damage +
                ", 目标=" + (target != null ? target.getType().getDescription().getString() : "null"));

        // 每次攻击都添加新的指示器
        damageIndicators.add(new DamageIndicator(target, damage));

        // 限制最大数量
        if (damageIndicators.size() > 15) {
            damageIndicators.remove(0);
        }

        System.out.println("DamageIndicatorManager: 当前指示器数量=" + damageIndicators.size());
    }

    /**
     * 更新所有伤害数值指示器
     */
    public static void updateIndicators() {
        Iterator<DamageIndicator> iterator = damageIndicators.iterator();
        while (iterator.hasNext()) {
            DamageIndicator indicator = iterator.next();

            // 移除目标实体检查，只要时间未过期就显示
            if (indicator.isExpired()) {
                iterator.remove();
            } else {
                indicator.update();
            }
        }
    }

    /**
     * 获取当前活跃的伤害数值指示器
     */
    public static List<DamageIndicator> getActiveIndicators() {
        return new ArrayList<>(damageIndicators);
    }

    /**
     * 清除所有伤害数值指示器
     */
    public static void clearIndicators() {
        damageIndicators.clear();
    }
}