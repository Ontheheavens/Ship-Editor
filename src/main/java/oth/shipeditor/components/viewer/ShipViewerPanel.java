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
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
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
    private final LayerManager layerManager;

    @Getter
    private final WorldPointsPainter miscPointsPainter;

    @Getter
    private final GuidesPainter guidesPainter;

    @Getter
    private final ViewerControl controls;

    public ShipViewerPanel() {
        this.setMinimumSize(panelSize);
        this.setBackground(Color.GRAY);

        // Required for early initializing of base coordinate systems functionality.
        BaseWorldPoint.initStaticListening();

        this.layerManager = new LayerManager();
        this.layerManager.initListeners();

        this.miscPointsPainter = WorldPointsPainter.create();
        this.addPainter(this.miscPointsPainter, 3);

        this.guidesPainter = new GuidesPainter(this);
        EventBus.publish(new ViewerGuidesToggled(true,
                true, true, true));
        this.addPainter(this.guidesPainter, 4);

        this.controls = ShipViewerControls.create(this);
        this.setMouseControl(this.controls);
        this.initViewerStateListeners();
        this.initLayerListening();
    }

    private void initViewerStateListeners() {
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
    }

    private void initLayerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                ShipLayer newLayer = checked.newLayer();
                if (newLayer.getShipSprite() != null) {
                    this.loadLayer(newLayer);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                ShipLayer newLayer = checked.updated();
                if (newLayer.getShipSprite() != null && checked.spriteChanged()) {
                    this.loadLayer(newLayer);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerShipDataInitialized checked) {
                LayerPainter source = checked.source();
                ShipLayer activeLayer = this.layerManager.getActiveLayer();
                if (source != activeLayer.getPainter()) return;
                this.addPainter(source.getCenterPointsPainter(), checked.ordering());
                this.addPainter(source.getBoundsPainter(), checked.ordering());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                if (checked.old() != null) {
                    this.removeLayerPainters(checked.old());
                }
                if (checked.selected() != null) {
                    this.loadLayerPointPainters(checked.selected());
                }
                this.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerRemovalConfirmed checked) {
                this.unloadLayer(checked.removed());
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
            boolean added = this.addPainter(iterated, 4);
            if (added) {
                log.info("Loaded to viewer:{}", iterated);
            }
        }
    }

    private void removeLayerPainters(ShipLayer layer) {
        LayerPainter mainPainter = layer.getPainter();
        if (mainPainter == null) return;
        List<Painter> layerPainters = mainPainter.getAllPainters();
        for (Painter iterated : layerPainters) {
            boolean removed = this.removePainter(iterated, 4);
            if (removed) {
                log.info("Removed from viewer:{}", iterated);
            }
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
        // TODO: consider the ordering of added layers later.
        this.addPainter(newPainter);
        this.centerViewpoint();
        EventBus.publish(new ShipLayerLoadConfirmed(layer));
    }

    private void unloadLayer(ShipLayer layer) {
        this.removeLayerPainters(layer);
        this.removePainter(layer.getPainter());
    }

    public void centerViewpoint() {
        ShipLayer activeLayer = this.layerManager.getActiveLayer();
        if (activeLayer == null) return;
        AffineTransform worldToScreen = this.getWorldToScreen();
        // Get the center of the sprite in screen coordinates.
        LayerPainter activeLayerPainter = activeLayer.getPainter();
        Point2D spriteCenter = activeLayerPainter.getSpriteCenter();
        Point2D centerScreen = worldToScreen.transform(spriteCenter, null);
        // Calculate the delta values to center the sprite.
        double dx = (this.getWidth() / 2.0f) - centerScreen.getX();
        double dy = (this.getHeight() / 2.0f) - centerScreen.getY();
        this.translate(dx, dy);
        this.repaint();
    }

}
