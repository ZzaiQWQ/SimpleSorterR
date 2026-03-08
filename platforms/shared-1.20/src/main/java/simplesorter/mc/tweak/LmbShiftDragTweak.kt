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

        if (button == 0 && isShift) {
            val client = MinecraftClient.getInstance()
            if (client.player?.currentScreenHandler?.cursorStack?.isEmpty == true && !slot.stack.isEmpty) {
                isDraggingShiftClick = true
                draggedSlots.clear()
                draggedSlots.add(slot.id)
                return false
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
                        0,
                        SlotActionType.QUICK_MOVE,
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
