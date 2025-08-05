package dev.opan.modules.impl.core;

import dev.opan.UltraByte;
import lombok.Getter;
import dev.opan.modules.Module;
import dev.opan.modules.RegisterModule;
import net.minecraft.util.Identifier;

@Getter
@RegisterModule(name = "Capes", description = "Applies the opan cape to yourself and to other users.", category = Module.Category.CORE, toggled = true, drawn = false)
public class CapesModule extends Module {
    public CapesModule() {
        this.capeTexture = Identifier.of(UltraByte.MOD_ID, "textures/cape.png");
    }


    private final Identifier capeTexture;
}
