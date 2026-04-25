# Fatality Mod 架构重构设计

## 设计目标
- **事件驱动架构**: 基于事件总线实现松耦合的系统交互
- **API分离设计**: 清晰的模块边界和接口定义
- **扩展性**: 支持插件式扩展和模块化开发
- **重构属性系统**: 统一、可扩展的属性计算框架
- **重构饰品系统**: 灵活、可组合的饰品效果系统

## 新架构概览

### 核心模块划分
```
fatality/
├── api/                    # 公共API接口定义
│   ├── events/            # 事件API
│   ├── attributes/        # 属性API
│   ├── accessories/       # 饰品API
│   └── systems/          # 系统API
├── core/                  # 核心实现
│   ├── events/           # 事件系统实现
│   ├── attributes/       # 属性系统实现
│   ├── accessories/      # 饰品系统实现
│   ├── systems/          # 系统实现
│   └── registry/         # 注册系统
├── modules/              # 功能模块
│   ├── boss/            # BOSS系统
│   ├── combat/          # 战斗系统
│   ├── ui/              # UI系统
│   └── world/           # 世界系统
└── integration/         # 集成模块
    ├── forge/           # Forge集成
    └── client/          # 客户端集成
```

### 事件驱动架构

#### 事件总线设计
- **FatalityEventBus**: 统一的事件总线管理
- **EventPriority**: 事件优先级系统
- **EventCancellation**: 事件取消机制

#### 核心事件类型
- **PlayerAttributeEvent**: 玩家属性变化事件
- **AccessoryEquipEvent**: 饰品装备事件
- **CombatEvent**: 战斗相关事件
- **SystemEvent**: 系统事件

### API分离设计

#### 属性系统API
```java
public interface IAttributeSystem {
    float getAttribute(Player player, String attributeId);
    void registerAttribute(String attributeId, AttributeCalculator calculator);
    void addAttributeModifier(Player player, AttributeModifier modifier);
}
```

#### 饰品系统API
```java
public interface IAccessorySystem {
    void equipAccessory(Player player, ItemStack accessory);
    void unequipAccessory(Player player, ItemStack accessory);
    List<ItemStack> getEquippedAccessories(Player player);
}
```

### 属性系统重构

#### 新的属性计算框架
- **AttributeRegistry**: 属性注册中心
- **AttributeCalculator**: 属性计算器接口
- **CompositeAttribute**: 复合属性支持
- **AttributeModifier**: 属性修改器

#### 属性分类
- **基础属性**: 生命、法力、攻击力等
- **战斗属性**: 暴击、穿透、抗性等
- **特殊属性**: 元素伤害、特殊效果等

### 饰品系统重构

#### 新的饰品框架
- **AccessorySlot**: 饰品槽位系统
- **AccessoryEffect**: 饰品效果接口
- **AccessoryComposite**: 饰品组合效果
- **AccessoryRegistry**: 饰品注册中心

#### 饰品效果类型
- **属性加成效果**: 直接增加属性值
- **特殊能力效果**: 提供特殊能力
- **条件触发效果**: 满足条件时触发
- **组合套装效果**: 多饰品组合效果

## 实现计划

### 第一阶段：基础架构搭建
1. 创建新的目录结构
2. 实现事件总线系统
3. 定义核心API接口
4. 重构属性系统基础

### 第二阶段：系统重构
1. 重构饰品系统
2. 实现新的属性计算框架
3. 集成事件驱动架构
4. 迁移现有功能

### 第三阶段：功能完善
1. 优化系统性能
2. 添加扩展性支持
3. 完善API文档
4. 测试验证

## 技术要点

### 事件驱动优势
- **松耦合**: 系统间通过事件通信，降低依赖
- **可扩展**: 新功能通过监听事件即可集成
- **可测试**: 事件系统便于单元测试

### API分离优势
- **清晰边界**: 明确的接口定义和实现分离
- **模块化**: 各系统独立开发维护
- **版本兼容**: API稳定，实现可替换

### 扩展性设计
- **插件机制**: 支持第三方插件扩展
- **配置驱动**: 基于配置的系统行为
- **热重载**: 支持运行时配置更新

## 迁移策略

### 渐进式迁移
1. 保持现有功能正常运行
2. 逐步迁移到新架构
3. 并行运行，逐步切换
4. 最终移除旧代码

### 兼容性保证
1. API向后兼容
2. 数据迁移工具
3. 版本升级指南

## 预期收益

### 开发效率提升
- 清晰的模块边界，便于团队协作
- 标准化的接口设计，降低学习成本
- 事件驱动架构，简化系统集成

### 维护性提升
- 松耦合设计，降低修改影响范围
- 统一的错误处理机制
- 完善的日志和监控

### 扩展性提升
- 插件式架构，支持功能扩展
- 配置驱动，灵活调整系统行为
- 标准化API，便于第三方集成