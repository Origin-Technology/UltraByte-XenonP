package dev.ultrabyte.modules.impl.visuals;

import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.RenderWorldEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.ColorSetting;
import dev.ultrabyte.settings.impl.ModeSetting;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.animations.Easing;
import dev.ultrabyte.utils.color.ColorUtils;
import dev.ultrabyte.utils.graphics.Renderer3D;
import dev.ultrabyte.utils.minecraft.WorldUtils;
import dev.ultrabyte.utils.system.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

@RegisterModule(name = "BlockHighlight", description = "Replaces the default Minecraft block highlight with a more customizable one.", category = Module.Category.VISUALS)
public class BlockHighlightModule extends Module {
    public ModeSetting animationMode = new ModeSetting("Animation", "The animation that will be applied to the rendering.", "Static", new String[]{"Static", "Slide"});
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the target block.", "Outline", new String[]{"None", "Fill", "Outline", "Both"});
    public NumberSetting slideSmoothness = new NumberSetting("Smoothness", "The smoothness for the slide while target block is changing.", 1, 0, 20);
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());

    private BlockPos prevPosition = null;
    private Vec3d renderPosition = null;

    private long animationStart = 0;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (mode.getValue().equalsIgnoreCase("None")) return;

        if (!(mc.crosshairTarget instanceof BlockHitResult hitResult)) return;

        BlockPos position = hitResult.getBlockPos();

        if(animationMode.getValue().equals("Slide") && position != null) {
            if(renderPosition == null) renderPosition = MathUtils.getVec(position);

            if(!WorldUtils.equals(position, prevPosition)) {
                animationStart = System.currentTimeMillis();
                prevPosition = position;
            }
        }

        Vec3d offset = MathUtils.getVec(position);

        if(animationMode.getValue().equalsIgnoreCase("Slide") && renderPosition != null) {
            float easing = Easing.ease(Easing.toDelta(animationStart, (int) (Math.pow(slideSmoothness.getValue().doubleValue(), 1.4d) * 1000)), Easing.Method.EASE_OUT_QUART);
            renderPosition = renderPosition.add(MathUtils.scale(MathUtils.getVec(position).subtract(renderPosition), easing));

            offset = renderPosition;
        }

        BlockState state = mc.world.getBlockState(position);
        if (state.isAir() || !mc.world.getWorldBorder().contains(position)) return;

        VoxelShape shape = state.getOutlineShape(mc.world, position);
        if (shape.isEmpty()) return;

        if (mode.getValue().equalsIgnoreCase("Fill") || mode.getValue().equalsIgnoreCase("Both")) Renderer3D.renderBox(event.getMatrices(), shape.getBoundingBox().offset(offset), fillColor.getColor());
        if (mode.getValue().equalsIgnoreCase("Outline") || mode.getValue().equalsIgnoreCase("Both")) Renderer3D.renderBoxOutline(event.getMatrices(), shape.getBoundingBox().offset(offset), outlineColor.getColor());
    }

    @Override
    public String getMetaData() {
        return mode.getValue();
    }
}
