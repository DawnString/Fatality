package cn.dawnstring.fatality.entity;

import cn.dawnstring.fatality.Fatality;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Boss基类 - 实现血量阶段管理、群系限制和AI行为切换
 */
public abstract class BaseBoss extends Monster {
    // 同步数据定义
    private static final EntityDataAccessor<Integer> DATA_CURRENT_PHASE = SynchedEntityData.defineId(BaseBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IN_VALID_BIOME = SynchedEntityData.defineId(BaseBoss.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_PHASE_TRANSITION_PROGRESS = SynchedEntityData.defineId(BaseBoss.class, EntityDataSerializers.FLOAT);

    // 背景音乐实例
    @Nullable
    protected SimpleSoundInstance backgroundMusic;

    // 在类字段区域添加无敌状态相关字段
    protected int invulnerableDuration = 0; // 无敌持续时间（tick）
    protected boolean isInvulnerable = false; // 无敌状态标志

    // 战斗场地管理器
    protected final BossArenaManager arenaManager;
    protected boolean requiresArena = false; // 是否需要战斗场地

    // 通用血条贴图配置
    public static final ResourceLocation HEALTH_BAR_DECORATION = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/health_bossbar_decoration.png");
    public static final ResourceLocation HEALTH_BAR_MAIN = ResourceLocation.fromNamespaceAndPath("fatality", "textures/gui/health_bossbar_main.png");

    // 血条尺寸配置
    public static final int DECORATION_WIDTH = 260;
    public static final int DECORATION_HEIGHT = 52;
    public static final int MAIN_BAR_WIDTH = 240;
    public static final int MAIN_BAR_HEIGHT = 4;

    // 血条主体在装饰中的偏移量（根据你提供的坐标）
    public static final int MAIN_BAR_OFFSET_X = 10; // 左上角X坐标
    public static final int MAIN_BAR_OFFSET_Y = 23; // 左上角Y坐标

    // 血条缩放比例和显示控制
    protected float healthBarScale = 0.5f;
    protected boolean showCustomHealthBar = true;

    // 血条颜色配置
    protected int customHealthBarColor = 0xFF800080; // 默认紫色

    // Boss阶段配置
    protected final List<BossPhase> phases = new ArrayList<>();
    protected final Set<ResourceLocation> validBiomes = new HashSet<>();
    protected final int biomeCheckRadius = 64; // 群系检查半径

    // 阶段转换相关
    public boolean isTransitioning = false;
    protected int transitionTicks = 0;
    protected final int transitionDuration = 40; // 阶段转换持续时间（ticks）

    // Boss事件
    protected final BossEvent bossEvent;

    // 仇恨系统相关
    protected final Map<Player, Float> playerDamageMap = new HashMap<>(); // 玩家伤害记录
    protected Player currentTarget = null; // 当前仇恨目标
    protected int targetSwitchCooldown = 0; // 目标切换冷却
    protected final int TARGET_SWITCH_COOLDOWN_TICKS = 100; // 目标切换冷却时间（ticks，5秒）
    protected final float TARGET_SWITCH_THRESHOLD = 1.2f; // 目标切换阈值（新目标伤害需比当前目标高20%）

    public BaseBoss(EntityType<? extends Monster> type, Level level)
    {
        super(type, level);
        this.bossEvent = new ServerBossEvent(Component.literal(this.getDisplayName().getString()),
                BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        this.arenaManager = new BossArenaManager(this);

        // 初始化阶段和群系配置
        initializePhases();
        initializeValidBiomes();

        // 应用初始阶段属性
        if (!phases.isEmpty()) {
            applyPhaseAttributes(phases.get(0));
        }
    }

    // 血条相关方法
    public float getHealthBarScale() {
        return healthBarScale;
    }

    public void setHealthBarScale(float scale) {
        this.healthBarScale = scale;
    }

    public int getScaledDecorationWidth() {
        return (int)(DECORATION_WIDTH * healthBarScale);
    }

    public int getScaledDecorationHeight() {
        return (int)(DECORATION_HEIGHT * healthBarScale);
    }

    public int getScaledMainBarWidth() {
        return (int)(MAIN_BAR_WIDTH * healthBarScale);
    }

    public int getScaledMainBarHeight() {
        return (int)(MAIN_BAR_HEIGHT * healthBarScale);
    }

    public boolean shouldShowCustomHealthBar() {
        return showCustomHealthBar;
    }

    // 获取阶段名称（子类可以重写）
    public String getCurrentPhaseName() {
        return "Boss";
    }

    // 获取阶段颜色（子类可以重写）
    public int getPhaseHealthBarColor() {
        return customHealthBarColor;
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CURRENT_PHASE, 0);
        this.entityData.define(DATA_IN_VALID_BIOME, true);
        this.entityData.define(DATA_PHASE_TRANSITION_PROGRESS, 0.0f);
        // 可以添加无敌状态的同步数据，如果需要客户端同步的话
        // this.entityData.define(DATA_INVULNERABLE, false);
    }

    /**
     * 重写hurt方法，追踪玩家伤害并更新仇恨系统
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 检查无敌状态
        if (isInvulnerable || invulnerableDuration > 0) {
            // 无敌状态下免疫伤害，但可以播放被击音效等
            if (!this.level().isClientSide()) {
                // 播放无敌状态下的特效或音效
                this.playSound(SoundEvents.SHIELD_BLOCK, 1.0f, 1.0f);
            }
            return false; // 免疫伤害
        }

        boolean hurt = super.hurt(source, amount);

        if (!this.level().isClientSide() && hurt) {
            // 检查伤害来源是否为玩家
            if (source.getEntity() instanceof Player player) {
                // 记录玩家伤害
                recordPlayerDamage(player, amount);
                // 更新仇恨目标
                updateAggroTarget();

                // 如果Boss需要战斗场地但尚未激活，自动激活
                if (requiresArena && !arenaManager.isArenaActive()) {
                    activateArena(player.blockPosition(), 129, 50); // 默认129x129，高度50
                }
            }
        }

        return hurt;
    }

    /**
     * 处理玩家死亡事件（子类可重写）
     */
    public void onPlayerDeath(Player player)
    {
        arenaManager.onPlayerDeath(player);
        cleanupBossProjectiles();
        broadcastMessage(Component.nullToEmpty("§6" + this.getName().getString() + "失去了对你的兴趣！"));
    }

    /**
     * 清理所有boss投掷物
     */
    protected void cleanupBossProjectiles()
    {
    }

    /**
     * 播放背景音乐
     */
    protected void playBackgroundMusic()
    {
    }

    /**
     * 记录玩家伤害
     */
    protected void recordPlayerDamage(Player player, float damage) {
        float currentDamage = playerDamageMap.getOrDefault(player, 0f);
        playerDamageMap.put(player, currentDamage + damage);

        // 定期清理过期的伤害记录（防止内存泄漏）
        if (this.tickCount % 6000 == 0) { // 每5分钟清理一次
            cleanupOldDamageRecords();
        }
    }

    /**
     * 停止背景音乐
     */
    private void stopBackgroundMusic()
    {
        if (backgroundMusic != null) {
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            soundManager.stop(backgroundMusic);
            backgroundMusic = null;
        }
    }

    /**
     * 清理过期的伤害记录
     */
    protected void cleanupOldDamageRecords() {
        // 移除伤害为0的记录
        playerDamageMap.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    /**
     * 更新仇恨目标
     */
    protected void updateAggroTarget() {
        if (targetSwitchCooldown > 0) {
            targetSwitchCooldown--;
            return;
        }

        Player highestDamagePlayer = getHighestDamagePlayer();

        if (highestDamagePlayer != null) {
            // 检查是否需要切换目标
            if (currentTarget == null) {
                // 没有当前目标，直接设置
                setAggroTarget(highestDamagePlayer);
            } else {
                float currentTargetDamage = playerDamageMap.getOrDefault(currentTarget, 0f);
                float highestDamage = playerDamageMap.getOrDefault(highestDamagePlayer, 0f);

                // 如果新目标的伤害比当前目标高20%以上，则切换目标
                if (highestDamage > currentTargetDamage * TARGET_SWITCH_THRESHOLD) {
                    setAggroTarget(highestDamagePlayer);
                }
            }
        } else if (currentTarget == null) {
            // 如果没有仇恨目标，寻找最近的玩家作为目标
            findNearestPlayerTarget();
        }
    }

    // 新增方法：寻找最近的玩家作为目标
    protected void findNearestPlayerTarget()
    {
        if (!this.level().isClientSide()) {
            // 在64格范围内寻找玩家
            net.minecraft.world.phys.AABB searchArea = this.getBoundingBox().inflate(64.0);
            java.util.List<net.minecraft.world.entity.player.Player> players =
                    this.level().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, searchArea);

            if (!players.isEmpty()) {
                // 选择最近的玩家
                net.minecraft.world.entity.player.Player nearestPlayer = null;
                double nearestDistance = Double.MAX_VALUE;

                for (net.minecraft.world.entity.player.Player player : players) {
                    if (player.isAlive()) {
                        double distance = this.distanceToSqr(player);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestPlayer = player;
                        }
                    }
                }

                if (nearestPlayer != null) {
                    this.setTarget(nearestPlayer);
                }
            }
        }
    }

    /**
     * 获取伤害最高的玩家
     */
    @Nullable
    protected Player getHighestDamagePlayer() {
        Player highestPlayer = null;
        float highestDamage = 0f;

        for (Map.Entry<Player, Float> entry : playerDamageMap.entrySet()) {
            Player player = entry.getKey();
            float damage = entry.getValue();

            // 检查玩家是否有效（存活且在范围内）
            if (isValidTarget(player) && damage > highestDamage) {
                highestDamage = damage;
                highestPlayer = player;
            }
        }

        return highestPlayer;
    }

    /**
     * 检查玩家是否为有效目标
     */
    protected boolean isValidTarget(Player player) {
        return player != null &&
                player.isAlive() &&
                player.level() == this.level() &&
                this.distanceTo(player) <= 64; // 64格范围内
    }

    /**
     * 设置仇恨目标
     */
    protected void setAggroTarget(Player player) {
        this.currentTarget = player;
        this.targetSwitchCooldown = TARGET_SWITCH_COOLDOWN_TICKS;

        // 设置Mob的目标
        this.setTarget(player);

        // 触发仇恨目标变化事件
        onAggroTargetChanged(player);
    }


    /**
     * 仇恨目标变化时的处理
     */
    protected void onAggroTargetChanged(Player newTarget)
    {
    }

    /**
     * 获取当前仇恨目标
     */
    @Nullable
    public Player getAggroTarget() {
        return currentTarget;
    }

    /**
     * 获取玩家对Boss造成的总伤害
     */
    public float getPlayerDamage(Player player) {
        return playerDamageMap.getOrDefault(player, 0f);
    }

    /**
     * 重置玩家伤害记录
     */
    public void resetPlayerDamage(Player player) {
        playerDamageMap.remove(player);
        if (currentTarget == player) {
            currentTarget = null;
            this.setTarget(null);
        }
    }

    /**
     * 重置所有玩家伤害记录
     */
    public void resetAllPlayerDamage() {
        playerDamageMap.clear();
        currentTarget = null;
        this.setTarget(null);
    }

    /**
     * 应用阶段属性
     */
    protected void applyPhaseAttributes(BossPhase phase) {
        if (phase != null) {
            // 设置最大血量
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(phase.getMaxHealth());
            // 设置攻击伤害
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(phase.getAttackDamage());
            // 设置移动速度
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(phase.getMovementSpeed());
            // 设置护甲
            this.getAttribute(Attributes.ARMOR).setBaseValue(phase.getArmor());

            // 确保血量不超过最大血量
            if (this.getHealth() > this.getMaxHealth()) {
                this.setHealth(this.getMaxHealth());
            }
        }
    }


    /**
     * 初始化Boss阶段配置 - 子类必须实现
     */
    protected abstract void initializePhases();

    /**
     * 初始化有效群系 - 子类可以重写
     */
    protected void initializeValidBiomes() {
    }

    /**
     * 获取当前阶段
     */
    public int getCurrentPhase() {
        return this.entityData.get(DATA_CURRENT_PHASE);
    }

    /**
     * 设置当前阶段
     */
    protected void setCurrentPhase(int phase) {
        this.entityData.set(DATA_CURRENT_PHASE, phase);
    }

    /**
     * 获取当前阶段配置
     */
    @Nullable
    public BossPhase getCurrentPhaseConfig() {
        int phase = getCurrentPhase();
        if (phase >= 0 && phase < phases.size()) {
            return phases.get(phase);
        }
        return null;
    }

    /**
     * 检查是否在有效群系内
     */
    public boolean isInValidBiome() {
        return this.entityData.get(DATA_IN_VALID_BIOME);
    }

    /**
     * 检查当前位置的群系是否有效
     */
    protected boolean checkBiomeValidity() {
        if (validBiomes.isEmpty()) return true; // 如果没有限制，则所有群系都有效

        BlockPos pos = this.blockPosition();
        Biome biome = this.level().getBiome(pos).value();
        ResourceLocation biomeId = ForgeRegistries.BIOMES.getKey(biome);

        return biomeId != null && validBiomes.contains(biomeId);
    }

    /**
     * 检查周围区域是否在有效群系内
     */
    protected boolean checkAreaBiomeValidity() {
        if (validBiomes.isEmpty()) return true;

        BlockPos center = this.blockPosition();
        boolean foundValid = false;

        // 检查周围区域
        for (int x = -biomeCheckRadius; x <= biomeCheckRadius; x += 16) {
            for (int z = -biomeCheckRadius; z <= biomeCheckRadius; z += 16) {
                BlockPos checkPos = center.offset(x, 0, z);
                Biome biome = this.level().getBiome(checkPos).value();
                ResourceLocation biomeId = ForgeRegistries.BIOMES.getKey(biome);

                if (biomeId != null && validBiomes.contains(biomeId)) {
                    foundValid = true;
                    break;
                }
            }
            if (foundValid) break;
        }

        return foundValid;
    }

    /**
     * 检查并处理阶段转换
     */
    protected void checkPhaseTransition() {
        float healthRatio = this.getHealth() / this.getMaxHealth();
        BossPhase currentPhase = getCurrentPhaseConfig();

        if (currentPhase != null) {
            // 检查是否需要转换到下一阶段
            if (healthRatio <= currentPhase.getHealthThreshold() && getCurrentPhase() < phases.size() - 1) {
                startPhaseTransition(getCurrentPhase() + 1);
            }
        }
    }

    /**
     * 开始阶段转换
     */
    protected void startPhaseTransition(int targetPhase) {
        if (targetPhase < 0 || targetPhase >= phases.size()) return;

        this.isTransitioning = true;
        this.transitionTicks = 0;

        // 触发阶段转换开始事件
        onPhaseTransitionStart(getCurrentPhase(), targetPhase);
    }

    /**
     * 处理阶段转换
     */
    protected void handlePhaseTransition() {
        transitionTicks++;

        // 更新转换进度
        float progress = (float) transitionTicks / transitionDuration;
        this.entityData.set(DATA_PHASE_TRANSITION_PROGRESS, Math.min(progress, 1.0f));

        // 转换完成
        if (transitionTicks >= transitionDuration) {
            completePhaseTransition();
        } else {
            // 转换过程中的特殊效果
            onPhaseTransitionTick(transitionTicks, transitionDuration);
        }
    }

    /**
     * 完成阶段转换
     */
    protected void completePhaseTransition() {
        int targetPhase = getCurrentPhase() + 1;
        if (targetPhase >= phases.size()) {
            targetPhase = phases.size() - 1;
        }

        // 应用新阶段的属性
        applyPhaseAttributes(targetPhase);
        setCurrentPhase(targetPhase);

        this.isTransitioning = false;
        this.transitionTicks = 0;
        this.entityData.set(DATA_PHASE_TRANSITION_PROGRESS, 0.0f);

        // 触发阶段转换完成事件
        onPhaseTransitionComplete(targetPhase);
    }

    /**
     * 应用阶段属性
     */
    protected void applyPhaseAttributes(int phase) {
        if (phase < 0 || phase >= phases.size()) return;

        BossPhase phaseConfig = phases.get(phase);

        // 保存当前血量比例，用于在新阶段保持相同的血量比例
        float healthRatio = this.getHealth() / this.getMaxHealth();

        // 应用属性加成
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(phaseConfig.getMaxHealth());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(phaseConfig.getAttackDamage());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(phaseConfig.getMovementSpeed());
        this.getAttribute(Attributes.ARMOR).setBaseValue(phaseConfig.getArmor());

        // 根据新阶段的最大血量计算新血量，保持相同的血量比例
        float newHealth = (float) (phaseConfig.getMaxHealth() * healthRatio);
        this.setHealth(Math.max(1.0f, newHealth)); // 确保血量至少为1
    }

    @Override
    public void tick()
    {
        super.tick();

        // 更新无敌计时器
        if (invulnerableDuration > 0) {
            invulnerableDuration--;
            if (invulnerableDuration <= 0) {
                isInvulnerable = false;
            }
        }

        // 处理战斗场地逻辑（服务器端）
        if (!this.level().isClientSide()) {
            handleArenaLogic();
        }
    }

    // 添加无敌状态相关的方法
    public void setInvulnerableDuration(int duration) {
        this.invulnerableDuration = duration;
        this.isInvulnerable = duration > 0;
    }

    public int getInvulnerableDuration() {
        return invulnerableDuration;
    }

    public boolean isInvulnerable() {
        return isInvulnerable || invulnerableDuration > 0;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.isInvulnerable = invulnerable;
        if (!invulnerable) {
            this.invulnerableDuration = 0;
        }
    }

    /**
     * 处理战斗场地逻辑
     */
    protected void handleArenaLogic() {
        if (requiresArena && arenaManager.isArenaActive()) {
            // 检查玩家是否在场地内
            arenaManager.checkPlayersInArena();

            // 显示边界粒子效果
            arenaManager.showBoundaryParticles();

            // 检查是否还有玩家在场地内
            if (!arenaManager.hasPlayersInArena()) {
                this.discard(); // 没有玩家在场地内，Boss消失
            }
        }
    }

    /**
     * 激活战斗场地（子类可调用）
     */
    protected void activateArena(BlockPos center, int size, Integer height) {
        this.requiresArena = true;
        arenaManager.activateArena(center, size, height);
    }

    /**
     * 停用战斗场地（子类可调用）
     */
    protected void deactivateArena()
    {
        arenaManager.deactivateArena();
        this.requiresArena = false;
    }

    @Override
    public void die(DamageSource cause)
    {
        super.die(cause);

        // 停止背景音乐
        if (this.level().isClientSide()) {
            stopBackgroundMusic();
        }

        // 处理Boss死亡事件
        if (arenaManager != null) {
            arenaManager.onBossDeath();
        }

        cleanupBossProjectiles();
        broadcastMessage(Component.literal("§a" + getDisplayName().getString() + "已被打败！"));

        this.bossEvent.setProgress(0.0f);
    }

    /**
     * 广播消息给所有在战斗场地内的玩家
     */
    private void broadcastMessage(Component message) {
        if (this.level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.players().forEach(player -> {
            player.sendSystemMessage(message);
        });
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("CurrentPhase", getCurrentPhase());
        compound.putBoolean("InValidBiome", isInValidBiome());
        compound.putBoolean("IsTransitioning", isTransitioning);
        compound.putInt("TransitionTicks", transitionTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setCurrentPhase(compound.getInt("CurrentPhase"));
        this.entityData.set(DATA_IN_VALID_BIOME, compound.getBoolean("InValidBiome"));
        this.isTransitioning = compound.getBoolean("IsTransitioning");
        this.transitionTicks = compound.getInt("TransitionTicks");

        // 重新应用阶段属性
        applyPhaseAttributes(getCurrentPhase());
    }

    // 事件方法 - 子类可以重写
    protected void onBiomeValidityChanged(boolean isValid) {
        // 群系有效性变化时的处理
        if (!isValid) {
            this.setTarget(null);
        }
    }

    protected void onPhaseTransitionStart(int fromPhase, int toPhase) {
        // 阶段转换开始时的处理
        this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0f, 0.8f);
    }

    protected void onPhaseTransitionTick(int currentTick, int totalTicks) {
        // 阶段转换过程中的处理（每tick调用）
    }

    protected void onPhaseTransitionComplete(int newPhase) {
        // 阶段转换完成时的处理
        this.playSound(SoundEvents.ENDER_DRAGON_AMBIENT, 1.0f, 0.6f);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    // Getter方法
    public boolean requiresArena() {
        return requiresArena;
    }

    public void setRequiresArena(boolean requiresArena) {
        this.requiresArena = requiresArena;
    }

    public BossArenaManager getArenaManager() {
        return arenaManager;
    }

    /**
     * Boss阶段配置类
     */
    public static class BossPhase {
        private final String name;
        private final float healthThreshold; // 触发该阶段的血量阈值（0-1）
        private final double maxHealth;
        private final double attackDamage;
        private final double movementSpeed;
        private final double armor;

        public BossPhase(String name, float healthThreshold, double maxHealth,
                         double attackDamage, double movementSpeed, double armor) {
            this.name = name;
            this.healthThreshold = healthThreshold;
            this.maxHealth = maxHealth;
            this.attackDamage = attackDamage;
            this.movementSpeed = movementSpeed;
            this.armor = armor;
        }

        // Getters
        public String getName() { return name; }
        public float getHealthThreshold() { return healthThreshold; }
        public double getMaxHealth() { return maxHealth; }
        public double getAttackDamage() { return attackDamage; }
        public double getMovementSpeed() { return movementSpeed; }
        public double getArmor() { return armor; }
    }
}
