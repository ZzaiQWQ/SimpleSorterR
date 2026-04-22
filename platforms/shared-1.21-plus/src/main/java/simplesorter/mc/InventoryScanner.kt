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
        // Hot-reload config if file changed
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
        // 动态黑名单：检查容器类名是否在配置的 blockedContainers 中
        val slotsToSort = if (handler is PlayerScreenHandler) {
            scanPlayerInventory(handler, player)
        } else {
            val handlerClassName = handler.javaClass.simpleName
            val isBlocked = simplesorter.mc.config.SimpleSorterConfig.blockedContainers.any { blocked ->
                handlerClassName.contains(blocked, ignoreCase = true)
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
        val mergeKey = if (stack.isEmpty) "minecraft:air" else "$itemId|${stack.components}"
        
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
