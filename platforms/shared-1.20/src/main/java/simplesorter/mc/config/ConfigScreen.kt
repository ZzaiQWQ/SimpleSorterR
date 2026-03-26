package simplesorter.mc.config

import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ConfigScreen {
    fun build(parent: Screen?): Screen {
        SimpleSorterConfig.load()

        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.simplesorter.title"))
            .setSavingRunnable { 
                SimpleSorterConfig.save() 
            }
            .setTransparentBackground(true)
            
        val entryBuilder = builder.entryBuilder()
        val general = builder.getOrCreateCategory(Text.translatable("config.simplesorter.category.general"))
        
        general.addEntry(
            entryBuilder.startBooleanToggle(Text.translatable("config.simplesorter.requireZ"), SimpleSorterConfig.requireZForConfig)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.simplesorter.requireZ.tooltip"))
                .setSaveConsumer { value: Boolean -> SimpleSorterConfig.requireZForConfig = value }
                .build()
        )
        
        general.addEntry(
            entryBuilder.startStrList(Text.translatable("config.simplesorter.categoryOrder"), SimpleSorterConfig.categoryOrder)
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
                .setTooltip(Text.translatable("config.simplesorter.categoryOrder.tooltip"))
                .setSaveConsumer { list: List<String> -> SimpleSorterConfig.categoryOrder = list.toMutableList() }
                .build()
        )

        // ── Auto-replace category ──
        val autoReplace = builder.getOrCreateCategory(Text.translatable("config.simplesorter.category.autoReplace"))

        autoReplace.addEntry(
            entryBuilder.startBooleanToggle(Text.translatable("config.simplesorter.replaceSameItem"), SimpleSorterConfig.autoReplaceSameItem)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.simplesorter.replaceSameItem.tooltip"))
                .setSaveConsumer { value: Boolean -> SimpleSorterConfig.autoReplaceSameItem = value }
                .build()
        )

        autoReplace.addEntry(
            entryBuilder.startBooleanToggle(Text.translatable("config.simplesorter.replaceSameType"), SimpleSorterConfig.autoReplaceSameType)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.simplesorter.replaceSameType.tooltip"))
                .setSaveConsumer { value: Boolean -> SimpleSorterConfig.autoReplaceSameType = value }
                .build()
        )

        autoReplace.addEntry(
            entryBuilder.startBooleanToggle(Text.translatable("config.simplesorter.autoRefill"), SimpleSorterConfig.autoRefillStack)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.simplesorter.autoRefill.tooltip"))
                .setSaveConsumer { value: Boolean -> SimpleSorterConfig.autoRefillStack = value }
                .build()
        )

        autoReplace.addEntry(
            entryBuilder.startIntSlider(Text.translatable("config.simplesorter.refillThreshold"), SimpleSorterConfig.refillThreshold, 1, 63)
                .setDefaultValue(20)
                .setTooltip(Text.translatable("config.simplesorter.refillThreshold.tooltip"))
                .setSaveConsumer { value: Int -> SimpleSorterConfig.refillThreshold = value }
                .build()
        )
        
        return builder.build()
    }
}
