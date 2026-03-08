package simplesorter.mc.tweak

import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot

/**
 * Base interface for all MouseTweaks modules.
 * This makes it incredibly easy to add/remove/update individual tweaks.
 */
interface MouseTweakModule {
    /**
     * Called when the mouse is initially clicked in the inventory.
     * @return true if the vanilla click should be cancelled.
     */
    fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        return false
    }

    /**
     * Called while the mouse is dragged across the screen.
     */
    fun onMouseDragged(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean) {}

    /**
     * Called when the mouse button is released.
     */
    fun onMouseReleased(button: Int) {}
    
    /**
     * Called when the scroll wheel is used.
     * @return true if the scroll event was handled and vanilla should ignore it.
     */
    fun onMouseScrolled(screen: HandledScreen<*>, slot: Slot?, amount: Double): Boolean {
        return false
    }
}
