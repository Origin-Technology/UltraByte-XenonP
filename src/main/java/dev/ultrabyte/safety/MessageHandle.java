package dev.ultrabyte.safety;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.managers.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import tech.origin.xenonauth.wss.MessageHandler;

public class MessageHandle implements MessageHandler {
    @Override
    public void handleMessage(String message) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(message));
    }

    @Override
    public void handleReconnecting(String message) {
        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("UltraByte"),
                        Text.of(message)
                )
        );
    }

    @Override
    public void handleReconnectSuccess(String message) {
        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("UltraByte"),
                        Text.of(message)
                )
        );
    }

    @Override
    public void handleReconnectFailed(String message) {
        ConfigManager.canSave = false;
        UltraByte.MODULE_MANAGER.getModules().clear();
        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("UltraByte"),
                        Text.of(message)
                )
        );
    }

    @Override
    public void handleAuthFailed(String message) {
        ConfigManager.canSave = false;
        UltraByte.MODULE_MANAGER.getModules().clear();
        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("UltraByte"),
                        Text.of(message)
                )
        );
    }

    @Override
    public void handleNotice(String message) {
        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("来自 Origin 的信息"),
                        Text.of(message)
                )
        );;
    }

    @Override
    public void handleClose(String message) {
        ConfigManager.canSave = false;
        UltraByte.MODULE_MANAGER.getModules().clear();
        MinecraftClient.getInstance().getToastManager().add(
                new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING,
                        Text.of("UltraByte"),
                        Text.of(message)
                )
        );
    }

    @Override
    public String handleKeepAlive() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }
}