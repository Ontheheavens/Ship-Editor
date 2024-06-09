package oth.shipeditor.components.viewer;

import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.ViewerFocusRequestQueued;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;
import oth.shipeditor.communication.events.viewer.layers.*;
import oth.shipeditor.components.viewer.control.LayerViewerControls;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponSprites;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.undo.UndoOverseer;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.StaticController;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * This one is a conceptual root of the whole app.
 * It is responsible for the foundation of editing workflow - visual display of ships and its point features.
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Getter
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public final class PrimaryViewer extends Viewer implements LayerViewer {

    private static final Dimension minimumPanelSize = new Dimension(240, 120);

    private final LayerManager layerManager;

    private PaintOrderController paintOrderController;

    @Setter
    private boolean cursorInViewer;

    private ViewerControl viewerControls;

    public PrimaryViewer() {
        this.setMinimumSize(minimumPanelSize);
        this.setBackground(Color.GRAY);

        this.layerManager = new LayerManager();
        this.layerManager.initListeners();

        this.setMaintainAspectRatio(false);
    }

    @SuppressWarnings("OverlyComplexAnonymousInnerClass")
    public PrimaryViewer commenceInitialization() {
        this.paintOrderController = new PaintOrderController(this);
        this.addPainter(this.paintOrderController);

        EventBus.publish(new ViewerGuidesToggled(true, true,
                true, true));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (cursorInViewer) return;
                PrimaryViewer.this.requestFocusInWindow();
                cursorInViewer = true;
                setRepaintQueued();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!cursorInViewer) return;
                cursorInViewer = false;
                setRepaintQueued();
            }
        });

        viewerControls = LayerViewerControls.create(this);
        this.setMouseControl(viewerControls);
        this.initViewerStateListeners();
        this.initLayerListening();
        this.setDropTarget(new ViewerDropReceiver(this));
        StaticController.setViewer(this);
        return this;
    }

    public void setRepaintQueued() {
        this.paintOrderController.setRepaintQueued(true);
    }

    private void initViewerStateListeners() {
        EventBus.subscribe(event -> {
            if(event instanceof ViewerRepaintQueued || event instanceof LayerWasSelected) {
                setRepaintQueued();
            }
        });
        EventBus.subscribe(event -> {
            if(event instanceof ViewerFocusRequestQueued) {
                this.requestFocusInWindow();
            }
        });
        EventBus.subscribe(event -> {
            if(event instanceof ViewerTransformsReset) {
                this.resetTransform();
                this.centerViewpoint();
            }
        });
        EventBus.subscribe(event -> {
            if(event instanceof ViewerBackgroundChanged checked) {
                Color background = checked.newColor();
                Color opaque = new Color(background.getRed(),
                        background.getGreen(), background.getBlue(), 255);
                this.setBackground(opaque);
                setRepaintQueued();
            }
        });
    }

    private void initLayerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerSpriteLoadQueued checked) {
                ViewerLayer layer = checked.updated();
                Sprite sprite = checked.sprite();
                this.loadSpriteToLayer(layer, sprite);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerLayerRemovalConfirmed checked) {
                PrimaryViewer.unloadLayer(checked.removed());
                setRepaintQueued();
            }
        });
    }

    public void loadSpriteToLayer(ViewerLayer layer, Sprite sprite) {
        if (layer.getPainter() == null && sprite != null) {
            this.loadLayer(layer, sprite);
        } else if (sprite != null) {
            LayerPainter painter = layer.getPainter();
            Sprite oldSprite = painter.getSprite();
            EditDispatch.postLayerSpriteSwapped(painter, oldSprite, sprite);
        }
    }

    @Override
    public AffineTransform getTransformWorldToScreen() {
        return this.getWorldToScreen();
    }

    /**
     * @return layer that is currently active in viewer; might be null, in which case caller is expected to handle that.
     */
    @Override
    public LayerPainter getSelectedLayer() {
        ViewerLayer activeLayer = layerManager.getActiveLayer();
        if (activeLayer == null) {
            return null;
        }
        return activeLayer.getPainter();
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    @Override
    public ViewerLayer loadLayer(ViewerLayer layer, Sprite sprite) {
        LayerPainter newPainter = null;
        boolean painterCreated = false;
        if (layer.getPainter() == null) {
            if (layer instanceof ShipLayer checkedLayer) {
                ShipPainter shipPainter = new ShipPainter(checkedLayer);
                shipPainter.setBaseHullSprite(sprite);
                newPainter = shipPainter;
            } else if (layer instanceof WeaponLayer checkedLayer) {
                WeaponPainter weaponPainter = new WeaponPainter(checkedLayer);
                WeaponSprites weaponSprites = weaponPainter.getWeaponSprites();
                weaponSprites.setTurretSprite(sprite);
                newPainter = weaponPainter;
            }
            layer.setPainter(newPainter);
            if (newPainter != null) {
                newPainter.setSprite(sprite);
            }
        } else {
            newPainter = layer.getPainter();
        }

        layerManager.setActiveLayer(layer);

        if (newPainter != null) {
            List<ViewerLayer> layers = layerManager.getLayers();
            if (layers.size() > 1) {
                // Ideally, this needs to loop through all layers starting from last, checking if painter is present.
                // Then get anchor of that and place new painter anchor next to it.
                var prevLayer = layers.get(layers.indexOf(layer) - 1);
                var layerPainter = prevLayer.getPainter();
                if (layerPainter != null) {
                    var layerAnchor = layerPainter.getAnchor();
                    var prevLayerWidth = layerPainter.getSpriteSize();
                    Point2D widthPoint = new Point2D.Double(layerAnchor.getX() + prevLayerWidth.width, layerAnchor.getY());

                    newPainter.updateAnchorOffset(widthPoint);
                    UndoOverseer.finishAllEdits();
                }
            }
        }
        EventBus.publish(new LayerSpriteLoadConfirmed(layer, sprite));
        EventBus.publish(new ActiveLayerUpdated(layer));
        return layer;
    }

    private static void unloadLayer(ViewerLayer layer) {
        LayerPainter mainPainter = layer.getPainter();
        UndoOverseer.cleanupRemovedLayer(mainPainter);
    }

    public void centerViewpoint() {
        ViewerLayer activeLayer = this.layerManager.getActiveLayer();
        if (activeLayer == null) return;
        AffineTransform worldToScreen = this.getWorldToScreen();
        // Get the center of the sprite in screen coordinates.
        LayerPainter activePainter = activeLayer.getPainter();
        Point2D spriteCenter = activePainter.getSpriteCenter();
        Point2D centerScreen = worldToScreen.transform(spriteCenter, null);
        // Calculate the delta values to center the sprite.
        Point2D midpoint = this.getViewerMidpoint();
        double dx = midpoint.getX() - centerScreen.getX();
        double dy = midpoint.getY() - centerScreen.getY();
        this.translate(dx, dy);
        setRepaintQueued();
    }

    public Point2D getViewerMidpoint() {
        double x = (this.getWidth() / 2.0f);
        double y = (this.getHeight() / 2.0f);
        return new Point2D.Double(x, y);
    }

}
