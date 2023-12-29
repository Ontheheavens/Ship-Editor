package oth.shipeditor.components.viewer.layers.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringConstants;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 28.12.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Getter
public class WeaponAnimator {

    private boolean initialized;

    private boolean drawGlow;

    private boolean alwaysAnimate;

    @Setter
    private int currentFrame;

    private int frameCount;

    /**
     * The delay in milliseconds between each frame.
     */
    private int frameDelay;

    private Map<Integer, Sprite> turretFrameSprites;

    private Map<Integer, Sprite> hardpointFrameSprites;

    private long lastRefreshTime;

    private boolean playing;

    @Setter
    private Consumer<Integer> onFrameChange;

    public void initAnimations(WeaponSpecFile specFile, WeaponSprites sprites) {
        this.initialized = true;
        this.alwaysAnimate = specFile.isAlwaysAnimate();

        this.frameCount = specFile.getNumFrames();
        if (specFile.getFrameRate() == 0) {
            this.frameDelay = Integer.MAX_VALUE;
        } else {
            this.frameDelay = 1000 / specFile.getFrameRate();
        }

        this.initFrames(sprites);
    }

    public void setFrameRate(int frameRate) {
        if (frameRate == 0) {
            this.frameDelay = Integer.MAX_VALUE;
        } else {
            this.frameDelay = 1000 / frameRate;
        }
    }

    public int getFrameRate() {
        if (frameDelay == Integer.MAX_VALUE) {
            return 0;
        } else {
            return 1000 / frameDelay;
        }
    }

    public void startAnimation() {
        if (!initialized) return;
        playing = true;
        lastRefreshTime = System.currentTimeMillis();
        if (frameDelay == Integer.MAX_VALUE && frameCount >= 2) {
            this.currentFrame = 1;
        }
    }

    public void stopAnimation() {
        playing = false;
        this.currentFrame = 0;
    }

    private void initFrames(WeaponSprites sprites) {
        this.turretFrameSprites = new LinkedHashMap<>();
        this.hardpointFrameSprites = new LinkedHashMap<>();

        int numberOfFrames = this.getFrameCount();

        Sprite turretSprite = sprites.getTurretSprite();
        Path turretPath = turretSprite.getPath();

        Sprite hardpointSprite = sprites.getHardpointSprite();
        Path hardpointPath = hardpointSprite.getPath();

        WeaponAnimator.loadFramesForMount(numberOfFrames, turretPath, turretFrameSprites);
        WeaponAnimator.loadFramesForMount(numberOfFrames, hardpointPath, hardpointFrameSprites);
    }

    private static void loadFramesForMount(int numberOfFrames, Path mountPath, Map<Integer, Sprite> framesMap) {
        String fileName = mountPath.getFileName().toString();

        String dotWithExtension = StringConstants.EXTENSION_DOT + FileUtilities.getExtension(fileName);
        for (int i = 0; i < numberOfFrames; i++) {
            String frameIndex = String.format("%02d", i);
            String frameFileName = fileName.replace("00" + dotWithExtension, frameIndex + dotWithExtension);
            Path framePath = mountPath.resolveSibling(frameFileName);
            Sprite loadedSprite = FileLoading.loadSprite(framePath.toFile());
            framesMap.put(i, loadedSprite);
        }
    }

    private void updateFrame() {
        long elapsedSinceLastRefresh = System.currentTimeMillis() - lastRefreshTime;
        if (elapsedSinceLastRefresh > frameDelay) {
            lastRefreshTime = System.currentTimeMillis();
            if (currentFrame < frameCount - 1) {
                currentFrame++;
            } else {
                currentFrame = 0;
            }
            if (onFrameChange != null) {
                onFrameChange.accept(currentFrame);
            }
        }
    }

    public Sprite getCurrentSprite(WeaponMount mount) {
        if (playing) {
            updateFrame();
        }
        if (mount == WeaponMount.HARDPOINT) {
            return hardpointFrameSprites.get(currentFrame);
        } else {
            return turretFrameSprites.get(currentFrame);
        }
    }

}
