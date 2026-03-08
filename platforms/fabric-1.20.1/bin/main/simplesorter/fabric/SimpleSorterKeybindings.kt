package simplesorter.fabric

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object SimpleSorterKeybindings {
    lateinit var sortKey: KeyBinding
    lateinit var configKey: KeyBinding

    fun register() {
        sortKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.sort",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.inventory" // 1.20.1: use string constant
            )
        )

        configKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "key.categories.inventory"
            )
        )
    }
}
