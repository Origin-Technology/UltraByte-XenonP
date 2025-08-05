package dev.ultrabyte.mixins;

import com.mojang.authlib.GameProfile;
import dev.ultrabyte.UltraByte;
import dev.ultrabyte.modules.impl.core.CapesModule;
import dev.ultrabyte.utils.IMinecraft;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin implements IMinecraft {
    @Shadow @Final private GameProfile profile;

    @Inject(method = "getSkinTextures", at = @At("TAIL"), cancellable = true)
    private void getSkinTextures(CallbackInfoReturnable<SkinTextures> info) {
        if (((profile.getName().equals(mc.player.getGameProfile().getName()) && profile.getId().equals(mc.player.getGameProfile().getId()))) && UltraByte.MODULE_MANAGER.getModule(CapesModule.class).isToggled() && UltraByte.MODULE_MANAGER.getModule(CapesModule.class).getCapeTexture() != null) {
            Identifier identifier = UltraByte.MODULE_MANAGER.getModule(CapesModule.class).getCapeTexture();
            SkinTextures texture = info.getReturnValue();

            info.setReturnValue(new SkinTextures(texture.texture(), texture.textureUrl(), identifier, identifier, texture.model(), texture.secure()));
        }
    }
}
