package simplesorter.fabric.mixin;

import simplesorter.mc.InventoryScanner;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Unique
    private long lastSortTime = 0;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (simplesorter.fabric.SimpleSorterKeybindings.INSTANCE.getSortKey().matchesKey(keyCode, scanCode)) {
            long now = System.currentTimeMillis();
            if (now - lastSortTime > 500) {
                lastSortTime = now;
                InventoryScanner.INSTANCE.requestSort();
            }
            cir.setReturnValue(true);
            return;
        }

        System.out.println("[SimpleSorter] HandledScreen.keyPressed: keyCode=" + keyCode + " scanCode=" + scanCode);

        if (simplesorter.fabric.SimpleSorterKeybindings.INSTANCE.getConfigKey().matchesKey(keyCode, scanCode)) {
            simplesorter.mc.config.SimpleSorterConfig config = simplesorter.mc.config.SimpleSorterConfig.INSTANCE;
            boolean requireZ = config.getRequireZForConfig();
            boolean isZPressed = GLFW.glfwGetKey(
                    net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                    GLFW.GLFW_KEY_Z) == GLFW.GLFW_PRESS;
            System.out.println("[SimpleSorter] Config key matched! requireZ=" + requireZ + ", isZPressed=" + isZPressed);
            if (!requireZ || isZPressed) {
                System.out.println("[SimpleSorter] Opening config screen...");
                net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
                client.setScreen(simplesorter.mc.config.ConfigScreen.INSTANCE.build(client.currentScreen));
                cir.setReturnValue(true);
                return;
            } else {
                System.out.println("[SimpleSorter] Config screen NOT opened because Z was required but not pressed.");
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        // In 1.21.1, getSlotAt is a protected concept, we might need an accessor or
        // shadowed method.
        // But we can check slots if we iterate them.
        net.minecraft.screen.slot.Slot slot = ((simplesorter.fabric.mixin.HandledScreenAccessor) screen)
                .invokeGetSlotAt(mouseX, mouseY);
        boolean isShift = net.minecraft.client.gui.screen.Screen.hasShiftDown();
        boolean cancel = simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseClicked(screen, slot, button, isShift);
        if (cancel) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"))
    private void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY,
            CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        net.minecraft.screen.slot.Slot slot = ((simplesorter.fabric.mixin.HandledScreenAccessor) screen)
                .invokeGetSlotAt(mouseX, mouseY);
        boolean isShift = net.minecraft.client.gui.screen.Screen.hasShiftDown();
        simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseDragged(screen, slot, button, isShift);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseReleased(button);
    }
}
