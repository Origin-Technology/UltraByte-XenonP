package dev.ultrabyte.mixins;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.events.impl.EntitySpawnEvent;
import dev.ultrabyte.modules.impl.visuals.AtmosphereModule;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void getSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> info) {
        if (UltraByte.MODULE_MANAGER.getModule(AtmosphereModule.class).isToggled() && UltraByte.MODULE_MANAGER.getModule(AtmosphereModule.class).modifyFog.getValue()) {
            info.setReturnValue(UltraByte.MODULE_MANAGER.getModule(AtmosphereModule.class).fogColor.getColor().getRGB());
        }
    }

    @Inject(method = "addEntity", at = @At(value = "HEAD"))
    private void addEntity(Entity entity, CallbackInfo info) {
        UltraByte.EVENT_HANDLER.post(new EntitySpawnEvent(entity));
    }
}
