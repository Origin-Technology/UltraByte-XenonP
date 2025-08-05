package dev.ultrabyte.managers;

import dev.ultrabyte.UltraByte;
import lombok.Getter;
import dev.ultrabyte.modules.impl.core.FriendModule;

import java.awt.*;
import java.util.ArrayList;

@Getter
public class FriendManager {
    private final ArrayList<String> friends = new ArrayList<>();

    public boolean contains(String name) {
        if (getFriendFire()) return false;
        return friends.stream().anyMatch(name::equalsIgnoreCase);
    }

    public void add(String name) {
        if (contains(name)) return;
        friends.add(name);
    }

    public void remove(String name) {
        friends.removeIf(name::equalsIgnoreCase);
    }

    public void clear() {
        friends.clear();
    }

    public boolean getFriendFire() {
        return UltraByte.MODULE_MANAGER.getModule(FriendModule.class).friendlyFire.getValue();
    }

    public void sendFriendMessage(String name) {
        UltraByte.MODULE_MANAGER.getModule(FriendModule.class).sendFriendMessage(name);
    }

    public Color getDefaultFriendColor() {
        return getDefaultFriendColor(255);
    }

    public Color getDefaultFriendColor(int alpha) {
        return new Color(85, 255, 255, alpha);
    }
}
