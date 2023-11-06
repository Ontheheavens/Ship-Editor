package oth.shipeditor.components.viewer.layers;

import de.javagl.viewer.Painter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.LayerAnchorDragged;
import oth.shipeditor.communication.events.viewer.layers.LayerRotationQueued;
import oth.shipeditor.communication.events.viewer.layers.ViewerLayerRemovalConfirmed;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetQueued;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.undo.UndoOverseer;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.StaticController;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 28.07.2023
 */
@Getter
@SuppressWarnings("ClassWithTooManyMethods")
public abstract class LayerPainter implements Painter {

    private final List<AbstractPointPainter> allPainters;

    private Point2D anchor = new Point2D.Double(0, 0);

    private float spriteOpacity = 1.0f;

    @Setter
    private double rotationRadians;

    private final ViewerLayer parentLayer;

    @Setter
    private Sprite sprite;

    @Setter(AccessLevel.PROTECTED)
    private boolean uninitialized = true;

    @Setter
    private boolean shouldDrawPainter = true;

    private final List<BusEventListener> listeners;

    protected LayerPainter(ViewerLayer layer) {
        this.parentLayer = layer;
        this.allPainters = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.initLayerListeners();
    }

    public Dimension getSpriteSize() {
        Sprite spriteContainer = getSprite();
        var spriteImage = spriteContainer.getImage();
        return new Dimension(spriteImage.getWidth(), spriteImage.getHeight());
    }

    public BufferedImage getSpriteImage() {
        return sprite.getImage();
    }

    private void initLayerListeners() {
        BusEventListener removalListener = event -> {
            if (event instanceof ViewerLayerRemovalConfirmed checked) {
                if (checked.removed() != this.getParentLayer()) return;
                this.cleanupForRemoval();
            }
        };
        listeners.add(removalListener);
        EventBus.subscribe(removalListener);
        BusEventListener anchorDragListener = event -> {
            if (event instanceof LayerAnchorDragged checked && checked.selected() == this) {
                AffineTransform screenToWorld = checked.screenToWorld();
                Point2D difference = checked.difference();
                Point2D wP = screenToWorld.transform(difference, null);
                double roundedX = Math.round(wP.getX() * 2) / 2.0;
                double roundedY = Math.round(wP.getY() * 2) / 2.0;
                Point2D corrected = new Point2D.Double(roundedX, roundedY);
                updateAnchorOffset(corrected);
            }
        };
        listeners.add(anchorDragListener);
        EventBus.subscribe(anchorDragListener);

        BusEventListener rotationListener = event -> {
            if (event instanceof LayerRotationQueued checked) {
                if (checked.layer() != this) return;
                this.rotateToTarget(checked.worldTarget());
            }
        };
        listeners.add(rotationListener);
        EventBus.subscribe(rotationListener);
    }

    public abstract Point2D getEntityCenter();

    public void setAnchor(Point2D inputAnchor) {
        Point2D oldAnchor = this.getAnchor();

        Point2D difference = new Point2D.Double(oldAnchor.getX() - inputAnchor.getX(),
                oldAnchor.getY() - inputAnchor.getY());
        EventBus.publish(new AnchorOffsetQueued(this, difference));

        this.anchor = inputAnchor;
    }

    public void reconfigureSpriteCircumstance(Sprite updated) {
        BufferedImage previous = this.getSpriteImage();
        var spriteHeight = previous.getHeight();

        BufferedImage updatedImage = updated.getImage();
        var updatedHeight = updatedImage.getHeight();

        int heightDifference = spriteHeight - updatedHeight;

        var painterList = this.getAllPainters();
        painterList.forEach(abstractPointPainter -> {
            List<? extends BaseWorldPoint> pointsIndex = abstractPointPainter.getPointsIndex();
            pointsIndex.forEach((Consumer<BaseWorldPoint>) baseWorldPoint -> {
                Point2D oldPosition = baseWorldPoint.getPosition();
                baseWorldPoint.setPosition(oldPosition.getX(), oldPosition.getY() - heightDifference);
            });
        });

        this.setSprite(updated);
    }

    public boolean isLayerActive() {
        ViewerLayer layer = this.getParentLayer();
        if (layer == null) return false;
        return StaticController.getActiveLayer() == layer;
    }

    public void cleanupForRemoval() {
        cleanupPointPainters();
        listeners.forEach(EventBus::unsubscribe);
        UndoOverseer.cleanupRemovedLayer(this);
    }

