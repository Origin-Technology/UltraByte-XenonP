package dev.opan.modules.impl.miscellaneous;

import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.settings.impl.StringSetting;

@RegisterModule(name = "NameProtect", description = "Hides your current in game name.", category = Module.Category.MISCELLANEOUS)
public class NameProtectModule extends Module {
    public StringSetting name = new StringSetting("Name", "The name to use as a replacement.", "opan");
}
