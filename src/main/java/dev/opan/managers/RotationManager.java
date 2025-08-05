package dev.opan.managers;

import dev.opan.UltraByte;
import lombok.Getter;
import dev.opan.events.SubscribeEvent;
import dev.opan.events.impl.*;
import dev.opan.mixins.accessors.EntityAccessor;
import dev.opan.modules.Module;
import dev.opan.modules.impl.core.RotationsModule;
import dev.opan.utils.IMinecraft;
import dev.opan.utils.animations.Easing;
import dev.opan.utils.rotations.Rotation;
import dev.opan.utils.system.MathUtils;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;

import static dev.opan.utils.miscellaneous.BlockUtil.calculatePreciseHitVector;
import static dev.opan.utils.rotations.RotationUtils.getRotations;

public class RotationManager implements IMinecraft {
    private final PriorityBlockingQueue<Rotation> queue = new PriorityBlockingQueue<>(11, this::compareRotations);
    @Getter private Rotation rotation = null;

    private float prevFixYaw;

    private float prevYaw;
    private float prevPitch;

    @Getter private float serverYaw;
    @Getter private float serverPitch;

    private float prevRenderYaw, prevRenderPitch;
    private long lastRenderTime = 0L;

    private static final HashMap<String, Integer> PRIORITIES = new HashMap<>();
    static {
        //数字越大优先级越高 分别调用同优先级会进行排队
        PRIORITIES.put("KillAura", 2);
        PRIORITIES.put("PistonKick", 3);
        PRIORITIES.put("SelfFill", 4);
        PRIORITIES.put("AutoCrystal", 5);
        PRIORITIES.put("SpeedMine", 6);
        PRIORITIES.put("Phase", 7);
    }




    public RotationManager() {
        UltraByte.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent(priority = Integer.MIN_VALUE)
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        queue.removeIf(rotation -> System.currentTimeMillis() - rotation.getTime() > 100);
        rotation = queue.peek();

        if (rotation == null) return;
        lastRenderTime = System.currentTimeMillis();
    }

    @SubscribeEvent(priority = Integer.MAX_VALUE)
    public void onUpdateMovement(UpdateMovementEvent event) {
        if (rotation == null) return;

        prevYaw = mc.player.getYaw();
        prevPitch = mc.player.getPitch();

        mc.player.setYaw(rotation.getYaw());
        mc.player.setPitch(rotation.getPitch());
    }

    @SubscribeEvent(priority = Integer.MIN_VALUE)
    public void onUpdateMovement$POST(UpdateMovementEvent.Post event) {
        if (rotation == null) return;

        mc.player.setYaw(prevYaw);
        mc.player.setPitch(prevPitch);
    }

    @SubscribeEvent
    public void onUpdateVelocity(UpdateVelocityEvent event) {
        if (mc.player == null) return;
        if (!UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) return;
        if (rotation == null) return;

        event.setVelocity(EntityAccessor.invokeMovementInputToVelocity(event.getMovementInput(), event.getSpeed(), rotation.getYaw()));
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onKeyboardTick(KeyboardTickEvent event) {
        if (mc.player == null || mc.world == null || mc.player.isRiding()) return;
        if (!UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) return;
        if (rotation == null) return;

        float movementForward = event.getMovementForward();
        float movementSideways = event.getMovementSideways();

        float delta = (mc.player.getYaw() - rotation.getYaw()) * MathHelper.RADIANS_PER_DEGREE;

        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);

        event.setMovementForward(Math.round(movementForward * cos + movementSideways * sin));
        event.setMovementSideways(Math.round(movementSideways * cos - movementForward * sin));
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent event) {
        if (mc.player == null || mc.world == null || mc.player.isRiding()) return;
        if (!UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) return;
        if (rotation == null) return;

        prevFixYaw = mc.player.getYaw();
        mc.player.setYaw(rotation.getYaw());
    }

