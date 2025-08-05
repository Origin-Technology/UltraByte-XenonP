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

import java.util.HashMap;
import java.util.List;

import static dev.ultrabyte.modules.impl.combat.PistonKickModule.getEnemies;

/**
 * @author NiuRen0827
 * Time:2:05
 */
@RegisterModule(name = "PistonCrystal", description = "Push crystal to hole.", category = Module.Category.COMBAT)
public class PistonCrystalModule extends Module {
    public CategorySetting General = new CategorySetting("General", "");
    private final BooleanSetting torch = new BooleanSetting("Torch", "", new CategorySetting.Visibility(General),false);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround","", new CategorySetting.Visibility(General),false);
    private final BooleanSetting noEating = new BooleanSetting("EatingPause","",new CategorySetting.Visibility(General),true);
    private final BooleanSetting a112 = new BooleanSetting("1.12.2","", new CategorySetting.Visibility(General),false);
    public NumberSetting range = new NumberSetting("Range", "", new CategorySetting.Visibility(General),5.0, 0.0, 6.0);
    public NumberSetting radius = new NumberSetting("CheckRadius", "", new CategorySetting.Visibility(General),2, 0, 5);
    public CategorySetting Place = new CategorySetting("Place", "");
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "", new CategorySetting.Visibility(Place),5.0, 0.0, 6.0);
    private final BooleanSetting pTempPos = new BooleanSetting("TempPos","", new CategorySetting.Visibility(Place),false);
    public CategorySetting Delay = new CategorySetting("Delay", "");
    public NumberSetting updateDelay = new NumberSetting("UpdateDelay", "", new CategorySetting.Visibility(Delay),100, 0, 1000);
    public CategorySetting Damage = new CategorySetting("Damage", "");
    public NumberSetting minDamage = new NumberSetting("MinDamage", "", new CategorySetting.Visibility(Damage),6, 1, 36);
    public CategorySetting Rotate = new CategorySetting("Rotate", "");
    private final BooleanSetting rotate = new BooleanSetting("Rotate","", new CategorySetting.Visibility(Rotate),true);
    private final Timer timer = new Timer();

    public static HashMap<BlockPos, Direction> pistonPos;
    public static List<BlockPos> pistonOffsetPos;
    public static HashMap<BlockPos, Direction> tempPistonPos;
    public static BlockPos redStonePos;

    public static List<BlockPos> crystalPos;
    public static BlockPos finalPos;
    public static BlockPos finalObsPos;
    public static BlockPos tempPos;



    public static PlayerEntity bestTarget;
    public static EndCrystalEntity crystal = null;



    private boolean isSurround(PlayerEntity enemy) {
        for (Direction i : Direction.values()) {
            if (BlockUtil.clientCanPlace(enemy.getBlockPos().offset(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean canPlaceCrystal(BlockPos pos, PlayerEntity enemy) {
        return (BlockUtil.canClick(pos.down())
                && (BlockUtil.getBlock(pos.down()) == Blocks.OBSIDIAN || BlockUtil.getBlock(pos.down()) == Blocks.BEDROCK)
                && ((a112.getValue() ? BlockUtil.getBlock(pos) == Blocks.AIR : BlockUtil.clientCanPlace(pos))
                || BlockUtil.hasEntityBlockCrystal(pos.down(),false, true)))
                && DamageUtils.getCrystalDamage(enemy, enemy.getBoundingBox(), pos, pos, true) > minDamage.getValue().floatValue();
    }

    @SubscribeEvent
    public void onGameLoop(GameLoopEvent event) {
        // calc dmg for crystal && find piston pos & red stone pos
        double op = Double.MIN_VALUE;
        BlockPos maxDamagePos;
        double damage = 18497194;

        for (BlockPos pos : crystalPos) {
            if (bestTarget == null) return;

            for (Entity e : mc.world.getEntities()) {
                if (e instanceof EndCrystalEntity crystalEntity) {
                    if (!crystal.isAlive()) continue;
                    if ((crystalEntity.getBlockPos() == pos))
                        crystal = crystalEntity;
                }
            }

            if (crystal == null) return;
            damage = DamageUtils.getCrystalDamage(bestTarget, bestTarget.getBoundingBox(), crystal, false);
            if (damage > op && BlockUtil.clientCanPlace(pos, false)){
                op = damage;
                maxDamagePos = pos;

                finalPos = maxDamagePos;
                finalObsPos = finalPos.add(0, -1, 0);
            }

            if (finalPos == null) return;

            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN || dir == Direction.UP) return;
                pistonPos.put(finalPos.offset(dir), dir);
            }
        }
        for (BlockPos pistonPos : pistonOffsetPos) {

        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!timer.passedMs(updateDelay.getValue().intValue())) return;
        if (getNull()) return;
        if (noEating.getValue() && mc.player.isUsingItem()) return;
        if (onlyGround.getValue() && !mc.player.isOnGround()) return;

        for (PlayerEntity player : getEnemies(range.getValue().intValue())) {
            if (player.isDead()) return;
            if (!isSurround(player)) return;

            bestTarget = player;

            for (BlockPos pos : BlockUtil.getSphere(radius.getValue().intValue(), Vec3d.of(player.getBlockPos()))) {

                for (Direction i : Direction.values()) {
                    if (i == Direction.DOWN || i == Direction.UP) return;

                    BlockPos offset = pos.offset(i);
                    if (canPlaceCrystal(offset, player)) {
                        crystalPos.add(offset);
                        if (pTempPos.getValue()) {
                       //     BlockUtil.placeCrystal(offset, rotate.getValue());

                            for (Direction i2 : Direction.values()) {
                                if (i2 == Direction.DOWN || i2 == Direction.UP) return;
                                BlockPos pistonOffset = tempPos.offset(i2);
                                if (!BlockUtil.clientCanPlace(pistonOffset)) return;

                                if (mc.player.squaredDistanceTo(pistonOffset.getX(), pistonOffset.getY(), pistonOffset.getZ()) > placeRange.getValue().doubleValue()) return;
                                /*
                                       NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
                                       SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
                                       WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
                                       EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

                                     */

                                tempPistonPos.put(pistonOffset, i2.getOpposite());
                            }
                        }

                    }
                }
            }
        }

    }

}
