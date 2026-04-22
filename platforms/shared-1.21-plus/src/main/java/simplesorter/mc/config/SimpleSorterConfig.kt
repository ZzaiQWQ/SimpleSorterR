package simplesorter.mc.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.attribute.FileTime

object SimpleSorterConfig {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val CONFIG_FILE = FabricLoader.getInstance().configDir.resolve("simplesorter.json")

    // The default hotkey to sort the inventory
    var requireZForConfig: Boolean = true // Checkbox: "Require Z"

    // Auto-replace options
    var autoReplaceSameItem: Boolean = true
    var autoReplaceSameType: Boolean = true
    var autoRefillStack: Boolean = true
    var refillThreshold: Int = 20

    // Container blacklist - class names that should NOT be sorted
    var blockedContainers: MutableList<String> = mutableListOf(
        "AbstractFurnaceScreenHandler",
        "FurnaceScreenHandler",
        "BlastFurnaceScreenHandler",
        "SmokerScreenHandler",
        "CraftingScreenHandler",
        "AnvilScreenHandler",
        "EnchantmentScreenHandler",
        "BrewingStandScreenHandler",
        "GrindstoneScreenHandler",
        "StonecutterScreenHandler",
        "LoomScreenHandler",
        "SmithingScreenHandler",
        "CartographyTableScreenHandler",
        "BeaconScreenHandler",
        "HopperScreenHandler",
        "MerchantScreenHandler"
    )

    // Persisted specific-profile locked slots
    var worldLockedSlots: MutableMap<String, MutableSet<Int>> = mutableMapOf()

    // Creative tab sort order
    var tabOrder: MutableList<String> = mutableListOf(
        "minecraft:building_blocks",
        "minecraft:colored_blocks",
        "minecraft:natural_blocks",
        "minecraft:functional_blocks",
        "minecraft:redstone_blocks",
        "minecraft:tools_and_utilities",
        "minecraft:combat",
        "minecraft:food_and_drinks",
        "minecraft:ingredients",
        "minecraft:spawn_eggs"
    )

    // Items that always sort to the very front
    var pinnedItems: MutableList<String> = mutableListOf()

    // Track file modification time for hot-reload
    private var lastModified: FileTime? = null

    init {
        load()
    }

    /**
     * Check if config file was modified externally and reload if needed.
     * Called before each sort to support hot-reload.
     */
    fun reloadIfChanged() {
        try {
            if (!Files.exists(CONFIG_FILE)) return
            val currentMod = Files.getLastModifiedTime(CONFIG_FILE)
            if (lastModified == null || currentMod != lastModified) {
                load()
                simplesorter.mc.CreativeTabSorter.invalidate()
            }
        } catch (_: Exception) {}
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

            if (json.has("blockedContainers")) {
                val arr = json.getAsJsonArray("blockedContainers")
                val list = mutableListOf<String>()
                for (e in arr) list.add(e.asString)
                blockedContainers = list
            }
            
            // Full reload: clear old data before loading
            worldLockedSlots.clear()
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
            
            // Load tabOrder (also supports old "categoryOrder" field)
            val tabKey = if (json.has("tabOrder")) "tabOrder" else if (json.has("categoryOrder")) "categoryOrder" else null
            if (tabKey != null) {
                val array = json.getAsJsonArray(tabKey)
                val loadedList = mutableListOf<String>()
                for (elem in array) {
                    loadedList.add(elem.asString)
                }
                tabOrder = loadedList
            }

            if (json.has("pinnedItems")) {
                val arr = json.getAsJsonArray("pinnedItems")
                val list = mutableListOf<String>()
                for (e in arr) list.add(e.asString)
                pinnedItems = list
            }

            lastModified = Files.getLastModifiedTime(CONFIG_FILE)
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

            val blockedArr = com.google.gson.JsonArray()
            for (name in blockedContainers) blockedArr.add(name)
            json.add("blockedContainers", blockedArr)

            val lockedMapObj = JsonObject()
            for ((k, v) in worldLockedSlots) {
                val arr = com.google.gson.JsonArray()
                for (slotIndex in v) arr.add(slotIndex)
                lockedMapObj.add(k, arr)
            }
            json.add("worldLockedSlots", lockedMapObj)

            val array = com.google.gson.JsonArray()
            for (cat in tabOrder) array.add(cat)
            json.add("tabOrder", array)

            val pinnedArr = com.google.gson.JsonArray()
            for (item in pinnedItems) pinnedArr.add(item)
            json.add("pinnedItems", pinnedArr)

            Files.writeString(CONFIG_FILE, GSON.toJson(json))
            lastModified = Files.getLastModifiedTime(CONFIG_FILE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