    @SubscribeEvent
    public void onPlayerJump$POST(PlayerJumpEvent.Post event) {
        if (mc.player == null || mc.world == null || mc.player.isRiding()) return;
        if (!UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) return;
        if (rotation == null) return;

        mc.player.setYaw(prevFixYaw);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (!packet.changesLook()) return;

            serverYaw = packet.getYaw(mc.player.getYaw());
            serverPitch = packet.getPitch(mc.player.getPitch());
        }
    }

    public void rotate(float[] rotations, int priority) {
        rotate(rotations[0], rotations[1], priority);
    }

    public void rotate(float yaw, float pitch, int priority) {
        queue.removeIf(rotation -> rotation.getModule() == null && rotation.getPriority() == priority);
        queue.add(new Rotation(yaw, pitch, priority));
    }

    public void rotate(float[] rotations, Module module) {
        rotate(rotations[0], rotations[1], module);
    }

    public void rotate(float yaw, float pitch, Module module) {
        queue.removeIf(rotation -> rotation.getModule() == module);
        queue.add(new Rotation(yaw, pitch, module, getModulePriority(module)));
    }

    public void PistonRotate(float yaw, float pitch, int priority){
          rotate(yaw, pitch, priority);
    }

    public void rotate(float[] rotations, Module module, int priority) {
        rotate(rotations[0], rotations[1], module, priority);
    }

    public void rotate(float yaw, float pitch, Module module, int priority) {
        queue.removeIf(rotation -> rotation.getModule() == module);
        queue.add(new Rotation(yaw, pitch, module, priority));
    }

    public void packetRotate(float[] rotations) {
        packetRotate(rotations[0], rotations[1]);
    }

    public void SNAPPacketRotate(float yaw, float pitch) {
        switch (UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).snapBackMode.getValue()) {
            case "ServerFull" -> {
                if (serverYaw == yaw && serverPitch == pitch) {
                    Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.Full(UltraByte.POSITION_MANAGER.getServerX(), UltraByte.POSITION_MANAGER.getServerY(), UltraByte.POSITION_MANAGER.getServerZ(), yaw, pitch, UltraByte.POSITION_MANAGER.isServerOnGround(), mc.player.horizontalCollision));
                  }

            }
            case "ServerLook" -> {
                if (serverYaw == yaw && serverPitch == pitch) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, UltraByte.POSITION_MANAGER.isServerOnGround(), mc.player.horizontalCollision));
                }
            }
            case "ClientFull" -> {
                if (mc.player != null) {
                    mc.player.networkHandler.sendPacket
                            (new PlayerMoveC2SPacket.Full
                                    (
                                            mc.player.getX(),
                                            mc.player.getY(),
                                            mc.player.getZ(),
                                            yaw,
                                            pitch,
                                            mc.player.isOnGround(),
                                            mc.player.horizontalCollision
                                    ));
                  }
            }
            case "ClientLook" -> {
                if (mc.player != null) {
                    Objects.requireNonNull(
                                    mc.getNetworkHandler())
                            .sendPacket(new
                                    PlayerMoveC2SPacket.
                                            LookAndOnGround(
                                    yaw,
                                    pitch,
                                    mc.player.isOnGround(),
                                    mc.player.horizontalCollision
                            ));
                }

            }
            case "ServerYaw-pitch" -> {
                if (mc.player != null) {
                    if (serverYaw == yaw && serverPitch == pitch) {
                        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.Full(
                                mc.player.getX(),
                                mc.player.getY(),
                                mc.player.getZ(),
                                yaw,
                                pitch,
                                UltraByte.POSITION_MANAGER.isServerOnGround(),
                                mc.player.horizontalCollision
                        ));
                    }
                }
            }
        }
}






    // 常量池定义（提升可维护性）
    private static final double VERTICAL_THRESHOLD_SQ = 1.0E-8; // (1e-4)^2
    private static final float YAW_OFFSET = 90.0f;
    private static final double HIT_VEC_OFFSET = 0.5;

    // 新增平滑过渡相关成员变量
    private float currentYaw;
    private float currentPitch;
    private static final float DEFAULT_SMOOTH_STEP = 0.5f;
    private static final float DEFAULT_JITTER_STRENGTH = 0.02f;

    public float[] getRotation(Vec3d eyesPos, Vec3d vec) {
        // 保持原有实现不变
        if (eyesPos == null || vec == null) return new float[]{0f, 0f};

        final double diffX = vec.x - eyesPos.x;
        final double diffY = vec.y - eyesPos.y;
        final double diffZ = vec.z - eyesPos.z;

        final double diffXZSq = diffX * diffX + diffZ * diffZ;
        if (diffXZSq < VERTICAL_THRESHOLD_SQ) {
            return new float[]{0f, (float)(diffY > 0 ? -90 : 90)};
        }

        if (Math.abs(diffX) < 1e-9 && Math.abs(diffY) < 1e-9 && Math.abs(diffZ) < 1e-9) {
            return new float[]{0f, 0f};
        }

        final double diffXZ = Math.sqrt(diffXZSq);
        final float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - YAW_OFFSET);
        final float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));

        return new float[]{
                MathHelper.wrapDegrees(yaw),
                MathHelper.wrapDegrees(pitch)
        };
    }

    public float[] getRotation(Vec3d vec) {
        return getRotation(getEyesPos(mc.player), vec);
    }

    public void Spam(BlockPos pos, Direction side) {
        final Vec3d sideVector = Vec3d.of(side.getVector());
        final Vec3d offset = new Vec3d(
                sideVector.getX() * HIT_VEC_OFFSET,
                sideVector.getY() * HIT_VEC_OFFSET,
                sideVector.getZ() * HIT_VEC_OFFSET
        );
        Spam(pos.toCenterPos().add(offset));
    }

    public void Spam(Vec3d directionVec) {
        rotate(getRotations(directionVec),1);
        SpamRotate(directionVec);
    }

    public void SpamRotate(Vec3d directionVec) {
        final RotationsModule module = UltraByte.MODULE_MANAGER.getModule(RotationsModule.class);
        if (module == null) return;

        final float[] angle = getRotation(directionVec);
        final float actualYaw = UltraByte.ROTATION_MANAGER.prevFixYaw;
        final float actualPitch = UltraByte.ROTATION_MANAGER.prevPitch;

        // 同步当前角度
        currentYaw = actualYaw;
        currentPitch = actualPitch;

        if (!module.Spam_grimRotation.getValue()) {
            if (module.useSmoothRotation.getValue()) {
                smoothRotation(angle[0], angle[1], module);
            } else {
                packetRotate(angle[0], angle[1]);
            }
            return;
        }

        final int fovThreshold = module.fov.getValue().intValue();
        final float yawDiff = MathHelper.angleBetween(angle[0], actualYaw);
        final float pitchDiff = Math.abs(angle[1] - actualPitch);

        if (yawDiff < fovThreshold && pitchDiff < fovThreshold) return;

        if (module.useSmoothRotation.getValue()) {
            smoothRotation(angle[0], angle[1], module);
        } else {
            packetRotate(angle[0], angle[1]);
        }
    }

    // 新增平滑过渡方法（带随机扰动）
