package dev.ultrabyte.modules.impl.combat;

import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.GameLoopEvent;
import dev.ultrabyte.events.impl.PlayerUpdateEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.settings.impl.CategorySetting;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.minecraft.DamageUtils;
import dev.ultrabyte.utils.miscellaneous.BlockUtil;
import dev.ultrabyte.utils.system.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static dev.ultrabyte.modules.impl.combat.PistonKickModule.canPlace;
import static dev.ultrabyte.modules.impl.combat.PistonKickModule.getEnemies;

/**
 * @author NiuRen0827
 * Time:2:05
 */
@RegisterModule(name = "PistonCrystal", description = "Push crystal to hole.", category = Module.Category.COMBAT)
public class PistonCrystalModule extends Module {
    public CategorySetting General = new CategorySetting("General", "");
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround","", new CategorySetting.Visibility(General),false);
    private final BooleanSetting noEating = new BooleanSetting("EatingPause","",new CategorySetting.Visibility(General),true);
    private final BooleanSetting a112 = new BooleanSetting("1.12.2","", new CategorySetting.Visibility(General),false);
    public NumberSetting range = new NumberSetting("Range", "", new CategorySetting.Visibility(General),5.0, 0.0, 6.0);
    public CategorySetting Place = new CategorySetting("Place", "");
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "", new CategorySetting.Visibility(Place),5.0, 0.0, 6.0);
    public NumberSetting calcRadius = new NumberSetting("CalciumRadius", "", new CategorySetting.Visibility(Place),2, 2, 3);
    private final BooleanSetting pTempPos = new BooleanSetting("TempPos","", new CategorySetting.Visibility(Place),false);
    public CategorySetting Delay = new CategorySetting("Delay", "");
    public NumberSetting updateDelay = new NumberSetting("UpdateDelay", "", new CategorySetting.Visibility(Delay),100, 0, 1000);
    public CategorySetting Damage = new CategorySetting("Damage", "");
    public NumberSetting minDamage = new NumberSetting("MinDamage", "", new CategorySetting.Visibility(Damage),6, 1, 36);
    public CategorySetting Rotate = new CategorySetting("Rotate", "");
    private final BooleanSetting rotate = new BooleanSetting("Rotate","", new CategorySetting.Visibility(Rotate),true);
    private final Timer updateTimer = new Timer();

    public static BlockPos pistonPos;
    public static BlockPos redStonePos;

    public static List<BlockPos> crystalPos;
    public static BlockPos finalPos;
    public static BlockPos finalObsPos;

    public static PlayerEntity bestTarget;
    public static EndCrystalEntity crystal = null;



    @SubscribeEvent
    public void onGameLoop(GameLoopEvent event) {

    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
    }

    private void update() {
        if (getNull()) {
            return;
        }
        if (!mc.player.isOnGround() && onlyGround.getValue()) {
            return;
        }
        if (noEating.getValue() && mc.player.isUsingItem()) {
            return;
        }
        if (updateTimer.passedMs(updateDelay.getValue().longValue())) {
            updateTimer.reset();
        } else return;

    }
    private BlockPos getBestPistonPos(BlockPos cPos) {
        //  ArrayList<BlockPos> surround = new ArrayList<>();
        //        ArrayList<BlockPos> surroundUp = new ArrayList<>();
        // for (Direction i : Direction.values()) {
        //            if (!(i == Direction.DOWN || i == Direction.UP)) {
        //                surround.add(cPos.offset(i));
        //            }
        //        }

        BlockPos bestPos = null;
        ArrayList<BlockPos> tempPosList = new ArrayList<>();
        for (BlockPos temp : BlockUtil.getSphere(calcRadius.getValue().floatValue(), cPos.toCenterPos())) {
            if (canPlace(temp, placeRange.getValue().doubleValue())) {
                tempPosList.add(temp);
            }
        }
        double range = Float.MIN_VALUE;
        for (BlockPos temp : tempPosList) {
            double dis = mc.player.squaredDistanceTo(temp.toCenterPos());
            if (dis > range) {
                range = dis;
                bestPos = temp;
            }
        }
        return bestPos;
    }
    private boolean canPlaceCrystal(BlockPos pos, PlayerEntity enemy) {
        return (BlockUtil.canClick(pos.down())
                && (BlockUtil.getBlock(pos.down()) == Blocks.OBSIDIAN || BlockUtil.getBlock(pos.down()) == Blocks.BEDROCK)
                && ((a112.getValue() ? BlockUtil.getBlock(pos) == Blocks.AIR : BlockUtil.clientCanPlace(pos))
                || BlockUtil.hasEntityBlockCrystal(pos.down(),false, true)))
                && DamageUtils.getCrystalDamage(enemy, enemy.getBoundingBox(), pos, pos, true) > minDamage.getValue().floatValue();
    }
}
