package simplesorter.mc.tweak

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.slot.Slot
import simplesorter.mc.KeyBindingHolder
import simplesorter.mc.LockManager
import simplesorter.mc.config.SimpleSorterConfig

class AltClickLockTweak : MouseTweakModule {
    private var dragLockState: Boolean? = null
    private val draggedSlots = mutableSetOf<Int>()
    private var playerLocksChanged = false

    override fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        if (slot == null || button != 0) return false
        if (!KeyBindingHolder.isLockKeyHeld) return false

        val client = MinecraftClient.getInstance()
        val playerInventory = client.player?.inventory ?: return false
        val handler = client.player?.currentScreenHandler ?: return false

        val shouldLock = !isLocked(slot, handler.syncId, playerInventory)

        dragLockState = shouldLock
        draggedSlots.clear()
        playerLocksChanged = false

        applyLock(slot, handler.syncId, playerInventory, shouldLock)
        draggedSlots.add(slot.id)
        return true
    }

    override fun onMouseDragged(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean) {
        if (slot == null || button != 0) return
        if (!KeyBindingHolder.isLockKeyHeld) return

        val shouldLock = dragLockState ?: return
        if (!draggedSlots.add(slot.id)) return

        val client = MinecraftClient.getInstance()
        val playerInventory = client.player?.inventory ?: return
        val handler = client.player?.currentScreenHandler ?: return

        applyLock(slot, handler.syncId, playerInventory, shouldLock)
    }

    override fun onMouseReleased(button: Int) {
        if (button != 0) return

        if (playerLocksChanged) {
            SimpleSorterConfig.save()
        }

        dragLockState = null
        draggedSlots.clear()
        playerLocksChanged = false
    }

    private fun isLocked(
        slot: Slot,
        currentSyncId: Int,
        playerInventory: net.minecraft.entity.player.PlayerInventory
    ): Boolean {
        return if (slot.inventory == playerInventory) {
            LockManager.getPlayerLockedSlots().contains(slot.index)
        } else {
            LockManager.getContainerLockedSlots(currentSyncId).contains(slot.id)
        }
    }

    private fun applyLock(
        slot: Slot,
        currentSyncId: Int,
        playerInventory: net.minecraft.entity.player.PlayerInventory,
        locked: Boolean
    ) {
        if (isLocked(slot, currentSyncId, playerInventory) == locked) return

        if (slot.inventory == playerInventory) {
            val lockedSlots = LockManager.getPlayerLockedSlots()
            if (locked) {
                lockedSlots.add(slot.index)
            } else {
                lockedSlots.remove(slot.index)
            }
            playerLocksChanged = true
        } else {
            LockManager.toggleContainerSlotLock(currentSyncId, slot.id)
        }
    }
}
