package dev.opan.managers;

import dev.opan.UltraByte;
import dev.opan.modules.Module;
import dev.opan.utils.IMinecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

/**
 * InputManager
 * @author DSJ_
 */
public class InputManager implements IMinecraft {
    private GLFWKeyCallbackI originalKeyCallback;
    private long windowHandle;

    public InputManager() {
        init();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void init() {
        try {
            if (mc == null || mc.getWindow() == null) {
                UltraByte.LOGGER.error("无法初始化 InputManager: Minecraft 客户端或窗口尚未准备好");
                return;
            }

            if (windowHandle != 0 && originalKeyCallback != null) {
                GLFW.glfwSetKeyCallback(windowHandle, originalKeyCallback);
            }

            windowHandle = mc.getWindow().getHandle();
            originalKeyCallback = GLFW.glfwSetKeyCallback(windowHandle, this::handleKeyCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (windowHandle != 0 && originalKeyCallback != null) {
            GLFW.glfwSetKeyCallback(windowHandle, originalKeyCallback);
        }
    }

    private void handleKeyCallback(long window, int key, int scancode, int action, int mods) {
        int keyCode = (key == GLFW.GLFW_KEY_UNKNOWN) ? scancode : key;

        if (!mc.isWindowFocused() || mc.currentScreen != null) {
            if (originalKeyCallback != null) {
                originalKeyCallback.invoke(window, key, scancode, action, mods);
            }
            return;
        }

        boolean keyHandled = false;

        if (action == GLFW.GLFW_PRESS) {
            for (Module module : UltraByte.MODULE_MANAGER.getModules()) {
                if (module.getBind() == keyCode) {
                    module.onKeyPress(keyCode);
                    keyHandled = true;
                    break;
                }
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            for (Module module : UltraByte.MODULE_MANAGER.getModules()) {
                if (module.getBind() == keyCode) {
                    module.onKeyRelease(keyCode);
                    keyHandled = true;
                    break;
                }
            }
        }

        // 不将事件传递给我的手艺，防止冲突
        if (!keyHandled && originalKeyCallback != null) {
            originalKeyCallback.invoke(window, key, scancode, action, mods);
        }
    }
}