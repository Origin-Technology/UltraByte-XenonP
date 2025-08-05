package dev.ultrabyte.modules.impl.miscellaneous;

import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.settings.impl.NumberSetting;

@RegisterModule(name = "ExtraTab", description = "Extends the size of the player list.", category = Module.Category.MISCELLANEOUS)
public class ExtraTabModule extends Module {
    public NumberSetting limit = new NumberSetting("Limit", "The maximum amount of players that will be listed.", 1000, 1, 1000);
    public BooleanSetting friends = new BooleanSetting("Friends", "Highlights your friends on the player list.", true);

    @Override
    public String getMetaData() {
        return String.valueOf(limit.getValue().intValue());
    }
}
