package oth.shipeditor.components.viewer;

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
import oth.shipeditor.components.viewer.painters.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.GuidesPainter;
import oth.shipeditor.components.viewer.painters.HotkeyHelpPainter;
import oth.shipeditor.components.viewer.painters.WorldPointsPainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Despite being a bit of a God-class, this one is justified in its coupling as a conceptual root of the whole app.
 * It is responsible for the foundation of editing workflow - visual display of ships and its point features.
 * @author Ontheheavens
 * @since 29.04.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public final class PrimaryShipViewer extends Viewer implements ShipViewable {

    private static final Dimension panelSize = new Dimension(240, 120);

    @Getter
    private final LayerManager layerManager;

    @Getter
    private final WorldPointsPainter miscPointsPainter;

    @Getter
    private final GuidesPainter guidesPainter;

    private final HotkeyHelpPainter hotkeyPainter;

    @Getter
    private final ViewerControl controls;

    private int layerCount;

    /**
     * Usage of self as an argument is a suboptimal practice, but in this case it did not prove to be an issue.
     * However, keep this constructor in mind for future refactors.
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public PrimaryShipViewer() {
        this.setMinimumSize(panelSize);
        this.setBackground(Color.GRAY);

        // Required for early initializing of base coordinate systems functionality.
        BaseWorldPoint.initStaticListening();

        this.layerManager = new LayerManager();
        this.layerManager.initListeners();

        this.miscPointsPainter = WorldPointsPainter.create();
        this.addPainter(this.miscPointsPainter, 901);

        this.guidesPainter = new GuidesPainter(this);
        EventBus.publish(new ViewerGuidesToggled(true,
                true, true, true));
        this.addPainter(this.guidesPainter, 902);

        this.hotkeyPainter = new HotkeyHelpPainter();
        this.addPainter(this.hotkeyPainter, 903);

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
                Color opaque = new Color(background.getRed(),
                        background.getGreen(), background.getBlue(), 255);
                this.setBackground(opaque);
                this.revalidate();
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
                    PrimaryShipViewer.hideLayerPainters(checked.old());
                }
                if (checked.selected() != null) {
                    PrimaryShipViewer.showLayerPainters(checked.selected());
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

    private static void showLayerPainters(ShipLayer layer) {
        LayerPainter mainPainter = layer.getPainter();
        if (mainPainter == null) return;
        List<AbstractPointPainter> layerPainters = mainPainter.getAllPainters();
        for (AbstractPointPainter iterated : layerPainters) {
            iterated.setShown(true);
            log.info("Shown to viewer:{}", iterated);
        }
    }

    private static void hideLayerPainters(ShipLayer layer) {
        LayerPainter mainPainter = layer.getPainter();
        if (mainPainter == null) return;
        List<AbstractPointPainter> layerPainters = mainPainter.getAllPainters();
        for (AbstractPointPainter iterated : layerPainters) {
            iterated.setShown(false);
            log.info("Hidden from viewer:{}", iterated);
        }
    }

    @Override
    public void loadLayer(ShipLayer layer) {
        LayerPainter newPainter = new LayerPainter(layer, this, layerCount);
        ShipLayer activeLayer = this.layerManager.getActiveLayer();
        activeLayer.setPainter(newPainter);
        // Main sprite painter and said painter children point painters are distinct conceptually.
        // Layer might be selected and deselected, in which case children painters are loaded/unloaded.
        // At the same time main sprite painter remains loaded until layer is explicitly removed.
        this.addPainter(newPainter, layerCount);
        ++layerCount;
        this.centerViewpoint();
        EventBus.publish(new ShipLayerLoadConfirmed(layer));
    }

    private void unloadLayer(ShipLayer layer) {
        LayerPainter mainPainter = layer.getPainter();
        if (mainPainter != null) {
            List<AbstractPointPainter> layerPainters = mainPainter.getAllPainters();
            for (AbstractPointPainter iterated : layerPainters) {
                boolean removed = this.removePainter(iterated);
                if (removed) {
                    log.info("Removed from viewer:{}", iterated);
                }
            }
        }
        this.removePainter(layer.getPainter());
        --layerCount;
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
        Point2D midpoint = this.getViewerMidpoint();
        double dx = midpoint.getX() - centerScreen.getX();
        double dy = midpoint.getY() - centerScreen.getY();
        this.translate(dx, dy);
        this.repaint();
    }

    public Point2D getViewerMidpoint() {
        double x = (this.getWidth() / 2.0f);
        double y = (this.getHeight() / 2.0f);
        return new Point2D.Double(x, y);
    }

}