// 改进后的平滑过渡方法
    private void smoothRotation(float targetYaw, float targetPitch, RotationsModule module) {
        // 获取配置参数并约束范围
        final float smoothStep = MathHelper.clamp(module.smoothFactor.getValue().intValue(), 0.01f, 1.0f);

        // 计算最短路径角度差（处理-180~180环绕）
        final float deltaYaw = MathHelper.wrapDegrees(targetYaw - currentYaw);
        final float deltaPitch = targetPitch - currentPitch;

        // 应用精准线性插值
        currentYaw = MathHelper.wrapDegrees(currentYaw + deltaYaw * smoothStep);
        currentPitch = MathHelper.clamp(currentPitch + deltaPitch * smoothStep, -90f, 90f);

        // 发送精确角度数据包
        packetRotate(currentYaw, currentPitch);
    }




    public void packetRotate(float yaw, float pitch) {
        switch (UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).GrimRotations.getValue()) {
            case "WcAx3ps" -> {
                if (UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).Strict_grimRotation.getValue()) {
                    if (mc.player != null) {
                        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket
                                (new PlayerMoveC2SPacket.Full(
                                        mc.player.getX(),
                                        mc.player.getY(),
                                        mc.player.getZ(),
                                        yaw,
                                        pitch,
                                        mc.player.isOnGround(),
                                        mc.player.horizontalCollision
                                ));
                    }
               }else {
                    if (mc.player != null) {
                        Objects.requireNonNull(
                                mc.getNetworkHandler()).sendPacket
                                (new PlayerMoveC2SPacket.LookAndOnGround(
                                        yaw,
                                        pitch,
                                        mc.player.isOnGround(),
                                        mc.player.horizontalCollision
                                ));
                    }
                }
            }
            case "Catty" -> {
                if (mc.player != null) {
                    mc.player.networkHandler.sendPacket
                            (new PlayerMoveC2SPacket.Full
                                    (
                                            mc.player.getX(),
                                            mc.player.getY(),
                                            mc.player.getZ(),
                                            yaw,
                                            pitch,
                                            UltraByte.POSITION_MANAGER.isServerOnGround(),
                                            mc.player.horizontalCollision
                                    ));
                }
            }
            case "3ar" -> {
                if (mc.player != null) {
                    mc.player.networkHandler.sendPacket
                    (new PlayerMoveC2SPacket.Full
                    (
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            yaw,
                            pitch,
                            mc.player.isOnGround(),
                            mc.player.horizontalCollision
                    ));
                }
            }
            case "AI24" -> {
                if (mc.player != null && mc.player.isOnGround()) {
                    mc.player.networkHandler.sendPacket
                            (new PlayerMoveC2SPacket.Full
                                    (
                                            mc.player.getX(),
                                            mc.player.getY(),
                                            mc.player.getZ(),
                                            yaw,
                                            pitch,
                                            mc.player.isOnGround(),
                                            mc.player.horizontalCollision
                                    ));
                }else if(UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).Rotations.getValue()){
                    mc.player.networkHandler.sendPacket
                            (new PlayerMoveC2SPacket.Full
                                    (
                                            mc.player.getX(),
                                            mc.player.getY(),
                                            mc.player.getZ(),
                                            yaw,
                                            pitch,
                                            mc.player.isOnGround(),
                                            mc.player.horizontalCollision
                                    ));
                }
            }
            case "asphyxia" -> {
                final float wrappedYaw = MathHelper.wrapDegrees(yaw);
                final float clampedPitch = MathHelper.clamp(pitch, -90f, 90f);
                if(UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).Look.getValue() && mc.player != null) {
                    mc.player.networkHandler.sendPacket(
                            new PlayerMoveC2SPacket.LookAndOnGround(
                                    wrappedYaw,
                                    clampedPitch,
                                    mc.player.isOnGround(),
                                    mc.player.horizontalCollision
                            )
                    );
                }else if(mc.player != null){
                    mc.player.networkHandler.sendPacket
                            (new PlayerMoveC2SPacket.Full
                                    (
                                            mc.player.getX(),
                                            mc.player.getY(),
                                            mc.player.getZ(),
                                            wrappedYaw,
                                            clampedPitch,
                                            mc.player.isOnGround(),
                                            mc.player.horizontalCollision
                                    ));
                }
            }
        }
    }
    public static Vec3d getEyesPos(@NotNull Entity entity) {
        return entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);
    }
    public static float @NotNull [] calculateAngle(Vec3d to) {
        return calculateAngle(getEyesPos(mc.player), to);
    }

    public static float @NotNull [] calculateAngle(@NotNull Vec3d from, @NotNull Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));

        float yD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        float pD = (float) MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))), -90f, 90f);

        return new float[]{yD, pD};
    }

    public boolean inRenderTime() {
        return System.currentTimeMillis() - lastRenderTime < 1000;
    }

    public float[] getRenderRotations() {
        float from = MathUtils.wrapAngle(prevRenderYaw), to = MathUtils.wrapAngle(rotation == null ? mc.player.getYaw() : getServerYaw());
        float delta = to - from;
        if(delta > 180) delta -= 380;
        else if(delta < -180) delta += 360;

        float yaw = MathHelper.lerp(Easing.toDelta(lastRenderTime, 1000), from, from + delta);
        float pitch = MathHelper.lerp(Easing.toDelta(lastRenderTime, 1000), prevRenderPitch, rotation == null ? mc.player.getPitch() : getServerPitch());
        prevRenderYaw = yaw;
        prevRenderPitch = pitch;

        return new float[]{yaw, pitch};
    }

    public int getModulePriority(Module module) {
        return PRIORITIES.getOrDefault(module.getName(), 0);
    }

    private int compareRotations(Rotation target, Rotation rotation) {
        if (target.getPriority() == rotation.getPriority()) return -Long.compare(target.getTime(), rotation.getTime());
        return -Integer.compare(target.getPriority(), rotation.getPriority());
    }



    //穿墙射线
    public static boolean canReach(BlockPos targetPos, Vec3d hitVec) {
        if (mc.player == null || mc.world == null) return false;
        Vec3d eyesPos = new Vec3d(
                mc.player.getX(),
                mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                mc.player.getZ()
        );
        RaycastContext context = new RaycastContext(
                eyesPos,
                hitVec,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        );
        BlockHitResult result = mc.world.raycast(context);
        return result.getType() == HitResult.Type.MISS ||
                result.getBlockPos().equals(targetPos);
    }

    public static Vec3d getLookVector(float yaw, float pitch) {
        double yawRadians = Math.toRadians(-yaw); // 取负数修正顺时针旋转
        double pitchRadians = Math.toRadians(pitch);


        double cosPitch = Math.cos(pitchRadians);
        double sinPitch = Math.sin(pitchRadians);


        double cosYaw = Math.cos(yawRadians);
        double sinYaw = Math.sin(yawRadians);

        return new Vec3d(
                sinYaw * cosPitch,
                -sinPitch,
                cosYaw * cosPitch
        ).normalize();
    }

    public static float[] calculateStrictRotations(BlockPos pos, Direction side) {
        // 获取玩家延迟
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        int ping = playerListEntry != null ? playerListEntry.getLatency() : 0;
        double latencySeconds = ping / 1000.0;

        // 计算预测位置（包含延迟补偿）
        Vec3d velocity = mc.player.getVelocity();
        Vec3d predictedPos = mc.player.getPos().add(
                velocity.x * latencySeconds,
                velocity.y * latencySeconds,
                velocity.z * latencySeconds
        );

        // 计算精确命中点
        Vec3d hitVec = calculatePreciseHitVector(pos, side);
        return getRotations(
                hitVec.x - predictedPos.x,
                hitVec.y - predictedPos.y,
                hitVec.z - predictedPos.z
        );
    }

