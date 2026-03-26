package simplesorter.mc

import simplesorter.sort.SlotSnapshot
import simplesorter.sort.Sorter
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registries
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.slot.SlotActionType
import org.slf4j.LoggerFactory

object InventoryScanner {
    private val logger = LoggerFactory.getLogger("simplesorter")

    // State for auto-repeat sorting
    private var sorting = false
    private var sortPassesRemaining = 0
    private const val MAX_PASSES = 10  // Safety limit

    /**
     * Called by Mixin when R is pressed. Starts the auto-sort process.
     */
    fun requestSort() {
        if (sorting) return  // Already sorting
        sorting = true
        sortPassesRemaining = MAX_PASSES
        logger.info("[SimpleSorter] Sort requested!")
    }

    /**
     * Called every tick. If sorting is active, does one sort pass.
     * Repeats until sorted or max passes reached.
     */
    fun tickSort() {
        if (!sorting) return

        val client = MinecraftClient.getInstance()
        val player = client.player
        val handler = player?.currentScreenHandler
        val interactionManager = client.interactionManager

        if (player == null || handler == null || interactionManager == null) {
            sorting = false
            return
        }

        // If inventory screen was closed, stop sorting
        if (client.currentScreen == null) {
            sorting = false
            return
        }

        // Safety: clear cursor first — place items back into the correct inventory
        val cursorStack = handler.cursorStack
        if (cursorStack != null && !cursorStack.isEmpty) {
            val isContainer = handler !is PlayerScreenHandler
            for (slot in handler.slots) {
                val isTargetSlot = if (isContainer) {
                    // Sorting container — put cursor items back into the container
                    slot.inventory != player.inventory && slot.stack.isEmpty
                } else {
                    // Sorting player inventory — put into main inventory
                    slot.inventory == player.inventory && slot.index in 9..35 && slot.stack.isEmpty
                }
                if (isTargetSlot) {
                    interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.PICKUP, player)
                    return  // Wait one tick for the cursor to clear
                }
            }
            // Can't clear cursor — abort
            sorting = false
            return
        }

        // Scan and sort
        val slotsToSort = if (handler is PlayerScreenHandler) {
            scanPlayerInventory(handler, player)
        } else {
            scanContainerSlots(handler, player)
        }

        if (slotsToSort.isEmpty() || slotsToSort.all { it.itemId == "minecraft:air" }) {
            sorting = false
            return
        }

        val sorter = Sorter()
        val clickCount = sorter.doSort(slotsToSort)

        if (clickCount > 0) {
            InventoryActions.executeAllClicks()
            sortPassesRemaining--
            if (sortPassesRemaining <= 0) {
                logger.info("[SimpleSorter] Max passes reached, stopping.")
                sorting = false
            }
            // Otherwise: next tick will do another pass
        } else {
            // 0 clicks = fully sorted!
            logger.info("[SimpleSorter] Fully sorted!")
            sorting = false
        }
    }

    private fun scanPlayerInventory(handler: net.minecraft.screen.ScreenHandler, player: net.minecraft.entity.player.PlayerEntity): List<SlotSnapshot> {
        val snapshots = mutableListOf<SlotSnapshot>()
        val locked = LockManager.getPlayerLockedSlots()
        for (slot in handler.slots) {
            if (slot.inventory == player.inventory && slot.index in 9..35) {
                if (!locked.contains(slot.index)) {
                    snapshots.add(makeSnapshot(slot))
                }
            }
        }
        return snapshots
    }

    private fun scanContainerSlots(handler: net.minecraft.screen.ScreenHandler, player: net.minecraft.entity.player.PlayerEntity): List<SlotSnapshot> {
        val snapshots = mutableListOf<SlotSnapshot>()
        val locked = LockManager.getContainerLockedSlots(handler.syncId)
        for (slot in handler.slots) {
            if (slot.inventory != player.inventory) {
                if (!locked.contains(slot.id)) {
                    snapshots.add(makeSnapshot(slot))
                }
            }
        }
        return snapshots
    }

    private fun makeSnapshot(slot: net.minecraft.screen.slot.Slot): SlotSnapshot {
        val stack = slot.stack
        val itemId = if (stack.isEmpty) "minecraft:air" else Registries.ITEM.getId(stack.item).toString()
        // 1.20.1: use NBT instead of components
        val mergeKey = if (stack.isEmpty) "minecraft:air" else "$itemId|${stack.nbt}"
        
        val categoryId = getCategoryId(stack)
        var catIndex = simplesorter.mc.config.SimpleSorterConfig.categoryOrder.indexOf(categoryId)
        if (catIndex == -1) catIndex = 999 // Unknown categories go to the end
        
        return SlotSnapshot(
            slotId = slot.id,
            itemId = itemId,
            mergeKey = mergeKey,
            count = stack.count,
            maxCount = if (stack.isEmpty) 64 else stack.maxCount,
            categoryIndex = catIndex
        )
    }

    private fun getCategoryId(stack: net.minecraft.item.ItemStack): String {
        if (stack.isEmpty) return "minecraft:air"
        val item = stack.item
        val className = item.javaClass.simpleName

        // Combat
        if (stack.isIn(net.minecraft.registry.tag.ItemTags.SWORDS) || 
            stack.isIn(net.minecraft.registry.tag.ItemTags.AXES)) {
            return "minecraft:combat"
        }
        if (className.contains("Armor") || className.contains("Bow") || 
            className.contains("Crossbow") || className.contains("Trident") || 
            className.contains("Shield")) {
            return "minecraft:combat"
        }
        
        // Tools & Utilities
        if (stack.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES) || 
            stack.isIn(net.minecraft.registry.tag.ItemTags.SHOVELS) || 
            stack.isIn(net.minecraft.registry.tag.ItemTags.HOES)) {
            return "minecraft:tools_and_utilities"
        }
        if (className.contains("FishingRod") || className.contains("Shears") || 
            className.contains("FlintAndSteel") || className.contains("Bucket")) {
            return "minecraft:tools_and_utilities"
        }
        
        // Food & Drinks — 1.20.1: use item.foodComponent instead of DataComponentTypes.FOOD
        if (item.foodComponent != null) {
            return "minecraft:food_and_drinks"
        }
        if (className.contains("Potion") || className.contains("HoneyBottle")) {
            return "minecraft:food_and_drinks"
        }
        
        // Blocks
        if (item is net.minecraft.item.BlockItem) {
            val blockName = item.block.javaClass.simpleName
            if (blockName.contains("RedstoneWire") || 
                blockName.contains("RedstoneTorch") || 
                blockName.contains("GateBlock") || 
                blockName.contains("Dispenser") || 
                blockName.contains("Piston") || 
                blockName.contains("Observer")) {
                return "minecraft:redstone_blocks"
            }
            if (stack.isIn(net.minecraft.registry.tag.ItemTags.LOGS) || 
                stack.isIn(net.minecraft.registry.tag.ItemTags.SAND) || 
                stack.isIn(net.minecraft.registry.tag.ItemTags.LEAVES) || 
                stack.isIn(net.minecraft.registry.tag.ItemTags.SAPLINGS) || 
                blockName.contains("Plant")) {
                return "minecraft:natural_blocks"
            }
            return "minecraft:building_blocks"
        }
        
        // Default
        return "minecraft:ingredients"
    }
}
