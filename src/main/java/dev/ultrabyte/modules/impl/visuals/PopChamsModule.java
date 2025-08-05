package dev.ultrabyte.modules.impl.visuals;

import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.PlayerPopEvent;
import dev.ultrabyte.events.impl.RenderWorldEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.ColorSetting;
import dev.ultrabyte.settings.impl.ModeSetting;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.animations.Easing;
import dev.ultrabyte.utils.color.ColorUtils;
import dev.ultrabyte.utils.graphics.Renderer3D;
import dev.ultrabyte.utils.minecraft.StaticPlayerEntity;

import java.awt.*;

@RegisterModule(name = "PopChams", description = "Renders chams when an entity pops a totem.", category = Module.Category.VISUALS)
public class PopChamsModule extends Module {
    public NumberSetting duration = new NumberSetting("Duration", "The duration for the pop chams fade.", 1500, 0, 5000);
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the pop chams.", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());

    private StaticPlayerEntity model;
    private long startTime;

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent event) {
        if(event.getPlayer() == mc.player) return;
        this.model = new StaticPlayerEntity(event.getPlayer());
        this.startTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if(getNull() || model == null || !Renderer3D.isFrustumVisible(model.getBoundingBox()) || System.currentTimeMillis() - startTime > duration.getValue().intValue()) return;

        float ease = 1.0f - Easing.toDelta(startTime, duration.getValue().intValue());
        Color fill = ColorUtils.getColor(fillColor.getColor(), (int)(fillColor.getColor().getAlpha() * ease));
        Color out = ColorUtils.getColor(outlineColor.getColor(), (int)(outlineColor.getColor().getAlpha() * ease));

        model.render(event, mode.getValue().equals("Fill") || mode.getValue().equals("Both"), fill, mode.getValue().equals("Outline") || mode.getValue().equals("Both"), out);
    }
}
