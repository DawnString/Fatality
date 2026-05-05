# Fatality Mod - 完整项目分析文档

> 基于 Minecraft 1.20.1 Forge 的综合性模组项目  
> 包名: `cn.dawnstring.fatality`

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 依赖框架与技术栈](#2-依赖框架与技术栈)
- [3. 系统架构](#3-系统架构)
- [4. 核心业务逻辑详解](#4-核心业务逻辑详解)
- [5. 注册机制与事件驱动架构](#5-注册机制与事件驱动架构)
- [6. 网络通信系统](#6-网络通信系统)
- [7. 配置与系统注册管理](#7-配置与系统注册管理)
- [8. 战斗系统详解](#8-战斗系统详解)
- [9. 客户端系统](#9-客户端系统)
- [10. 数据流与调用链](#10-数据流与调用链)
- [11. 插件系统](#11-插件系统)
- [12. 待完善与潜在问题](#12-待完善与潜在问题)

---

## 1. 项目概述

### 基础信息

| 属性 | 值 |
|------|-----|
| Mod ID | `fatality` |
| 主类 | `cn.dawnstring.fatality.Fatality` |
| 版本 | 1.0.0 |
| 目标版本 | Minecraft 1.20.1 |
| 核心模组加载器 | **Forge** (net.minecraftforge) |
| 构建系统 | Gradle (build.gradle) |

### 项目定位

本项目是一个**大型综合性 RPG 模组**，在原版 Minecraft 基础上增加了：
- **复杂武器系统**：多种武器类型（近战/远程/魔法），多层伤害计算公式
- **饰品系统**：可装备的饰品，提供暴击、伤害加成等属性
- **属性系统**：玩家属性升级与管理
- **Boss 系统**：自定义 Boss 战斗机制
- **游戏阶段系统**：基于游戏进程的阶段解锁与事件系统
- **自定义 UI**：HUD 覆盖层、伤害数字显示、主菜单音乐替换
- **插件系统**：模块化热插拔架构
- **双架构并存**：新旧两套系统架构并行运行

---

## 2. 依赖框架与技术栈

### 模组框架

- **MinecraftForge** (v1.20.1)
  - `net.minecraftforge.fml.common.Mod` → 模组入口注解
  - `net.minecraftforge.eventbus.api.IEventBus` → 事件总线
  - `net.minecraftforge.registries.DeferredRegister` → 延迟注册
  - `net.minecraftforge.network` → 网络通信
  - `MinecraftForge.EVENT_BUS` → Forge 全局事件总线

### Minecraft 关键依赖

```
net.minecraft           # 核心游戏体系
├── world.item          # 物品体系 (SwordItem, ItemStack)
├── world.entity        # 实体体系 (LivingEntity, Player)
├── world.effect        # 状态效果 (MobEffects)
├── network.chat        # 聊天/文本组件
├── resources           # 资源定位 (ResourceLocation)
├── sounds              # 音效系统 (SoundEvent)
├── client              # 客户端 APIs (Minecraft.getInstance())
└── server              # 服务端 APIs
```

### 日志与构建

- **Log4j** → `org.apache.logging.log4j.Logger`
- **Gradle** → 构建管理

---

## 3. 系统架构

### 3.1 双架构并存策略

本项目采用了**新旧两套架构并存**的设计模式。在 `ArchitectureConfig` 中通过开关控制：

```
ArchitectureConfig.ENABLE_EVENT_DRIVEN_ARCHITECTURE = true;  // 新架构开关
ArchitectureConfig.ENABLE_API_SEPARATION = true;              // API分离开关
```

#### 旧系统（直接式）
- `ManaRegenerationHandler` - 直接在主类中注册到 Forge 事件总线
- `LifeRingEffectManager` - 直接在主类中注册
- 部分物品/武器直接继承并重写 Minecraft 原版方法

#### 新系统（插件式 + 事件驱动）
- **SystemRegistry** - 系统注册表，统一管理所有子系统
- **PluginManager** - 插件管理器，支持插件注册与启用/禁用
- **ForgeIntegration** - Forge 集成层，连接新架构与 Forge 事件总线
- **MigrationHelper** - 迁移协助，输出迁移状态

### 3.2 主类初始化流程

```
Fatality 构造函数
  │
  ├── 1. initializeNewArchitecture()
  │       ├── ArchitectureConfig.validateConfig()     -- 验证配置
  │       ├── System.out.println(configSummary)        -- 打印配置摘要
  │       └── if (ENABLE_EVENT_DRIVEN_ARCHITECTURE)
  │               ├── SystemRegistry.initializeAll()   -- 初始化系统注册表
  │               │       ├── AttributeSystem (依赖: 无, 优先级: 0)
  │               │       ├── AccessorySystem (依赖: attribute, 优先级: 1)
  │               │       ├── CombatSystem (依赖: attribute,accessory, 优先级: 2)
  │               │       └── BossSystem (依赖: attribute,combat, 优先级: 3)
  │               ├── PluginManager.getInstance().initialize()  -- 初始化插件
  │               ├── ForgeIntegration.initialize()              -- Forge集成初始化
  │               └── MigrationHelper.printMigrationStatus()     -- 打印迁移状态
  │
  ├── 2. ConfigManager.getInstance().initialize()      -- 配置管理器初始化
  ├── 3. ModRegistry.register(modEventBus)              -- 注册所有物品/容器
  ├── 4. modEventBus.addListener(this::commonSetup)     -- 通用设置
  ├── 5. modEventBus.addListener(this::clientSetup)     -- 客户端设置
  ├── 6. MinecraftForge.EVENT_BUS.register(this)        -- 注册指令命令
  ├── 7. 注册 ManaRegenerationHandler (旧系统)
  ├── 8. 注册 LifeRingEffectManager (旧系统)
  ├── 9. SOUND_EVENTS.register(modEventBus)             -- 注册音效
  └── 10. if (CLIENT) → 注册 MainMenuReplacer           -- 客户端主菜单替换
```

### 3.3 包结构

```
cn.dawnstring.fatality/
├── Fatality.java                    # 主模组入口
│
├── api/                             # API 层（新架构）
│   ├── FatalityAPI.java             # 统一对外 API
│   ├── accessories/
│   │   └── IAccessorySystem.java    # 饰品系统接口
│   ├── attributes/
│   │   └── IAttributeSystem.java    # 属性系统接口
│   ├── plugins/
│   │   └── IPlugin.java             # 插件接口
│   └── systems/
│       └── IModSystem.java          # 模块系统接口
│
├── client/                          # 客户端专属
│   ├── MainMenuReplacer.java        # 主菜单替换
│   ├── CustomHudRenderer.java       # 自定义 HUD 渲染
│   └── DamageIndicatorRenderer.java # 伤害数字渲染
│
├── config/                          # 配置层
│   └── ArchitectureConfig.java      # 架构配置开关
│
├── core/                            # 核心系统（新架构）
│   ├── config/
│   │   └── ConfigManager.java       # 配置管理器
│   ├── plugins/
│   │   └── PluginManager.java       # 插件管理器
│   └── systems/
│       └── SystemRegistry.java      # 系统注册表
│
├── events/                          # 事件系统
│   └── GameEventCommand.java        # 游戏事件命令
│
├── gamestage/                       # 游戏阶段系统
│   ├── GameStageCommand.java        # 游戏阶段命令
│   └── ...                          # 阶段相关类
│
├── integration/                     # 集成层
│   ├── MigrationHelper.java         # 迁移辅助
│   └── forge/
│       └── ForgeIntegration.java    # Forge 集成
│
├── inventory/                       # 容器系统
│   └── AccessoryInventory.java      # 饰品栏容器
│
├── items/                           # 物品系统
│   ├── BaseWeapon.java              # 武器基类
│   ├── AccessoryItem.java           # 饰品物品基类
│   └── WeaponEnum.java              # 武器类型枚举
│
├── network/                         # 网络通信
│   ├── NetworkManager.java          # 网络管理器
│   └── DamageIndicatorPacket.java   # 伤害指示数据包
│
├── registry/                        # 注册表
│   └── ModRegistry.java             # 模组注册中心
│
├── system/                          # 业务系统
│   ├── AttributeSystem.java         # 属性系统
│   ├── AccessorySystem.java         # 饰品系统
│   ├── ManaRegenerationHandler.java # 魔力恢复处理器
│   └── LifeRingEffectManager.java   # 生命之环效果
│
├── modules/                         # 模块（新架构）
│   ├── combat/
│   │   └── CombatSystem.java        # 战斗系统
│   └── boss/
│       └── BossSystem.java          # Boss 系统
│
└── utils/                           # 工具类
    └── TooltipHelper.java           # 工具提示辅助
```

---

## 4. 核心业务逻辑详解

### 4.1 武器系统（`BaseWeapon.java`）

#### 4.1.1 类层次结构

```
SwordItem (net.minecraft)
  └── BaseWeapon (cn.dawnstring.fatality.items)
        └── [具体武器子类] (未在当前代码中找到)
```

#### 4.1.2 武器类型枚举（`WeaponEnum`）

```
MELEE   → 近战武器
RANGED  → 远程武器
MAGIC   → 魔法武器
```

#### 4.1.3 武器构造参数

```java
public BaseWeapon(Tier tier, Properties properties, int attackDamage,
                  float attackSpeedInSeconds,
                  float baseDamageMultiplier,
                  float criticalChance,
                  float criticalDamageMultiplier,
                  float damageFluctuation,
                  WeaponEnum weaponType)
```

**关于攻击速度的计算**：  
`BaseWeapon` 接受以**秒**为单位的攻击间隔（`attackSpeedInSeconds`），内部转换为 Minecraft 的 `attackSpeed` 属性值：

```java
// 转换：1.0f / attackSpeedInSeconds
// 例如传入 1.5 秒 → Minecraft 速度为 0.666...
super(tier, attackDamage, (1.0f / attackSpeedInSeconds), properties);
```

#### 4.1.4 多层伤害计算公式

```
非暴击伤害 = 面板伤害 x 基础伤害加成(饰品) x 其他加成(饰品/药水) x 0.9 x 浮动值
暴击伤害   = 面板伤害 x 基础伤害加成(饰品) x 其他加成(饰品/药水) x 0.8 x 爆伤倍率 x 浮动值
```

**详细拆解：**

1. **面板伤害（`getBaseDamage`）**
   ```
   weaponDamage = baseAttackDamage + player.getAttribute(ATTACK_DAMAGE)
   return weaponDamage x baseDamageMultiplier
   ```

2. **基础伤害加成（`calculateAccessoryBaseBonus`）**
   - 遍历饰品栏，累加每个饰品的伤害加成百分比
   - 默认至少 1.0（100%）
   - 饰品加成示例：
     - 史诗攻击护符: +30%
     - 高级攻击护符: +20%
     - 普通攻击护符: +10%
     - 其他饰品: +5%

3. **其他加成（`calculateOtherBonus`）**
   - 力量效果: (等级+1) x 13%
   - 虚弱效果: (等级+1) x -20%
   - 保底 10%

4. **浮动值（`calculateDamageFluctuation`）**
   - 范围: `[1 - damageFluctuation, 1 + damageFluctuation]` 之间的随机值

5. **暴击判断（`isCriticalHit`）**
   - 基础暴击率 + 饰品暴击率加成
   - 生成随机浮点数比较

6. **暴击伤害倍率（`getCriticalDamageMultiplier`）**
   - 基础暴击倍率 + 饰品暴击伤害加成（按武器类型区分）

#### 4.1.5 伤害执行流程（`hurtEnemy` 重写）

```
玩家攻击 -> BaseWeapon.hurtEnemy(stack, target, attacker)
  -> calculateFinalDamage() [多层公式计算最终伤害]
  -> target.hurt(playerAttack, damage) [应用伤害]
  -> 若失败 -> target.hurt(generic, damage) [重试]
  -> 发送 DamageIndicatorPacket [网络包]
  -> 若伤害成功:
       -> 暴击时: onCriticalHit() [显示暴击消息]
       -> onHitEnemy() [武器特效回调]
```

### 4.2 饰品系统

#### 4.2.1 饰品物品（`AccessoryItem`）

饰品基类提供以下可重写的加成方法：
- `getCriticalChanceBonus()` → 暴击率加成
- `getMeleeCriticalDamageBonus()` → 近战暴击伤害加成
- `getRangedCriticalDamageBonus()` → 远程暴击伤害加成
- `getMagicCriticalDamageBonus()` → 魔法暴击伤害加成

#### 4.2.2 饰品栏（`AccessoryInventory`）

- 独立于原版物品栏的饰品存储系统
- 使用 `ItemHandler` 管理槽位
- 通过 `AccessoryInventory.get(player)` 静态方法获取玩家饰品栏

#### 4.2.3 饰品与武器系统的交互

```
BaseWeapon.hurtEnemy()
  ├── calculateAccessoryBaseBonus()
  │     └── 遍历饰品栏获取基础伤害加成
  ├── calculateAccessoryCriticalChanceBonus()
  │     └── 遍历获取暴击率加成
  ├── calculateAccessoryCriticalBonus()
  │     └── 遍历获取暴击伤害加成（按武器类型分发）
  └── ...
```

**重要问题**：`calculateAccessoryCriticalChanceBonus()` 和 `calculateAccessoryCriticalBonus()` 中通过 `Minecraft.getInstance().player` 获取玩家引用，这仅在**客户端**可用。如果服务端调用将返回 `null`，导致饰品加成在服务端失效。

---

## 5. 注册机制与事件驱动架构

### 5.1 物品/容器注册（`ModRegistry`）

通过 `ModRegistry.register(modEventBus)` 统一注册：
- 使用 `DeferredRegister` 延迟注册机制
- 注册内容：所有物品（武器、饰品）、容器（饰品栏 GUI）
- 注册到 `modEventBus`（模组事件总线）

### 5.2 音效注册

使用独立的 `DeferredRegister<SoundEvent>`:

```java
public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
    DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
```

已注册音效：
- `main_menu_music` - 主菜单背景音乐
- `end_of_nightmare_fight_music` - 战斗音乐

### 5.3 事件总线注册

| 事件总线 | 注册内容 | 生命周期 |
|---------|---------|---------|
| `modEventBus` | `FMLCommonSetupEvent`, `FMLClientSetupEvent` | 模组加载 |
| `MinecraftForge.EVENT_BUS` | `Fatality` (指令), `ManaRegenerationHandler`, `LifeRingEffectManager` | 游戏运行 |
| `MinecraftForge.EVENT_BUS` (Client) | `MainMenuReplacer`, `CustomHudRenderer`, `DamageIndicatorRenderer` | 客户端 |

---

## 6. 网络通信系统（`NetworkManager`）

### 6.1 网络架构

- 使用 Forge 的 `SimpleChannel` 网络系统
- 仅在客户端环境初始化（`FMLCommonSetupEvent` 中判断 `FMLEnvironment.dist == Dist.CLIENT`）

### 6.2 数据包（`DamageIndicatorPacket`）

**方向**: 服务端 -> 客户端  
**用途**: 在客户端显示伤害数字  
**内容**:
- `targetId` - 受击实体 ID
- `damage` - 伤害数值
- `attackerId` - 攻击者实体 ID

**发送时机**（在 `BaseWeapon.hurtEnemy` 中）：
```java
NetworkManager.INSTANCE.send(
    PacketDistributor.TRACKING_ENTITY.with(() -> target),
    new DamageIndicatorPacket(target.getId(), finalDamage, player.getId())
);
```

**注意**：网络包发送在 `!target.level().isClientSide()` 条件下，即仅在服务端发送。

---

## 7. 配置与系统注册管理

### 7.1 架构配置（`ArchitectureConfig`）

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `ENABLE_EVENT_DRIVEN_ARCHITECTURE` | boolean | `true` | 启用事件驱动架构 |
| `ENABLE_API_SEPARATION` | boolean | `true` | 启用 API 分离 |
| `ENABLE_NEW_ATTRIBUTE_SYSTEM` | boolean | `true` | 新属性系统 |
| `ENABLE_NEW_ACCESSORY_SYSTEM` | boolean | `true` | 新饰品系统 |
| `ENABLE_MODULAR_SYSTEMS` | boolean | `true` | 模块化系统 |
| `ENABLE_EVENT_LOGGING` | boolean | `false` | 事件日志 |
| `EVENT_PROCESSING_THREADS` | int | `2` | 事件处理线程数 |
| `ENABLE_ATTRIBUTE_CACHING` | boolean | `true` | 属性缓存 |
| `ATTRIBUTE_CACHE_DURATION` | int | `20` | 缓存时长(tick) |
| `MAX_ACCESSORY_SLOTS` | int | `6` | 最大饰品栏位 |

### 7.2 系统注册表（`SystemRegistry`）

依赖注入式注册：
```java
// 注册核心系统（无依赖，优先级 0）
SystemRegistry.register(AttributeSystem.getInstance(), new String[]{}, 0);

// 注册饰品系统（依赖 attribute，优先级 1）
SystemRegistry.register(AccessorySystem.getInstance(), new String[]{"attribute"}, 1);

// 注册模块系统（依赖前置系统）
SystemRegistry.register(CombatSystem.getInstance(), new String[]{"attribute", "accessory"}, 2);
SystemRegistry.register(BossSystem.getInstance(), new String[]{"attribute", "combat"}, 3);
```

**特点**：
- 支持依赖声明（`dependencies` 数组）
- 支持优先级排序
- 支持按类型或 ID 查询
- `initializeAll()` 按优先级初始化所有已注册系统

---

## 8. 战斗系统详解

### 8.1 伤害执行链路

```
玩家攻击（左键）-> Minecraft 原版攻击逻辑
  -> BaseWeapon.hurtEnemy(stack, target, attacker)
    -> calculateFinalDamage(player, stack, target)
      ├── getBaseDamage()           # 面板伤害
      ├── calculateAccessoryBaseBonus()  # 饰品基础伤害加成
      ├── calculateOtherBonus()     # 药水/状态效果加成
      ├── calculateDamageFluctuation()   # 伤害浮动值
      ├── isCriticalHit()           # 暴击判定
      │     ├── 武器基础暴击率
      │     └── 饰品暴击率加成
      └── getCriticalDamageMultiplier()  # 暴击倍率（暴击时）
            ├── 武器基础暴击倍率
            └── 饰品暴击伤害加成（按武器类型）
    -> target.hurt(damageSource, damage)
    -> 发送 DamageIndicatorPacket
    -> onCriticalHit() + onHitEnemy() 回调
```

### 8.2 伤害特点

1. **多层累乘**：面板 x 基础加成 x 其他加成 x 浮动 x (暴击分支)
2. **数值保证**：大量 `Math.max()` 和 `if` 检查确保数值不为 0、NaN 或 Infinity
3. **容错处理**：伤害应用失败时尝试使用 `damageSources().generic()` 重试
4. **双重路径**：暴击和非暴击使用不同公式

---

## 9. 客户端系统

### 9.1 HUD 渲染器（`CustomHudRenderer`）

- 注册时机：`FMLClientSetupEvent`
- 注册到：`MinecraftForge.EVENT_BUS`
- 功能：自定义游戏内 HUD 覆盖层

### 9.2 伤害指示器（`DamageIndicatorRenderer`）

- 注册时机：`FMLClientSetupEvent`
- 注册到：`MinecraftForge.EVENT_BUS`
- 接收来自服务端的 `DamageIndicatorPacket`
- 在实体上方渲染伤害数字

### 9.3 主菜单替换（`MainMenuReplacer`）

- 注册时机：构造函数中判断 `FMLEnvironment.dist == Dist.CLIENT`
- 替换原版主菜单 UI 或背景音乐

### 9.4 工具提示系统（`TooltipHelper`）

武器工具提示显示内容：
- 武器名称与属性
- 基础伤害倍率
- 暴击率与暴击伤害
- 伤害浮动范围
- 特殊效果描述
- 物品故事（`story` 字段）

---

## 10. 数据流与调用链

### 10.1 玩家攻击完整流

```
[玩家左键攻击]
     |
     v
[Minecraft 碰撞检测]
     |
     v
[SwordItem.hurtEnemy]
     |
     v
[BaseWeapon.hurtEnemy 重写方法]
     |
     v
[calculateFinalDamage]
     |
     +---> getBaseDamage               (面板伤害)
     +---> calculateAccessoryBaseBonus  (饰品基础加成)
     +---> calculateOtherBonus          (药水/状态)
     +---> calculateDamageFluctuation   (浮动值)
     +---> isCriticalHit? ---- 否 ----> [非暴击公式]
     |                        |
     |                         -> [暴击公式]
     |                              |
     +---> getCriticalDamageMultiplier (暴击倍率)
     |
     v
[target.hurt] --- 失败? ---> [target.hurt generic 重试]
     |
     v
[发送 DamageIndicatorPacket]
     |
     v
[onCriticalHit / onHitEnemy 回调]
```

### 10.2 模组初始化数据流

```
[FMLCommonSetupEvent]
     |
     v
[commonSetup]
     |
     +---> 是客户端? ---> [NetworkManager.register]
     |
[FMLClientSetupEvent]
     |
     v
[clientSetup]
     |
     +---> 注册 CustomHudRenderer
     +---> 注册 DamageIndicatorRenderer
     |
[RegisterCommandsEvent]
     |
     v
[GameStageCommand.register]
[GameEventCommand.register]
```

---

## 11. 插件系统（`PluginManager`）

### 11.1 架构

```
PluginManager (单例)
├── initialize()        # 初始化所有插件
├── registerPlugin()    # 注册新插件
├── getPlugin()         # 获取插件实例
├── isPluginEnabled()   # 检查插件启用状态
└── getPluginIds()      # 获取所有插件 ID
```

### 11.2 插件接口（`IPlugin`）

```java
public interface IPlugin {
    String getId();          // 插件唯一标识
    String getName();        // 插件名称
    String getVersion();     // 插件版本
    void onInitialize();     // 初始化回调
    void onShutdown();       // 关闭回调
    boolean isEnabled();     // 是否启用
    void setEnabled(boolean enabled);  // 设置启用状态
}
```

---

## 12. 待完善与潜在问题

### 12.1 代码质量问题

| 问题 | 文件 | 描述 |
|------|------|------|
| 客户端服务端混用 | `BaseWeapon.java` | `calculateAccessoryCriticalChanceBonus()` 和 `calculateAccessoryCriticalBonus()` 通过 `Minecraft.getInstance().player` 获取玩家，该方法**只在客户端工作**。服务端调用将返回 `null`，导致饰品暴击加成完全失效 |
| 硬编码数值 | `BaseWeapon.java` | 饰品加成使用字符串 `itemName.contains(...)` 硬编码匹配，扩展性差 |
| 中文字符串编码混乱 | 多个文件 | 注释出现乱码，文件编码可能不一致 |
| 混用日志方式 | `Fatality.java` / `BaseWeapon.java` | 部分使用 `LOGGER` (Log4j)，部分使用 `System.out.println` |
| 魔法数字 | `BaseWeapon.java` | 多处出现 `0.9f`, `0.8f`, `0.1f` 等未命名常量 |
| 重复遍历饰品栏 | `BaseWeapon.java` | 伤害计算中三次独立遍历饰品栏（基础加成、暴击率、暴击伤害），建议合并 |

### 12.2 架构问题

| 问题 | 描述 |
|------|------|
| 新旧架构并存 | 同时维护两套系统增加复杂度，如 `ManaRegenerationHandler` 仍注册在旧总线，而新系统通过 `SystemRegistry` 管理 |
| 网络初始化位置 | `NetworkManager.register()` 在 `FMLCommonSetupEvent` 中且仅在客户端执行，但服务端也需要网络通信 |
| 单例滥用 | `AttributeSystem`, `AccessorySystem` 等使用单例模式 |
| 配置无持久化 | `ArchitectureConfig` 使用静态变量，未对接 Forge 配置系统，重启后重置 |
| 缺乏 API 版本控制 | `FatalityAPI` 无版本兼容策略 |

### 12.3 安全与稳定性问题

| 问题 | 描述 |
|------|------|
| 服务端 NullPointer 风险 | 客户端专用 API (`Minecraft.getInstance()`) 在服务端环境调用 |
| 伤害计算可能不一致 | 客户端与服务端独立计算伤害，网络包仅为显示用途，可能导致显示与实际不符 |
| 并发问题 | `EVENT_PROCESSING_THREADS = 2` 但无明显线程安全措施 |

### 12.4 建议改进方向

1. **统一伤害计算路径**：确保客户端和服务端使用相同逻辑，服务端作为权威计算端
2. **重构饰品属性获取**：使用 Capability 或 Attributes 系统替代字符串匹配
3. **统一日志框架**：全部迁移到 Log4j
4. **配置持久化**：集成 Forge 的 `ModConfig` 系统
5. **合并饰品栏遍历**：一次遍历完成所有属性收集
6. **完善模组开发文档**：解决中文编码问题
7. **抽象属性提供者**：创建 `IAttributeProvider` 接口，使饰品/药水/附魔等统一提供属性加成
