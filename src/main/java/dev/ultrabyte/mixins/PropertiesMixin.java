package dev.ultrabyte.mixins;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.modules.impl.visuals.AtmosphereModule;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public class PropertiesMixin {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void getTimeOfDay(CallbackInfoReturnable<Long> info) {
        if (UltraByte.MODULE_MANAGER.getModule(AtmosphereModule.class).isToggled() && UltraByte.MODULE_MANAGER.getModule(AtmosphereModule.class).modifyTime.getValue()) {
            info.setReturnValue(UltraByte.MODULE_MANAGER.getModule(AtmosphereModule.class).time.getValue().longValue() * 100L);
        }
    }
}
