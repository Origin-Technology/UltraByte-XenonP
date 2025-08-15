package dev.ultrabyte.commands.impl;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.commands.Command;
import dev.ultrabyte.commands.RegisterCommand;

@RegisterCommand(name = "list", description = "list origin-network user(s)", syntax = "")
public class ListCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length >= 1) {
            UltraByte.COMMAND_QUEUE.add("LIST");
        } else {
            messageSyntax();
        }
    }
}
