package dev.ultrabyte.utils.minecraft;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static dev.ultrabyte.utils.IMinecraft.mc;

/**
 * @author NiuRen0827
 * Time:11:50
 */
public class SoundUtils {

    public static final SoundEvent THE_WORLD = registerSoundEvent("theworld_sound");
    public static final SoundEvent heiwenziduo = registerSoundEvent("heavendoor");


    public static void playSound(final SoundEvent sound) {
        playSound(sound, 1.0f, 1f);
    }

    public static void playSound(final SoundEvent sound, float volume, float pitch) {
        if (mc.player != null) {
            mc.executeSync(() -> mc.player.playSound(sound, volume, pitch));
        }
    }

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of("ultrabyte", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
