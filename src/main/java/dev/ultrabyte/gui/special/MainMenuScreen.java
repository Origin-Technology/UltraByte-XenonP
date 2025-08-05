package dev.ultrabyte.gui.special;

import dev.ultrabyte.UltraByte;
import dev.ultrabyte.utils.IMinecraft;
import dev.ultrabyte.utils.color.ColorUtils;
import dev.ultrabyte.utils.graphics.Renderer2D;
import dev.ultrabyte.utils.system.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.awt.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class MainMenuScreen extends Screen implements IMinecraft {
    private List<String> splashTexts = new ArrayList<>();
    private int currentSplashIndex = 0;
    private String currentSplashText = "";
    private int charIndex = 0;

    private enum SplashState { // SEX!
        TYPING,
        PAUSE_AFTER_TYPING,
        ERASING,
        PAUSE_AFTER_ERASING
    }

    private SplashState splashState = SplashState.TYPING;
    private final int buttonWidth = 80, buttonHeight = 16;

    private long lastCharTime = 0;
    private long pauseStartTime = 0;

    // Animation
    private final Map<String, ButtonAnimation> buttonAnimations = new HashMap<>();
    private static final float ANIMATION_DURATION = 800.0f;
    private boolean isFirstRender = true;
    private float titleAlpha = 0.0f;
    private long openTime;

    private static class ButtonAnimation {
        public float targetX;
        public float targetY;
        public float startX;
        public float startY;
        public float hoverAlpha = 0.0f;
        public long lastHoverTime;
        public boolean isHovering;

        public ButtonAnimation(float targetX, float targetY, float startX, float startY) {
            this.targetX = targetX;
            this.targetY = targetY;
            this.startX = startX;
            this.startY = startY;
            this.lastHoverTime = System.currentTimeMillis();
        }
    }

    public MainMenuScreen() {
        super(Text.literal(UltraByte.MOD_ID + "-menu"));
        loadSplashTexts();
    }

    @Override
    protected void init() {
        super.init();

        openTime = System.currentTimeMillis();
        titleAlpha = 0.0f;
        lastCharTime = System.currentTimeMillis();
        pauseStartTime = System.currentTimeMillis();
        splashState = SplashState.TYPING;

        buttonAnimations.put("Singleplayer", new ButtonAnimation(width/2f - buttonWidth - 2, height/2f, -buttonWidth, height/2f));
        buttonAnimations.put("Multiplayer", new ButtonAnimation(width/2f, height/2f, width/2f, height + buttonHeight));
        buttonAnimations.put("Game Settings", new ButtonAnimation(width/2f + buttonWidth + 2, height/2f, width + buttonWidth, height/2f));
        buttonAnimations.put("Quit Game", new ButtonAnimation(width - buttonWidth/2f - 2, height - buttonHeight - 2, width + buttonWidth, height - buttonHeight - 2));
    }

    public boolean shouldPause() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (isFirstRender) {
            openTime = System.currentTimeMillis();
            isFirstRender = false;
        }

        MatrixStack matrices = context.getMatrices();
        float animProgress = getAnimationProgress();

        titleAlpha = Math.min(1.0f, animProgress * 2.0f);
        float textAlpha = Math.min(1.0f, animProgress * 1.5f);

        // 背景
        Renderer2D.renderQuad(matrices, 0, 0, width, height, new Color(25, 25, 25,255));

        for(int i = 0; i < width; i++) {
            Color color = ColorUtils.getRainbow(2L, 0.7f, 1.0f, 255, i*5L);
            Renderer2D.renderQuad(matrices, i, 0, i + 1, 1, color);
        }

        Color titleColor = ColorUtils.getRainbow(2L, 0.7f, 1.0f, (int)(255 * titleAlpha), width/2*5L);
        drawText(context, Formatting.WHITE + "O" + Formatting.RESET + "pan", width/2f - UltraByte.FONT_MANAGER.getWidth("UltraByte"), height/2f - UltraByte.FONT_MANAGER.getHeight()*2 - 5, 2, titleColor);

        String date = new SimpleDateFormat("MM/dd/yy").format(new Date()) + " " + new SimpleDateFormat("hh:mm aa").format(new Date());
        Color timeColor = new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), (int)(255 * textAlpha));
        drawText(context, date, width/2f - UltraByte.FONT_MANAGER.getWidth(date)/2f, 6, 1, timeColor);

        Color versionColor = new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), (int)(255 * textAlpha));
        drawText(context, UltraByte.MOD_NAME + " " + UltraByte.MOD_VERSION + "-mc" + UltraByte.MINECRAFT_VERSION + "+" + UltraByte.GIT_REVISION + "." + UltraByte.GIT_HASH, 2, height - UltraByte.FONT_MANAGER.getHeight() - 2, 1, versionColor);

        drawAnimatedButton(context, "Singleplayer", mouseX, mouseY, animProgress);
        drawAnimatedButton(context, "Multiplayer", mouseX, mouseY, animProgress);
        drawAnimatedButton(context, "Game Settings", mouseX, mouseY, animProgress);
        drawAnimatedButton(context, "Quit Game", mouseX, mouseY, animProgress);

        updateSplashText();
        Color splashColor = new Color(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), (int)(255 * textAlpha));
        drawText(context, currentSplashText, width/2f - UltraByte.FONT_MANAGER.getWidth(currentSplashText)/2f, height/2f + buttonHeight + 5f, 1, splashColor);

        drawClientStatus(context, textAlpha);
        updateHoverStates(mouseX, mouseY);
    }

    private void updateSplashText() {
        if (splashTexts.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        String fullText = splashTexts.get(currentSplashIndex);

        int charDelay = 100;
        int pauseDelay = 2000;
        switch (splashState) {
            case TYPING:
                if (currentTime - lastCharTime > charDelay) {
                    lastCharTime = currentTime;

                    if (charIndex < fullText.length()) {
                        charIndex++;
                        currentSplashText = fullText.substring(0, charIndex);
                    } else {
                        splashState = SplashState.PAUSE_AFTER_TYPING;
                        pauseStartTime = currentTime;
                    }
                }
                break;

            case PAUSE_AFTER_TYPING:
                if (currentTime - pauseStartTime > pauseDelay) {
                    splashState = SplashState.ERASING;
                }
                break;

            case ERASING:
                if (currentTime - lastCharTime > charDelay) {
                    lastCharTime = currentTime;

                    if (charIndex > 0) {
                        charIndex--;
                        currentSplashText = fullText.substring(0, charIndex);
                    } else {
                        splashState = SplashState.PAUSE_AFTER_ERASING;
                        pauseStartTime = currentTime;
                        currentSplashIndex = (currentSplashIndex + 1) % splashTexts.size();
                    }
                }
                break;

            case PAUSE_AFTER_ERASING:
                if (currentTime - pauseStartTime > pauseDelay) {
                    splashState = SplashState.TYPING;
                }
                break;
        }
    }

    private float getAnimationProgress() {
        float elapsed = System.currentTimeMillis() - openTime;
        return Math.min(1.0f, elapsed / ANIMATION_DURATION);
    }

    private void drawAnimatedButton(DrawContext context, String text, int mouseX, int mouseY, float animProgress) {
        ButtonAnimation anim = buttonAnimations.get(text);
        if (anim == null) return;

        float progress = easeOutCubic(animProgress);
        float currentX = anim.startX + (anim.targetX - anim.startX) * progress;
        float currentY = anim.startY + (anim.targetY - anim.startY) * progress;

        long currentTime = System.currentTimeMillis();
        float hoverDelta = (currentTime - anim.lastHoverTime) / 200.0f; // 200ms transition
        anim.lastHoverTime = currentTime;

        if (anim.isHovering) {
            anim.hoverAlpha = Math.min(1.0f, anim.hoverAlpha + hoverDelta);
        } else {
            anim.hoverAlpha = Math.max(0.0f, anim.hoverAlpha - hoverDelta);
        }

        Color bgColor = new Color(0, 0, 0, 50 + (int)(30 * anim.hoverAlpha));
        Color textColor = new Color(
                Color.GRAY.getRed() + (int)((Color.WHITE.getRed() - Color.GRAY.getRed()) * anim.hoverAlpha),
                Color.GRAY.getGreen() + (int)((Color.WHITE.getGreen() - Color.GRAY.getGreen()) * anim.hoverAlpha),
                Color.GRAY.getBlue() + (int)((Color.WHITE.getBlue() - Color.GRAY.getBlue()) * anim.hoverAlpha),
                255
        );

        Renderer2D.renderQuad(context.getMatrices(), currentX - buttonWidth/2f, currentY, currentX + buttonWidth/2f, currentY + buttonHeight, bgColor);
        drawText(context, text, currentX - UltraByte.FONT_MANAGER.getWidth(text)/2f, currentY + 4, 1, textColor);
    }

    private void updateHoverStates(int mouseX, int mouseY) {
        for (Map.Entry<String, ButtonAnimation> entry : buttonAnimations.entrySet()) {
            ButtonAnimation anim = entry.getValue();
            anim.isHovering = isHoveringButton(anim.targetX, anim.targetY, mouseX, mouseY);
        }
    }

    private float easeOutCubic(float x) {
        return 1 - (float)Math.pow(1 - x, 3);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0) {
            if(width/2f - UltraByte.FONT_MANAGER.getWidth("UltraByte") <= mouseX && height/2f - UltraByte.FONT_MANAGER.getHeight()*2 - 5 <= mouseY && width/2f + UltraByte.FONT_MANAGER.getWidth("UltraByte") > mouseX && height/2f - 5 > mouseY) {
                try {
                    Util.getOperatingSystem().open(new URI("https://youtu.be/INE4RacaApQ?si=ShQU8VjfpgdxW8nb"));
                } catch (Exception ignored) { }
                playClickSound();
            }

            ButtonAnimation singleplayerAnim = buttonAnimations.get("Singleplayer");
            if (singleplayerAnim != null && isHoveringButton(singleplayerAnim.targetX, singleplayerAnim.targetY, mouseX, mouseY)) {
                mc.setScreen(new SelectWorldScreen(this));
                playClickSound();
            }

            ButtonAnimation multiplayerAnim = buttonAnimations.get("Multiplayer");
            if (multiplayerAnim != null && isHoveringButton(multiplayerAnim.targetX, multiplayerAnim.targetY, mouseX, mouseY)) {
                mc.setScreen(new MultiplayerScreen(this));
                playClickSound();
            }

            ButtonAnimation settingsAnim = buttonAnimations.get("Game Settings");
            if (settingsAnim != null && isHoveringButton(settingsAnim.targetX, settingsAnim.targetY, mouseX, mouseY)) {
                mc.setScreen(new OptionsScreen(this, mc.options));
                playClickSound();
            }

            ButtonAnimation quitAnim = buttonAnimations.get("Quit Game");
            if (quitAnim != null && isHoveringButton(quitAnim.targetX, quitAnim.targetY, mouseX, mouseY)) {
                mc.scheduleStop();
                playClickSound();
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawClientStatus(DrawContext context, float alpha) {
        if (UltraByte.UPDATE_STATUS.equalsIgnoreCase("none")) return;

        String primaryText = "";
        String secondaryText = "";
        Color baseColor = Color.WHITE;

        if (UltraByte.UPDATE_STATUS.equalsIgnoreCase("update-available")) {
            secondaryText = "An update is available for UltraByte.";
            primaryText = "Please restart the game to apply changes.";
            baseColor = Color.ORANGE;
        }

        if (UltraByte.UPDATE_STATUS.equalsIgnoreCase("failed-connection")) {
            secondaryText = "Failed to connect to UltraByte's servers.";
            primaryText = "Please make sure you have a working internet connection.";
            baseColor = Color.RED;
        }

        if (UltraByte.UPDATE_STATUS.equalsIgnoreCase("failed")) {
            secondaryText = "Failed to update UltraByte.";
            primaryText = "Please make sure the auto-updater is working properly.";
            baseColor = Color.RED;
        }

        if (UltraByte.UPDATE_STATUS.equalsIgnoreCase("up-to-date")) {
            primaryText = "UltraByte is on the latest version.";
        }

        if (primaryText.isEmpty()) return;

        Color color = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(baseColor.getAlpha() * alpha));

        if(!secondaryText.isEmpty()) drawText(context, secondaryText, width/2f - UltraByte.FONT_MANAGER.getWidth(secondaryText)/2f, height - 30, 1, color);
        drawText(context, primaryText, width/2f - UltraByte.FONT_MANAGER.getWidth(primaryText)/2f, height - 20, 1, color);
    }

    public boolean isHoveringButton(double x, double y, double mouseX, double mouseY) {
        return x - buttonWidth/2f <= mouseX && y <= mouseY && x + buttonWidth/2f > mouseX && y + buttonHeight > mouseY;
    }

    private void drawText(DrawContext context, String text, float x, float y, float scale, Color color) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 0);
        UltraByte.FONT_MANAGER.drawText(context, text, 0, 0, color);
        context.getMatrices().pop();
    }

    private void loadSplashTexts() {
        Identifier identifier = Identifier.of(UltraByte.MOD_ID, "splash.txt");

        try {
            Resource resource = mc.getResourceManager().getResource(identifier).orElseThrow();
            splashTexts = resource.getReader().lines().toList();
            if (!splashTexts.isEmpty()) {
                currentSplashIndex = (int) MathUtils.random(splashTexts.size(), 0);
                currentSplashText = "";
            }
        } catch (Exception ignored) {
            splashTexts.add("Welcome to UltraByte!");
        }
    }

    private void playClickSound() {
        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }
}