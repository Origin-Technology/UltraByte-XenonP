package dev.ultrabyte.commands.impl;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.commands.Command;
import dev.ultrabyte.commands.RegisterCommand;
import dev.ultrabyte.modules.Module;
import dev.ultrabyte.utils.chat.ChatUtils;
import dev.ultrabyte.utils.input.KeyboardUtils;

import java.util.List;

@RegisterCommand(name = "bind", tag = "Bind", description = "Changes the toggle keybind of a module.", syntax = "<[module]> <[key]|reset|hold <key>> | <reset|list> ", aliases = {"b", "key", "keybind"})
public class BindCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 3) {
            // Handle bind <module> hold <key> command
            if (args[1].equalsIgnoreCase("hold")) {
                Module module = UltraByte.MODULE_MANAGER.getModule(args[0]);
                if (module == null) {
                    UltraByte.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                    return;
                }

                int keyCode = KeyboardUtils.getKeyNumber(args[2]);
                if (keyCode == 0) {
                    UltraByte.CHAT_MANAGER.tagged("Invalid key specified.", getTag(), getName());
                    return;
                }

                module.setBind(keyCode);
                module.setHoldMode(true);
                UltraByte.CHAT_MANAGER.tagged("Successfully bound the " + ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + " module to the " + ChatUtils.getPrimary() + KeyboardUtils.getKeyName(module.getBind()) + ChatUtils.getSecondary() + " key in hold mode.", getTag(), getName());

                // 保存配置
                UltraByte.CONFIG_MANAGER.saveConfig();
            } else {
                messageSyntax();
            }
        } else if (args.length == 2) {
            Module module = UltraByte.MODULE_MANAGER.getModule(args[0]);
            if (module == null) {
                UltraByte.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                return;
            }

            if (args[1].equalsIgnoreCase("reset")) {
                module.setBind(0);
                module.setHoldMode(false);
                UltraByte.CHAT_MANAGER.tagged("Successfully reset the toggle keybind of the " + ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + " module.", getTag(), getName());

                // 保存配置
                UltraByte.CONFIG_MANAGER.saveConfig();
            } else if (args[1].equalsIgnoreCase("hold")) {
                UltraByte.CHAT_MANAGER.tagged("Please specify a key after 'hold'. Usage: bind " + module.getName() + " hold <key>", getTag(), getName());
            } else {
                // Regular key binding (toggle mode)
                module.setBind(KeyboardUtils.getKeyNumber(args[1]));
                module.setHoldMode(false);
                UltraByte.CHAT_MANAGER.tagged("Successfully bound the " + ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + " module to the " + ChatUtils.getPrimary() + KeyboardUtils.getKeyName(module.getBind()) + ChatUtils.getSecondary() + " key.", getTag(), getName());

                // 保存配置
                UltraByte.CONFIG_MANAGER.saveConfig();
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reset" -> {
                    UltraByte.MODULE_MANAGER.getModules().forEach(m -> {
                        m.setBind(0);
                        m.setHoldMode(false);
                    });
                    UltraByte.CHAT_MANAGER.tagged("Successfully reset every module's toggle keybind.", getTag(), getName());

                    // 保存配置
                    UltraByte.CONFIG_MANAGER.saveConfig();
                }
                case "list" -> {
                    List<Module> modules = UltraByte.MODULE_MANAGER.getModules().stream().filter(m -> m.getBind() != 0).toList();

                    if (modules.isEmpty()) {
                        UltraByte.CHAT_MANAGER.tagged("There are currently no bound modules.", getTag(), getName() + "-list");
                    } else {
                        StringBuilder builder = new StringBuilder();
                        int index = 0;

                        for (Module module : modules) {
                            index++;
                            builder.append(ChatUtils.getSecondary()).append(module.getName())
                                    .append(ChatUtils.getPrimary()).append(" [")
                                    .append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(module.getBind()).toUpperCase())
                                    .append(module.isHoldMode() ? " HOLD" : "")
                                    .append(ChatUtils.getPrimary()).append("]")
                                    .append(index == modules.size() ? "" : ", ");
                        }

                        UltraByte.CHAT_MANAGER.message("Bound Modules " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + modules.size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + builder, getName() + "-list");
                    }
                }
                default -> messageSyntax();
            }
        } else {
            messageSyntax();
        }
    }
}