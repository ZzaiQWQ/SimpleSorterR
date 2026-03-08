package simplesorter.mc.config

import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ConfigScreen {
    fun build(parent: Screen?): Screen {
        SimpleSorterConfig.load()

        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("SimpleSorter 排序设置"))
            .setSavingRunnable { 
                SimpleSorterConfig.save() 
            }
            .setTransparentBackground(true)
            
        val entryBuilder = builder.entryBuilder()
        val general = builder.getOrCreateCategory(Text.literal("常规/通用"))
        
        general.addEntry(
            entryBuilder.startBooleanToggle(Text.literal("需要长按 Z 键才能打开设置"), SimpleSorterConfig.requireZForConfig)
                .setDefaultValue(true)
                .setTooltip(Text.literal("如果启用此选项，在按下设置快捷键的同时必须长按 Z 键才能打开此菜单。"))
                .setSaveConsumer { value: Boolean -> SimpleSorterConfig.requireZForConfig = value }
                .build()
        )
        
        general.addEntry(
            entryBuilder.startStrList(Text.literal("分类排序顺序"), SimpleSorterConfig.categoryOrder)
                .setDefaultValue(mutableListOf(
                    "minecraft:tools_and_utilities",
                    "minecraft:combat",
                    "minecraft:building_blocks",
                    "minecraft:natural_blocks",
                    "minecraft:functional_blocks",
                    "minecraft:redstone_blocks",
                    "minecraft:food_and_drinks",
                    "minecraft:ingredients",
                    "minecraft:spawn_eggs"
                ))
                .setTooltip(Text.literal("排在上面的分类优先排序。你可以添加自定义物品组 (Item Group ID) 来优先排序它们。"))
                .setSaveConsumer { list: List<String> -> SimpleSorterConfig.categoryOrder = list.toMutableList() }
                .build()
        )
        
        return builder.build()
    }
}