    protected void cleanupPointPainters() {
        List<AbstractPointPainter> painters = this.getAllPainters();
        for (AbstractPointPainter pointPainter : painters) {
            pointPainter.cleanupPointPainter();
        }
        this.allPainters.clear();
    }

    public void setSpriteOpacity(float opacity) {
        if (opacity < 0.0f) {
            this.spriteOpacity = 0.0f;
        } else this.spriteOpacity = Math.min(opacity, 1.0f);
    }

    @Override
    public String toString() {
        Class<? extends LayerPainter> identity = this.getClass();
        return identity.getSimpleName() + " #" + this.hashCode();
    }

    private void rotateToTarget(Point2D worldTarget) {
        Point2D center = getRotationAnchor();
        double deltaX = worldTarget.getX() - center.getX();
        double deltaY = worldTarget.getY() - center.getY();

        double radians = -Math.atan2(deltaX, deltaY);

        double rotationDegrees = Math.toDegrees(radians) + 180;
        double result = rotationDegrees;
        if (ControlPredicates.isRotationRoundingEnabled()) {
            result = Math.round(rotationDegrees);
        }
        this.rotateLayer(result);
    }

    @SuppressWarnings("WeakerAccess")
    protected Point2D getRotationAnchor() {
        return this.getSpriteCenter();
    }

    @SuppressWarnings("WeakerAccess")
    public void rotateLayer(double rotationDegrees) {
        EditDispatch.postLayerRotated(this, this.getRotationRadians(), Math.toRadians(rotationDegrees));
    }

    public AffineTransform getWithRotation(AffineTransform worldToScreen) {
        AffineTransform transform = new AffineTransform(worldToScreen);
        transform.concatenate(getRotationTransform());
        return transform;
    }

    public AffineTransform getRotationTransform() {
        double rotation = this.getRotationRadians();
        Point2D center = this.getRotationAnchor();
        double centerX = center.getX();
        double centerY = center.getY();
        return AffineTransform.getRotateInstance(rotation, centerX, centerY);
    }

    public AffineTransform getWithRotationInverse(AffineTransform worldToScreen) {
        AffineTransform transform;
        AffineTransform worldToScreenCopy = new AffineTransform(worldToScreen);
        try {
            AffineTransform inverseRotation = getRotationTransform();
            worldToScreenCopy.concatenate(inverseRotation);
            transform = worldToScreenCopy.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException("Non-invertible rotation transform of layer!", e);
        }
        return transform;
    }

    public int getSpriteWidth() {
        Sprite spriteContainer = getSprite();
        var spriteImage = spriteContainer.getImage();
        return spriteImage.getWidth();
    }

    public int getSpriteHeight() {
        Sprite spriteContainer = getSprite();
        var spriteImage = spriteContainer.getImage();
        return spriteImage.getHeight();
    }

    /**
     * Note: if called programmatically outside of usual user input flow,
     * {@link oth.shipeditor.undo.UndoOverseer} needs to finish all edits programmatically as well,
     * for consistent undo/redo behaviour.
     * @param updated new position of the anchor offset.
     */
    public void updateAnchorOffset(Point2D updated) {
        EditDispatch.postAnchorOffsetChanged(this, updated);
    }

    /**
     * @return world position of the sprite center. Is dependent on anchor position.
     */
    public Point2D getSpriteCenter() {
        Point2D difference = this.getSpriteCenterDifferenceToAnchor();
        return new Point2D.Double((anchor.getX() + difference.getX()),
                (anchor.getY() + difference.getY()));
    }

    public Point2D getSpriteCenterDifferenceToAnchor() {
        return new Point2D.Double((this.getSpriteWidth() / 2.0f), (this.getSpriteHeight() / 2.0f));
    }

    protected void paintContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        int width = this.getSpriteWidth();
        int height = this.getSpriteHeight();
        Sprite spriteContainer = getSprite();
        g.drawImage(spriteContainer.getImage(), (int) anchor.getX(), (int) anchor.getY(), width, height, null);

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!shouldDrawPainter) return;
        AffineTransform oldAT = g.getTransform();
        g.transform(worldToScreen);

        float alpha = this.getSpriteOpacity();
        Composite old = Utility.setAlphaComposite(g, alpha);

        this.paintContent(g, worldToScreen, w, h);

        g.setComposite(old);
        g.setTransform(oldAT);
    }

}
