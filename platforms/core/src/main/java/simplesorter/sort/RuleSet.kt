package simplesorter.sort

/**
 * Sorting priority rules.
 * Lower number = appears first in inventory.
 * 
 * User-requested order: Blocks → Tools → Weapons → Armor → Food → Misc
 */
data class SortRule(
    val priority: Int,
    val matchLogic: (String) -> Boolean
)

object RuleSet {
    val defaultRules = listOf(
        // === 1. Blocks (10-19) ===
        SortRule(10) { id -> isBlock(id) },

        // === 2. Tools (20-29) ===
        SortRule(20) { it.contains("pickaxe") },
        SortRule(21) { it.contains("shovel") },
        SortRule(22) { it.contains("hoe") },
        SortRule(23) { it.contains("shears") },
        SortRule(24) { it.contains("flint_and_steel") },
        SortRule(25) { it.contains("fishing_rod") },
        SortRule(26) { it.contains("spyglass") },
        SortRule(27) { it.contains("brush") },

        // === 3. Weapons (30-39) ===
        SortRule(30) { it.contains("sword") },
        SortRule(31) { it.contains("bow") && !it.contains("bowl") },
        SortRule(32) { it.contains("crossbow") },
        SortRule(33) { it.contains("trident") },
        SortRule(34) { it.contains("mace") },
        SortRule(35) { it.contains("arrow") },

        // === 4. Armor (40-49) ===
        SortRule(40) { it.contains("helmet") },
        SortRule(41) { it.contains("chestplate") },
        SortRule(42) { it.contains("leggings") },
        SortRule(43) { it.contains("boots") },
        SortRule(44) { it.contains("shield") },
        SortRule(45) { it.contains("elytra") },

        // === 5. Food (50-59) ===
        SortRule(50) { it.contains("golden_apple") || it.contains("enchanted_golden_apple") },
        SortRule(51) { id -> isFood(id) },

        // === 6. Redstone (60-69) ===
        SortRule(60) { id -> isRedstone(id) },

        // === 7. Everything else (100) ===
        SortRule(100) { true }
    )

    // Common food items
    private fun isFood(id: String): Boolean {
        val foods = listOf(
            "apple", "bread", "beef", "cooked_beef", "steak",
            "porkchop", "cooked_porkchop", "chicken", "cooked_chicken",
            "mutton", "cooked_mutton", "rabbit", "cooked_rabbit",
            "cod", "cooked_cod", "salmon", "cooked_salmon",
            "potato", "baked_potato", "carrot", "golden_carrot",
            "melon_slice", "sweet_berries", "glow_berries",
            "dried_kelp", "cookie", "pumpkin_pie", "cake",
            "beetroot", "beetroot_soup", "mushroom_stew",
            "rabbit_stew", "suspicious_stew", "honey_bottle",
            "rotten_flesh", "spider_eye", "poisonous_potato",
            "chorus_fruit", "tropical_fish", "pufferfish"
        )
        val name = id.removePrefix("minecraft:")
        return foods.any { name == it }
    }

    // Common block items
    private fun isBlock(id: String): Boolean {
        val name = id.removePrefix("minecraft:")
        val blockSuffixes = listOf(
            "stone", "dirt", "grass_block", "cobblestone", "sand", "gravel",
            "ore", "log", "wood", "planks", "slab", "stairs", "fence",
            "wall", "bricks", "block", "glass", "pane", "leaves",
            "wool", "carpet", "concrete", "terracotta", "clay",
            "obsidian", "netherrack", "soul_sand", "soul_soil",
            "basalt", "blackstone", "deepslate", "tuff", "calcite",
            "dripstone", "amethyst", "copper", "prismarine",
            "sandstone", "andesite", "diorite", "granite",
            "chest", "barrel", "furnace", "crafting_table",
            "torch", "lantern", "campfire"
        )
        return blockSuffixes.any { name.contains(it) }
    }

    private fun isRedstone(id: String): Boolean {
        val name = id.removePrefix("minecraft:")
        val redstone = listOf(
            "redstone", "repeater", "comparator", "piston",
            "sticky_piston", "observer", "dropper", "dispenser",
            "hopper", "lever", "button", "pressure_plate",
            "tripwire", "daylight_detector", "target"
        )
        return redstone.any { name.contains(it) }
    }

    fun getPriority(itemId: String): Int {
        return defaultRules.firstOrNull { it.matchLogic(itemId) }?.priority ?: 100
    }
}
