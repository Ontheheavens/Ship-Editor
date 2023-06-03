package oth.shipeditor.components.viewer;

import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;
import oth.shipeditor.communication.events.viewer.layers.PainterAdditionQueued;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerLoadConfirmed;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.components.viewer.control.ShipViewerControls;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.GuidesPainter;
import oth.shipeditor.components.viewer.painters.WorldPointsPainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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
    private final List<LayerPainter> layerPainters;

    @Getter @Setter
    private LayerPainter selectedLayer;

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

        this.layerPainters = new ArrayList<>();

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
        // TODO: all of these layer mechanisms are super bad, need to be streamlined and compacted later.
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerUpdated checked) {
                ShipLayer newLayer = checked.updated();
                if (newLayer.getShipSprite() != null) {
                    this.loadLayer(newLayer);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PainterAdditionQueued checked) {
                this.addPainter(checked.toAdd(), checked.ordering());
            }
        });
    }

    @Override
    public AffineTransform getTransformWorldToScreen() {
        return this.getWorldToScreen();
    }

    @Override
    public void repaintView() {
        this.repaint();
    }

    @Override
    public void loadLayer(ShipLayer layer) {
        LayerPainter newPainter = new LayerPainter(layer, this);
        this.layerPainters.add(newPainter);
        this.addPainter(newPainter, 2);
        this.selectedLayer = newPainter;
        this.layerLoaded = true;
        this.centerViewpoint();
        EventBus.publish(new ShipLayerLoadConfirmed(layer));
        ShipViewerPanel.toggleGuides(true, true, true);
    }

    private static void toggleGuides(boolean enableGuides,
                                     boolean enableBorders,
                                     boolean enableCenter) {
        EventBus.publish(new ViewerGuidesToggled(
                        enableGuides,
                        enableBorders,
                        enableCenter
                )
        );
    }

    private void centerViewpoint() {
        if (this.selectedLayer == null) return;
        AffineTransform worldToScreen = this.getWorldToScreen();
        // Get the center of the sprite in screen coordinates.
        Point spriteCenter = this.selectedLayer.getSpriteCenter();
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
