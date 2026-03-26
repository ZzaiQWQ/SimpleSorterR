package simplesorter.mc.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

object SimpleSorterConfig {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val CONFIG_FILE = FabricLoader.getInstance().configDir.resolve("simplesorter.json")

    // The default hotkey to sort the inventory
    var sortKeyRaw: String = "key.keyboard.r"
    
    var requireZForConfig: Boolean = true // Checkbox: "Require Z"

    // Auto-replace options
    var autoReplaceSameItem: Boolean = true
    var autoReplaceSameType: Boolean = true
    var autoRefillStack: Boolean = true
    var refillThreshold: Int = 20

    // Persisted specific-profile locked slots
    var worldLockedSlots: MutableMap<String, MutableSet<Int>> = mutableMapOf()

    // Default category order, matching standard Creative Mode Tabs
    var categoryOrder: MutableList<String> = mutableListOf(
        "minecraft:tools_and_utilities",
        "minecraft:combat",
        "minecraft:building_blocks",
        "minecraft:natural_blocks",
        "minecraft:functional_blocks",
        "minecraft:redstone_blocks",
        "minecraft:food_and_drinks",
        "minecraft:ingredients",
        "minecraft:spawn_eggs"
    )

    init {
        load()
    }

    fun load() {
        if (!Files.exists(CONFIG_FILE)) {
            save()
            return
        }
        try {
            val jsonStr = Files.readString(CONFIG_FILE)
            val json = GSON.fromJson(jsonStr, JsonObject::class.java)

            if (json.has("requireZForConfig")) requireZForConfig = json.get("requireZForConfig").asBoolean
            if (json.has("autoReplaceSameItem")) autoReplaceSameItem = json.get("autoReplaceSameItem").asBoolean
            if (json.has("autoReplaceSameType")) autoReplaceSameType = json.get("autoReplaceSameType").asBoolean
            if (json.has("autoRefillStack")) autoRefillStack = json.get("autoRefillStack").asBoolean
            if (json.has("refillThreshold")) refillThreshold = json.get("refillThreshold").asInt
            
            if (json.has("worldLockedSlots")) {
                val mapObj = json.getAsJsonObject("worldLockedSlots")
                for (entry in mapObj.entrySet()) {
                    val list = mutableSetOf<Int>()
                    for (e in entry.value.asJsonArray) {
                        list.add(e.asInt)
                    }
                    worldLockedSlots[entry.key] = list
                }
            }
            
            if (json.has("categoryOrder")) {
                val array = json.getAsJsonArray("categoryOrder")
                val loadedList = mutableListOf<String>()
                for (elem in array) {
                    loadedList.add(elem.asString)
                }
                if (loadedList.isNotEmpty()) {
                    categoryOrder = loadedList
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            val json = JsonObject()
            json.addProperty("requireZForConfig", requireZForConfig)
            json.addProperty("autoReplaceSameItem", autoReplaceSameItem)
            json.addProperty("autoReplaceSameType", autoReplaceSameType)
            json.addProperty("autoRefillStack", autoRefillStack)
            json.addProperty("refillThreshold", refillThreshold)

            val lockedMapObj = JsonObject()
            for ((k, v) in worldLockedSlots) {
                val arr = com.google.gson.JsonArray()
                for (slotIndex in v) arr.add(slotIndex)
                lockedMapObj.add(k, arr)
            }
            json.add("worldLockedSlots", lockedMapObj)

            val array = com.google.gson.JsonArray()
            for (cat in categoryOrder) array.add(cat)
            json.add("categoryOrder", array)

            Files.writeString(CONFIG_FILE, GSON.toJson(json))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
