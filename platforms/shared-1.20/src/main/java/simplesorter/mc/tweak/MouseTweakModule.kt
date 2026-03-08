package simplesorter.mc.tweak

import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot

/**
 * Base interface for all MouseTweaks modules.
 */
interface MouseTweakModule {
    fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        return false
    }

    fun onMouseDragged(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean) {}

    fun onMouseReleased(button: Int) {}
    
    fun onMouseScrolled(screen: HandledScreen<*>, slot: Slot?, amount: Double): Boolean {
        return false
    }
}
