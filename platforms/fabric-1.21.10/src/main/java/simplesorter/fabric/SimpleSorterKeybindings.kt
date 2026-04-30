package simplesorter.fabric

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

object SimpleSorterKeybindings {
    private val CATEGORY = KeyBinding.Category.create(Identifier.of("simplesorter", "simplesorter"))

    lateinit var sortKey: KeyBinding
    lateinit var configKey: KeyBinding
    lateinit var blockContainerKey: KeyBinding
    lateinit var lockKey: KeyBinding
    lateinit var batchDropKey: KeyBinding

    fun register() {
        sortKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.sort", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
            )
        )

        configKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.config", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                CATEGORY
            )
        )

        blockContainerKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.block_container",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
            )
        )

        lockKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.lock",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                CATEGORY
            )
        )

        batchDropKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.simplesorter.batchdrop",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_CAPS_LOCK,
                CATEGORY
            )
        )

        simplesorter.mc.KeyBindingHolder.lockKey = lockKey
        simplesorter.mc.KeyBindingHolder.batchDropKey = batchDropKey
    }
}
