package dev.ultrabyte.modules.impl.core;

import dev.ultrabyte.UltraByte;
import lombok.Getter;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import net.minecraft.util.Identifier;

@Getter
@RegisterModule(name = "Capes", description = "Applies the ultrabyte cape to yourself and to other users.", category = Module.Category.CORE, toggled = true, drawn = false)
public class CapesModule extends Module {
    public CapesModule() {
        this.capeTexture = Identifier.of(UltraByte.MOD_ID, "textures/cape.png");
    }


    private final Identifier capeTexture;
}
