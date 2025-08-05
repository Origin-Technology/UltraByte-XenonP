package dev.ultrabyte.modules.impl.miscellaneous;

import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;

@RegisterModule(name = "MouseFix", description = "Fixes multiple mouse issues.", category = dev.ultrabyte.modules.Module.Category.MISCELLANEOUS)
public class MouseFixModule extends Module {
    public BooleanSetting customDebounce = new BooleanSetting("CustomDebounce", "Implements a custom debounce timer on mouse inputs.", true);
}
