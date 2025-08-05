package dev.ultrabyte.commands.impl;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.commands.Command;
import dev.ultrabyte.commands.RegisterCommand;
import dev.ultrabyte.utils.chat.ChatUtils;
import dev.ultrabyte.utils.system.FileUtils;

import java.io.IOException;

@RegisterCommand(name = "config", tag = "Config", description = "Allows you to manage the client's configuration system.", syntax = "<load|save> <[name]> | <reload|save|current>")
public class ConfigCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "load" -> {
                    if (!FileUtils.fileExists(UltraByte.MOD_NAME + "/Configs/" + args[1] + ".json")) {
                        UltraByte.CHAT_MANAGER.tagged("The specified configuration does not exist.", getTag(), getName());
                        return;
                    }

                    try {
                        UltraByte.CONFIG_MANAGER.loadModules(args[1]);
                        UltraByte.CHAT_MANAGER.tagged("Successfully loaded the " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " configuration.", getTag(), getName());
                    } catch (IOException exception) {
                        UltraByte.CHAT_MANAGER.tagged("Failed to load the " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " configuration.", getTag(), getName());
                    }
                }
                case "save" -> {
                    try {
                        UltraByte.CONFIG_MANAGER.saveModules(args[1]);
                        UltraByte.CHAT_MANAGER.tagged("Successfully saved the configuration to " + ChatUtils.getPrimary() + args[1] + ".json" + ChatUtils.getSecondary() + ".", getTag(), getName());
                    } catch (IOException exception) {
                        UltraByte.CHAT_MANAGER.tagged("Failed to save the " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " configuration.", getTag(), getName());
                    }
                }
                default -> messageSyntax();
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload" -> {
                    UltraByte.CONFIG_MANAGER.loadConfig();
                    UltraByte.CHAT_MANAGER.tagged("Successfully reloaded the current configuration.", getTag(), getName());
                }
                case "save" -> {
                    UltraByte.CONFIG_MANAGER.saveConfig();
                    UltraByte.CHAT_MANAGER.tagged("Successfully saved the current configuration.", getTag(), getName());
                }
                case "current" -> UltraByte.CHAT_MANAGER.tagged("The client is currently using the " + ChatUtils.getPrimary() + UltraByte.CONFIG_MANAGER.getCurrentConfig() + ChatUtils.getSecondary() + " configuration.", getTag(), getName());
                default -> messageSyntax();
            }
        } else {
            messageSyntax();
        }
    }
}
