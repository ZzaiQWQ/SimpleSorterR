package simplesorter.mc.tweak

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Util

/**
 * Feature: Holding an item on the cursor and Shift+Double-Clicking another item 
 * instantly moves ALL identical items from that inventory to the opposite inventory.
 */
class LmbShiftDoubleClickTweak : MouseTweakModule {
    private var lastClickedSlot: Slot? = null
    private var lastClickTime: Long = 0

    override fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        if (slot == null || button != 0 || !isShift) return false

        val client = MinecraftClient.getInstance()
        val player = client.player
        val handler = player?.currentScreenHandler
        val interactionManager = client.interactionManager

        if (player == null || handler == null || interactionManager == null) return false

        val cursorStack = handler.cursorStack
        // We MUST be holding an item on the cursor to do this tweak
        if (cursorStack.isEmpty) return false

        val now = Util.getMeasuringTimeMs()
        val isDoubleClick = (slot == lastClickedSlot && now - lastClickTime < 250L)

        lastClickedSlot = slot
        lastClickTime = now

        if (isDoubleClick) {
            // Check if the clicked slot has the same item as the cursor
            if (!slot.stack.isEmpty && ItemStack.areItemsAndComponentsEqual(cursorStack, slot.stack)) {
                
                val targetInventory = slot.inventory
                
                // Find all slots in this same inventory (chest or player grid) that have the identical item
                for (s in handler.slots) {
                    if (s.inventory == targetInventory && !s.stack.isEmpty && ItemStack.areItemsAndComponentsEqual(cursorStack, s.stack)) {
                        // Fast transfer it!
                        interactionManager.clickSlot(
                            handler.syncId,
                            s.id,
                            0,
                            SlotActionType.QUICK_MOVE,
                            player
                        )
                    }
                }
                
                // Cancel the vanilla double click behavior (which would normally gather items to cursor)
                return true
            }
        }
        
        return false
    }
}
