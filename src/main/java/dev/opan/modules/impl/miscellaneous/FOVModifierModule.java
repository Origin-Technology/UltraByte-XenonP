package dev.opan.modules.impl.miscellaneous;

import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import dev.opan.settings.impl.BooleanSetting;
import dev.opan.settings.impl.NumberSetting;

@RegisterModule(name = "FOVModifier", description = "Gives you more customizability for the games FOV.", category = Module.Category.MISCELLANEOUS)
public class FOVModifierModule extends Module {
    public NumberSetting fov = new NumberSetting("FOV", "The FOV you want to use.", 120, 50, 150);
    public BooleanSetting items = new BooleanSetting("Items", "Modify items FOV as well.", false);

    @Override
    public String getMetaData() {
        return fov.getValue().intValue() + "";
    }
}
