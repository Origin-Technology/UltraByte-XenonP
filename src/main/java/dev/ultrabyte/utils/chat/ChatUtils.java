package dev.ultrabyte.utils.chat;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.modules.impl.core.CommandsModule;
import dev.ultrabyte.utils.text.FormattingUtils;
import net.minecraft.util.StringIdentifiable;

public class ChatUtils {
    public static StringIdentifiable getPrimary() {
        return FormattingUtils.getFormatting(UltraByte.MODULE_MANAGER.getModule(CommandsModule.class).primaryMessageColor.getValue());
    }

    public static StringIdentifiable getSecondary() {
        return FormattingUtils.getFormatting(UltraByte.MODULE_MANAGER.getModule(CommandsModule.class).secondaryMessageColor.getValue());
    }
}
