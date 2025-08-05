package dev.ultrabyte.commands.impl;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.commands.Command;
import dev.ultrabyte.commands.RegisterCommand;
import dev.ultrabyte.modules.impl.core.FriendModule;
import dev.ultrabyte.utils.chat.ChatUtils;

import java.util.List;

@RegisterCommand(name = "friend", tag = "Friend", description = "Allows you to manage the client's friend list.", syntax = "<add|del> <[player]> | <clear|list>", aliases = {"f", "friends"})
public class FriendCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add" -> {
                    if (!UltraByte.FRIEND_MANAGER.contains(args[1])) {
                        if (UltraByte.MODULE_MANAGER.getModule(FriendModule.class).friendMessage.getValue()) {
                            UltraByte.FRIEND_MANAGER.sendFriendMessage(args[1]);
                        }
                        UltraByte.FRIEND_MANAGER.add(args[1]);
                        UltraByte.CHAT_MANAGER.tagged("Successfully added " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " to your friends list.", getTag(), getName());
                    } else {
                        UltraByte.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " is already on your friends list.", getTag(), getName());
                    }
                }
                case "del" -> {
                    if (UltraByte.FRIEND_MANAGER.contains(args[1])) {
                        UltraByte.FRIEND_MANAGER.remove(args[1]);
                        UltraByte.CHAT_MANAGER.tagged("Successfully removed " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " from your friends list.", getTag(), getName());
                    } else {
                        UltraByte.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " is not on your friends list.", getTag(), getName());
                    }
                }
                default -> messageSyntax();
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "clear" -> {
                    UltraByte.FRIEND_MANAGER.clear();
                    UltraByte.CHAT_MANAGER.tagged("Successfully cleared your friends list.", getTag(), getName() + "-list");
                }
                case "list" -> {
                    List<String> friends = UltraByte.FRIEND_MANAGER.getFriends();

                    if (friends.isEmpty()) {
                        UltraByte.CHAT_MANAGER.tagged("You currently have no friends.", getTag());
                    } else {
                        StringBuilder builder = new StringBuilder();
                        int index = 0;

                        for (String name : friends) {
                            index++;
                            builder.append(ChatUtils.getSecondary()).append(name)
                                .append(index == friends.size() ? "" : ", ");
                        }

                        UltraByte.CHAT_MANAGER.message("Friends " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + friends.size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + builder, getName() + "-list");
                    }
                }
                default -> messageSyntax();
            }
        } else {
            messageSyntax();
        }
    }
}
