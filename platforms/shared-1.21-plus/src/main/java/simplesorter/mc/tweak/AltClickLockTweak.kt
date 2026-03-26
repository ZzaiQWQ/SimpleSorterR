package simplesorter.mc.tweak

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot
import simplesorter.mc.KeyBindingHolder
import simplesorter.mc.LockManager

class AltClickLockTweak : MouseTweakModule {
    override fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        if (slot == null || button != 0) return false
        if (!KeyBindingHolder.isLockKeyHeld) return false

        val client = MinecraftClient.getInstance()
        val playerInventory = client.player?.inventory ?: return false
        val handler = client.player?.currentScreenHandler ?: return false

        if (slot.inventory == playerInventory) {
            LockManager.togglePlayerSlotLock(slot.index)
        } else {
            LockManager.toggleContainerSlotLock(handler.syncId, slot.id)
        }
        return true
    }
}
