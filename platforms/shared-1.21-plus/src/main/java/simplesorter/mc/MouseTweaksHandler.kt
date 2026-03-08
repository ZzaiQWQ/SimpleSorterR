package simplesorter.mc

import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot
import simplesorter.mc.tweak.LmbShiftDragTweak
import simplesorter.mc.tweak.LmbShiftDoubleClickTweak
import simplesorter.mc.tweak.LmbSpaceDoubleClickTweak
import simplesorter.mc.tweak.MouseTweakModule

/**
 * A perfectly clean dispatcher. It holds all the loaded tweaks and forwards events to them.
 * You never need to touch this again to add new features!
 */
object MouseTweaksHandler {
    
    // Register all tweaks here
    private val tweaks: List<MouseTweakModule> = listOf(
        LmbShiftDragTweak(),
        LmbShiftDoubleClickTweak(),
        LmbSpaceDoubleClickTweak()
        // More will be added here!
    )

    @JvmStatic
    fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        var cancel = false
        for (tweak in tweaks) {
            if (tweak.onMouseClicked(screen, slot, button, isShift)) {
                cancel = true
            }
        }
        return cancel
    }

    @JvmStatic
    fun onMouseDragged(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean) {
        for (tweak in tweaks) {
            tweak.onMouseDragged(screen, slot, button, isShift)
        }
    }

    @JvmStatic
    fun onMouseReleased(button: Int) {
        for (tweak in tweaks) {
            tweak.onMouseReleased(button)
        }
    }
    
    @JvmStatic
    fun onMouseScrolled(screen: HandledScreen<*>, slot: Slot?, amount: Double): Boolean {
        var cancel = false
        for (tweak in tweaks) {
            if (tweak.onMouseScrolled(screen, slot, amount)) {
                cancel = true
            }
        }
        return cancel
    }
}
