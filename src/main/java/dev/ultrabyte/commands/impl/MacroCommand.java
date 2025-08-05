package dev.ultrabyte.commands.impl;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.commands.Command;
import dev.ultrabyte.commands.RegisterCommand;
import dev.ultrabyte.utils.chat.ChatUtils;
import dev.ultrabyte.utils.input.KeyboardUtils;

import java.util.Arrays;

@RegisterCommand(name = "macro", tag = "Macro", description = "Allows you to manage the client's macro system.", syntax = "add <[key]> <[message]> | remove <[key]> | <clear|list>")
public class MacroCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("add")) {
                int key = KeyboardUtils.getKeyNumber(args[1]);
                if (key == 0) {
                    UltraByte.CHAT_MANAGER.tagged("The keybind specified is not valid.", getTag());
                    return;
                }

                StringBuilder builder = new StringBuilder();
                String[] array = Arrays.copyOfRange(args, 2, args.length);
                int index = 0;

                for (String str : array) {
                    index++;
                    builder.append(str).append(index == array.length ? "" : " ");
                }

                UltraByte.MACRO_MANAGER.add(builder.toString(), key);
                UltraByte.CHAT_MANAGER.tagged("Successfully added " + ChatUtils.getPrimary() + builder + ChatUtils.getSecondary() + " as a macro, bound to the " + ChatUtils.getPrimary() + KeyboardUtils.getKeyName(key) + ChatUtils.getSecondary() + " key.", getTag(), "command-" + getName());
            } else {
                messageSyntax();
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                int key = KeyboardUtils.getKeyNumber(args[1]);
                if (key == 0) {
                    UltraByte.CHAT_MANAGER.tagged("The keybind specified is not valid.", getTag());
                    return;
                }

                if (!UltraByte.MACRO_MANAGER.containsValue(key)) {
                    UltraByte.CHAT_MANAGER.tagged("There is no macro with the " + ChatUtils.getPrimary() + KeyboardUtils.getKeyName(key) + ChatUtils.getSecondary() + " key.", getTag(), "command-" + getName());
                } else {
                    UltraByte.CHAT_MANAGER.tagged("Successfully removed the " + ChatUtils.getPrimary() + UltraByte.MACRO_MANAGER.getKey(key) + ChatUtils.getSecondary() + " macro.", getTag(), "command-" + getName());
                    UltraByte.MACRO_MANAGER.remove(UltraByte.MACRO_MANAGER.getKey(key));
                }
            } else {
                messageSyntax();
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                UltraByte.MACRO_MANAGER.clear();
                UltraByte.CHAT_MANAGER.tagged("Successfully removed all macros.", getTag(), "command-" + getName());
            } else if (args[0].equalsIgnoreCase("list")) {
                if (UltraByte.MACRO_MANAGER.getMacros().isEmpty()) {
                    UltraByte.CHAT_MANAGER.tagged("There are currently no macros added.", getTag(), "command-" + getName() + "-list");
                } else {
                    StringBuilder builder = new StringBuilder();
                    int index = 0;

                    for (String message : UltraByte.MACRO_MANAGER.getMacros().keySet()) {
                        index++;

                        int key = UltraByte.MACRO_MANAGER.getValue(message);
                        builder.append(ChatUtils.getSecondary()).append(message)
                                .append(ChatUtils.getPrimary()).append(" [")
                                .append(ChatUtils.getSecondary()).append(KeyboardUtils.getKeyName(key))
                                .append(ChatUtils.getPrimary()).append("]")
                                .append(ChatUtils.getSecondary())
                                .append(index == UltraByte.MACRO_MANAGER.getMacros().size() ? "" : ", ");
                    }

                    UltraByte.CHAT_MANAGER.message("Macros " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + UltraByte.MACRO_MANAGER.getMacros().size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + builder, "command-" + getName() + "-list");
                }
            } else {
                messageSyntax();
            }
        } else {
            messageSyntax();
        }
    }
}
