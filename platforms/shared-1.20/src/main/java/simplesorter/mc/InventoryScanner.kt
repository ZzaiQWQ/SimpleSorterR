package simplesorter.mc

import simplesorter.sort.SlotSnapshot
import simplesorter.sort.Sorter
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
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
     * Called from HandledScreenMixin.keyPressed when the sort key is pressed.
     */
    fun requestSort() {
        if (sorting) return  // Already sorting
        simplesorter.mc.config.SimpleSorterConfig.reloadIfChanged()
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

        // Only sort when a HandledScreen (inventory/container GUI) is open
        if (client.currentScreen == null || client.currentScreen !is HandledScreen<*>) {
            sorting = false
            return
        }

        // Safety: clear cursor first — try merge into same item stack, then empty slot
        val cursorStack = handler.cursorStack
        if (cursorStack != null && !cursorStack.isEmpty) {
            val isContainer = handler !is PlayerScreenHandler
            
            // Step 1: Try to merge into an existing stack of the same item
            for (slot in handler.slots) {
                val isTargetSide = if (isContainer) slot.inventory != player.inventory else (slot.inventory == player.inventory && slot.index in 9..35)
                if (isTargetSide && !slot.stack.isEmpty && ItemStack.canCombine(cursorStack, slot.stack) && slot.stack.count < slot.stack.maxCount) {
                    interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.PICKUP, player)
                    return  // Wait one tick
                }
            }
            
            // Step 2: Try empty slot on the same side
            for (slot in handler.slots) {
                val isTargetSide = if (isContainer) slot.inventory != player.inventory else (slot.inventory == player.inventory && slot.index in 9..35)
                if (isTargetSide && slot.stack.isEmpty) {
                    interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.PICKUP, player)
                    return
                }
            }
            
            // Step 3: Try any empty slot as last resort
            for (slot in handler.slots) {
                if (slot.stack.isEmpty) {
                    interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.PICKUP, player)
                    return
                }
            }
            
            // Can't clear cursor — abort
            sorting = false
            return
        }

        // Scan and sort
        // 动态黑名单：检查容器类名是否在配置的 blockedContainers 中
        val slotsToSort = if (handler is PlayerScreenHandler) {
            scanPlayerInventory(handler, player)
        } else {
            val handlerClassName = handler.javaClass.simpleName
            val isBlocked = simplesorter.mc.config.SimpleSorterConfig.blockedContainers.any { blocked ->
                handlerClassName.equals(blocked, ignoreCase = true)
            }
            if (isBlocked) {
                sorting = false
                return
            }
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
        val mergeKey = if (stack.isEmpty) "minecraft:air" else "$itemId|${stack.nbt}"
        
        val sortIndex = if (stack.isEmpty) Int.MAX_VALUE
                        else CreativeTabSorter.getSortIndex(stack.item)
        
        return SlotSnapshot(
            slotId = slot.id,
            itemId = itemId,
            mergeKey = mergeKey,
            count = stack.count,
            maxCount = if (stack.isEmpty) 64 else stack.maxCount,
            categoryIndex = sortIndex
        )
    }
}
