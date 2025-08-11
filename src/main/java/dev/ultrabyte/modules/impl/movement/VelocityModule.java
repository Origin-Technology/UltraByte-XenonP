
package dev.ultrabyte.modules.impl.movement;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.PacketReceiveAsyncEvent;
import dev.ultrabyte.events.impl.PacketReceiveSyncEvent;
import dev.ultrabyte.events.impl.PacketSendEvent;
import dev.ultrabyte.events.impl.TickEvent;
import dev.ultrabyte.mixins.accessors.EntityVelocityUpdateS2CPacketAccessor;
import dev.ultrabyte.mixins.accessors.Vec3dAccessor;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.modules.impl.core.RotationsModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.settings.impl.CategorySetting;
import dev.ultrabyte.settings.impl.ModeSetting;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.utils.minecraft.NetworkUtils;
import dev.ultrabyte.utils.miscellaneous.BlockPosX;
import dev.ultrabyte.utils.miscellaneous.BlockUtil;
import dev.ultrabyte.utils.rotations.RotationUtils;
import dev.ultrabyte.utils.system.ClientUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

@RegisterModule(name = "Velocity", description = "Modifies the amount of knockback that you receive.", category = Module.Category.MOVEMENT)
public class VelocityModule extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The method that will be used to achieve the knockback modification.", "Normal", new String[]{"Normal", "Cancel","NewGrim","Watchdog"});
    public ModeSetting modes = new ModeSetting("GrimWallMode", "The method that will be used to achieve the knockback modification.", new ModeSetting.Visibility(mode, "NewGrim"),"WallNormal", new String[]{"WallNormal", "WallCancel","WallNewGrim"});

    public NumberSetting horizontal = new NumberSetting("Horizontal", "The amount of horizontal knockback that you will receive.", new ModeSetting.Visibility(mode, "Normal", "Grim"), 0, 0, 100);
    public NumberSetting vertical = new NumberSetting("Vertical", "The amount of vertical knockback that you will receive.", new ModeSetting.Visibility(mode, "Normal", "Grim"), 0, 0, 100);
    public BooleanSetting explosions = new BooleanSetting("Explosions", "Modifies knockback received from explosions.", true);
    public BooleanSetting R = new BooleanSetting("R-c", "Modifies knockback received from explosions.",new ModeSetting.Visibility(mode, "NewGrim"), true);
    public BooleanSetting pause = new BooleanSetting("Pause", "Pauses the velocity for a certain duration whenever you get rubberbanded.", new ModeSetting.Visibility(mode, "Cancel", "Grim", "NewGrim", "WallCancel"), true);
    public BooleanSetting fishingHook = new BooleanSetting("FishingHook", "fishingHook.", new ModeSetting.Visibility(mode, "Cancel", "Grim", "WallCancel"), true);
    public CategorySetting antiPushCategory = new CategorySetting("AntiPush", "Prevents certain things from pushing you.");
    public BooleanSetting antiPush = new BooleanSetting("AntiPush", "Entities", "Prevents other entities from pushing you.", new CategorySetting.Visibility(antiPushCategory), true);
    public BooleanSetting antiLiquidPush = new BooleanSetting("AntiLiquidPush", "Liquids", "Prevents liquids from pushing you.", new CategorySetting.Visibility(antiPushCategory), false);
    public BooleanSetting antiBlockPush = new BooleanSetting("AntiBlockPush", "Blocks", "Prevents you from being pushed outside of blocks.", new CategorySetting.Visibility(antiPushCategory), true);
    public BooleanSetting antiFishingRod = new BooleanSetting("AntiFishingRod", "FishingRods", "Prevents fishing rods from pushing you.", new CategorySetting.Visibility(antiPushCategory), false);
    public BooleanSetting customSkip = new BooleanSetting("CustomSkip","when the server does't reply the wrong command, u may use it.",new ModeSetting.Visibility(mode,"NewGrim"),false);
    public NumberSetting skipTicks = new NumberSetting("CustomSkipTicks","ticks to skip",new ModeSetting.Visibility(mode,"NewGrim"),4,3,6);
    public BooleanSetting wall = new BooleanSetting("MoveGrim off ", "After closing, it is a wall-penetrating counterattack retreat; there is no counter mode outside the wall..",new ModeSetting.Visibility(mode, "NewGrim"), true);

    private boolean cancel;
    private boolean go;
    public int tick;
    public int idk = 0;
    public int tickE = 0;
    public int resetPersec = 8;
    public int grimTCancel = 0;
    public int updates = 0;
    private int ticksToSkip = 0;
    private int ticksToUpdate = 0;

    private int skips = 0;
    private boolean accept = false;
    private boolean skipping = false;
    private boolean needSkips = false;
    //  private final LinkedBlockingDeque<Packet<net.minecraft.client.network.ClientPlayNetworkHandler>> s32s = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<CommonPingS2CPacket> s32s = new LinkedBlockingDeque<>();
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        updates++;

        if (resetPersec > 0) {
            if (updates >= 0 || updates >= resetPersec) {
                updates = 0;
                if (grimTCancel > 0) {
                    grimTCancel--;
                }
            }
        }
        if (go) {
            idk++;
        }
        if (idk == 1) {
            go = false;
            idk = 0;
        }


        if (!cancel) return;
        if ((!pause.getValue() || UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L))) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), UltraByte.ROTATION_MANAGER.getServerYaw(), UltraByte.ROTATION_MANAGER.getServerPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
            //   mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));

        }

        cancel = false;
    }
    @SubscribeEvent
    public void onPacketSyncReceive(PacketReceiveAsyncEvent event) {
        if (mc.world != null) {
            if (mc.player != null) {
                if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
                    if (packet.getEntityId() != mc.player.getId()) return;
                    switch (modes.getValue()) {
                        case "WallNormal" -> {
                            if (isInsideBlock()) {
                                if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L))
                                    return;
                                ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityX((int) (((packet.getVelocityX() / 8000.0 - mc.player.getVelocity().x) * (horizontal.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().x * 8000));
                                ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityY((int) (((packet.getVelocityY() / 8000.0 - mc.player.getVelocity().y) * (vertical.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().y * 8000));
                                ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityZ((int) (((packet.getVelocityZ() / 8000.0 - mc.player.getVelocity().z) * (horizontal.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().z * 8000));
                                cancel =true;
                            }
                        }
                        case "WallCancel" -> {
                            if (isInsideBlock()) {
                                if (mc.player == null) return;
                                if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L))
                                    return;
                                ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityX((int) (((packet.getVelocityX() / 8000.0 - mc.player.getVelocity().x) * (horizontal.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().x * 8000));
                                ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityY((int) (((packet.getVelocityY() / 8000.0 - mc.player.getVelocity().y) * (vertical.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().y * 8000));
                                ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityZ((int) (((packet.getVelocityZ() / 8000.0 - mc.player.getVelocity().z) * (horizontal.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().z * 8000));
                                cancel =true;
                            }
                        }
                        case "WallNewGrim" -> {
                            //在墙里直接取消
                            if (isInsideBlock()) {
                                if (mc.player == null) return;
                                if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L))
                                    return;
                                event.setCancelled(true);
                                cancel =true;
                            }
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public void onPacketSyncReceive(PacketReceiveSyncEvent event) {
        if (mode.getValue().equalsIgnoreCase("NewGrim")) {
            if (mc.player == null) return;
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket){
                UltraByte.CHAT_MANAGER.message("[OPCat] Big idiot master lagging (=^･ｪ･^=)");

            }
            if (event.getPacket() instanceof CommandSuggestionsS2CPacket chat){
                accept = true;
                //event.setCancelled(true);
                //你可以选择不显示
            }
            if (needSkips && ticksToSkip > 0){
                if (event.getPacket() instanceof CommonPingS2CPacket) {
                    ticksToSkip--;
                    event.setCancelled(true);
                    s32s.add((CommonPingS2CPacket) event.getPacket());
                    if (ticksToSkip == 0) {
                        ticksToSkip++;
                        //  BlockHitResult result = new BlockHitResult((new Vec3d(mc.player.getBlockZ(), mc.player.getBlockY(), mc.player.getBlockZ())), Direction.UP, mc.player.getBlockPos().down(), false);
                        BlockHitResult blockHitResult = RotationUtils.customRaycast(mc.player.getYaw(1), 90, 4.5);
                        if (BlockUtil.getBlock(blockHitResult.getBlockPos()) instanceof AirBlock) {
                            return;
                        }

                        Box box = new Box(blockHitResult.getBlockPos().up());

                        if (!box.intersects(mc.player.getBoundingBox())) {
                            return;
                        }

                        ticksToSkip--;

                        while (!s32s.isEmpty()) {
                            Objects.requireNonNull(mc.getNetworkHandler()).onPing(s32s.poll());
                        }


                        float yaw = mc.player.getYaw() + new Random().nextFloat();
                        if (mc.player.prevYaw != yaw || mc.player.prevPitch != 90) {
                            if (R.getValue()) {
                                Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw + 720 * 64, 90, mc.player.isOnGround(), mc.player.horizontalCollision));
                            }
                        } else {
                            Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(mc.player.isOnGround(), mc.player.horizontalCollision));
                        }
                        NetworkUtils.sendSequencedPacket(sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, sequence));
                        // mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult,);
                        if (!customSkip.getValue())
                            mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(0,"/msg ")); // 发送一个命令，等待服务器回复，来测试延迟。
                        int ticksToSkip = customSkip.getValue() ? skipTicks.getValue().intValue() : 1;
                        accept = false;
                        skipping = true;
                        skips = 0;
                        if (ClientUtils.instance.runnables.isEmpty()) {
                            ClientUtils.skipTicks(ticksToSkip);
                            //ClientUtils.addRunnable(() -> GrimDisabler.instance.releasePost(true));
                            ClientUtils.addRunnable(() ->{
                                mc.player.lastRenderX = mc.player.getX();
                                mc.player.lastRenderY = mc.player.getY();
                                mc.player.lastRenderZ = mc.player.getZ();
                                mc.player.lastRenderYaw = mc.player.getYaw();
                                mc.player.lastRenderPitch = mc.player.getPitch();
                                mc.gameRenderer.tick();
                                mc.worldRenderer.tick();
                                mc.getNetworkHandler().sendPacket(ClientTickEndC2SPacket.INSTANCE);
                            });
                            if (!customSkip.getValue())
                                ClientUtils.addRunnable(() ->{
                                    skips++;
                                    if (ClientUtils.instance.getSkips() == 1) {
                                        if (!accept) {
                                            ClientUtils.skipTicks(2); //如果服务器还未响应则继续。
                                        } else if (skipping) {
                                            if (skips < 3)
                                                ClientUtils.skipTicks(1);
                                            skipping = false;
                                        }
                                    }
                                });
                        } else {
                            ClientUtils.skipTicks(ticksToSkip);
                        }
                        ticksToUpdate = 2;

                        needSkips = false;
                    }
                }
            }
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket pac
                && pac.getStatus() == 31
                && pac.getEntity(mc.world) instanceof FishingBobberEntity
                && fishingHook.getValue()) {
            FishingBobberEntity fishHook = (FishingBobberEntity) pac.getEntity(mc.world);
            if (fishHook.getHookedEntity() == mc.player) {
                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getEntityId() != mc.player.getId()) return;

            switch (mode.getValue()) {
                case "Normal" -> {
                    ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityX((int) (((packet.getVelocityX() / 8000.0 - mc.player.getVelocity().x) * (horizontal.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().x * 8000));
                    ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityY((int) (((packet.getVelocityY() / 8000.0 - mc.player.getVelocity().y) * (vertical.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().y * 8000));
                    ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityZ((int) (((packet.getVelocityZ() / 8000.0 - mc.player.getVelocity().z) * (horizontal.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().z * 8000));
                }
                case "Cancel" -> {
                    if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) return;

                    event.setCancelled(true);
                }
                case "NewGrim" -> {
                    if (wall.getValue()) {
                        if (mc.player == null) return;
                        if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) return;
                        grimCancel(event);
                    }
                }
                case "Watchdog" -> {
                    if (mc.player == null) return;

                    if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) return;

                    event.setCancelled(true);

                    double newYVelocity = packet.getVelocityY() / 8000.0D;
                    mc.player.setVelocity(mc.player.getVelocity().x, newYVelocity, mc.player.getVelocity().z);

                    tickE = 1;

                    if (Math.random() < 0.1) {
                        double hFactor = 0.01 + Math.random() * 0.04;
                        double newXVelocity = packet.getVelocityX() / 8000.0D * hFactor;
                        double newZVelocity = packet.getVelocityZ() / 8000.0D * hFactor;
                        mc.player.setVelocity(newXVelocity, newYVelocity, newZVelocity);
                    }
                }
            }
        }

        if (event.getPacket() instanceof ExplosionS2CPacket packet && explosions.getValue()) {
            switch (mode.getValue()) {
                case "Normal" -> {
                    if (packet.playerKnockback().isPresent())
                        ((Vec3dAccessor) packet.playerKnockback().get()).setX((float) (packet.playerKnockback().get().getX() * (horizontal.getValue().doubleValue() / 100.0)));
                    if (packet.playerKnockback().isPresent())
                        ((Vec3dAccessor) packet.playerKnockback().get()).setY((float) (packet.playerKnockback().get().getY() * (vertical.getValue().doubleValue() / 100.0)));
                    if (packet.playerKnockback().isPresent())
                        ((Vec3dAccessor) packet.playerKnockback().get()).setZ((float) (packet.playerKnockback().get().getZ() * (horizontal.getValue().doubleValue() / 100.0)));
                }
                case "Cancel" -> {
                    if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) return;

                    event.setCancelled(true);
                    cancel = true;
                }

                case "NewGrim" -> {
                    //在墙里直接取消
                    if (isInsideBlock()) {

                        if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) return;
                        if (packet.playerKnockback().isPresent())
                            ((Vec3dAccessor) packet.playerKnockback().get()).setX((float) (packet.playerKnockback().get().getX() * (horizontal.getValue().doubleValue() / 100.0)));
                        if (packet.playerKnockback().isPresent())
                            ((Vec3dAccessor) packet.playerKnockback().get()).setY((float) (packet.playerKnockback().get().getY() * (vertical.getValue().doubleValue() / 100.0)));
                        if (packet.playerKnockback().isPresent())
                            ((Vec3dAccessor) packet.playerKnockback().get()).setZ((float) (packet.playerKnockback().get().getZ() * (horizontal.getValue().doubleValue() / 100.0)));
                    } else {
                        if (wall.getValue()) {
                            if (mc.player == null) return;
                            if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) return;
                            grimCancel(event);
                        }
                    }
                }
            }

            if (event.isCancelled()) {
                mc.executeSync(() -> {
                    Vec3d vec3d = packet.center();
                    mc.world.playSound(vec3d.getX(), vec3d.getY(), vec3d.getZ(), packet.explosionSound().value(), SoundCategory.BLOCKS, 4.0F, (1.0F + (mc.world.random.nextFloat() - mc.world.random.nextFloat()) * 0.2F) * 0.7F, false);
                    mc.world.addParticle(packet.explosionParticle(), vec3d.getX(), vec3d.getY(), vec3d.getZ(), 1.0, 0.0, 0.0);
                });
                //  cancel = true;
            }
        }



        if (tick > 0)
            tick--;
    }


    @Override
    public void onEnable() {
        accept = true;
        tick = 0;
        grimTCancel = 0;
    }

    public static boolean isInsideBlock() {
        // 如果 mc.world 或 mc.player 为 null，返回 false
        if (mc.world == null || mc.player == null) {
            return false;
        }

        BlockPos playerPos = getPlayerPos(true);

        Block block = BlockUtil.getBlock(playerPos);

        if (block == Blocks.LADDER || block == Blocks.FIRE) {
            return false;
        }
        if (block == Blocks.ENDER_CHEST) {
            return true;
        }
        if (mc.world != null) {
            if (mc.player != null) {
                return mc.world.canCollide(mc.player, mc.player.getBoundingBox());
            }
        }
        return true;
    }

    public static BlockPos getPlayerPos(boolean fix) {
        return new BlockPosX(mc.player.getPos(), fix);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        //这里是teleportConfirmC2SPacket 取消然后发烂的 目前看来站立可以反很多
        if (event.getPacket() instanceof TeleportConfirmC2SPacket teleportConfirmC2SPacket && teleportConfirmC2SPacket.getTeleportId() != 0 && mode.getValue().equalsIgnoreCase("Grim")) {
            if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) return;
            if (mc.player != null && !isInsideBlock()) {
                event.setCancelled(true);
            }
            event.setCancelled(true);
        }
        if (mode.getValue().equalsIgnoreCase("Watchdog")) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket && mc.player != null && mc.player.hurtTime > 0) {
                PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) event.getPacket();

                if (mc.player.hurtTime < 5 && (Math.abs(mc.player.getVelocity().x) > 0.3 || Math.abs(mc.player.getVelocity().z) > 0.3)) {
                    if (Math.random() < 0.7) {
                        event.setCancelled(true);
                    }
                }
            }
        }
        if (tickE == 1) {
            if (mc.player != null && mc.player.hurtTime > 1) {
                if (event.getPacket() instanceof KeepAliveC2SPacket && mode.getValue().equalsIgnoreCase("Grim")) {
                    if (pause.getValue() && !UltraByte.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(100L)) return;
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public String getMetaData() {
        if (mode.getValue().equalsIgnoreCase("Cancel")) return "0%, 0%";
        if (mode.getValue().equalsIgnoreCase("Grim")) return "Grim";
        if (mode.getValue().equalsIgnoreCase("NewGrim")) if (mc.player != null) {
            return "V/E% = 0% 0%";
        }
        return horizontal.getValue().intValue() + "%, " + vertical.getValue().intValue() + "%";
    }

    private void grimCancel(PacketReceiveSyncEvent event) {
        event.setCancelled(true);
        needSkips = true;
        ticksToSkip = 1;
    }
}
