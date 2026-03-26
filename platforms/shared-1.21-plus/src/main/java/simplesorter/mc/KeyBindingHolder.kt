package simplesorter.mc

import net.minecraft.client.option.KeyBinding

/**
 * Holds references to keybindings and their active state.
 * The flag-based state is updated by the mixin's keyPressed handler
 * so that tweaks can check if the key is currently held during mouse events.
 */
object KeyBindingHolder {
    var lockKey: KeyBinding? = null
    var batchDropKey: KeyBinding? = null

    var lockKeyHeldSupplier: (() -> Boolean)? = null
    var batchDropKeyHeldSupplier: (() -> Boolean)? = null

    val isLockKeyHeld: Boolean
        get() = lockKeyHeldSupplier?.invoke() ?: checkKeyData(lockKey)

    val isBatchDropKeyHeld: Boolean
        get() = batchDropKeyHeldSupplier?.invoke() ?: checkKeyData(batchDropKey)

    private fun checkKeyData(keyBinding: KeyBinding?): Boolean {
        if (keyBinding == null || keyBinding.isUnbound) return false
        val translationKey = keyBinding.boundKeyTranslationKey
        val handle = net.minecraft.client.MinecraftClient.getInstance().window.handle

        var isPressed = false
        try {
            val boundKey = net.minecraft.client.util.InputUtil.fromTranslationKey(translationKey)
            val code = boundKey.code
            if (code != -1) {
                isPressed = if (boundKey.category == net.minecraft.client.util.InputUtil.Type.MOUSE) {
                    org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, code) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                } else {
                    org.lwjgl.glfw.GLFW.glfwGetKey(handle, code) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                }
            }
        } catch (e: Throwable) {
            // ignore
        }

        if (!isPressed) {
            if (translationKey == "key.keyboard.left.alt") {
                isPressed = org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT) == org.lwjgl.glfw.GLFW.GLFW_PRESS
            } else if (translationKey == "key.keyboard.caps.lock") {
                isPressed = org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_CAPS_LOCK) == org.lwjgl.glfw.GLFW.GLFW_PRESS
            }
        }

        return isPressed
    }
}
