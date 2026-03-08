package simplesorter.action

/**
 * Represents a single click action on a specific slot.
 */
data class ClickAction(
    val slotId: Int,
    val button: Int, // 0 for Left, 1 for Right, etc.
    val actionType: Int // Mapping to SlotActionType
)

/**
 * Holds a queue of impending clicks to be sent to the server.
 */
object ClickQueue {
    private val queue = mutableListOf<ClickAction>()

    fun enqueue(action: ClickAction) {
        queue.add(action)
    }

    fun enqueue(actions: List<ClickAction>) {
        queue.addAll(actions)
    }

    fun hasNext(): Boolean = queue.isNotEmpty()

    fun dequeue(): ClickAction? = if (queue.isNotEmpty()) queue.removeAt(0) else null

    fun clear() {
        queue.clear()
    }
}
