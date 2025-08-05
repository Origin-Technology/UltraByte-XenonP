package dev.opan.mixins;

import dev.opan.UltraByte;
import dev.opan.modules.impl.visuals.FullBrightModule;
import dev.opan.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Shadow @Final private SimpleFramebuffer lightmapFramebuffer;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/SimpleFramebuffer;endWrite()V", shift = At.Shift.BEFORE))
    private void update$endWrite(float delta, CallbackInfo info) {
        FullBrightModule module = UltraByte.MODULE_MANAGER.getModule(FullBrightModule.class);
        if (UltraByte.MODULE_MANAGER != null && module.isToggled() && module.mode.getValue().equalsIgnoreCase("Gamma")) {
            lightmapFramebuffer.clear();
        }
    }

    @Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
    private void getDarknessFactor(float delta, CallbackInfoReturnable<Float> info) {
        if (UltraByte.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && UltraByte.MODULE_MANAGER.getModule(NoRenderModule.class).blindness.getValue()) {
            info.setReturnValue(0.0f);
        }
    }
}
