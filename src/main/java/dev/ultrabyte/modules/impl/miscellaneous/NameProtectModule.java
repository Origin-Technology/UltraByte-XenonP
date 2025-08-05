package dev.ultrabyte.modules.impl.miscellaneous;

import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.StringSetting;

@RegisterModule(name = "NameProtect", description = "Hides your current in game name.", category = Module.Category.MISCELLANEOUS)
public class NameProtectModule extends Module {
    public StringSetting name = new StringSetting("Name", "The name to use as a replacement.", "ultrabyte");
}
