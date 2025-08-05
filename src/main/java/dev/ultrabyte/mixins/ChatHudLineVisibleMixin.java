package dev.ultrabyte.mixins;

import dev.ultrabyte.utils.mixins.IChatHudLineVisible;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public class ChatHudLineVisibleMixin implements IChatHudLineVisible {
    @Unique private boolean clientMessage = false;
    @Unique private String clientIdentifier = "";

    @Override
    public boolean ultrabyte$isClientMessage() {
        return clientMessage;
    }

    @Override
    public void ultrabyte$setClientMessage(boolean clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public String ultrabyte$getClientIdentifier() {
        return clientIdentifier;
    }

    @Override
    public void ultrabyte$setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}
