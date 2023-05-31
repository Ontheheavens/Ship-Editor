package oth.shipeditor.components;

import de.javagl.viewer.Viewer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformsReset;
import oth.shipeditor.components.control.ShipViewerControls;
import oth.shipeditor.components.painters.BoundPointsPainter;
import oth.shipeditor.components.painters.GuidesPainter;
import oth.shipeditor.components.painters.LayerPainter;
import oth.shipeditor.components.painters.WorldPointsPainter;
import oth.shipeditor.representation.ShipLayer;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 29.04.2023
 */
@Log4j2
public class ShipViewerPanel extends Viewer {

    @Getter
    private boolean spriteLoaded;

    @Getter
    private final List<LayerPainter> layerPainters;

    @Getter @Setter
    private LayerPainter selectedLayer;

    @Getter
    private final WorldPointsPainter pointsPainter;

    @Getter
    private final BoundPointsPainter boundsPainter;

    @Getter
    private final GuidesPainter guidesPainter;

    @Getter
    private final ShipViewerControls controls;

    public ShipViewerPanel() {
        this.setMinimumSize(new Dimension(240, 120));
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ShipViewerPanel.this.centerViewpoint();
            }
        });
        this.setBackground(Color.GRAY);

        this.layerPainters = new ArrayList<>();

        this.pointsPainter = new WorldPointsPainter();
        this.addPainter(pointsPainter, 3);

        this.boundsPainter = new BoundPointsPainter(this);
        this.addPainter(boundsPainter, 4);

        this.guidesPainter = new GuidesPainter(this);
        this.addPainter(guidesPainter, 4);

        controls = new ShipViewerControls(this);
        this.setMouseControl(controls);

        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if(event instanceof ViewerRepaintQueued) {
                repaint();
            }
        });
        EventBus.subscribe(event -> {
            if(event instanceof ViewerTransformsReset) {
                controls.setZoomLevel(1);
                resetTransform();
                centerViewpoint();
            }
        });
        EventBus.subscribe(event -> {
            if(event instanceof ViewerBackgroundChanged checked) {
                setBackground(checked.newColor());
                repaint();
            }
        });
    }

    public void loadLayer(ShipLayer newLayer) {
        LayerPainter newPainter = new LayerPainter(newLayer, this);
        this.layerPainters.add(newPainter);
        this.addPainter(newPainter, 2);
        this.selectedLayer = newPainter;
        this.spriteLoaded = true;
        this.centerViewpoint();
    }

    public Point getShipCenterAnchor() {
        if (getSelectedLayer() != null) {
            return new Point(0, getSelectedLayer().getShipSprite().getHeight());
        } else return new Point();
    }

    public void centerViewpoint() {
        AffineTransform worldToScreen = this.getWorldToScreen();
        // Get the center of the sprite in screen coordinates.
        Point2D centerScreen = worldToScreen.transform(this.getSelectedLayer().getSpriteCenter(), null);
        // Calculate the delta values to center the sprite.
        double dx = (this.getWidth() / 2f) - centerScreen.getX();
        double dy = (this.getHeight() / 2f) - centerScreen.getY();
        this.translate(dx, dy);
        this.repaint();
    }

}
