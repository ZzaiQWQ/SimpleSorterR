package simplesorter.mc

import simplesorter.action.ClickAction
import simplesorter.action.ClickQueue
import net.minecraft.client.MinecraftClient
import net.minecraft.item.BundleItem
import net.minecraft.screen.slot.SlotActionType
import org.slf4j.LoggerFactory

object InventoryActions {
    private val logger = LoggerFactory.getLogger("simplesorter")
    
    /**
     * Execute ALL queued clicks instantly in one burst.
     * This performs the entire sort in a single tick for instant results.
     * 
     * Bundle fix: When cursor or target slot contains a Bundle,
     * use RIGHT_CLICK (button=1) instead of LEFT_CLICK (button=0)
     * to prevent items from being stuffed into the bundle.
     * Reference: Inventive Inventory - InteractionHandler.java
     */
    fun executeAllClicks() {
        val client = MinecraftClient.getInstance()
        val player = client.player
        val interactionManager = client.interactionManager
        val currentScreenHandler = player?.currentScreenHandler

        if (player == null || interactionManager == null || currentScreenHandler == null) return

        var count = 0
        while (ClickQueue.hasNext()) {
            val click = ClickQueue.dequeue() ?: break
            
            val actionType = SlotActionType.entries[click.actionType]
            
            // Bundle fix: detect if cursor or slot is a Bundle
            var button = click.button
            if (actionType == SlotActionType.PICKUP && button == 0) {
                val cursorStack = currentScreenHandler.cursorStack
                val slotStack = currentScreenHandler.getSlot(click.slotId).stack
                
                val cursorIsBundle = !cursorStack.isEmpty && cursorStack.item is BundleItem
                val slotIsBundle = !slotStack.isEmpty && slotStack.item is BundleItem
                val cursorHasItem = !cursorStack.isEmpty
                val slotHasItem = !slotStack.isEmpty
                
                // 手上有物品 + 点收纳袋 → 右键安全交换
                // 手上是收纳袋 + 点有物品的格子 → 右键安全交换
                if ((cursorHasItem && slotIsBundle) || (cursorIsBundle && slotHasItem)) {
                    button = 1  // RIGHT_CLICK → safe swap
                }
            }
            
            interactionManager.clickSlot(
                currentScreenHandler.syncId,
                click.slotId,
                button,
                actionType,
                player
            )
            count++
        }
        
        if (count > 0) {
            logger.info("[SimpleSorter] Executed $count clicks instantly.")
        }
    }
}

