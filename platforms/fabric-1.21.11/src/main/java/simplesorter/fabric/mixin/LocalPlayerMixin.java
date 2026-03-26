package simplesorter.fabric.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import simplesorter.mc.LockManager;

/**
 * Prevents dropping items from locked hotbar slots when pressing Q.
 */
@Mixin(ClientPlayerEntity.class)
public class LocalPlayerMixin {

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void onDropSelectedItem(boolean dropAll, CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        PlayerInventory inventory = self.getInventory();
        int selectedSlot = ((PlayerInventoryAccessor) inventory).getSelectedSlot();

        if (LockManager.INSTANCE.getPlayerLockedSlots().contains(selectedSlot)) {
            cir.setReturnValue(false);
        }
    }
}
