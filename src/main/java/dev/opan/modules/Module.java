package dev.opan.modules;

import dev.opan.UltraByte;
import lombok.Getter;
import dev.opan.events.impl.ToggleModuleEvent;
import dev.opan.settings.Setting;
import dev.opan.settings.impl.*;
import dev.opan.utils.IMinecraft;
import dev.opan.utils.animations.Animation;
import dev.opan.utils.animations.Easing;
import dev.opan.utils.chat.ChatUtils;
import lombok.Setter;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Module implements IMinecraft {
    private final String name, description;
    private final Category category;

    private final boolean persistent;
    private boolean toggled;

    @Setter
    @Getter
    private boolean holdMode;

    private final List<Setting> settings;

    public BooleanSetting chatNotify;
    public BooleanSetting drawn;
    public BindSetting bind;

    private final Animation animationOffset;

    public Module() {
        RegisterModule annotation = getClass().getAnnotation(RegisterModule.class);

        name = annotation.name();
        description = annotation.description();
        category = annotation.category();
        persistent = annotation.persistent();
        toggled = annotation.toggled();
        holdMode = false;
        settings = new ArrayList<>();
        animationOffset = new Animation(300, Easing.Method.EASE_OUT_CUBIC);

        chatNotify = new BooleanSetting("ChatNotify", "Notifies you in chat whenever the module gets toggled on or off.", true);
        drawn = new BooleanSetting("Drawn", "Renders the module's name on the HUD's module list.", annotation.drawn());
        bind = new BindSetting("Bind", "The keybind that toggles the module on or off. When in hold mode, the module is active only while the key is pressed.", annotation.bind());

        if (persistent) toggled = true;
        if (toggled) {
            UltraByte.EVENT_HANDLER.subscribe(this);
        }
    }

    public boolean getNull() {
        return (mc.player == null || mc.world == null);
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public String getMetaData() {
        return "";
    }

    public void setToggled(boolean toggled) {
        setToggled(toggled, true);
    }

    public void setToggled(boolean toggled, boolean notify) {
        if (persistent) return;
        if (toggled == this.toggled) return;

        this.toggled = toggled;
        UltraByte.EVENT_HANDLER.post(new ToggleModuleEvent(this, this.toggled));

        if (this.toggled) {
            animationOffset.setEasing(Easing.Method.EASE_OUT_CUBIC);
            if (notify && chatNotify.getValue()) {
                UltraByte.CHAT_MANAGER.message(ChatUtils.getPrimary() + name + ChatUtils.getSecondary() + ".toggled = " + Formatting.GREEN + "true" + ChatUtils.getSecondary() + ";", "toggle-" + getName().toLowerCase());
            }

            onEnable();
            if (this.toggled) UltraByte.EVENT_HANDLER.subscribe(this);
        } else {
            animationOffset.setEasing(Easing.Method.EASE_IN_CUBIC);

            UltraByte.EVENT_HANDLER.unsubscribe(this);
            onDisable();

            if (notify && chatNotify.getValue()) {
                UltraByte.CHAT_MANAGER.message(ChatUtils.getPrimary() + name + ChatUtils.getSecondary() + ".toggled = " + Formatting.RED + "false" + ChatUtils.getSecondary() + ";", "toggle-" + getName().toLowerCase());
            }
        }
    }

    public boolean onKeyPress(int keyCode) {
        if (keyCode != getBind() || keyCode == 0) return false;

        // 日志记录当前状态
        UltraByte.LOGGER.info(name + " onKeyPress: currentToggled=" + toggled + ", holdMode=" + holdMode);

        if (holdMode) {
            // Hold 模式下，按下键时始终激活
            setToggled(true);
        } else {
            // 普通模式下，切换状态
            boolean newState = !toggled;
            setToggled(newState);
        }

        return true;
    }

    public boolean onKeyRelease(int keyCode) {
        if (keyCode == getBind() && keyCode != 0 && holdMode) {
            setToggled(false);
            return true;
        }
        return false;
    }

    public int getBind() {
        return bind.getValue();
    }

    public void setBind(int bind) {
        this.bind.setValue(bind);
    }

    public void resetValues() {
        for (Setting uncastedSetting : settings) {
            if (uncastedSetting instanceof BooleanSetting setting) setting.resetValue();
            if (uncastedSetting instanceof NumberSetting setting) setting.resetValue();
            if (uncastedSetting instanceof ModeSetting setting) setting.resetValue();
            if (uncastedSetting instanceof StringSetting setting) setting.resetValue();
            if (uncastedSetting instanceof BindSetting setting) setting.resetValue();
            if (uncastedSetting instanceof WhitelistSetting setting) setting.clear();
            if (uncastedSetting instanceof ColorSetting setting) setting.resetValue();
        }

        // Reset hold mode when resetting values
        holdMode = false;
    }

    public Setting getSetting(String name) {
        return settings.stream().filter(s -> s.getName().equalsIgnoreCase(name) && !(s instanceof CategorySetting)).findFirst().orElse(null);
    }

    @Getter
    public enum Category {
        COMBAT("Combat"),
        PLAYER("Player"),
        VISUALS("Visuals"),
        MOVEMENT("Movement"),
        MISCELLANEOUS("Miscellaneous"),
        CORE("Core");

        private final String name;

        Category(String name) {
            this.name = name;
        }
    }
}