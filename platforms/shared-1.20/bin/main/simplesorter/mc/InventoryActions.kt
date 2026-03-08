package simplesorter.mc

import simplesorter.action.ClickAction
import simplesorter.action.ClickQueue
import net.minecraft.client.MinecraftClient
import net.minecraft.screen.slot.SlotActionType
import org.slf4j.LoggerFactory

object InventoryActions {
    private val logger = LoggerFactory.getLogger("simplesorter")
    
    /**
     * Execute ALL queued clicks instantly in one burst.
     * This performs the entire sort in a single tick for instant results.
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
            
            // 1.20.1: use values() instead of entries
            val actionType = SlotActionType.values()[click.actionType]
            
            interactionManager.clickSlot(
                currentScreenHandler.syncId,
                click.slotId,
                click.button,
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
