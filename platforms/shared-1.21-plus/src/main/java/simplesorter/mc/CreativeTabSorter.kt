package simplesorter.mc

import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

/**
 * Builds a global sort index based on Minecraft's Creative Mode tab ordering.
 * Respects config settings:
 *   - tabOrder: controls which creative tabs come first
 *   - pinnedItems: items that always sort to the very front
 */
object CreativeTabSorter {
    private val logger = LoggerFactory.getLogger("simplesorter")
    private var sortIndexMap: Map<Item, Int>? = null

    /**
     * Get the creative tab sort index for an item.
     * Lower index = appears earlier in sorted inventory.
     * Items not found in any creative tab get index 99999.
     */
    @JvmStatic
    fun getSortIndex(item: Item): Int {
        val map = sortIndexMap ?: buildSortIndex().also { sortIndexMap = it }
        return map[item] ?: 99999
    }

    /**
     * Invalidate the cached sort index.
     * Call this when config changes or when joining a new world.
     */
    @JvmStatic
    fun invalidate() {
        sortIndexMap = null
        logger.info("[SimpleSorter] Creative tab sort index invalidated")
    }

    private fun buildSortIndex(): Map<Item, Int> {
        val map = LinkedHashMap<Item, Int>()
        var globalIndex = 0

        // Phase 0: Pinned items come first
        val pinnedItems = simplesorter.mc.config.SimpleSorterConfig.pinnedItems
        for (itemId in pinnedItems) {
            try {
                val resLoc = Identifier.tryParse(itemId) ?: continue
                val optItem = Registries.ITEM.getOrEmpty(resLoc)
                if (optItem.isPresent && !map.containsKey(optItem.get())) {
                    map[optItem.get()] = globalIndex++
                } else if (!optItem.isPresent) {
                    logger.warn("[SimpleSorter] Pinned item not found: {}", itemId)
                }
            } catch (e: Exception) {
                logger.warn("[SimpleSorter] Invalid pinned item ID: {}", itemId)
            }
        }

        // Try to ensure tab contents are built
        try {
            val client = MinecraftClient.getInstance()
            val connection = client.networkHandler
            if (connection != null) {
                ItemGroups.updateDisplayContext(
                    connection.enabledFeatures,
                    client.player?.isCreativeLevelTwoOp ?: false,
                    connection.registryManager
                )
            }
        } catch (e: Exception) {
            logger.warn("[SimpleSorter] Failed to rebuild creative tab contents: {}", e.message)
        }

        // Phase 1: Process tabs in the configured order
        val tabOrder = simplesorter.mc.config.SimpleSorterConfig.tabOrder
        val registry = Registries.ITEM_GROUP

        // Build a map of tab ID -> tab object
        val tabById = LinkedHashMap<String, net.minecraft.item.ItemGroup>()
        for (tab in registry) {
            val key = registry.getId(tab)
            if (key != null) {
                tabById[key.toString()] = tab
            }
        }

        // Process tabs in config order first
        val processedTabs = mutableSetOf<String>()
        for (tabId in tabOrder) {
            val tab = tabById[tabId] ?: continue
            processedTabs.add(tabId)
            try {
                for (stack in tab.displayStacks) {
                    val item = stack.item
                    if (!map.containsKey(item)) {
                        map[item] = globalIndex++
                    }
                }
            } catch (e: Exception) {
                // Tab not built yet, skip
            }
        }

        // Phase 2: Process any remaining tabs not in config (e.g. mod tabs)
        for ((tabId, tab) in tabById) {
            if (processedTabs.contains(tabId)) continue
            // Skip special tabs
            if (tabId == "minecraft:search" || tabId == "minecraft:hotbar" || tabId == "minecraft:inventory") continue
            try {
                for (stack in tab.displayStacks) {
                    val item = stack.item
                    if (!map.containsKey(item)) {
                        map[item] = globalIndex++
                    }
                }
            } catch (e: Exception) {
                // Tab not built yet, skip
            }
        }

        // Fallback: if no items found, use registry order
        if (map.isEmpty()) {
            logger.warn("[SimpleSorter] Creative tabs empty, using registry order as fallback")
            for (item in Registries.ITEM) {
                map[item] = globalIndex++
            }
        }

        logger.info("[SimpleSorter] Built creative tab sort index: {} items ({} pinned)", map.size, pinnedItems.size)
        return map
    }
}
