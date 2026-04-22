package simplesorter.fabric.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {


    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (simplesorter.fabric.SimpleSorterKeybindings.INSTANCE.getSortKey().matchesKey(keyCode, scanCode)) {
            simplesorter.mc.InventoryScanner.INSTANCE.requestSort();
            cir.setReturnValue(true);
        }
        if (simplesorter.fabric.SimpleSorterKeybindings.INSTANCE.getConfigKey().matchesKey(keyCode, scanCode)) {
            MinecraftClient client = MinecraftClient.getInstance();
            simplesorter.mc.config.SimpleSorterConfig config = simplesorter.mc.config.SimpleSorterConfig.INSTANCE;
            boolean requireZ = config.getRequireZForConfig();
            boolean isZPressed = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(),
                    org.lwjgl.glfw.GLFW.GLFW_KEY_Z) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            if (!requireZ || isZPressed) {
                client.setScreen(simplesorter.mc.config.ConfigScreen.INSTANCE.build(client.currentScreen));
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        Slot slot = ((simplesorter.fabric.mixin.HandledScreenAccessor) screen)
                .invokeGetSlotAt(mouseX, mouseY);
        boolean isShift = Screen.hasShiftDown();
        boolean cancel = simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseClicked(screen, slot, button, isShift);
        if (cancel) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"))
    private void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY,
            CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        Slot slot = ((simplesorter.fabric.mixin.HandledScreenAccessor) screen)
                .invokeGetSlotAt(mouseX, mouseY);
        boolean isShift = Screen.hasShiftDown();
        simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseDragged(screen, slot, button, isShift);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseReleased(button);
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void onDrawSlot(net.minecraft.client.gui.DrawContext context, Slot slot, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        boolean isLocked = false;
        if (slot.inventory == client.player.getInventory()) {
            isLocked = simplesorter.mc.LockManager.INSTANCE.getPlayerLockedSlots().contains(slot.getIndex());
        } else {
            isLocked = simplesorter.mc.LockManager.INSTANCE.getContainerLockedSlots(client.player.currentScreenHandler.syncId).contains(slot.id);
        }

        if (isLocked) {
            // Draw a semi-transparent red overlay to indicate the slot is locked
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x60FF0000);
        }
    }
}
