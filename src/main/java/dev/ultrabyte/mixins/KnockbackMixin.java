package dev.ultrabyte.mixins;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class KnockbackMixin {
 /*   @Redirect(
            method = "takeKnockback",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"
            )
    )
    private void redirectKnockback(LivingEntity entity, double x, double y, double z) {
        if (entity instanceof PlayerEntity player) {
            // 精确计算击退方向（修复原版方向问题）
            Vec3d knockback = new Vec3d(x, y, z).normalize()
                    .multiply(entity.getVelocity().length()); // 保持原强度

            KnockbackManager.applyDecayingKnockback(player, knockback);
        } else {
            entity.setVelocity(x, y, z); // 非玩家保持原逻辑
        }
    }*/
}