public static boolean validatePhysicalState(BlockPos pos, Direction side) {
        // 计算精确的命中点
        Vec3d hitVec = calculatePreciseHitVector(pos, side);

        // 获取玩家的视线方向
        Vec3d lookVec = RotationManager.getLookVector(
                mc.player.getYaw(), mc.player.getPitch()
        );

        // 验证视线方向是否正确
        Vec3d toTarget = hitVec.subtract(mc.player.getPos()).normalize();
        if (lookVec.dotProduct(toTarget) < 0.95) {
            return false;
        }

        // 修复后的距离验证（两种方案任选其一）
        // 方案1：使用坐标分量
        //if (mc.player.distanceTo(hitVec.x, hitVec.y, hitVec.z) > 5.0) {
        //    return false;
        //}

         //方案2：使用 Vec3d 的 distanceTo
         if (hitVec.distanceTo(mc.player.getPos()) > 5.0) {
             return false;
         }

        return true;
    }

    //shi

    private int ticksExisted;


    public void lookAt(BlockPos pos, Direction side) {
        final Vec3d hitVec = pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5));
        lookAt(hitVec);
    }

    public void lookAt(Vec3d directionVec) {
        rotate(getRotations(directionVec),1);
        snapAt(directionVec);
    }

    public void snapAt(Vec3d directionVec) {
        float[] angle = getRotation(directionVec);
        if (UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).Spam_grimRotation.getValue()) {
            if (MathHelper.angleBetween(angle[0], UltraByte.ROTATION_MANAGER.prevFixYaw) < UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).fov.getValue().intValue() && Math.abs(angle[1] - UltraByte.ROTATION_MANAGER.prevPitch) < UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).fov.getValue().intValue()) {
                return;
            }
        }
        packetRotate(angle[0], angle[1]);
    }
}
