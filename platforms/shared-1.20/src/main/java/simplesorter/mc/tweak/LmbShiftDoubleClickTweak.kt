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
        if (cursorStack.isEmpty) return false

        val now = Util.getMeasuringTimeMs()
        val isDoubleClick = (slot == lastClickedSlot && now - lastClickTime < 250L)

        lastClickedSlot = slot
        lastClickTime = now

        if (isDoubleClick) {
            // 1.20.1: use canCombine which checks both item type and NBT
            if (!slot.stack.isEmpty && ItemStack.canCombine(cursorStack, slot.stack)) {
                
                val targetInventory = slot.inventory
                
                for (s in handler.slots) {
                    if (s.inventory == targetInventory && !s.stack.isEmpty && ItemStack.canCombine(cursorStack, s.stack)) {
                        interactionManager.clickSlot(
                            handler.syncId,
                            s.id,
                            0,
                            SlotActionType.QUICK_MOVE,
                            player
                        )
                    }
                }
                
                return true
            }
        }
        
        return false
    }
}
