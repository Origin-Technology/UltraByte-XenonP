package dev.ultrabyte.managers;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.utils.IMinecraft;
import net.minecraft.util.Formatting;

/**
 * @author NiuRen0827
 * Time:17:12
 */
public class NotifyManager implements IMinecraft {
    public static void sendLag() {
        UltraByte.CHAT_MANAGER.message(Formatting.BOLD + "[CombatCore]移动包异常！您回弹了");
    }

    public static boolean sendWarning() {
        if (UltraByte.RENDER_MANAGER.getFps() < 30) {
            UltraByte.CHAT_MANAGER.message(Formatting.BOLD + "[CombatCore]您的fps低于30 请关闭视觉渲染进行优化");
            return true;
        }
        return false;
    }

}
