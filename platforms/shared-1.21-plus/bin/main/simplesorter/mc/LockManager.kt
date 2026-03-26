package simplesorter.mc

import net.minecraft.client.MinecraftClient
import simplesorter.mc.config.SimpleSorterConfig

object LockManager {
    // Session-based container locked slots (resets on GUI close / new syncId)
    private val containerLockedSlots = mutableSetOf<Int>()
    private var lastSyncId = -1

    // Current player+world profile ID including UUID to prevent player A locking player B's items
    private fun getProfileId(): String {
        val client = MinecraftClient.getInstance()
        val playerUUID = client.player?.uuidAsString ?: "unknown-player"
        val serverInfo = client.currentServerEntry
        val id = if (serverInfo != null) {
            "${playerUUID}_mp_${serverInfo.address}"
        } else {
            val server = client.server
            if (server != null) {
                // Use the save folder path which is unique per world save
                val savePath = server.getSavePath(net.minecraft.util.WorldSavePath.ROOT)
                val saveFolderName = savePath.parent?.fileName?.toString() ?: server.saveProperties.levelName
                "${playerUUID}_sp_${saveFolderName}"
            } else {
                "${playerUUID}_unknown-world"
            }
        }
        System.out.println("[SimpleSorter] Profile ID: $id")
        return id
    }

    fun getPlayerLockedSlots(): MutableSet<Int> {
        val id = getProfileId()
        return SimpleSorterConfig.worldLockedSlots.getOrPut(id) { mutableSetOf() }
    }

    fun togglePlayerSlotLock(slotIndex: Int) {
        val set = getPlayerLockedSlots()
        if (set.contains(slotIndex)) {
            set.remove(slotIndex)
        } else {
            set.add(slotIndex)
        }
        SimpleSorterConfig.save()
    }

    fun getContainerLockedSlots(currentSyncId: Int): Set<Int> {
        if (currentSyncId != lastSyncId) {
            containerLockedSlots.clear()
            lastSyncId = currentSyncId
        }
        return containerLockedSlots
    }

    fun toggleContainerSlotLock(currentSyncId: Int, slotId: Int) {
        if (currentSyncId != lastSyncId) {
            containerLockedSlots.clear()
            lastSyncId = currentSyncId
        }
        if (containerLockedSlots.contains(slotId)) {
            containerLockedSlots.remove(slotId)
        } else {
            containerLockedSlots.add(slotId)
        }
    }
}
