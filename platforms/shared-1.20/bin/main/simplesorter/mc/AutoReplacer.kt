package simplesorter.mc

import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import simplesorter.mc.config.SimpleSorterConfig

/**
 * Auto-replace tools when broken & auto-refill stackable items.
 * Uses clickSlot() to send server-side packets (no ghost items).
 *
 * playerScreenHandler slot mapping:
 *   0: crafting output, 1-4: crafting grid, 5-8: armor
 *   9-35: main inventory, 36-44: hotbar (0-8), 45: offhand
 */
object AutoReplacer {
    private var prevMainHandItem: ItemStack = ItemStack.EMPTY
    private var prevMainHandSlot: Int = -1
    private var cooldown: Int = 0

    @JvmStatic
    fun tick() {
        if (cooldown > 0) { cooldown--; return }

        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        if (client.currentScreen != null) {
            prevMainHandItem = ItemStack.EMPTY
            prevMainHandSlot = -1
            return
        }
        val interactionManager = client.interactionManager ?: return
        val inventory = player.getInventory()
        val selectedSlot: Int = getSelectedSlot(inventory)
        val currentItem = inventory.getStack(selectedSlot)
        val handler = player.playerScreenHandler
        val syncId = handler.syncId

        // ─── Tool/Weapon replacement: detect item breaking ───
        if (SimpleSorterConfig.autoReplaceSameItem || SimpleSorterConfig.autoReplaceSameType) {
            if (selectedSlot == prevMainHandSlot
                && !prevMainHandItem.isEmpty
                && currentItem.isEmpty
                && prevMainHandItem.maxDamage > 0) {
                val replacementSlot = findReplacement(inventory, prevMainHandItem)
                if (replacementSlot != -1) {
                    val hotbarMenuSlot = 36 + selectedSlot
                    interactionManager.clickSlot(syncId, replacementSlot, 0, SlotActionType.PICKUP, player)
                    interactionManager.clickSlot(syncId, hotbarMenuSlot, 0, SlotActionType.PICKUP, player)
                    cooldown = 3
                }
            }
        }

        // ─── Stackable item refill ───
        if (SimpleSorterConfig.autoRefillStack) {
            if (!currentItem.isEmpty && currentItem.isStackable) {
                val maxStack = currentItem.maxCount
                val threshold = if (maxStack >= 64) {
                    SimpleSorterConfig.refillThreshold
                } else {
                    maxStack / 2
                }

                if (currentItem.count <= threshold && currentItem.count < maxStack) {
                    val sourceSlot = findRefillSource(inventory, currentItem)
                    if (sourceSlot != -1) {
                        val hotbarMenuSlot = 36 + selectedSlot
                        interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player)
                        interactionManager.clickSlot(syncId, hotbarMenuSlot, 0, SlotActionType.PICKUP, player)
                        if (!handler.cursorStack.isEmpty) {
                            interactionManager.clickSlot(syncId, sourceSlot, 0, SlotActionType.PICKUP, player)
                        }
                        cooldown = 3
                    }
                }
            }
        }

        prevMainHandItem = if (currentItem.isEmpty) ItemStack.EMPTY else currentItem.copy()
        prevMainHandSlot = selectedSlot
    }

    private fun findReplacement(inventory: net.minecraft.entity.player.PlayerInventory, brokenItem: ItemStack): Int {
        if (SimpleSorterConfig.autoReplaceSameItem) {
            for (i in 9..35) {
                val candidate = inventory.getStack(i)
                if (!candidate.isEmpty && candidate.item == brokenItem.item) {
                    return i
                }
            }
        }
        if (SimpleSorterConfig.autoReplaceSameType) {
            val brokenType = getToolType(brokenItem)
            if (brokenType != null) {
                for (i in 9..35) {
                    val candidate = inventory.getStack(i)
                    if (!candidate.isEmpty && candidate.maxDamage > 0) {
                        if (getToolType(candidate) == brokenType) {
                            return i
                        }
                    }
                }
            }
        }
        return -1
    }

    private fun findRefillSource(inventory: net.minecraft.entity.player.PlayerInventory, targetItem: ItemStack): Int {
        for (i in 9..35) {
            val candidate = inventory.getStack(i)
            // 1.20.1: use canCombine (checks item + nbt equality)
            if (!candidate.isEmpty && ItemStack.canCombine(targetItem, candidate)) {
                return i
            }
        }
        return -1
    }

    private fun getToolType(stack: ItemStack): String? {
        val id = stack.item.toString().lowercase()
        return when {
            id.contains("pickaxe") -> "pickaxe"
            id.contains("axe") && !id.contains("pickaxe") -> "axe"
            id.contains("sword") -> "sword"
            id.contains("shovel") -> "shovel"
            id.contains("hoe") -> "hoe"
            id.contains("crossbow") -> "crossbow"
            id.contains("bow") && !id.contains("bowl") -> "bow"
            id.contains("trident") -> "trident"
            id.contains("shield") -> "shield"
            id.contains("shears") -> "shears"
            id.contains("fishing_rod") -> "fishing_rod"
            id.contains("flint_and_steel") -> "flint_and_steel"
            else -> null
        }
    }

    private var selectedSlotField: java.lang.reflect.Field? = null

    private fun getSelectedSlot(inventory: net.minecraft.entity.player.PlayerInventory): Int {
        if (selectedSlotField == null) {
            for (name in listOf("selectedSlot", "field_7536", "f_36071_")) {
                try {
                    val f = inventory.javaClass.getDeclaredField(name)
                    f.isAccessible = true
                    selectedSlotField = f
                    break
                } catch (_: NoSuchFieldException) {}
            }
            if (selectedSlotField == null) {
                for (f in inventory.javaClass.declaredFields) {
                    if (f.type == Int::class.javaPrimitiveType && !java.lang.reflect.Modifier.isStatic(f.modifiers)) {
                        f.isAccessible = true
                        val v = f.getInt(inventory)
                        if (v in 0..8) {
                            selectedSlotField = f
                            break
                        }
                    }
                }
            }
        }
        return try {
            selectedSlotField?.getInt(inventory) ?: 0
        } catch (_: Exception) { 0 }
    }
}
