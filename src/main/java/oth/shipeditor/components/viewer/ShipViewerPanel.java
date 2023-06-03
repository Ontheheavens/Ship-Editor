package oth.shipeditor.components.viewer;

import de.javagl.viewer.Painter;
import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;
import oth.shipeditor.communication.events.viewer.layers.*;
import oth.shipeditor.components.viewer.control.ShipViewerControls;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.GuidesPainter;
import oth.shipeditor.components.viewer.painters.WorldPointsPainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public final class ShipViewerPanel extends Viewer implements ShipViewable {

    private static final Dimension panelSize = new Dimension(240, 120);

    @Getter
    private boolean layerLoaded;

    @Getter
    private final LayerManager layerManager;

    @Getter
    private final WorldPointsPainter miscPointsPainter;

    // TODO: add coordinate axes painter.
    @Getter
    private final GuidesPainter guidesPainter;

    @Getter
    private final ViewerControl controls;

    public ShipViewerPanel() {
        this.setMinimumSize(panelSize);
        this.setBackground(Color.GRAY);

        this.layerManager = new LayerManager();
        this.layerManager.initListeners();

        this.miscPointsPainter = WorldPointsPainter.create();
        this.addPainter(this.miscPointsPainter, 3);

        this.guidesPainter = new GuidesPainter(this);
        this.addPainter(this.guidesPainter, 4);

        this.controls = ShipViewerControls.create(this);
        this.setMouseControl(this.controls);

        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if(event instanceof ViewerRepaintQueued) {
                this.repaint();
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
                this.setBackground(background);
                this.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                ShipLayer newLayer = checked.newLayer();
                if (newLayer.getShipSprite() != null) {
                    this.loadLayer(newLayer);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerUpdated checked) {
                ShipLayer newLayer = checked.updated();
                if (newLayer.getShipSprite() != null && checked.spriteChanged()) {
                    this.loadLayer(newLayer);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerPaintersInitialized checked) {
                LayerPainter source = checked.source();
                ShipLayer activeLayer = this.layerManager.getActiveLayer();
                if (source != activeLayer.getPainter()) return;
                this.addPainter(source.getHullPointsPainter(), checked.ordering());
                this.addPainter(source.getBoundsPainter(), checked.ordering());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                this.removeLayerPainters(checked.old());
                this.loadLayerPointPainters(checked.selected());
                this.repaint();
            }
        });
    }

    @Override
    public AffineTransform getTransformWorldToScreen() {
        return this.getWorldToScreen();
    }

    /**
     * @return layer that is currently active in viewer; might be null, in which case caller is expected to handle that.
     */
    @Override
    @SuppressWarnings("ReturnOfNull")
    public LayerPainter getSelectedLayer() {
        ShipLayer activeLayer = layerManager.getActiveLayer();
        if (activeLayer == null) {
            return null;
        }
        return activeLayer.getPainter();
    }

    private void loadLayerPointPainters(ShipLayer layer) {
        LayerPainter mainPainter = layer.getPainter();
        if (mainPainter == null) return;
        List<Painter> layerPainters = mainPainter.getAllPainters();
        for (Painter iterated : layerPainters) {
            log.info("Loading painter:" + iterated);
            // TODO: check for layer ordering consistency later.
            this.addPainter(iterated, 4);
        }
    }

    private void removeLayerPainters(ShipLayer layer) {
        LayerPainter mainPainter = layer.getPainter();
        if (mainPainter == null) return;
        List<Painter> layerPainters = mainPainter.getAllPainters();
        for (Painter iterated : layerPainters) {
            log.info("Removing painter:" + iterated);
            // TODO: check for layer ordering consistency later.
            this.removePainter(iterated, 4);
        }
    }

    @Override
    public void loadLayer(ShipLayer layer) {
        LayerPainter newPainter = new LayerPainter(layer, this);
        ShipLayer activeLayer = this.layerManager.getActiveLayer();
        activeLayer.setPainter(newPainter);
        // Main sprite painter and said painter children point painters are distinct conceptually.
        // Layer might be selected and deselected, in which case children painters are loaded/unloaded.
        // At the same time main sprite painter remains loaded until layer is explicitly removed.
        this.addPainter(newPainter);
        this.layerLoaded = true;
        this.centerViewpoint();
        EventBus.publish(new ShipLayerLoadConfirmed(layer));
        // TODO: sort out guides toggling/layer switching, add coordinate axes.
        ShipViewerPanel.toggleGuides(true, true, true);
    }

    private static void toggleGuides(boolean guidesOn, boolean bordersOn, boolean centerOn) {
        EventBus.publish(new ViewerGuidesToggled(guidesOn, bordersOn, centerOn));
    }

    private void centerViewpoint() {
        ShipLayer activeLayer = this.layerManager.getActiveLayer();
        if (activeLayer == null) return;
        AffineTransform worldToScreen = this.getWorldToScreen();
        // Get the center of the sprite in screen coordinates.
        LayerPainter activeLayerPainter = activeLayer.getPainter();
        Point spriteCenter = activeLayerPainter.getSpriteCenter();
        Point2D centerScreen = worldToScreen.transform(spriteCenter, null);
        // Calculate the delta values to center the sprite.
        double dx = (this.getWidth() / 2.0f) - centerScreen.getX();
        double dy = (this.getHeight() / 2.0f) - centerScreen.getY();
        this.translate(dx, dy);
        this.repaint();
    }

    @Override
    public Point getPanelLocation() {
        return this.getLocation();
    }

}
