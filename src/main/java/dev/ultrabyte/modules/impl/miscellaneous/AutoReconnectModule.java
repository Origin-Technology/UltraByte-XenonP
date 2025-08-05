package dev.ultrabyte.modules.impl.miscellaneous;

import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.NumberSetting;

@RegisterModule(name = "AutoReconnect", description = "Automatically reconnects you to a server after a specified time period.", category = Module.Category.MISCELLANEOUS)
public class AutoReconnectModule extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "The amount of seconds that have to pass before reconnecting.", 5, 0, 20);

    @Override
    public String getMetaData() {
        return String.valueOf(delay.getValue().intValue());
    }
}
