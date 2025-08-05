package dev.ultrabyte.modules.impl.visuals;

import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.NumberSetting;

@RegisterModule(name = "AspectRatio", description = "Modifies the game's aspect ratio.", category = Module.Category.VISUALS)
public class AspectRatioModule extends Module {
    public NumberSetting ratio = new NumberSetting("Ratio", "The aspect ratio that will be applied to the game's rendering.", 1.78f, 0.0f, 5.0f);

    @Override
    public String getMetaData() {
        return String.valueOf(ratio.getValue().floatValue());
    }
}
