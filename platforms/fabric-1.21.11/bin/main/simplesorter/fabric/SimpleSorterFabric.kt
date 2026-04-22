package simplesorter.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.slf4j.LoggerFactory
import simplesorter.mc.AutoReplacer
import simplesorter.mc.InventoryScanner

class SimpleSorterFabric : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("simplesorter")

    override fun onInitializeClient() {
        logger.info("Initializing SimpleSorter for 1.21.11!")

        simplesorter.fabric.SimpleSorterKeybindings.register()
        
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (simplesorter.fabric.SimpleSorterKeybindings.configKey.wasPressed()) {
                val config = simplesorter.mc.config.SimpleSorterConfig
                val requireZ = config.requireZForConfig
                val isZPressed = org.lwjgl.glfw.GLFW.glfwGetKey(
                    client.window.handle,
                    org.lwjgl.glfw.GLFW.GLFW_KEY_Z
                ) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                if (!requireZ || isZPressed) {
                    client.setScreen(simplesorter.mc.config.ConfigScreen.build(client.currentScreen))
                }
            }

            InventoryScanner.tickSort()
            AutoReplacer.tick()
        }
    }
}
