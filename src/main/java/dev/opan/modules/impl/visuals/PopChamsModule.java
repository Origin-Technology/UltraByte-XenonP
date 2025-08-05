package dev.opan.modules.impl.visuals;

import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.PlayerPopEvent;
import dev.opan.events.impl.RenderWorldEvent;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.settings.impl.ColorSetting;
import dev.opan.settings.impl.ModeSetting;
import dev.opan.settings.impl.NumberSetting;
import dev.opan.utils.animations.Easing;
import dev.opan.utils.color.ColorUtils;
import dev.opan.utils.graphics.Renderer3D;
import dev.opan.utils.minecraft.StaticPlayerEntity;

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
