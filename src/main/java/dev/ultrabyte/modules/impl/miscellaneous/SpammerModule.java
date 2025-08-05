package dev.ultrabyte.modules.impl.miscellaneous;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.TickEvent;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.modules.RegisterModule;
import dev.ultrabyte.settings.impl.BooleanSetting;
import dev.ultrabyte.settings.impl.NumberSetting;
import dev.ultrabyte.settings.impl.StringSetting;
import dev.ultrabyte.utils.system.FileUtils;
import dev.ultrabyte.utils.system.MathUtils;
import dev.ultrabyte.utils.system.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RegisterModule(name = "Spammer", description = "Spams messages in chat from a text file.", category = Module.Category.MISCELLANEOUS)
public class SpammerModule extends Module {
    public StringSetting fileName = new StringSetting("FileName", "The name of the spammer text file.", "spammer.txt");
    public NumberSetting delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
    public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
    public BooleanSetting shuffled = new BooleanSetting("Shuffled", "Sends the spammer messages out of order.", false);

    private final Timer timer = new Timer();
    private List<String> messages = new ArrayList<>();
    private int line;

    @Override
    public void onEnable() {
        line = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if(getNull()) return;

        File file = new File(UltraByte.MOD_NAME + "/Client/" + fileName.getValue());
        messages = FileUtils.readLines(file);

        if(!messages.isEmpty() && timer.hasTimeElapsed(delay.getValue().intValue() * 1000)) {
            if(line >= messages.size()) line = 0;

            String message = shuffled.getValue() ? messages.get((int) MathUtils.random(messages.size(), 0)) : messages.get(line);

            mc.player.networkHandler.sendChatMessage((greenText.getValue() ? "> " : "") + message);
            line++;
            timer.reset();
        }
    }
}
