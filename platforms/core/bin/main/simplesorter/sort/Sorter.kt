package simplesorter.sort

import simplesorter.action.ClickAction
import simplesorter.action.ClickQueue

data class SlotSnapshot(
    val slotId: Int, 
    val itemId: String, 
    val mergeKey: String, 
    val count: Int, 
    val maxCount: Int,
    val categoryIndex: Int = Int.MAX_VALUE
)

class Sorter {

    fun doSort(slots: List<SlotSnapshot>): Int {
        val clicks = mutableListOf<ClickAction>()
        val current = slots.map { it.copy() }.toMutableList()

        // Phase 1: Merge partial stacks
        mergeStacks(current, clicks)

        // Phase 2: Sort items into correct positions
        sortPositions(current, clicks)

        if (clicks.isNotEmpty()) {
            ClickQueue.enqueue(clicks)
        }
        return clicks.size
    }

    private fun mergeStacks(current: MutableList<SlotSnapshot>, clicks: MutableList<ClickAction>) {
        val groups = mutableMapOf<String, MutableList<Int>>()
        for (i in current.indices) {
            val s = current[i]
            if (s.itemId != "minecraft:air" && s.count > 0 && s.maxCount > 1) {
                groups.getOrPut(s.mergeKey) { mutableListOf() }.add(i)
            }
        }

        for ((_, indices) in groups) {
            if (indices.size <= 1) continue
            val maxStack = current[indices[0]].maxCount
            if (indices.all { current[it].count >= maxStack }) continue

            var f = 0; var b = indices.size - 1
            while (f < b) {
                val ti = indices[f]; val si = indices[b]
                val tc = current[ti].count; val sc = current[si].count
                if (tc >= maxStack) { f++; continue }
                if (sc <= 0) { b--; continue }
                val space = maxStack - tc
                if (sc <= space) {
                    clicks.add(ClickAction(current[si].slotId, 0, 0))
                    clicks.add(ClickAction(current[ti].slotId, 0, 0))
                    current[ti] = current[ti].copy(count = tc + sc)
                    current[si] = current[si].copy(itemId = "minecraft:air", count = 0)
                    b--
                } else {
                    clicks.add(ClickAction(current[si].slotId, 0, 0))
                    clicks.add(ClickAction(current[ti].slotId, 0, 0))
                    clicks.add(ClickAction(current[si].slotId, 0, 0))
                    current[ti] = current[ti].copy(count = maxStack)
                    current[si] = current[si].copy(count = sc - space)
                    f++
                }
            }
        }
    }

    private fun sortPositions(current: MutableList<SlotSnapshot>, clicks: MutableList<ClickAction>) {
        // Build the desired sorted order
        val target = current.sortedWith(
            compareBy<SlotSnapshot> {
                if (it.itemId == "minecraft:air" || it.count <= 0) Int.MAX_VALUE
                else it.categoryIndex
            }
                .thenBy { it.itemId }
                .thenBy { it.mergeKey }
                .thenByDescending { it.count }
        )

        val used = BooleanArray(current.size)

        for (i in target.indices) {
            if (target[i].itemId == "minecraft:air" || target[i].count <= 0) continue
            if (current[i].mergeKey == target[i].mergeKey && current[i].count == target[i].count) {
                used[i] = true
            }
        }

        for (i in target.indices) {
            if (used[i]) continue
            val want = target[i]
            if (want.itemId == "minecraft:air" || want.count <= 0) break

            val j = (0 until current.size).firstOrNull { k ->
                !used[k] && k != i &&
                current[k].mergeKey == want.mergeKey && current[k].count == want.count
            } ?: continue

            val slotA = current[j].slotId  // source
            val slotB = current[i].slotId  // destination

            if (current[i].itemId == "minecraft:air" || current[i].count <= 0) {
                clicks.add(ClickAction(slotA, 0, 0))
                clicks.add(ClickAction(slotB, 0, 0))
            } else {
                clicks.add(ClickAction(slotA, 0, 0))
                clicks.add(ClickAction(slotB, 0, 0))
                clicks.add(ClickAction(slotA, 0, 0))
            }

            // Update tracking
            val tmp = current[i]
            current[i] = current[j].copy(slotId = current[i].slotId)
            current[j] = tmp.copy(slotId = current[j].slotId)
            used[i] = true
            // Don't mark j as used — the item at j might need to be moved too
        }
    }
}
