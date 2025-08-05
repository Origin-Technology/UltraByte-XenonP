package dev.opan.mixins;

import dev.opan.UltraByte;
import dev.opan.modules.impl.core.FontModule;
import net.minecraft.client.font.Glyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Glyph.class)
public interface GlyphMixin {
    @Inject(method = "getShadowOffset", at = @At("HEAD"), cancellable = true)
    private void getShadowOffset(CallbackInfoReturnable<Float> info) {
        if (UltraByte.MODULE_MANAGER != null && UltraByte.MODULE_MANAGER.getModule(FontModule.class).isToggled() && !UltraByte.MODULE_MANAGER.getModule(FontModule.class).shadowMode.getValue().equalsIgnoreCase("Default")) {
            info.setReturnValue(UltraByte.FONT_MANAGER.getShadowOffset());
        }
    }
}
