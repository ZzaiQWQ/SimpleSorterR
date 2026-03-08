package simplesorter.mc.tweak

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

/**
 * Feature: Shift+Left-Click dragging across slots to quick-move them.
 */
class LmbShiftDragTweak : MouseTweakModule {
    private var isDraggingShiftClick = false
    private val draggedSlots = mutableSetOf<Int>()

    override fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        if (slot == null) return false

        // Start Shift-Click dragging ONLY if it's Left Click (0) and Shift is held
        if (button == 0 && isShift) {
            val client = MinecraftClient.getInstance()
            // Only start if we are NOT holding any item on the cursor
            if (client.player?.currentScreenHandler?.cursorStack?.isEmpty == true && !slot.stack.isEmpty) {
                isDraggingShiftClick = true
                draggedSlots.clear()
                draggedSlots.add(slot.id)
                return false // Let vanilla do the first click
            }
        }
        return false
    }

    override fun onMouseDragged(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean) {
        if (slot == null) return
        
        if (isDraggingShiftClick && button == 0 && isShift) {
            if (!draggedSlots.contains(slot.id) && !slot.stack.isEmpty) {
                val client = MinecraftClient.getInstance()
                val player = client.player
                val interactionManager = client.interactionManager
                val handler = player?.currentScreenHandler

                if (player != null && interactionManager != null && handler != null) {
                    interactionManager.clickSlot(
                        handler.syncId,
                        slot.id,
                        0, // left click
                        SlotActionType.QUICK_MOVE, // Shift-click action
                        player
                    )
                    draggedSlots.add(slot.id)
                }
            }
        }
    }

    override fun onMouseReleased(button: Int) {
        if (button == 0) {
            isDraggingShiftClick = false
            draggedSlots.clear()
        }
    }
}
