package dev.ultrabyte.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.ultrabyte.events.Event;
import net.minecraft.item.ItemStack;

@Getter @AllArgsConstructor
public class ConsumeItemEvent extends Event {
    private final ItemStack stack;
}
