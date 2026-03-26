package simplesorter.fabric

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object SimpleSorterKeybindings {
    lateinit var sortKey: KeyBinding
    lateinit var configKey: KeyBinding
    lateinit var lockKey: KeyBinding
    lateinit var batchDropKey: KeyBinding

    fun register() {
        sortKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.sort", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyBinding.INVENTORY_CATEGORY
            )
        )

        configKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.config", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                KeyBinding.INVENTORY_CATEGORY
            )
        )

        lockKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.lock",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                KeyBinding.INVENTORY_CATEGORY
            )
        )

        batchDropKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.batchdrop",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_CAPS_LOCK,
                KeyBinding.INVENTORY_CATEGORY
            )
        )

        simplesorter.mc.KeyBindingHolder.lockKey = lockKey
        simplesorter.mc.KeyBindingHolder.batchDropKey = batchDropKey
    }
}
