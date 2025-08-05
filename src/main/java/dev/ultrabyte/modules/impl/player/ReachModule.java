package dev.ultrabyte.modules.impl.player;

import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.NumberSetting;

@RegisterModule(name = "Reach", description = "Allows you to modify the distance at which you can interact with blocks.", category = Module.Category.PLAYER)
public class ReachModule extends Module {
    public NumberSetting amount = new NumberSetting("Amount", "The maximum distance at which you will be able to interact with blocks.", 6.0, 0.0, 8.0);

    @Override
    public String getMetaData() {
        return String.valueOf(amount.getValue().doubleValue());
    }
}
