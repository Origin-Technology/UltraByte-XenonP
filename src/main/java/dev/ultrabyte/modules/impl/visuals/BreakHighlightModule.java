package dev.ultrabyte.modules.impl.visuals;

import lombok.AllArgsConstructor;
import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.PlayerMineEvent;
import dev.ultrabyte.events.impl.RenderWorldEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.*;
import dev.ultrabyte.utils.animations.Easing;
import dev.ultrabyte.utils.color.ColorUtils;
import dev.ultrabyte.utils.graphics.Renderer3D;
import dev.ultrabyte.utils.minecraft.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@RegisterModule(name = "BreakHighlight", description = "Renders blocks that are being mined by other players.", category = Module.Category.VISUALS)
public class BreakHighlightModule extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the mine esp.", "Outline", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting texts = new BooleanSetting("BreakId", "TextId.", false);

    public ColorSetting fillColors = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(mode, "Smooth"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColors = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(mode, "Smooth"), ColorUtils.getDefaultOutlineColor());

    public NumberSetting scales = new NumberSetting("TextScale", "The scaling that will be applied to the text ESP rendering.", 30, 10, 100);
    public ColorSetting color = new ColorSetting("TextColor", "The color that will be used for the text ESP rendering.", new ColorSetting.Color(Color.WHITE, false, false));

    private final Map<Integer, Mine> mineMap = new HashMap<>();

    @SubscribeEvent
    public void onPlayerMine(PlayerMineEvent event) {
        if(getNull() || event.getActorID() == mc.player.getId()) return;

        Mine mine = new Mine(event.getPosition(), WorldUtils.getBreakTime((PlayerEntity) mc.world.getEntityById(event.getActorID()), mc.world.getBlockState(event.getPosition())), System.currentTimeMillis());
        if(!mineMap.containsKey(event.getActorID())) {
            mineMap.put(event.getActorID(), mine);
        } else {
            if(!mineMap.get(event.getActorID()).pos.equals(event.getPosition())) mineMap.replace(event.getActorID(), mine);
        }
    }
    private float prevProgress;
    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if(getNull() || mineMap.isEmpty()) return;

        mineMap.entrySet().removeIf(e -> clearMine(e.getKey(), e.getValue().pos));

        mineMap.forEach((id, mine) -> {
            if(mc.world.getBlockState(mine.pos).getBlock().equals(Blocks.AIR)) return;

            float scale = Easing.toDelta(mine.time, (int) mine.breakTime);
            Box box = new Box(mine.pos).contract(0.5).expand(scale / 2.0);
            if (mode.getValue().equalsIgnoreCase("Fill") || mode.getValue().equalsIgnoreCase("Both")) Renderer3D.renderBox(event.getMatrices(), box, fillColor.getColor());
             //Text

         //   if(text.getValue())Renderer3D.renderText(event.getMatrices(), String.valueOf(id), box,scales.getValue().intValue(), false, color.getColor());
            if (mode.getValue().equalsIgnoreCase("Outline") || mode.getValue().equalsIgnoreCase("Both")) Renderer3D.renderBoxOutline(event.getMatrices(), box, outlineColor.getColor());
            //NewMod
            Color fill;
            Color outline;
            fill = new Color(255 - (int) (MathHelper.clamp(scale, 0.0f, 1.0f) * 255), (int) (MathHelper.clamp(scale, 0.0f, 1.0f) * 255), 0, fillColors.getAlpha());
            outline = new Color(255 - (int) (MathHelper.clamp(scale, 0.0f, 1.0f) * 255), (int) (MathHelper.clamp(scale, 0.0f, 1.0f) * 255), 0, outlineColors.getAlpha());
            if (mode.getValue().equalsIgnoreCase("Smooth")) Renderer3D.renderBox(event.getMatrices(), box, fill);
            if (mode.getValue().equalsIgnoreCase("Smooth")) Renderer3D.renderBoxOutline(event.getMatrices(), box, outline);
        });

    }


    private boolean clearMine(int id, BlockPos pos) {
        if(mc.world.getEntityById(id) == null) return true;
        return Math.sqrt(mc.world.getEntityById(id).squaredDistanceTo(pos.toCenterPos())) > 6;
    }

    @AllArgsConstructor
    private static class Mine {
        private final BlockPos pos;
        private final float breakTime;
        private final long time;
    }
}
