package dev.ultrabyte.utils.minecraft;

import com.mojang.authlib.GameProfile;
import dev.ultrabyte.events.impl.RenderWorldEvent;
import dev.ultrabyte.mixins.accessors.LimbAnimatorAccessor;
import dev.ultrabyte.utils.IMinecraft;
import dev.ultrabyte.utils.graphics.ModelRenderer;
import dev.ultrabyte.utils.mixins.ILivingEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;
import java.util.UUID;

public class StaticPlayerEntity extends OtherClientPlayerEntity implements IMinecraft {
    public StaticPlayerEntity(PlayerEntity player) {
        super(mc.world, new GameProfile(UUID.randomUUID(), player.getName().getString()));

        ((ILivingEntity) this).ultrabyte$setStaticPlayerEntity(true);

        copyPositionAndRotation(player);
        prevYaw = getYaw();
        prevPitch = getPitch();
        headYaw = player.headYaw;
        prevHeadYaw = headYaw;
        bodyYaw = player.bodyYaw;
        prevBodyYaw = bodyYaw;

        setSneaking(player.isSneaking());

        limbAnimator.setSpeed(player.limbAnimator.getSpeed());
        ((LimbAnimatorAccessor) limbAnimator).setPos(player.limbAnimator.getPos());

        setPose(player.getPose());
    }

    public void render(RenderWorldEvent event, boolean fill, Color fillColor, boolean outline, Color outlineColor) {
        ModelRenderer.renderModel(this, true, 1.0f, event.getTickDelta(), new ModelRenderer.Render(fill, fillColor, outline, outlineColor, false));
    }
}
