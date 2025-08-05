package dev.ultrabyte.managers;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.events.SubscribeEvent;
import dev.ultrabyte.events.impl.TickEvent;
import dev.ultrabyte.utils.IMinecraft;

import java.util.ArrayList;

public class TaskManager implements IMinecraft {
    private final ArrayList<Runnable> tasks = new ArrayList<>();

    public TaskManager() {
        UltraByte.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!tasks.isEmpty()) {
            tasks.getFirst().run();
            tasks.removeFirst();
        }
    }

    public void submit(Runnable runnable) {
        tasks.add(runnable);
    }
}
