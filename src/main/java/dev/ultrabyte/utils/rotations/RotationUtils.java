package dev.ultrabyte.utils.rotations;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.modules.impl.core.RotationsModule;
import dev.ultrabyte.utils.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

import java.util.WeakHashMap;

public class RotationUtils implements IMinecraft {
    public static float[] getRotations(Entity entity) {
        return getRotations(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()) / 2.0, entity.getZ());
    }

    public static float[] getRotations(Vec3d vec3d) {
        return getRotations(vec3d.x, vec3d.y, vec3d.z);
    }

    public static float[] getRotations(double x, double y, double z) {
        if (mc.player == null) return new float[]{0, 0};
    //    return getSmoothRotations(mc.player, x, y, z, UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).maxJitter.getValue().intValue(),UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).smoothFactor.getValue().intValue());
        return getRotations(mc.player, x, y, z, UltraByte.MODULE_MANAGER.getModule(RotationsModule.class).maxJitter.getValue().intValue()); // 默认最大抖动 4 度
    }


    public static float[] getRotations(Entity entity, double x, double y, double z, float maxJitter) {
        // 防御性编程：检查所有输入有效性
        if (entity == null ||
                Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) ||
                !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            return new float[]{0f, 0f};
        }

        // 获取精确眼部坐标（考虑潜行、游泳等姿态）
        Vec3d eyePos = entity.getEyePos();

        // 计算坐标差（使用final防止意外修改）
        final double deltaX = x - eyePos.x;
        final double deltaY = (y - eyePos.y) * -1.0; // 正确转换为屏幕空间Y轴方向
        final double deltaZ = z - eyePos.z;

        // 计算水平距离并防止极小值
        final double horizontalSq = deltaX * deltaX + deltaZ * deltaZ;
        final double horizontalDistance = (horizontalSq < 1e-14) ? 1e-7 : Math.sqrt(horizontalSq);

        // 使用atan2计算角度（避免手动计算导致的象限错误）
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDistance));

        // 角度规范化（使用Minecraft原生方法）
        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.wrapDegrees(pitch);

        // 安全钳制pitch角度（保留0.1度缓冲防止万向节锁）
        pitch = MathHelper.clamp(pitch, -89.9f, 89.9f);

        // 受控抖动系统（使用实体随机数生成器保证同步）
        if (maxJitter > 0) {
            final Random rand = entity.getRandom();
            final float effectiveJitter = Math.min(maxJitter, 5.0f); // 安全限制最大抖动幅度

            // 生成正态分布的抖动值（更自然的随机效果）
            float yawJitter = (rand.nextFloat() - 0.5f) * 2 * effectiveJitter;
            float pitchJitter = (rand.nextFloat() - 0.5f) * 2 * effectiveJitter;

            yaw += yawJitter;
            pitch += pitchJitter;

            // 最终角度验证
            yaw = MathHelper.wrapDegrees(yaw);
            pitch = MathHelper.clamp(MathHelper.wrapDegrees(pitch), -89.9f, 89.9f);
        }

        return new float[]{yaw, pitch};
    }



    public static float[] getRotations(Direction direction) {
        return switch (direction) {
            case DOWN -> new float[]{mc.player.getYaw(), 90.0f};
            case UP -> new float[]{mc.player.getYaw(), -90.0f};
            case NORTH -> new float[]{180.0f, mc.player.getPitch()};
            case SOUTH -> new float[]{0.0f, mc.player.getPitch()};
            case WEST -> new float[]{90.0f, mc.player.getPitch()};
            case EAST -> new float[]{-90.0f, mc.player.getPitch()};
        };
    }




    private static final WeakHashMap<Entity, RotationState> ROTATION_STATES = new WeakHashMap<>();

    public static float[] getSmoothRotations(Entity entity, double x, double y, double z, float maxJitter, float smoothFactor) {
        if (entity == null) return new float[]{0, 0};

        // 获取或初始化旋转状态
        RotationState state = ROTATION_STATES.computeIfAbsent(entity, e ->
                new RotationState(e.getYaw(), e.getPitch()));

        // 计算目标角度
        float[] target = calculateBaseAngles(entity, x, y, z);

        // 应用平滑过渡
        float deltaTime = getDeltaTime(); // 需要实现获取帧时间的方法
        float[] smoothed = smoothAngles(state, target, smoothFactor, deltaTime);

        // 添加随机抖动
        applyJitter(smoothed, maxJitter);

        // 更新状态
        state.update(smoothed[0], smoothed[1]);

        return smoothed;
    }

    private static float[] calculateBaseAngles(Entity entity, double x, double y, double z) {
        Vec3d eyePos = entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);
        double deltaX = x - eyePos.x;
        double deltaY = (y - eyePos.y) * -1.0;
        double deltaZ = z - eyePos.z;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDistance));

        return new float[]{
                MathHelper.wrapDegrees(yaw),
                MathHelper.clamp(MathHelper.wrapDegrees(pitch), -90f, 90f)
        };
    }

    private static float[] smoothAngles(RotationState state, float[] target, float smoothFactor, float deltaTime) {
        // 使用指数平滑并考虑时间补偿
        float factor = 1 - (float) Math.exp(-smoothFactor * deltaTime);

        float smoothedYaw = lerpAngle(state.lastYaw, target[0], factor);
        float smoothedPitch = lerpAngle(state.lastPitch, target[1], factor);

        return new float[]{
                MathHelper.wrapDegrees(smoothedYaw),
                MathHelper.clamp(MathHelper.wrapDegrees(smoothedPitch), -90f, 90f)
        };
    }

    private static void applyJitter(float[] angles, float maxJitter) {
        if (maxJitter > 0) {
            angles[0] += (float) ((Math.random() * 2 - 1) * maxJitter);
            angles[1] += (float) ((Math.random() * 2 - 1) * maxJitter);

            angles[0] = MathHelper.wrapDegrees(angles[0]);
            angles[1] = MathHelper.clamp(angles[1], -90f, 90f);
        }
    }

    private static float lerpAngle(float current, float target, float factor) {
        float delta = MathHelper.wrapDegrees(target - current);
        return current + delta * factor;
    }

    // 需要实现获取帧时间的方法（示例）
    private static float getDeltaTime() {
        return 0.05f; // 假设20TPS
    }

    private static class RotationState {
        float lastYaw;
        float lastPitch;

        RotationState(float yaw, float pitch) {
            this.lastYaw = yaw;
            this.lastPitch = pitch;
        }

        void update(float yaw, float pitch) {
            this.lastYaw = yaw;
            this.lastPitch = pitch;
        }
    }

    public static final Vec3d getRotationVector(float yaw, float pitch) {
        float f = pitch * ((float)Math.PI / 180F);
        float g = -yaw * ((float)Math.PI / 180F);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }




    private static final double EPSILON = 1e-7;
    private static final double BLOCK_OFFSET = 0.5;


    public static BlockHitResult performRaycast(Entity entity, double maxDistance) {
        // 参数校验
        if (entity == null || entity.getWorld() == null) {
            return BlockHitResult.createMissed(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN);
        }

        // 计算视线向量
        Vec3d startPos = entity.getEyePos();
        Vec3d lookVec = entity.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(lookVec.multiply(maxDistance));

        // 配置射线检测参数
        RaycastContext context = new RaycastContext(
                startPos,
                endPos,
                RaycastContext.ShapeType.COLLIDER,  // 检测碰撞箱
                RaycastContext.FluidHandling.NONE,  // 忽略流体
                entity
        );

        // 执行检测并返回结果
        return entity.getWorld().raycast(context);
    }

    public static BlockHitResult customRaycast(float yaw, float pitch, double maxDistance) {
        // 校验
        if (mc.player == null) {
            return BlockHitResult.createMissed(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN);
        }

        // 计算视线向量
        Vec3d startPos = mc.player.getEyePos();
        Vec3d lookVec = getRotationVector(yaw, pitch);
        Vec3d endPos = startPos.add(lookVec.multiply(maxDistance));

        // 配置射线检测参数
        RaycastContext context = new RaycastContext(
                startPos,
                endPos,
                RaycastContext.ShapeType.COLLIDER,  // 检测碰撞箱
                RaycastContext.FluidHandling.NONE,  // 忽略流体
                mc.player
        );

        // 执行检测并返回结果
        return  mc.player.getWorld().raycast(context);
    }

    /**
     * 计算精确的转向角度
     * @param entity 目标实体
     * @param target 目标坐标
     * @param maxJitter 最大抖动幅度（建议0-2）
     * @return [yaw, pitch] 角度数组
     */
    public static float[] calculatePreciseRotation(Entity entity, Vec3d target, float maxJitter) {
        // 防御性空值检查
        if (entity == null || target == null) {
            return new float[]{0f, 0f};
        }

        // 获取眼部坐标（考虑潜行状态）
        Vec3d eyePos = entity.getEyePos();

        // 计算坐标差
        double deltaX = target.x - eyePos.x;
        double deltaY = target.y - eyePos.y;
        double deltaZ = target.z - eyePos.z;

        // 处理极小值防止计算错误
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (horizontalDistance < EPSILON) {
            horizontalDistance = EPSILON;
        }

        // 计算基础角度
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDistance));

        // 角度规范化
        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.clamp(MathHelper.wrapDegrees(pitch), -89.9f, 89.9f);

        // 受控抖动系统
        if (maxJitter > 0) {
            Random rand = entity.getRandom();
            float effectiveJitter = Math.min(maxJitter, 2.0f);

            yaw += (rand.nextFloat() - 0.5f) * 2 * effectiveJitter;
            pitch += (rand.nextFloat() - 0.5f) * 2 * effectiveJitter;

            // 二次规范化确保角度有效
            yaw = MathHelper.wrapDegrees(yaw);
            pitch = MathHelper.clamp(pitch, -89.9f, 89.9f);
        }

        return new float[]{yaw, pitch};
    }

    /**
     * 自动对准方块面的完整解决方案
     * @param entity 操作实体
     * @param maxDistance 最大检测距离
     */
    public static void autoAlignToBlockFace(Entity entity, double maxDistance) {
        BlockHitResult hitResult = performRaycast(entity, maxDistance);

        // 获取碰撞面中心坐标
        Direction face = hitResult.getSide();
        Vec3d targetPos = hitResult.getPos().add(
                face.getOffsetX() * BLOCK_OFFSET,
                face.getOffsetY() * BLOCK_OFFSET,
                face.getOffsetZ() * BLOCK_OFFSET
        );

        // 计算精确角度（禁用抖动）
        float[] rotations = calculatePreciseRotation(entity, targetPos, 0f);

        // 应用角度（考虑服务器同步）
        entity.setYaw(rotations[0]);
        entity.setPitch(rotations[1]);
        entity.prevYaw = rotations[0];
        entity.prevPitch = rotations[1];
    }
}
