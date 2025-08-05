package dev.opan.mixins;

import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DownloadingTerrainScreen.class)
public class DownloadingTerrainScreenMixin extends Screen {
    @Shadow @Final private DownloadingTerrainScreen.WorldEntryReason worldEntryReason;

    protected DownloadingTerrainScreenMixin(Text title) {
        super(title);
    }
}
