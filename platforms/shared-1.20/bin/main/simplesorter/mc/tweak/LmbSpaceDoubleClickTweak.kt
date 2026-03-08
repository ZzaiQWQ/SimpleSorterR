package simplesorter.mc.tweak

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Util
import org.lwjgl.glfw.GLFW

/**
 * Feature: Holding Space and Double-Clicking any slot in an inventory
 * will instantly quick-move ALL items from that inventory to the opposite inventory.
 */
class LmbSpaceDoubleClickTweak : MouseTweakModule {
    private var lastClickedSlot: Slot? = null
    private var lastClickTime: Long = 0

    override fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        if (slot == null || button != 0) return false

        val client = MinecraftClient.getInstance()
        val player = client.player
        val handler = player?.currentScreenHandler
        val interactionManager = client.interactionManager

        if (player == null || handler == null || interactionManager == null) return false

        val isSpaceDown = GLFW.glfwGetKey(client.window.handle, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS
        if (!isSpaceDown) return false

        val now = Util.getMeasuringTimeMs()
        val isDoubleClick = (slot == lastClickedSlot && now - lastClickTime < 250L)

        lastClickedSlot = slot
        lastClickTime = now

        if (isDoubleClick) {
            val targetInventory = slot.inventory
            
            for (s in handler.slots) {
                if (s.inventory == targetInventory && !s.stack.isEmpty) {
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
        
        return true
    }
}
