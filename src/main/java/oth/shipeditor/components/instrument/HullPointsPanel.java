package oth.shipeditor.components.instrument;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CentersPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerShipDataInitialized;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.CenterPointsPainter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 06.06.2023
 */
@Log4j2
final class HullPointsPanel extends JPanel {

    /**
     * Reference to the hull center painter of the currently active layer.
     */
    private CenterPointsPainter centersPainter;

    private JLabel centerCoords;

    private JLabel collisionRadius;

    // TODO: Implement this whole panel. It should include ship center and shield center, and their radii.

    HullPointsPanel() {
        this.setBorder(new EmptyBorder(0, 6, 4, 6));
        LayoutManager layout = new GridLayout(3, 1);
        this.setLayout(layout);
        JPanel hullCenterPanel = createShipCenterPanel();
        this.add(hullCenterPanel);
        JPanel shieldCenterPanel = new JPanel();
        shieldCenterPanel.setBorder(BorderFactory.createTitledBorder("Shield"));
        this.add(shieldCenterPanel);
        this.initLayerListeners();
        this.initPointListener();
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof CentersPanelRepaintQueued) {
                this.refresh();
            }
        });
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerShipDataInitialized checked) {
                LayerPainter selectedLayerPainter = checked.source();
                this.centersPainter = selectedLayerPainter.getCenterPointsPainter();
                this.refresh();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                if (selected != null && selected.getPainter() != null) {
                    LayerPainter selectedLayerPainter = selected.getPainter();
                    this.centersPainter = selectedLayerPainter.getCenterPointsPainter();
                    this.refresh();
                }
            }
        });
    }

    private void refresh() {
        this.updateLabels();
        this.repaint();
    }

    private void updateLabels() {
        log.info("Centers panel updated!");
        String centerPosition = "Center not initialized";
        String collisionValue = "Collision not initialized";
        if (this.centersPainter != null) {
            ShipCenterPoint center = this.centersPainter.getCenterPoint();
            if (center != null) {
                Point2D position = center.getPosition();
                centerPosition = position.toString();
                collisionValue = String.valueOf(center.getCollisionRadius());
            }
        }
        centerCoords.setText(centerPosition);
        collisionRadius.setText(collisionValue);
    }

    private JPanel createShipCenterPanel() {
        JPanel hullCenterPanel = new JPanel();

        centerCoords = new JLabel();
        collisionRadius = new JLabel();

        updateLabels();

        hullCenterPanel.add(centerCoords);
        hullCenterPanel.add(collisionRadius);

        // Collision panel needs to have a radio button that enables interaction with center point, which is otherwise locked.

        // TODO: Also - gotta beautify this, big time!
        hullCenterPanel.setBorder(BorderFactory.createTitledBorder("Collision"));

        return hullCenterPanel;
    }

}
