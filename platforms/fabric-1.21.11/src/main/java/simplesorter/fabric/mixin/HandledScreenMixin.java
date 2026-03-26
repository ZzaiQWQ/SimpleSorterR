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
    private void onKeyPressed(net.minecraft.client.input.KeyInput keyInput, CallbackInfoReturnable<Boolean> cir) {
        if (simplesorter.fabric.SimpleSorterKeybindings.INSTANCE.getSortKey().matchesKey(keyInput)) {
            long now = System.currentTimeMillis();
            if (now - lastSortTime > 500) {
                lastSortTime = now;
                InventoryScanner.INSTANCE.requestSort();
            }
            cir.setReturnValue(true);
            return;
        }

        if (simplesorter.fabric.SimpleSorterKeybindings.INSTANCE.getConfigKey().matchesKey(keyInput)) {
            simplesorter.mc.config.SimpleSorterConfig config = simplesorter.mc.config.SimpleSorterConfig.INSTANCE;
            boolean requireZ = config.getRequireZForConfig();
            boolean isZPressed = GLFW.glfwGetKey(
                    net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                    GLFW.GLFW_KEY_Z) == GLFW.GLFW_PRESS;
            if (!requireZ || isZPressed) {
                net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
                client.setScreen(simplesorter.mc.config.ConfigScreen.INSTANCE.build(client.currentScreen));
                cir.setReturnValue(true);
                return;
            }
        }

    }


    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(net.minecraft.client.gui.Click click, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        net.minecraft.screen.slot.Slot slot = ((simplesorter.fabric.mixin.HandledScreenAccessor) screen)
                .invokeGetSlotAt(click.x(), click.y());
        boolean isShift = GLFW.glfwGetKey(net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                        GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        boolean cancel = simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseClicked(screen, slot, click.button(),
                isShift);
        if (cancel) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"))
    private void onMouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY,
            CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        net.minecraft.screen.slot.Slot slot = ((simplesorter.fabric.mixin.HandledScreenAccessor) screen)
                .invokeGetSlotAt(click.x(), click.y());
        boolean isShift = GLFW.glfwGetKey(net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(),
                        GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseDragged(screen, slot, click.button(), isShift);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void onMouseReleased(net.minecraft.client.gui.Click click, CallbackInfoReturnable<Boolean> cir) {
        simplesorter.mc.MouseTweaksHandler.INSTANCE.onMouseReleased(click.button());
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void onDrawSlot(net.minecraft.client.gui.DrawContext context, net.minecraft.screen.slot.Slot slot, int x, int y, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null) return;
        
        boolean isLocked = false;
        if (slot.inventory == client.player.getInventory()) {
            isLocked = simplesorter.mc.LockManager.INSTANCE.getPlayerLockedSlots().contains(slot.getIndex());
        } else {
            isLocked = simplesorter.mc.LockManager.INSTANCE.getContainerLockedSlots(client.player.currentScreenHandler.syncId).contains(slot.id);
        }
        
        if (isLocked) {
            // Draw a semi-transparent red overlay to explicitly indicate the slot is locked
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x60FF0000);
        }
    }
}
