package dev.ultrabyte.managers;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.utils.IMinecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KnockbackManager implements IMinecraft {

        // 修改为每tick衰减率（原0.65是每秒衰减率）
        private static final double DECAY_PER_TICK = 0.3; // 每tick保留30%速度
        private static final double STOP_THRESHOLD = 0.005; // 优化停止阈值
        private static final Map<UUID, Vec3d> decayingKnockbacks = new ConcurrentHashMap<>();

        public static void applyDecayingKnockback(PlayerEntity player, Vec3d knockback) {
            // 叠加时立即应用当前抗性（更符合原版逻辑）
            float resistance = 1 - player.getVelocityMultiplier();
            Vec3d adjusted = knockback.multiply(resistance);
            decayingKnockbacks.merge(player.getUuid(), adjusted, Vec3d::add);
        }

        public static void tick(PlayerEntity player) {
            UUID uuid = player.getUuid();
            Vec3d current = decayingKnockbacks.getOrDefault(uuid, Vec3d.ZERO);

            if (!current.equals(Vec3d.ZERO)) {
                // 应用逐tick衰减
                Vec3d newVelocity = current.multiply(DECAY_PER_TICK);

                // 实时更新速度（更平滑）
                player.addVelocity(newVelocity.subtract(current));
                decayingKnockbacks.put(uuid, newVelocity);

                // 优化清理条件
                if (newVelocity.lengthSquared() < STOP_THRESHOLD) {
                    decayingKnockbacks.remove(uuid);
                    player.setVelocity(player.getVelocity().subtract(newVelocity)); // 消除残余速度
                }
                UltraByte.CHAT_MANAGER.tagged("Grim" + newVelocity, "Packet");
            }
        }
}

