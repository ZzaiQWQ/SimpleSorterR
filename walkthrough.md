# SimpleSorter 3.3.0 — 改动记录

---

## 一、排序方式：手动分类 → Creative Tab 原生排序

### 问题
旧排序用 `getCategoryId()` 基于 Java 类名匹配分类（如 `className.contains("Armor")`），存在以下问题：
- 类名匹配在混淆/mod 环境下不可靠
- 同分类内只按物品 ID 字母排序（钻石镐排在铁镐前面因为 d < i）
- `functional_blocks` 和 `spawn_eggs` 分类从未被返回（空分类）
- `RuleSet.kt` 108 行代码是死代码，从未被调用

### 改动

#### [NEW] [CreativeTabSorter.kt](file:///e:/java/SimpleSorter-26.1%20-k/platforms/shared-26/src/main/java/simplesorter/mc/CreativeTabSorter.kt)

从 Minecraft 创造模式标签页构建全局排序索引：
1. **Phase 0**：置顶物品（`pinnedItems` 配置）获得最小索引
2. **Phase 1**：按 `tabOrder` 配置顺序遍历原版标签页
3. **Phase 2**：自动遍历剩余标签页（包括 mod 标签页）
4. **Fallback**：如果标签页为空，回退到 registry 顺序

使用 `getOptional()` 安全查找物品，避免无效 ID 误将空气置顶。索引构建后缓存，按 R 时通过文件修改时间检测配置变更。

#### [MODIFY] [InventoryScanner.kt](file:///e:/java/SimpleSorter-26.1%20-k/platforms/shared-26/src/main/java/simplesorter/mc/InventoryScanner.kt)

```diff
-        val categoryId = getCategoryId(stack)
-        var catIndex = SimpleSorterConfig.categoryOrder.indexOf(categoryId)
-        if (catIndex == -1) catIndex = 999
+        val sortIndex = if (stack.isEmpty) Int.MAX_VALUE
+                        else CreativeTabSorter.getSortIndex(stack.item)

         return SlotSnapshot(
             ...
-            categoryIndex = catIndex
+            categoryIndex = sortIndex
         )
```

删除了整个 `getCategoryId()` 方法（~60 行）和 `requestSort()` 中新增配置热更新检测。

#### [DELETE] RuleSet.kt

删除 108 行死代码（`getPriority()` 从未被调用）。

---

## 二、配置系统：categoryOrder → tabOrder + pinnedItems

### 改动

#### [MODIFY] [SimpleSorterConfig.kt](file:///e:/java/SimpleSorter-26.1%20-k/platforms/shared-26/src/main/java/simplesorter/mc/config/SimpleSorterConfig.kt)

| 旧字段 | 新字段 | 说明 |
|--------|--------|------|
| `categoryOrder` | `tabOrder` | 创造标签页排序顺序，可自定义 |
| _(无)_ | `pinnedItems` | 置顶物品列表，永远排最前 |
| _(无)_ | `lastModified` | 文件修改时间追踪 |
| _(无)_ | `reloadIfChanged()` | 检测 JSON 文件变更并自动重载 |

新的配置文件格式：

```json
{
  "tabOrder": [
    "minecraft:building_blocks",
    "minecraft:colored_blocks",
    "minecraft:natural_blocks",
    "minecraft:functional_blocks",
    "minecraft:redstone_blocks",
    "minecraft:tools_and_utilities",
    "minecraft:combat",
    "minecraft:food_and_drinks",
    "minecraft:ingredients",
    "minecraft:spawn_eggs"
  ],
  "pinnedItems": [],
  "blockedContainers": [
    "AbstractFurnaceMenu", "FurnaceMenu", "BlastFurnaceMenu",
    "SmokerMenu", "CraftingMenu", "AnvilMenu", "..."
  ]
}
```

### 配置生效方式

| 方式 | 是否实时 |
|------|---------|
| 游戏内 Z+I 保存 | ✅ 立即生效 |
| 直接编辑 JSON 文件 | ✅ 下次按 R 排序时自动检测并生效 |

---

## 三、配置界面：中文硬编码 → i18n 多语言

### 问题
`ConfigScreen.kt` 所有文字都是硬编码中文，不支持其他语言。

### 改动

#### [MODIFY] [ConfigScreen.kt](file:///e:/java/SimpleSorter-26.1%20-k/platforms/shared-26/src/main/java/simplesorter/mc/config/ConfigScreen.kt)

- 所有 `Component.literal("中文")` → `Component.translatable("simplesorter.config.xxx")`
- 移除了 `tabOrder` 和 `pinnedItems` 输入框（高级选项直接编辑 JSON）
- 添加提示引导用户编辑 JSON 文件

#### [MODIFY] [en_us.json](file:///e:/java/SimpleSorter-26.1%20-k/platforms/fabric-26.1/src/main/resources/assets/simplesorter/lang/en_us.json) / [zh_cn.json](file:///e:/java/SimpleSorter-26.1%20-k/platforms/fabric-26.1/src/main/resources/assets/simplesorter/lang/zh_cn.json)

新增 11 个翻译键，覆盖配置界面所有文字。

---

## 验证结果

- ✅ Clean build 通过 (13 tasks executed)
- ✅ 运行时日志：`Built creative tab sort index: 1487 items (0 pinned)`
- ✅ 排序功能正常：`Fully sorted!`

