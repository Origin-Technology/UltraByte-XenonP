package dev.opan.modules.impl.combat;

import dev.opan.UltraByte;
import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.PlayerUpdateEvent;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.modules.impl.core.RotationsModule;
import dev.opan.settings.impl.BooleanSetting;
import dev.opan.settings.impl.CategorySetting;
import dev.opan.settings.impl.ModeSetting;
import dev.opan.settings.impl.NumberSetting;
import dev.opan.utils.minecraft.InventoryUtils;
import dev.opan.utils.miscellaneous.BlockPosX;
import dev.opan.utils.miscellaneous.BlockUtil;
import dev.opan.utils.rotations.RotationUtils;
import dev.opan.utils.system.Timer;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.opan.managers.RotationManager.calculateAngle;
import static dev.opan.utils.miscellaneous.BlockUtil.*;

@RegisterModule(name = "PistonKick", description = "Piston Kick hole.", category = Module.Category.COMBAT)
public class PistonKickModule extends Module {
    public CategorySetting Misc = new CategorySetting("Misc", "");
    public ModeSetting autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", new CategorySetting.Visibility(Misc), "Silent", InventoryUtils.SWITCH_MODES);
    private final BooleanSetting torch = new BooleanSetting("Torch", "", new CategorySetting.Visibility(Misc),false);
    private final BooleanSetting selfGround = new BooleanSetting("SelfGround", "",new CategorySetting.Visibility(Misc),true);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround","", new CategorySetting.Visibility(Misc),false);
    private final BooleanSetting autoDisable = new BooleanSetting("AutoDisable","", new CategorySetting.Visibility(Misc),true);
    private final BooleanSetting noEating = new BooleanSetting("EatingPause","",new CategorySetting.Visibility(Misc),true);
    private final BooleanSetting inventory = new BooleanSetting("InventorySwap","",new CategorySetting.Visibility(Misc),true);

    public CategorySetting Rotation = new CategorySetting("Rotation", "");
    private final BooleanSetting rotate = new BooleanSetting("Rotate", "Rotate",new CategorySetting.Visibility(Rotation),true);
    private final BooleanSetting yawDeceive = new BooleanSetting("Yaw", "", new CategorySetting.Visibility(Rotation),true);
    public CategorySetting runnabl = new CategorySetting("Runnable", "");

    public BooleanSetting asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", new CategorySetting.Visibility(runnabl),true);

    public CategorySetting DelayY = new CategorySetting("Delay-Range", "");
    public NumberSetting updateDelay = new NumberSetting("UpdateDelay", "", new CategorySetting.Visibility(DelayY),100, 0, 1000);

    public NumberSetting Delay = new NumberSetting("PlaceDelay", "", new CategorySetting.Visibility(DelayY),100, 0, 1000);

    public NumberSetting range = new NumberSetting("Range", "", new CategorySetting.Visibility(DelayY),5.0, 0.0, 6.0);
    public NumberSetting placeRange = new NumberSetting("PlaceRange", "", new CategorySetting.Visibility(DelayY),5.0, 0.0, 6.0);
    public NumberSetting surroundCheck = new NumberSetting("Surround", "Surround Check Kick",  new CategorySetting.Visibility(DelayY),2, 0, 4);
        private final Timer timer = new Timer();
        private int ticks = 0;
        public static BlockPos Pos;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        public static BlockState modifyBlockState = Blocks.AIR.getDefaultState();






        //转头修复中 请勿动src 【tryPush】


