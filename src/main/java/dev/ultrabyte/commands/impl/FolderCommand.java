package dev.ultrabyte.commands.impl;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.commands.Command;
import dev.ultrabyte.commands.RegisterCommand;
import net.minecraft.util.Util;

import java.io.File;

@RegisterCommand(name = "folder", description = "Opens the clients folder.")
public class FolderCommand extends Command {
    @Override
    public void execute(String[] args) {
        File folder = new File(UltraByte.MOD_NAME);
        if (folder.exists()) {
            Util.getOperatingSystem().open(folder);
        } else {
            UltraByte.CHAT_MANAGER.info("Could not find the client's configuration folder.");
        }
    }
}
