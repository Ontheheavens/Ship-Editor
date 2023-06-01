package oth.shipeditor.components;

import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerGuidesToggled;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerLoadConfirmed;
import oth.shipeditor.components.control.ShipViewerControls;
import oth.shipeditor.components.control.ViewerControl;
import oth.shipeditor.components.painters.BoundPointsPainter;
import oth.shipeditor.components.painters.GuidesPainter;
import oth.shipeditor.components.painters.LayerPainter;
import oth.shipeditor.components.painters.WorldPointsPainter;
import oth.shipeditor.representation.ShipLayer;

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
    private boolean spriteLoaded;

    @Getter
    private final List<LayerPainter> layerPainters;

    @Getter @Setter
    private LayerPainter selectedLayer;

    @Getter
    private final WorldPointsPainter miscPointsPainter;

    @Getter
    private final BoundPointsPainter boundsPainter;

    @Getter
    private final GuidesPainter guidesPainter;

    @Getter
    private final ViewerControl controls;

    public ShipViewerPanel() {
        this.setMinimumSize(panelSize);
//        this.addComponentListener(new ResizeListener());
        this.setBackground(Color.GRAY);

        this.layerPainters = new ArrayList<>();

        this.miscPointsPainter = WorldPointsPainter.create();
        this.addPainter(this.miscPointsPainter, 3);

        this.boundsPainter = new BoundPointsPainter(this);
        this.addPainter(this.boundsPainter, 4);

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
    public void loadLayer(ShipLayer newLayer) {
        LayerPainter newPainter = new LayerPainter(newLayer, this);
        this.layerPainters.add(newPainter);
        this.addPainter(newPainter, 2);
        this.selectedLayer = newPainter;
        this.spriteLoaded = true;
        this.centerViewpoint();
        EventBus.publish(new ShipLayerLoadConfirmed(newLayer));
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
        double dx = (this.getWidth() / 2f) - centerScreen.getX();
        double dy = (this.getHeight() / 2f) - centerScreen.getY();
        this.translate(dx, dy);
        this.repaint();
    }

    @Override
    public Point getPanelLocation() {
        return this.getLocation();
    }

}