      /*  private boolean tryPush(BlockPos piston, Direction direction) {
            if (!mc.world.isAir(piston.offset(direction))) return false;
            if (isTrueFacing(piston, direction) && facingCheck(piston)) {
                if (BlockUtil.clientCanPlace(piston)) {
                    boolean canPower = false;
                    if (getPlaceSide(piston, placeRange.getValue().doubleValue()) != null) {
                        Pos = piston;
                        modifyBlockState = Blocks.PISTON.getDefaultState();
                        for (Direction i : Direction.values()) {
                            if (getBlock(piston.offset(i)) == getBlockType()) {
                                canPower = true;
                                break;
                            }
                        }
                        for (Direction i : Direction.values()) {
                            if (canPower) break;
                            if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                                canPower = true;
                            }
                        }
                        Pos = null;

                        if (ticks < Delay.getValue().intValue()) {
                            ticks++;
                        }
                        if (canPower) {
                            int pistonSlot = InventoryUtils.find(Items.PISTON , 0, autoSwitch.getValue().equalsIgnoreCase("AltSwap") || autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
                            int previousSlot = mc.player.getInventory().selectedSlot;
                            Direction side = BlockUtil.getPlaceSide(piston);
                            if (side != null) {
                                 //提前7-8tick转头至活塞放置位置 然后进行活塞朝向砖头
                               // if (posRotate.getValue()) lookAt(piston.offset(side), side.getOpposite());
                                //Grim转头速度限制 停留一会
                                if (posRotate.getValue()) {
                                    if (rotate.getValue()) UltraByte.ROTATION_MANAGER.rotate(calculateAngle(Vec3d.ofCenter(piston, 0)), UltraByte.ROTATION_MANAGER.getModulePriority(this));
                                }

                                InventoryUtils.switchSlot(autoSwitch.getValue(), pistonSlot, previousSlot);
                                //活塞朝向 pistonFacing(direction.getOpposite());
                                if (yawDeceive.getValue()) pistonFacing(direction.getOpposite());
                                BlockUtil.placeBlock(piston, PisRotate.getValue(), false);
                                InventoryUtils.switchBack(autoSwitch.getValue(), pistonSlot, previousSlot);


                                float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
                                float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();

                                //转头pos一遍 可能速度过快
                                //if (rotate.getValue() && yawDeceive.getValue()) {
                                    //lookAt(piston.offset(side), side.getOpposite());
                               // }

                                //SNAP Nolag
                                if (rotate.getValue() && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) {
                                    UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);
                                }

                                for (Direction i : Direction.values()) {
                                    if (getBlock(piston.offset(i)) == getBlockType()) {
                                        if (autoDisable.getValue()) {
                                            setToggled(false);
                                        }
                                        return true;
                                }
                            }
                                for (Direction i : Direction.values()) {
                                    if (i == Direction.UP && torch.getValue()) continue;
                                    if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                                        int powerSlot = InventoryUtils.find(Items.REDSTONE_BLOCK , 0, autoSwitch.getValue().equalsIgnoreCase("AltSwap") || autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
                                        int previousSlotc = mc.player.getInventory().selectedSlot;

                                        InventoryUtils.switchSlot(autoSwitch.getValue(), powerSlot, previousSlotc);
                                        if (rotate.getValue() && yawDeceive.getValue())
                                            lookAt(piston.offset(i), i.getOpposite());

                                        BlockUtil.placeBlock(piston.offset(i), RedRotate.getValue(), false);
                                        InventoryUtils.switchBack(autoSwitch.getValue(), powerSlot, previousSlotc);
                                        return true;
                                    }
                                }
                                return true;
                            }
                        }
                    }

                    else {
                        Direction powerFacing = null;
                        for (Direction i : Direction.values()) {
                            if (i == Direction.UP && torch.getValue()) continue;
                            if (powerFacing != null) break;
                            Pos = piston.offset(i);
                            modifyBlockState = getBlockType().getDefaultState();
                            if (BlockUtil.getPlaceSide(piston) != null) {
                                powerFacing = i;
                            }
                            Pos = null;
                            if (powerFacing != null && !canPlace(piston.offset(powerFacing))) {
                                powerFacing = null;
                            }
                        }
                            if (powerFacing != null) {
                                int powerSlot = InventoryUtils.find(Items.REDSTONE_BLOCK, 0, autoSwitch.getValue().equalsIgnoreCase("AltSwap") || autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
                                int oldSlot = mc.player.getInventory().selectedSlot;

                                InventoryUtils.switchSlot(autoSwitch.getValue(), powerSlot, oldSlot);

                                //提前转头
                               // if (posRotate.getValue() && yawDeceive.getValue()) {
                                  //  lookAt(piston.offset(powerFacing), powerFacing.getOpposite());
                               // }

                                BlockUtil.placeBlock(piston.offset(powerFacing), RedRotate.getValue(), false);
                                InventoryUtils.switchBack(autoSwitch.getValue(), powerSlot, oldSlot);

                                Pos = piston.offset(powerFacing);
                                modifyBlockState = getBlockType().getDefaultState();
                                 int pistonSlot = InventoryUtils.find(Items.PISTON , 0, autoSwitch.getValue().equalsIgnoreCase("AltSwap") || autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
                                int old = mc.player.getInventory().selectedSlot;

                                Direction side = BlockUtil.getPlaceSide(piston);
                                if (side != null) {
                                    //活塞的提前转头
                                  //  if (posRotate.getValue()) {
                                  //      lookAt(piston.offset(side), side.getOpposite());
                                  //      lookAt(piston, side.getOpposite());
                                        if (posRotate.getValue()) {
                                            if (rotate.getValue()) UltraByte.ROTATION_MANAGER.rotate(calculateAngle(Vec3d.ofCenter(piston, 0)), UltraByte.ROTATION_MANAGER.getModulePriority(this));
                                        }
                                  //  }

                                    if (yawDeceive.getValue()) {
                                        pistonFacing(direction.getOpposite());
                                    }


                                    InventoryUtils.switchSlot(autoSwitch.getValue(), pistonSlot, old);

                                    BlockUtil.placeBlock(piston, false, false);

                                    InventoryUtils.switchBack(autoSwitch.getValue(), pistonSlot, old);


                                    // if (rotate.getValue() && yawDeceive.getValue()) {
                                   //     lookAt(piston.offset(side), side.getOpposite());
                                   // }

                                    float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
                                    float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();

                                    //No lag
                                    if (rotate.getValue() && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) {
                                        UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);
                                    }
                                }
                                Pos = null;
                                return true;
                            }
                    }
                }
            }
            BlockState state = mc.world.getBlockState(piston);
            if (state.getBlock() instanceof PistonBlock && getBlockState(piston).get(FacingBlock.FACING) == direction) {
                for (Direction i : Direction.values()) {
                    if (getBlock(piston.offset(i)) == getBlockType()) {
                        if (autoDisable.getValue()) {
                            setToggled(false);
                            return true;
                        }
                        return false;
                    }
                }
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP && torch.getValue()) continue;
                    if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                        int powerSlot = InventoryUtils.find(Items.REDSTONE_BLOCK , 0, autoSwitch.getValue().equalsIgnoreCase("AltSwap") || autoSwitch.getValue().equalsIgnoreCase("AltPickup") ? 35 : 8);
                        int oldSlot = mc.player.getInventory().selectedSlot;

                        InventoryUtils.switchSlot(autoSwitch.getValue(), powerSlot, oldSlot);

                        //提前转头
                      //  if (posRotate.getValue() && yawDeceive.getValue()) {
                      //      lookAt(piston.offset(i), i.getOpposite());
                      //     lookAt(piston.offset(i), i.getOpposite());
                      //  }
                        if (posRotate.getValue()) {
                            if (rotate.getValue()) UltraByte.ROTATION_MANAGER.rotate(calculateAngle(Vec3d.ofCenter(piston, 0)), UltraByte.ROTATION_MANAGER.getModulePriority(this));
                        }
                        BlockUtil.placeBlock(piston.offset(i), RedRotate.getValue(), false);

                        InventoryUtils.switchBack(autoSwitch.getValue(), powerSlot, oldSlot);


                        return true;
                    }

                }

                ticks = 0;
            }
            return false;
        }
*/

