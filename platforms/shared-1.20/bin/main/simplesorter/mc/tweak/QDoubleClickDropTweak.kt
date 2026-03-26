package simplesorter.mc.tweak

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Util
import simplesorter.mc.KeyBindingHolder

/**
 * Feature: Hold the configured batch drop key (default: CapsLock) and double-click 
 * any item slot to throw ALL identical items from that inventory container.
 */
class QDoubleClickDropTweak : MouseTweakModule {
    private var lastClickedSlot: Slot? = null
    private var lastClickTime: Long = 0

    override fun onMouseClicked(screen: HandledScreen<*>, slot: Slot?, button: Int, isShift: Boolean): Boolean {
        if (slot == null || button != 0) return false
        if (!KeyBindingHolder.isBatchDropKeyHeld) return false

        val client = MinecraftClient.getInstance()
        val player = client.player
        val handler = player?.currentScreenHandler
        val interactionManager = client.interactionManager
        if (player == null || handler == null || interactionManager == null) return false

        val now = Util.getMeasuringTimeMs()
        val isDoubleClick = (slot == lastClickedSlot && now - lastClickTime < 250L)

        lastClickedSlot = slot
        lastClickTime = now

        if (isDoubleClick && !slot.stack.isEmpty) {
            val matchStack = slot.stack

            System.out.println("[SimpleSorter] Batch drop! Matching: ${matchStack.count}x ${matchStack.item}")

            val cursorStack = handler.cursorStack
            if (!cursorStack.isEmpty && ItemStack.canCombine(matchStack, cursorStack)) {
                interactionManager.clickSlot(
                    handler.syncId,
                    -999,
                    1,
                    SlotActionType.THROW,
                    player
                )
            }

            val targetInventory = slot.inventory
            for (s in handler.slots) {
                if (s.inventory == targetInventory && !s.stack.isEmpty && ItemStack.canCombine(matchStack, s.stack)) {
                    System.out.println("[SimpleSorter] Throwing slot ${s.id}: ${s.stack.count}x ${s.stack.item}")
                    interactionManager.clickSlot(
                        handler.syncId,
                        s.id,
                        1,
                        SlotActionType.THROW,
                        player
                    )
                }
            }
        }

        return true
    }
}
