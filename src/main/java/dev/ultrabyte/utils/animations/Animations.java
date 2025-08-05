package dev.ultrabyte.utils.animations;


import static dev.ultrabyte.utils.animations.FadeUtils.Ease.In;

public class Animations {
    public final FadeUtils fadeUtils = new FadeUtils(0);
    public double from = 0;
    public double to = 0;

    public double get(double target, long length) {
        if (target != to) {
            from = from + (to - from) * fadeUtils.ease(In);
            to = target;
            fadeUtils.reset();
        }
        fadeUtils.setLength(length);
        return from + (to - from) * fadeUtils.ease(In);
    }
}