    private boolean tryPush(BlockPos piston, Direction direction) {
        if (!mc.world.isAir(piston.offset(direction))) return false;
        if (isTrueFacing(piston, direction) && facingCheck(piston)) {
            if (BlockUtil.clientCanPlace(piston)) {
                boolean canPower = false;
                if (getPlaceSide(piston, placeRange.getValue().intValue()) != null) {
                    Pos = piston;
                    modifyBlockState = Blocks.PISTON.getDefaultState();
                    for (Direction i : Direction.values()) {
                        if (getBlock(piston.offset(i)) == getBlockType()) {
                            canPower = true;
                            break;
                        }
                    }
                    for (Direction i : Direction.values()) {
                        if (canPower) break;
                        if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                            canPower = true;
                        }
                    }
                    Pos = null;

                    if (canPower) {
                        int pistonSlot = findClass(PistonBlock.class);
                        Direction side = BlockUtil.getPlaceSide(piston);
                        if (side != null) {
                            if (rotate.getValue()) UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());
                            if (yawDeceive.getValue()) pistonFacing(direction.getOpposite());
                            int old = mc.player.getInventory().selectedSlot;
                            doSwap(pistonSlot);
                            BlockUtil.placeBlock(piston, false, false);
                            if (inventory.getValue()) {
                                doSwap(pistonSlot);
                                syncInventory();
                            } else {
                                doSwap(old);
                            }
                            if (rotate.getValue() && yawDeceive.getValue()) UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());

                            float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
                            float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();
                          //  if (rotate.getValue() && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);

                            for (Direction i : Direction.values()) {
                                if (getBlock(piston.offset(i)) == getBlockType()) {
                                    if (autoDisable.getValue()) {
                                        setToggled(false);
                                    }
                                    return true;
                                }
                            }
                            for (Direction i : Direction.values()) {
                                if (i == Direction.UP && torch.getValue()) continue;
                                if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                                    int oldSlot = mc.player.getInventory().selectedSlot;
                                    int powerSlot = findBlock(getBlockType());
                                    doSwap(powerSlot);
                                    BlockUtil.placeBlock(piston.offset(i), rotate.getValue(), false);
                                    if (inventory.getValue()) {
                                        doSwap(powerSlot);
                                        syncInventory();
                                    } else {
                                        doSwap(oldSlot);
                                    }
                                    return true;
                                }
                            }

                            return true;
                        }
                    }
                } else {
                    Direction powerFacing = null;
                    for (Direction i : Direction.values()) {
                        if (i == Direction.UP && torch.getValue()) continue;
                        if (powerFacing != null) break;
                        Pos = piston.offset(i);
                        modifyBlockState = getBlockType().getDefaultState();
                        if (BlockUtil.getPlaceSide(piston) != null) {
                            powerFacing = i;
                        }
                        Pos = null;
                        if (powerFacing != null && !canPlace(piston.offset(powerFacing))) {
                            powerFacing = null;
                        }
                    }
                    if (powerFacing != null) {
                        int oldSlot = mc.player.getInventory().selectedSlot;
                        int powerSlot = findBlock(getBlockType());
                        doSwap(powerSlot);
                        BlockUtil.placeBlock(piston.offset(powerFacing), rotate.getValue(), false);
                        if (inventory.getValue()) {
                            doSwap(powerSlot);
                            syncInventory();
                        } else {
                            doSwap(oldSlot);
                        }
/*                        if (mine.getValue()) {
                            PacketMine.INSTANCE.mine(piston.offset(powerFacing));
                        }*/

                        Pos = piston.offset(powerFacing);
                        modifyBlockState = getBlockType().getDefaultState();
                        int pistonSlot = findClass(PistonBlock.class);
                        Direction side = BlockUtil.getPlaceSide(piston);
                        if (side != null) {
                            if (rotate.getValue()) UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());

                            if (yawDeceive.getValue()) pistonFacing(direction.getOpposite());
                            int old = mc.player.getInventory().selectedSlot;
                            doSwap(pistonSlot);
                            BlockUtil.placeBlock(piston, false, false);
                            if (inventory.getValue()) {
                                doSwap(pistonSlot);
                               syncInventory();
                            } else {
                                doSwap(old);
                            }
                            if (rotate.getValue() && yawDeceive.getValue()) UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());

                            float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
                            float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();
                       //      if (rotate.getValue() && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);

                        }
                        Pos = null;
                        return true;
                    }
                }
            }
        }
        BlockState state = mc.world.getBlockState(piston);
        if (state.getBlock() instanceof PistonBlock && getBlockState(piston).get(FacingBlock.FACING) == direction) {
            for (Direction i : Direction.values()) {
                if (getBlock(piston.offset(i)) == getBlockType()) {
                    if (autoDisable.getValue()) {
                        setToggled(false);
                        return true;
                    }
                    return false;
                }
            }
            for (Direction i : Direction.values()) {
                if (i == Direction.UP && torch.getValue()) continue;
                if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                    int oldSlot = mc.player.getInventory().selectedSlot;
                    int powerSlot = findBlock(getBlockType());
                    doSwap(powerSlot);
                    BlockUtil.placeBlock(piston.offset(i), rotate.getValue(), false);
                    if (inventory.getValue()) {
                        doSwap(powerSlot);
                        syncInventory();
                    } else {
                        doSwap(oldSlot);
                    }
                    return true;
                }
            }
        }
        return false;
    }

   /* private boolean tryPush(BlockPos piston, Direction direction) {
        if (mc.world != null && !mc.world.isAir(piston.offset(direction))) return false;

            if (isTrueFacing(piston, direction) && facingCheck(piston)) {
                if (BlockUtil.clientCanPlace(piston)) {
                    boolean canPower = false;
                    if (getPlaceSide(piston, placeRange.getValue().intValue()) != null) {
                        Pos = piston;
                        modifyBlockState = Blocks.PISTON.getDefaultState();
                        for (Direction i : Direction.values()) {
                            if (getBlock(piston.offset(i)) == Blocks.REDSTONE_BLOCK) {
                                canPower = true;
                                break;
                            }
                        }
                        for (Direction i : Direction.values()) {
                            if (canPower) break;
                            if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                                canPower = true;
                            }
                        }
                        Pos = null;

                        if (canPower) {
                            int pistonSlot = findClass(PistonBlock.class);
                            Direction side = BlockUtil.getPlaceSide(piston);
                            if (side != null) {
                                if (rotate.getValue()){
                                   // UltraByte.ROTATION_MANAGER.rotate(RotationUtils.getRotations(Vec3d.of(piston)), this);
                                    UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());
                                }
                                pistonFacing(direction.getOpposite());
                                int old = mc.player.getInventory().selectedSlot;
                                doSwap(pistonSlot);
                                BlockUtil.placeBlock(piston, false, false);
                                if (inventory.getValue()) {
                                    doSwap(pistonSlot);
                                    syncInventory();
                                } else {
                                    doSwap(old);
                                }
                                UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());
                               // UltraByte.ROTATION_MANAGER.rotate(RotationUtils.getRotations(Vec3d.of(piston)), this);
                                // if (rotate.getValue() && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);

                               // UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());

                                for (Direction i : Direction.values()) {
                                    if (getBlock(piston.offset(i)) == Blocks.REDSTONE_BLOCK) {
                                        if (autoDisable.getValue()) {
                                            setToggled(false);
                                        }
                                        return true;
                                    }
                                }
                                for (Direction i : Direction.values()) {
                                    if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                                        int oldSlot = mc.player.getInventory().selectedSlot;
                                        int powerSlot = findBlock(Blocks.REDSTONE_BLOCK);
                                        doSwap(powerSlot);

                                        BlockUtil.placeBlock(piston.offset(i), rotate.getValue(), false);
                                        if (inventory.getValue()) {
                                            doSwap(powerSlot);
                                            syncInventory();
                                        } else {
                                            doSwap(oldSlot);
                                        }
                                        return true;
                                    }
                                }

                                return true;
                            }
                        }
                    } else {
                        Direction powerFacing = null;
                        for (Direction i : Direction.values()) {
                            if (powerFacing != null) break;
                            Pos = piston.offset(i);
                            modifyBlockState = Blocks.REDSTONE_BLOCK.getDefaultState();
                            if (BlockUtil.getPlaceSide(piston) != null) {
                                powerFacing = i;
                            }
                            Pos = null;
                            if (powerFacing != null && !canPlace(piston.offset(powerFacing))) {
                                powerFacing = null;
                            }
                        }
                        if (powerFacing != null) {
                            int oldSlot = mc.player.getInventory().selectedSlot;
                            int powerSlot = findBlock(Blocks.REDSTONE_BLOCK);
                            doSwap(powerSlot);
                            BlockUtil.placeBlock(piston.offset(powerFacing), rotate.getValue(), false);
                            if (inventory.getValue()) {
                                doSwap(powerSlot);
                                syncInventory();
                            } else {
                                doSwap(oldSlot);
                            }

                            Pos = piston.offset(powerFacing);
                            modifyBlockState = Blocks.REDSTONE_BLOCK.getDefaultState();
                            int pistonSlot = findClass(PistonBlock.class);
                            Direction side = BlockUtil.getPlaceSide(piston);
                            if (side != null) {
                                if (rotate.getValue()){
                                  //      UltraByte.ROTATION_MANAGER.rotate(RotationUtils.getRotations(Vec3d.of(piston)), this);
                                    UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());
                                }
                                    //UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());
                                if (yawDeceive.getValue()) pistonFacing(direction.getOpposite());
                                int old = mc.player.getInventory().selectedSlot;
                                doSwap(pistonSlot);
                                BlockUtil.placeBlock(piston, false, false);
                                if (inventory.getValue()) {
                                    doSwap(pistonSlot);
                                    syncInventory();
                                } else {
                                    doSwap(old);
                                }
                                UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());
                             //   UltraByte.ROTATION_MANAGER.rotate(RotationUtils.getRotations(Vec3d.of(piston)), this);

                                float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
                                float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();
                                //if (rotate.getValue() && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);

                          //      UltraByte.ROTATION_MANAGER.Spam(piston.offset(side), side.getOpposite());
                            }
                            Pos = null;
                            return true;
                        }
                    }
                }
            }
            if (getBlock(piston) instanceof PistonBlock && getBlockState(piston).get(FacingBlock.FACING) == direction) {
                for (Direction i : Direction.values()) {
                    if (getBlock(piston.offset(i)) == Blocks.REDSTONE_BLOCK) {
                        if (autoDisable.getValue()) {
                            setToggled(false);
                            return true;
                        }
                        return false;
                    }
                }
                for (Direction i : Direction.values()) {
                    if (canPlace(piston.offset(i), placeRange.getValue().intValue())) {
                        int oldSlot = mc.player.getInventory().selectedSlot;
                        int powerSlot = findBlock(Blocks.REDSTONE_BLOCK);
                        doSwap(powerSlot);
                        BlockUtil.placeBlock(piston.offset(i), rotate.getValue(), false);
                        if (inventory.getValue()) {
                            doSwap(powerSlot);
                            syncInventory();
                        } else {
                            doSwap(oldSlot);
                        }

                        //  if (rotate.getValue() && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);

                        return true;
                    }
                }
            }
        return false;
    }*/










    static boolean isTargetHere(BlockPos pos, Entity target) {
        return new Box(pos).intersects(target.getBoundingBox());
    }


    @SubscribeEvent
    public void onDisable() {
        ticks = 0;
    }
    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!timer.passedMs(updateDelay.getValue().intValue())) return;
        if (selfGround.getValue() && !mc.player.isOnGround()) {
            return;
        }
        if (findBlock(getBlockType()) == -1 || findClass(PistonBlock.class) == -1) {
            if (autoDisable.getValue()) setToggled(false);
            return;
        }
        if (noEating.getValue() && mc.player.isUsingItem())
            return;
        Runnable runnable = () -> {
            for (PlayerEntity player : getEnemies(range.getValue().intValue())) {
                if (!canPush(player)) continue;

            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN) continue;
                BlockPos pos = getEntityPos(player).offset(i);
                if (isTargetHere(pos, player)) {
                    if (mc.world.canCollide(player, new Box(pos))) {
                        if (tryPush(getEntityPos(player).offset(i.getOpposite()), i)) {
                            timer.reset();
                            return;
                        }
                        if (tryPush(getEntityPos(player).offset(i.getOpposite()).up(), i)) {
                            timer.reset();
                            return;
                        }
                    }
                }
            }

                float[] offset = new float[]{-0.25f, 0f, 0.25f};
                for (float x : offset) {
                    for (float z : offset) {
                        BlockPosX playerPos = new BlockPosX(player.getX() + x, player.getY() + 0.5, player.getZ() + z);
                        for (Direction i : Direction.values()) {
                            if (i == Direction.UP || i == Direction.DOWN) continue;
                            BlockPos pos = playerPos.offset(i);
                            if (isTargetHere(pos, player)) {
                                if (mc.world.canCollide(player, new Box(pos))) {
                                    if (tryPush(playerPos.offset(i.getOpposite()), i)) {
                                        timer.reset();
                                        return;
                                    }
                                    if (tryPush(playerPos.offset(i.getOpposite()).up(), i)) {
                                        timer.reset();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

                if (!mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 2.5, player.getZ())))) {
                    for (Direction i : Direction.values()) {
                        if (i == Direction.UP || i == Direction.DOWN) continue;
                        BlockPos pos = getEntityPos(player).offset(i);
                        Box box = player.getBoundingBox().offset(new Vec3d(i.getOffsetX(), i.getOffsetY(), i.getOffsetZ()));
                        if (getBlock(pos.up()) != Blocks.PISTON_HEAD && !mc.world.canCollide(player, box.offset(0, 1, 0)) && !isTargetHere(pos, player)) {
                            if (tryPush(getEntityPos(player).offset(i.getOpposite()).up(), i)) {
                                timer.reset();
                                return;
                            }
                            if (tryPush(getEntityPos(player).offset(i.getOpposite()), i)) {
                                timer.reset();
                                return;
                            }
                        }
                    }
                }

                for (float x : offset) {
                    for (float z : offset) {
                        BlockPosX playerPos = new BlockPosX(player.getX() + x, player.getY() + 0.5, player.getZ() + z);
                        for (Direction i : Direction.values()) {
                            if (i == Direction.UP || i == Direction.DOWN) continue;
                            BlockPos pos = playerPos.offset(i);
                            if (isTargetHere(pos, player)) {
                                if (tryPush(playerPos.offset(i.getOpposite()).up(), i)) {
                                    timer.reset();
                                    return;
                                }
                                if (tryPush(playerPos.offset(i.getOpposite()), i)) {
                                    timer.reset();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            if (autoDisable.getValue()) setToggled(false);
        };

        if (asynchronous.getValue()) executor.submit(runnable);
        else runnable.run();

    }
        private boolean facingCheck(BlockPos pos) {
            if (UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).lowVersion.getValue()) {
                Direction direction = getDirectionFromEntityLiving(pos, mc.player);
                return direction != Direction.UP && direction != Direction.DOWN;
            }
            return true;
        }
        private boolean isTrueFacing(BlockPos pos, Direction facing) {
            if (yawDeceive.getValue()) return true;
            Direction side = BlockUtil.getPlaceSide(pos);
            if (side == null) return false;
            Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
            float[] rotation = RotationUtils.getRotations(directionVec);
            return getFacingOrder(rotation[0], rotation[1]).getOpposite() == facing;
        }


    public static boolean isInWeb(PlayerEntity player) {
        Vec3d playerPos = player.getPos();
        for (float x : new float[]{0, 0.3F, -0.3f}) {
            for (float z : new float[]{0, 0.3F, -0.3f}) {
                for (int y : new int[]{0, 1, 2}) {
                    BlockPos pos = new BlockPosX(playerPos.getX() + x, playerPos.getY(), playerPos.getZ() + z).up(y);
                    if (isTargetHere(pos, player) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private Boolean canPush(PlayerEntity player) {
        if (onlyGround.getValue() && !player.isOnGround()) return false;
        if (isInWeb(player)) return false;
        float[] offset = new float[]{-0.25f, 0f, 0.25f};

        int progress = 0;

        if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX() + 1, player.getY() + 0.5, player.getZ())))) progress++;
        if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX() - 1, player.getY() + 0.5, player.getZ())))) progress++;
        if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() + 1)))) progress++;
        if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() - 1)))) progress++;

        for (float x : offset) {
            for (float z : offset) {
                BlockPosX playerPos = new BlockPosX(player.getX() + x, player.getY() + 0.5, player.getZ() + z);
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN) continue;
                    BlockPos pos = playerPos.offset(i);
                    if (isTargetHere(pos, player)) {
                        if (mc.world.canCollide(player, new Box(pos))) {
                            return true;
                        }
                        if (progress > surroundCheck.getValue().intValue() - 1) {
                            return true;
                        }
                    }
                }
            }
        }

        if (!mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 2.5, player.getZ())))) {
            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN) continue;
                BlockPos pos = getEntityPos(player).offset(i);
                Box box = player.getBoundingBox().offset(new Vec3d(i.getOffsetX(), i.getOffsetY(), i.getOffsetZ()));
                if (getBlock(pos.up()) != Blocks.PISTON_HEAD && !mc.world.canCollide(player, box.offset(0, 1, 0)) && !isTargetHere(pos, player)) {
                    if (mc.world.canCollide(player, new Box(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ())))) {
                        return true;
                    }
                }
            }
        }

        return progress > surroundCheck.getValue().intValue() - 1 || isHard(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ()));
    }
        private Block getBlock(BlockPos pos) {
            return mc.world.getBlockState(pos).getBlock();
        }

        private Block getBlockType() {
            if (torch.getValue()) {
                return Blocks.REDSTONE_TORCH;
            }
            return Blocks.REDSTONE_BLOCK;
        }

        private BlockState getBlockState(BlockPos pos) {
            return mc.world.getBlockState(pos);
        }


    public static ArrayList<Direction> checkAxis(double diff, Direction negativeSide, Direction positiveSide, boolean bothIfInRange) {
        ArrayList<Direction> valid = new ArrayList<>();
        if (diff < -0.5) {
            valid.add(negativeSide);
        }
        if (diff > 0.5) {
            valid.add(positiveSide);
        }
        if (bothIfInRange) {
            if (!valid.contains(negativeSide)) valid.add(negativeSide);
            if (!valid.contains(positiveSide)) valid.add(positiveSide);
        }
        return valid;
    }

    public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || ignoreCrystal && entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity)
                continue;
            return true;
        }
        return false;
    }
    public static boolean canPlace(BlockPos pos, double distance) {
        if (getPlaceSide(pos, distance) == null) return false;
        if (!canReplace(pos)) return false;
        return !hasEntity(pos, false);
    }

    public static double distanceToXZ(final double x, final double z, double x2, double z2) {
        final double dx = x2 - x;
        final double dz = z2 - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distanceToXZ(final double x, final double z) {
        return distanceToXZ(x, z, mc.player.getX(), mc.player.getZ());
    }

    public static boolean canPlace(BlockPos pos) {
        return canPlace(pos, 1000);
    }


    public  void syncInventory() {
        if (mc.player != null) {
            Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }
    }

    static int lastSlot = -1;
    static int lastSelect = -1;

    public static void switchToSlot(int slot) {
        if (mc.player != null) {
            mc.player.getInventory().selectedSlot = slot;
        }
        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }
    public static void inventorySwap(int slot, int selectedSlot) {
        if (slot == lastSlot) {
            switchToSlot(lastSelect);
            lastSlot = -1;
            lastSelect = -1;
            return;
        }
        if (slot - 36 == selectedSlot) return;
        if (mc.interactionManager != null) {
            if (mc.player != null) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, selectedSlot, SlotActionType.SWAP, mc.player);
            }
        }
    }

    public static int findBlockInventorySlot(Block block) {
        return findItemInventorySlot(block.asItem());
    }
    public static int findItemInventorySlot(Item item) {
        for (int i = 0; i < 45; ++i) {
            ItemStack stack = null;
            if (mc.player != null) {
                stack = mc.player.getInventory().getStack(i);
            }
            if (stack != null && stack.getItem() == item) return i < 9 ? i + 36 : i;
        }
        return -1;
    }
    private void doSwap(int slot) {
        if (inventory.getValue()) {
            inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            switchToSlot(slot);
        }
    }
    public int findBlock(Block blockIn) {
        if (inventory.getValue()) {
            return findBlockInventorySlot(blockIn);
        } else {
            return findBlock(blockIn);
        }
    }
    public int findClass(Class clazz) {
        if (inventory.getValue()) {
            return findClassInventorySlot(clazz);
        } else {
            return findClass(clazz);
        }
    }

    public static int findClassInventorySlot(Class clazz) {
        for (int i = 0; i < 45; ++i) {
            ItemStack stack = null;
            if (mc.player != null) {
                stack = mc.player.getInventory().getStack(i);
            }
            if (stack == ItemStack.EMPTY) continue;
            if (stack != null && clazz.isInstance(stack.getItem())) {
                return i < 9 ? i + 36 : i;
            }
            if (stack != null && (!(stack.getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem) stack.getItem()).getBlock())))
                continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }
    public static BlockPos getEntityPos(Entity entity) {
        return new BlockPosX(entity.getPos());
    }
    public static BlockPos getEntityPos(Entity entity, boolean fix) {
        return new BlockPosX(entity.getPos(), fix);
    }


    public static Direction getPlaceSide(BlockPos pos, double distance) {
        double dis = 114514;
        Direction side = null;
        for (Direction i : Direction.values()) {
            if (canClick(pos.offset(i)) && !canReplace(pos.offset(i))) {
                if (!isStrictDirection(pos.offset(i), i.getOpposite())) continue;
                double vecDis = 0;
                if (mc.player != null) {
                    vecDis = mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos().add(i.getVector().getX() * 0.5, i.getVector().getY() * 0.5, i.getVector().getZ() * 0.5));
                }
                if (MathHelper.sqrt((float) vecDis) > distance) {
                    continue;
                }
                if (side == null || vecDis < dis) {
                    side = i;
                    dis = vecDis;
                }
            }
        }
        return side;
    }

    public static Direction getDirectionFromEntityLiving(BlockPos pos, LivingEntity entity) {
        if (Math.abs(entity.getX() - ((double) pos.getX() + 0.5)) < 2.0 && Math.abs(entity.getZ() - ((double)pos.getZ() + 0.5)) < 2.0) {
            double d0 = entity.getY() + (double)entity.getEyeHeight(entity.getPose());
            if (d0 - (double)pos.getY() > 2.0) {
                return Direction.UP;
            }

            if ((double)pos.getY() - d0 > 0.0) {
                return Direction.DOWN;
            }
        }

        return entity.getHorizontalFacing().getOpposite();
    }

    public static Direction getFacingOrder(float yaw, float pitch) {
        // 转换为弧度（保留原始符号）
        final float pitchRadians = pitch * MathHelper.RADIANS_PER_DEGREE;
        final float yawRadians = -yaw * MathHelper.RADIANS_PER_DEGREE;

        // 计算三角函数值
        final float sinPitch = MathHelper.sin(pitchRadians);
        final float cosPitch = MathHelper.cos(pitchRadians);
        final float sinYaw = MathHelper.sin(yawRadians);
        final float cosYaw = MathHelper.cos(yawRadians);

        // 确定方向基向量
        final boolean isEastWestPositive = sinYaw > 0.0F;
        final boolean isUpDownPositive = sinPitch < 0.0F; // Minecraft 坐标系中向下为正
        final boolean isSouthNorthPositive = cosYaw > 0.0F;

        // 计算绝对分量
        final float absHorizontal = isEastWestPositive ? sinYaw : -sinYaw;
        final float absVertical = isUpDownPositive ? -sinPitch : sinPitch;
        final float absForward = isSouthNorthPositive ? cosYaw : -cosYaw;

        // 计算投影分量
        final float horizontalProjection = absHorizontal * cosPitch;
        final float forwardProjection = absForward * cosPitch;

        // 确定候选方向
        final Direction horizontalDir = isEastWestPositive ? Direction.EAST : Direction.WEST;
        final Direction verticalDir = isUpDownPositive ? Direction.UP : Direction.DOWN;
        final Direction forwardDir = isSouthNorthPositive ? Direction.SOUTH : Direction.NORTH;

        // 分量比较决策树
        if (absHorizontal > absForward) {
            return (absVertical > horizontalProjection) ? verticalDir : horizontalDir;
        } else {
            return (absVertical > forwardProjection) ? verticalDir : forwardDir;
        }
    }


    public static List<PlayerEntity> getEnemies(double range) {
        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isValid(player, range)) continue;
            list.add(player);
        }
        return list;
    }

    public static boolean isValid(Entity entity, double range) {
        boolean invalid = entity == null
                || !entity.isAlive()
                || entity.equals(mc.player)
                || entity instanceof PlayerEntity player
                && (UltraByte.FRIEND_MANAGER.contains(player.getName().getString()))
                || mc.player.getPos().distanceTo(entity.getPos()) > range;
        return !invalid;
    }

    public boolean isHard(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.NETHERITE_BLOCK || block == Blocks.ENDER_CHEST || block == Blocks.BEDROCK;
    }


    public void pistonFacing(Direction i) {
        if (i == Direction.EAST) {
            if (mc.player != null) {
                UltraByte.ROTATION_MANAGER.packetRotate(-90,0);
            }
        } else if (i == Direction.WEST) {
            if (mc.player != null) {
                UltraByte.ROTATION_MANAGER.packetRotate(90,0);
            }
        } else if (i == Direction.NORTH) {
            if (mc.player != null) {
                UltraByte.ROTATION_MANAGER.packetRotate(180,0);
            }
        } else if (i == Direction.SOUTH) {
            if (mc.player != null) {
                UltraByte.ROTATION_MANAGER.packetRotate(0,0);

            }
        }
    }





}
