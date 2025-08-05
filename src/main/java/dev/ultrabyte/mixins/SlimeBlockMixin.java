package dev.ultrabyte.mixins;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.modules.impl.movement.NoSlowModule;
import dev.ultrabyte.utils.IMinecraft;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeBlock.class)
public class SlimeBlockMixin implements IMinecraft {
    @Inject(method = "onSteppedOn", at = @At("HEAD"), cancellable = true)
    private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo info) {
        if (entity == mc.player && UltraByte.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled() && UltraByte.MODULE_MANAGER.getModule(NoSlowModule.class).slimeBlocks.getValue()) {
            info.cancel();
        }
    }
}
