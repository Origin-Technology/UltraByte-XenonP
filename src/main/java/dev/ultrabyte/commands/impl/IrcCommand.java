package dev.ultrabyte.commands.impl;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.commands.Command;
import dev.ultrabyte.commands.RegisterCommand;

@RegisterCommand(name = "c", description = "Send message to origin-network", syntax = "<[message]>")
public class IrcCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length >= 1) {
            StringBuilder fullString = new StringBuilder();
            int index = 0;

            for (String str : args) {
                fullString.append(str).append(index + 1 == args.length ? "" : " ");
                index++;
            }

            UltraByte.MESSAGE_QUEUE.add(fullString.toString());
        } else {
            messageSyntax();
        }
    }
}
