package dev.ultrabyte.utils.miscellaneous;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.modules.impl.core.RotationsModule;
import dev.ultrabyte.utils.IMinecraft;
import dev.ultrabyte.utils.minecraft.NetworkUtils;
import dev.ultrabyte.utils.rotations.RotationUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.ultrabyte.managers.RotationManager.*;


public class BlockUtil implements IMinecraft {
    public static final List<Block> shiftBlocks = Arrays.asList(
            Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE,
            Blocks.BIRCH_TRAPDOOR, Blocks.BAMBOO_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.CHERRY_TRAPDOOR,
            Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER,
            Blocks.ACACIA_TRAPDOOR, Blocks.ENCHANTING_TABLE, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX
    );

    public static boolean canSee(BlockPos pos, Direction side) {
        Vec3d testVec = pos.toCenterPos().add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);
        HitResult result = null;
        if (mc.world != null) {
            if (mc.player != null) {
                result = mc.world.raycast(new RaycastContext(getEyesPos(), testVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
            }
        }
        return result == null || result.getType() == HitResult.Type.MISS;
    }

    public static Vec3d getEyesPos() {
        if (mc.player != null) {
            return mc.player.getEyePos();
        }
        return null;
    }



    public static boolean clientCanPlace(BlockPos pos) {
        return clientCanPlace(pos, false);
    }
    public static boolean clientCanPlace(BlockPos pos, boolean ignoreCrystal) {
        if (!canReplace(pos)) return false;
        return !hasEntity(pos, ignoreCrystal);
    }

    public static List<Entity> getEntities(Box box) {
        List<Entity> list = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == null) continue;
            if (entity.getBoundingBox().intersects(box)) {
                list.add(entity);
            }
        }
        return list;
    }

    public static List<EndCrystalEntity> getEndCrystals(Box box) {
        List<EndCrystalEntity> list = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal) {
                if (crystal.getBoundingBox().intersects(box)) {
                    list.add(crystal);
                }
            }
        }
        return list;
    }
    public static boolean hasEntity(BlockPos pos, boolean ignoreCrystal) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || ignoreCrystal && entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity)
                continue;
            return true;
        }
        return false;
    }

    public static boolean hasCrystal(BlockPos pos) {
        for (Entity entity : getEndCrystals(new Box(pos))) {
            if (!entity.isAlive() || !(entity instanceof EndCrystalEntity))
                continue;
            return true;
        }
        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreCrystal && entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity)
                continue;
            return true;
        }
        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        for (Entity entity : getEntities(new Box(pos))) {
            if (!entity.isAlive() || ignoreItem && entity instanceof ItemEntity || ignoreCrystal && entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity)
                continue;
            return true;
        }
        return false;
    }


    public static Direction getBestNeighboring(BlockPos pos, Direction facing) {
        for (Direction i : Direction.values()) {
            if (facing != null && pos.offset(i).equals(pos.offset(facing, -1)) || i == Direction.DOWN) continue;
            if (getPlaceSide(pos, false, true) != null) return i;
        }
        Direction bestFacing = null;
        double distance = 0;
        for (Direction i : Direction.values()) {
            if (facing != null && pos.offset(i).equals(pos.offset(facing, -1)) || i == Direction.DOWN) continue;
            if (getPlaceSide(pos) != null) {
                if (bestFacing == null || mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()) < distance) {
                    bestFacing = i;
                    distance = mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos());
                }
            }
        }
        return bestFacing;
    }

    public static void placeCrystal(BlockPos pos, boolean rotate) {
        boolean offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5,facing.getVector().getY() * 0.5,facing.getVector().getZ() * 0.5);
        if (rotate) {
            UltraByte.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(vec));
        }
        clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
    }
    public static final CopyOnWriteArrayList<BlockPos> placedPos = new CopyOnWriteArrayList<>();

    public static void placeBlock(BlockPos pos, boolean rotate) {
        placeBlock(pos, rotate, true);
    }


    public static void lookAt(BlockPos pos, Direction side) {
        float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
        float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();
        final Vec3d hitVec = pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
        UltraByte.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(hitVec));
        if (UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);
    }

    public static void placeBlock(BlockPos pos, boolean rotate, boolean packet) {
        if (pos == null) {
            UltraByte.LOGGER.warn("Attempted to place block at null position");
            return;
        }

        Direction side = getPlaceSide(pos);
        if (side == null) {
            UltraByte.LOGGER.debug("No valid placement side found for {}", pos);
            return;
        }

        synchronized (placedPos) { // 线程安全操作
            if (!placedPos.add(pos)) {
                UltraByte.LOGGER.debug("Position {} already placed", pos);
                return;
            }
        }

        BlockPos targetPos = pos.offset(side);
        if (!World.isValid(targetPos)) {
            UltraByte.LOGGER.warn("Invalid target position: {}", targetPos);
            return;
        }

        clickBlock(pos.offset(side), side.getOpposite(), rotate, Hand.MAIN_HAND, null,false,true);
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }


    public static void clickBlock(BlockPos pos, Direction side, boolean rotate) {
        clickBlock(pos, side, rotate, Hand.MAIN_HAND);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand) {
        clickBlock(pos, side, rotate, hand, null,false,true);
    }

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, boolean packet) {
        clickBlock(pos, side, rotate, Hand.MAIN_HAND, null,false,true);
    }


    // 修复后的完整代码（已考虑线程安全、状态同步和防作弊机制）



      /* public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, Runnable callback, boolean crystalDestruction, boolean render) {
            if (mc.world == null || mc.player == null) return;

            // 1. 状态快照
            final float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
            final float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();
            final boolean originalSprint = mc.player.isSprinting();
            final boolean originalSneak = mc.player.isSneaking();

            try {

                // 3. 线程安全执行回调
                if (callback != null) {
                    mc.execute(callback::run);
                }

                // 4. 计算精确命中点
                Vec3d hitVec = WorldUtils.getHitVector(pos, side);
                BlockHitResult result = new BlockHitResult(hitVec, side, pos, false);

                // 5. 状态预处理
                //handleSprintState(originalSprint, true);  // 停止冲刺
               // handleSneakState(pos, side, originalSneak); // 自动潜行

                // 6. 原子化网络操作
                NetworkUtils.sendSequencedPacket(sequence -> {
                    // 6.1 视角旋转
                    if (rotate) {
                        try {
                            float[] rotations = RotationUtils.getRotations(hitVec.x, hitVec.y, hitVec.z);
                            if(rotations.length < 2) {
                                throw new IllegalArgumentException("Invalid rotations array length");
                            }

                            // 规范化角度值
                            float yaw = MathHelper.wrapDegrees(rotations[0]);
                            float pitch = MathHelper.clamp(rotations[1], -90f, 90f);

                            UltraByte.ROTATION_MANAGER.packetRotate(
                                    yaw,
                                    pitch
                            );
                        } catch (Exception e) {
                            UltraByte.LOGGER.error("Rotation calculation failed", e);
                            // 回退到玩家当前视角
                            UltraByte.ROTATION_MANAGER.packetRotate(
                                    mc.player.getYaw(),
                                    mc.player.getPitch()
                            );
                        }
                    }

                    // 6.2 发送交互数据包
                    return new PlayerInteractBlockC2SPacket(hand, result, sequence);
                });

                // 7. 客户端动画同步
                Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new HandSwingC2SPacket(hand));

            } catch (Exception e) {
                UltraByte.LOGGER.error("Block interaction failed", e);
            } finally {
                // 8. 状态恢复（智能回滚）
               // handleSneakState(pos, side, originalSneak); // 恢复潜行
              //  handleSprintState(originalSprint, false);    // 恢复冲刺

                // 9. 视角回弹（带防抖检测）
                if (rotate && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);

                if (render) {
                    RenderPosition renderPosition = new RenderPosition(pos);
                    if(!UltraByte.RENDER_MANAGER.renderPositions.contains(renderPosition)) UltraByte.RENDER_MANAGER.renderPositions.add(renderPosition);
                }
            }
        }

        // 状态处理方法（防止冗余数据包）
        private static void handleSprintState(boolean original, boolean modifying) {
            if (modifying && original) {
                mc.player.networkHandler.sendPacket(
                        new ClientCommandC2SPacket(mc.player,
                                ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }

        // 智能潜行管理（带方块类型检测）
        private static void handleSneakState(BlockPos pos, Direction side, boolean original) {
            BlockState state = mc.world.getBlockState(pos.offset(side));
            boolean shouldSneak = WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(state.getBlock());

            if (shouldSneak != original) {
                ClientCommandC2SPacket.Mode mode = shouldSneak ?
                        ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY :
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;

                mc.player.networkHandler.sendPacket(
                        new ClientCommandC2SPacket(mc.player, mode));
            }
        }*/



  /*  public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, Runnable runnable, boolean crystalDestruction, boolean render) {
         //SNAP
        float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
        float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();
        //线程
        if (runnable != null) runnable.run();

        Vec3d vec3d = pos.toCenterPos();
        BlockPos offsetPosition;

        offsetPosition = pos.offset(side);
        vec3d = vec3d.add(side.getOffsetX() / 2.0, side.getOffsetY() / 2.0, side.getOffsetZ() / 2.0);
        if (rotate) {
            // UltraByte.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(directionVec));
            UltraByte.ROTATION_MANAGER.packetRotate(RotationUtils.getRotations(vec3d.getX(), vec3d.getY(), vec3d.getZ()));
        }
        boolean sprint = mc.player.isSprinting();
        boolean sneak = WorldUtils.RIGHT_CLICKABLE_BLOCKS.contains(mc.world.getBlockState(offsetPosition).getBlock()) && !mc.player.isSneaking();

        if (sprint) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        if (sneak) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        // mc.interactionManager.interactBlock(mc.player, hand, result);

        BlockHitResult result = new BlockHitResult(vec3d, side, pos, false);
       // NetworkUtils.sendSequencedPacket(sequence -> new PlayerInteractBlockC2SPacket(hand, result, sequence));
        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(WorldUtils.getHitVector(pos, side), side, pos, false), 0));

        //mc.interactionManager.interactBlock(mc.player, hand, result);
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));

        if (sneak) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        if (sprint) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

        if (rotate && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);

        if (render) {
            RenderPosition renderPosition = new RenderPosition(pos);
            if(!UltraByte.RENDER_MANAGER.renderPositions.contains(renderPosition)) UltraByte.RENDER_MANAGER.renderPositions.add(renderPosition);
        }
    }*/

    private static boolean isValidDirection(BlockPos neighborPos, Direction facing) {
        RaycastContext context = new RaycastContext(
                mc.player.getEyePos(),
                neighborPos.toCenterPos(),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        );
        BlockHitResult hit = mc.world.raycast(context);
        return hit != null && hit.getSide() == facing;
    }

    public static Direction getPlaceSide(BlockPos pos) {
        return getPlaceSide(pos,true);
    }
    public static Direction getPlaceSide(BlockPos pos, boolean strict) {
        if (pos == null || !World.isValid(pos)) return null;

        final Vec3d eyePos = mc.player.getEyePos(); // 预计算不变值
        Direction bestSide = null;
        double minSqDistance = Double.MAX_VALUE;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);

            // 合并重复的条件判断
            if (!canClick(neighborPos) ||
                    mc.world.getBlockState(neighborPos).isAir() ||
                    canReplace(neighborPos)) {
                continue;
            }

            if (strict && !isValidDirection(neighborPos, dir.getOpposite())) {
                continue;
            }

            // 使用欧几里得距离平方优化性能
            Vec3d center = Vec3d.ofCenter(neighborPos);
            double dx = eyePos.x - center.x;
            double dy = eyePos.y - center.y;
            double dz = eyePos.z - center.z;
            double sqDistance = dx*dx + dy*dy + dz*dz;

            if (sqDistance < minSqDistance) {
                bestSide = dir;
                minSqDistance = sqDistance;
            }
        }

        return bestSide;
    }
    public static Direction getPlaceSide(BlockPos pos, boolean strict, boolean legit) {
        if (pos == null) return null;
        double dis = 114514;
        Direction side = null;
        for (Direction i : Direction.values()) {
            if (canClick(pos.offset(i)) && !canReplace(pos.offset(i))) {
                if (legit) {
                    if (!canSee(pos.offset(i), i.getOpposite())) continue;
                }
                if (strict) {
                    if (!isStrictDirection(pos.offset(i), i.getOpposite())) continue;
                }
                double vecDis = mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos().add(i.getVector().getX() * 0.5, i.getVector().getY() * 0.5, i.getVector().getZ() * 0.5));
                if (side == null || vecDis < dis) {
                    side = i;
                    dis = vecDis;
                }
            }
        }
        return side;
    }

    public static double distanceToXZ(final double x, final double z, double x2, double z2) {
        final double dx = x2 - x;
        final double dz = z2 - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distanceToXZ(final double x, final double z) {
        return distanceToXZ(x, z, mc.player.getX(), mc.player.getZ());
    }


    public static Direction getClickSide(BlockPos pos) {
        Direction side = null;
        double range = 100;
        for (Direction i : Direction.values()) {
            if (!canSee(pos, i)) continue;
            if (MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range) continue;
            side = i;
            range = MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
        }
        if (side != null)
            return side;
        side = Direction.UP;
        for (Direction i : Direction.values()) {

                if (!isStrictDirection(pos, i)) continue;
                if (!mc.world.isAir(pos.offset(i))) continue;

            if (MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range) continue;
            side = i;
            range = MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
        }
        return side;
    }

    public static Direction getClickSideStrict(BlockPos pos) {
        Direction side = null;
        double range = 100;
        for (Direction i : Direction.values()) {
            if (!canSee(pos, i)) continue;
            if (MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range) continue;
            side = i;
            range = MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
        }
        if (side != null)
            return side;
        side = null;
        for (Direction i : Direction.values()) {
                if (!isStrictDirection(pos, i)) continue;
                if (!mc.world.isAir(pos.offset(i))) continue;
            if (MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range) continue;
            side = i;
            range = MathHelper.sqrt((float) mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos()));
        }
        return side;
    }

    public static boolean isStrictDirection(BlockPos pos, Direction side) {
        if (mc.player.getBlockY() - pos.getY() >= 0 && side == Direction.DOWN) return false;
         {
            if (side == Direction.UP && pos.getY() > mc.player.getEyePos().getY()) {
                return false;
            }
        }

        if ((getBlock(pos.offset(side)) == Blocks.OBSIDIAN || getBlock(pos.offset(side)) == Blocks.BEDROCK || getBlock(pos.offset(side)) == Blocks.RESPAWN_ANCHOR)) return false;
        Vec3d eyePos = getEyesPos();
        Vec3d blockCenter = pos.toCenterPos();
        ArrayList<Direction> validAxis = new ArrayList<>();
        validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, false));
        validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true));
        validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, false));
        return validAxis.contains(side);
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

    public static ArrayList<BlockEntity> getTileEntities(){
        return getLoadedChunks().flatMap(chunk -> chunk.getBlockEntities().values().stream()).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Stream<WorldChunk> getLoadedChunks(){
        int radius = Math.max(2, mc.options.getClampedViewDistance()) + 3;
        int diameter = radius * 2 + 1;

        ChunkPos center = mc.player.getChunkPos();
        ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
        ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);

        return Stream.iterate(min, pos -> {
                    int x = pos.x;
                    int z = pos.z;
                    x++;

                    if(x > max.x)
                    {
                        x = min.x;
                        z++;
                    }

                    return new ChunkPos(x, z);

                }).limit((long) diameter *diameter)
                .filter(c -> mc.world.isChunkLoaded(c.x, c.z))
                .map(c -> mc.world.getChunk(c.x, c.z)).filter(Objects::nonNull);
    }

    public static ArrayList<BlockPos> getSphere(float range) {
        return getSphere(range, mc.player.getEyePos());
    }
    public static ArrayList<BlockPos> getSphere(float range, Vec3d pos) {
        ArrayList<BlockPos> list = new ArrayList<>();
        for (double x = pos.getX() - range; x < pos.getX() + range; ++x) {
            for (double z = pos.getZ() - range; z < pos.getZ() + range; ++z) {
                for (double y = pos.getY() - range; y < pos.getY() + range; ++y) {
                    BlockPos curPos = new BlockPosX(x, y, z);
                    if (curPos.toCenterPos().distanceTo(pos) > range) continue;
                    if (!list.contains(curPos)) {
                        list.add(curPos);
                    }
                }
            }
        }
        return list;
    }

    public static Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    public static boolean canReplace(BlockPos pos) {
        if (pos.getY() >= 320) return false;
        if (placedPos.contains(pos)) {
            return true;
        }
        return mc.world.getBlockState(pos).isReplaceable();
    }

    public static boolean canClick(BlockPos pos) {
        return mc.world.getBlockState(pos).isSolid() && (!(shiftBlocks.contains(getBlock(pos)) || getBlock(pos) instanceof BedBlock) || mc.player.isSneaking());
    }


    // 在类成员变量中添加
    private static boolean validateLookDirection(Vec3d lookVec, Vec3d targetVec) {
        final double angleEpsilon = 1e-3; // 约0.057度的余弦值容差

        // 确保输入合法
        if (lookVec.lengthSquared() < 1e-20 || targetVec.lengthSquared() < 1e-20) {
            throw new IllegalArgumentException("Zero vector cannot represent direction");
        }

        // 计算单位向量点积
        double dot = lookVec.normalize().dotProduct(targetVec.normalize());
        return dot >= 1.0 - angleEpsilon;
    }

    /**
     * 计算精确的方块面点击向量
     */
    public static Vec3d calculatePreciseHitVector(BlockPos pos, Direction side) {
        // 参数校验
        if (mc.world == null);
        if (!mc.world.isChunkLoaded(pos));

        BlockState state = mc.world.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(mc.world, pos);
        if (state.getBlock() instanceof FluidBlock) {
            shape = VoxelShapes.fullCube();
        }
        // 获取有效碰撞箱集合（处理多部分）
        List<Box> boxes = new ArrayList<>();
        if (shape.isEmpty()) {
            boxes.add(new Box(0, 0, 0, 1, 1, 1));
        } else {
            shape.forEachBox((x1, y1, z1, x2, y2, z2) ->
                    boxes.add(new Box(x1, y1, z1, x2, y2, z2))
            );
        }

        Vec3d bestHit = null;
        double minDistance = Double.MAX_VALUE;
        Vec3d eyesPos = getPreciseEyesPos();

        for (Box localBox : boxes) {
            Vec3d hitVec = calculateLocalHitVector(localBox, side)
                    .add(pos.getX(), pos.getY(), pos.getZ()); // 转换为世界坐标

            RaycastContext context = new RaycastContext(
                    eyesPos,
                    hitVec,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
            );

            BlockHitResult result = mc.world.raycast(context);
            if (result != null && result.getBlockPos().equals(pos)) {
                double dist = eyesPos.squaredDistanceTo(hitVec);
                if (dist < minDistance) {
                    bestHit = hitVec;
                    minDistance = dist;
                }
            }
        }

        return bestHit != null ? bestHit : Vec3d.ofCenter(pos);
    }

    private static Vec3d calculateLocalHitVector(Box localBox, Direction side) {
        double epsilon = 1e-5;
        double dx = Math.max(localBox.maxX - localBox.minX, epsilon);
        double dy = Math.max(localBox.maxY - localBox.minY, epsilon);
        double dz = Math.max(localBox.maxZ - localBox.minZ, epsilon);

        return new Vec3d(
                localBox.minX + dx * (0.5 + 0.5 * side.getOffsetX()),
                localBox.minY + dy * (0.5 + 0.5 * side.getOffsetY()),
                localBox.minZ + dz * (0.5 + 0.5 * side.getOffsetZ())
        );
    }

    private static Vec3d getPreciseEyesPos() {
        double yOffset = mc.player.getEyeHeight(mc.player.getPose());
        if (mc.player.isSneaking()) {
            yOffset -= 0.08; // 潜行视线修正
        }
        return new Vec3d(
                mc.player.getX(),
                mc.player.getY() + yOffset,
                mc.player.getZ()
        );
    }


    // 精确命中点计算（带边缘检测）
    // 三维向量射线投射（考虑方块碰撞箱）
    private static float[] calculatePreciseRotation(BlockPos targetPos, Direction side) {
        // 状态验证
        if (mc.player == null || mc.world == null) {
            throw new IllegalStateException("Game context not initialized");
        }

        // 获取有效碰撞箱（处理空碰撞箱）
        BlockState state = mc.world.getBlockState(targetPos);
        VoxelShape shape = state.getCollisionShape(mc.world, targetPos);
        if (shape.isEmpty()) {
            shape = VoxelShapes.fullCube(); // 默认完整方块
        }

        // 获取玩家精确视线原点（考虑潜行状态）
        Vec3d eyesPos = new Vec3d(
                mc.player.getX(),
                mc.player.getEyeY() - (mc.player.isSneaking() ? 0.08 : 0), // 修正潜行偏移
                mc.player.getZ()
        );

        // 多碰撞箱处理（选择最近有效面）
        List<Box> collisionBoxes = new ArrayList<>();
        shape.forEachBox((x1, y1, z1, x2, y2, z2) ->
                collisionBoxes.add(new Box(x1, y1, z1, x2, y2, z2))
        );

        // 计算各候选点的最佳命中点
        Vec3d bestHit = null;
        double minDistance = Double.MAX_VALUE;

        for (Box box : collisionBoxes) {
            // 计算当前碰撞箱的面中心
            Vec3d hitVec = calculateFaceCenter(targetPos, box, side);

            // 视线可达性验证
            BlockHitResult hitResult = mc.world.raycast(new RaycastContext(
                    eyesPos,
                    hitVec,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
            ));

            // 筛选有效且最近的命中点
            if (hitResult != null && hitResult.getBlockPos().equals(targetPos)) {
                double dist = eyesPos.squaredDistanceTo(hitVec);
                if (dist < minDistance) {
                    bestHit = hitVec;
                    minDistance = dist;
                }
            }
        }

        if (bestHit == null) {
            throw new IllegalStateException("No valid collision point found");
        }

        // 计算精确旋转（带数值校验）
        try {
            return RotationUtils.getRotations(bestHit.x, bestHit.y, bestHit.z);
        } catch (ArithmeticException e) {
            throw new IllegalStateException("Rotation calculation failed", e);
        }
    }

    private static Vec3d calculateFaceCenter(BlockPos pos, Box box, Direction side) {
        // 处理微标方块（如雪层）
        double epsilon = 1e-5;
        double dx = Math.max(box.maxX - box.minX, epsilon);
        double dy = Math.max(box.maxY - box.minY, epsilon);
        double dz = Math.max(box.maxZ - box.minZ, epsilon);

        // 计算面中心（带方向偏移修正）
        return new Vec3d(
                pos.getX() + box.minX + dx * (0.5 + 0.5 * side.getOffsetX()),
                pos.getY() + box.minY + dy * (0.5 + 0.5 * side.getOffsetY()),
                pos.getZ() + box.minZ + dz * (0.5 + 0.5 * side.getOffsetZ())
        );
    }

    private static final Random RANDOM = new SecureRandom();
    private static final float HUMANLIKE_DEVIATION = 1.5f;









    //重写

    // 增强版旋转计算（包含边缘检测和运动补偿）
   /* public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, Runnable callback, boolean crystalDestruction, boolean render) {
        if (mc.world == null || mc.player == null) return;

        // 捕获玩家状态
        MovementState.capture();

        try {
            // 计算精确的命中点
            Vec3d hitVec = calculatePreciseHitVector(pos, side);
            if (!canReach(pos, hitVec)) {
                UltraByte.LOGGER.warn("Block placement obstructed");
                return;
            }

            // 异步执行回调
            if (callback != null) {
                mc.execute(callback::run);
            }

            BlockHitResult result = new BlockHitResult(hitVec, side, pos, false);

            if (rotate) {
                // 计算严格的旋转角度
                float[] rotations = calculateStrictRotations(pos, side);
                if (rotations.length < 2) {
                    throw new IllegalStateException("Invalid rotation");
                }

                // 验证物理状态
                if (!validatePhysicalState(pos, side)) {
                    throw new IllegalStateException("Physical state invalid");
                }

                // 发送严格的旋转和放置包
                sendStrictRotationPacket(rotations, result, hand);

                // 标记旋转正在进行
                isRotationInProgress = true;
            } else {
                NetworkUtils.sendSequencedPacket(sequence -> new PlayerInteractBlockC2SPacket(hand, result, sequence));
            }

            // 客户端动画同步
            if (!isRotationInProgress) {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
            }

        } catch (Exception e) {
            UltraByte.LOGGER.error("Block interaction failed", e);
        } finally {
            // 恢复玩家状态
            MovementState.restore();
            isRotationInProgress = false;

            // 渲染标记
            if (render) {
                RenderPosition renderPosition = new RenderPosition(pos);
                if (!UltraByte.RENDER_MANAGER.renderPositions.contains(renderPosition)) {
                    UltraByte.RENDER_MANAGER.renderPositions.add(renderPosition);
                }
            }
        }
    }*/
    private static final Queue<Packet<?>> PACKET_QUEUE = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private static void sendStrictRotationPacket(float[] rotations, BlockHitResult result, Hand hand) {
        int currentSequence = SEQUENCE.incrementAndGet();

        // 1. 发送旋转包
        PACKET_QUEUE.offer(new PlayerMoveC2SPacket.LookAndOnGround(
                rotations[0], rotations[1], mc.player.isOnGround(),mc.player.horizontalCollision
        ));

        // 2. 发送放置包
        PACKET_QUEUE.offer(new PlayerInteractBlockC2SPacket(
                hand, result, currentSequence
        ));

        // 异步处理队列
        mc.execute(() -> {
            while (!PACKET_QUEUE.isEmpty()) {
                Packet<?> packet = PACKET_QUEUE.poll();
                mc.getNetworkHandler().sendPacket(packet);
                try {
                    // 动态延迟控制（基于玩家速度）
                    PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                    int ping = playerListEntry != null ? playerListEntry.getLatency() : 0;
                    int delay = Math.max(10, (int)(ping / 2));

                    if (mc.player.getVelocity().lengthSquared() > 0.1) {
                        delay += 20; // 跑步或跳跃时增加延迟
                    }
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
    private static class MovementState {
        static boolean wasSprinting;
        static boolean wasSneaking;
        static Vec3d lastMovementVec;
        static long lastStateUpdate;

        static void capture() {
            wasSprinting = mc.player.isSprinting();
            wasSneaking = mc.player.isSneaking();
            lastMovementVec = mc.player.getVelocity();
            lastStateUpdate = System.currentTimeMillis();
        }

        static void restore() {
            if (System.currentTimeMillis() - lastStateUpdate < 50) {
                mc.player.setSprinting(wasSprinting);
                mc.player.setSneaking(wasSneaking);
            }
        }
    }





    //重写2

    // 新增队列控制成员变量
    private static final Queue<BlockTask> blockQueue = new ConcurrentLinkedQueue<>();
    private static boolean isProcessing = false;

    // 新增任务封装类
    private static class BlockTask {
        final BlockPos pos;
        final Direction side;
        final Runnable callback;
        // 其他必要参数...

        public BlockTask(BlockPos pos, Direction side, Runnable callback) {
            this.pos = pos;
            this.side = side;
            this.callback = callback;
        }
    }

    // 修改后的点击方法
    public static void enqueueBlockPlacement(BlockPos pos, Direction side, Runnable callback) {
        blockQueue.offer(new BlockTask(pos, side, callback));
        if (!isProcessing) {
            processNextBlock();
        }
    }

    // 顺序放置三个方块
   // enqueueBlockPlacement(pos1, Direction.UP, () -> {
   //     UltraByte.LOGGER.info("First block placed");
   // });
//
   // enqueueBlockPlacement(pos2, Direction.EAST, () -> {
    //    UltraByte.LOGGER.info("Second block placed");
    //});

    //enqueueBlockPlacement(pos3, Direction.DOWN, () -> {
    //    UltraByte.LOGGER.info("Third block placed");
    //});

    private static void processNextBlock() {
        if (blockQueue.isEmpty()) {
            isProcessing = false;
            return;
        }

        isProcessing = true;
        BlockTask task = blockQueue.poll();

        new Thread(() -> {
            // 等待前次转头完成
            while (isRotationInProgress) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 原子性放置操作
            clickBlock(task.pos, task.side, true, Hand.MAIN_HAND, () -> {
                if (task.callback != null) {
                    mc.execute(task.callback::run);
                }

                // 触发下一个任务
                mc.execute(() -> processNextBlock());
            }, false, true);
        }).start();
    }

    // 修改原方法签名

    private static BlockPos pendingBlockPos;
    private static Hand pendingHand;
    private static boolean isRotationInProgress;

    public static void clickBlock(BlockPos pos, Direction side, boolean rotate, Hand hand, Runnable callback, boolean crystalDestruction, boolean render) {
        if (mc.world == null || mc.player == null) return;

        // 1. 状态快照（增加视角状态）
        final float prevYaw = UltraByte.ROTATION_MANAGER.getServerYaw();
        final float prevPitch = UltraByte.ROTATION_MANAGER.getServerPitch();
        final boolean originalSprint = mc.player.isSprinting();
        final boolean originalSneak = mc.player.isSneaking();
        Vec3d hitVec = calculatePreciseHitVector(pos, side);
        if (!canReach(pos, hitVec)) {
            UltraByte.LOGGER.warn("Block placement obstructed");
            return;
        }


        try {
            if (callback != null) {
                mc.execute(callback::run);
            }


            BlockHitResult result = new BlockHitResult(hitVec, side, pos, false);
            NetworkUtils.sendSequencedPacket(sequence -> {
                if (rotate) {
                    try {
                        // 增强的转头验证
                        float[] rotations = RotationUtils.getRotations(hitVec.x, hitVec.y, hitVec.z);
                        if (rotations.length < 2 || !canReach(pos, hitVec)) {
                            throw new IllegalStateException("Invalid rotation or path obstructed");
                        }
                       // Vec3d lookVec = RotationManager.getLookVector(rotations[0], rotations[1]);
                        //if (!validateLookDirection(lookVec, hitVec)) {
                        //    throw new IllegalStateException("Look direction mismatch");
                        //}
                        float[] angle = calculateAngle(Vec3d.of(pos));
                        UltraByte.ROTATION_MANAGER.packetRotate(angle[0], angle[1]);
                        isRotationInProgress = false;
                        pendingBlockPos = pos;
                        pendingHand = hand;
                        isRotationInProgress = true;

                    } catch (Exception e) {
                        UltraByte.LOGGER.error("Rotation calculation failed", e);
                        UltraByte.ROTATION_MANAGER.packetRotate(
                                mc.player.getYaw(),
                                mc.player.getPitch()
                        );
                        return new PlayerInteractBlockC2SPacket(hand, result, sequence);
                    }
                }
                return new PlayerInteractBlockC2SPacket(hand, result, sequence);
            });
            if (!isRotationInProgress) {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
            }

        } catch (Exception e) {
            UltraByte.LOGGER.error("Block interaction failed", e);
        } finally {
            if (!isRotationInProgress) {
                if (rotate && UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBack.getValue()) {
                    UltraByte.ROTATION_MANAGER.SNAPPacketRotate(prevYaw, prevPitch);
                }
            }

            if (render) {
                RenderPosition renderPosition = new RenderPosition(pos);
                if (!UltraByte.RENDER_MANAGER.renderPositions.contains(renderPosition)) {
                    UltraByte.RENDER_MANAGER.renderPositions.add(renderPosition);
                }
            }
        }
    }

}
