package simplesorter.mc.tweak

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Util
import simplesorter.mc.KeyBindingHolder
import simplesorter.mc.LockManager

class DepositMatchingDoubleClickTweak : MouseTweakModule {
    private var lastClickedSlot: Slot? = null
    private var lastClickTime: Long = 0

    override fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        if (slot == null || button != 0 || slot.stack.isEmpty) return false
        if (!KeyBindingHolder.isDepositMatchingKeyHeld) return false

        val client = MinecraftClient.getInstance()
        val player = client.player
        val handler = player?.currentScreenHandler
        val interactionManager = client.interactionManager

        if (player == null || handler == null || interactionManager == null) return false
        if (handler is PlayerScreenHandler || !handler.cursorStack.isEmpty) return false

        val now = Util.getMeasuringTimeMs()
        val isDoubleClick = (slot == lastClickedSlot && now - lastClickTime < 250L)

        lastClickedSlot = slot
        lastClickTime = now

        if (!isDoubleClick) {
            return true
        }

        val containerStacks = handler.slots
            .asSequence()
            .filter { it.inventory != player.inventory && !it.stack.isEmpty }
            .map { it.stack.copy() }
            .toList()

        if (containerStacks.isEmpty()) {
            return true
        }

        val lockedPlayerSlots = LockManager.getPlayerLockedSlots()

        for (s in handler.slots) {
            if (s.inventory != player.inventory) continue
            if (s.index !in 9..35) continue
            if (lockedPlayerSlots.contains(s.index)) continue
            if (s.stack.isEmpty) continue

            val hasMatch = containerStacks.any { target ->
                ItemStack.areItemsAndComponentsEqual(s.stack, target)
            }
            if (!hasMatch) continue

            interactionManager.clickSlot(
                handler.syncId,
                s.id,
                0,
                SlotActionType.QUICK_MOVE,
                player
            )
        }

        return true
    }
}
