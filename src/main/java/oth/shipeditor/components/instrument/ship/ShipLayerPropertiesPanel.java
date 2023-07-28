package oth.shipeditor.components.instrument.ship;

import oth.shipeditor.components.instrument.ViewerLayerWidgetsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 11.06.2023
 */
final class ShipLayerPropertiesPanel extends JPanel {

    ShipLayerPropertiesPanel() {
        this.setLayout(new BorderLayout());
        JPanel layerWidgetsPanel = new ViewerLayerWidgetsPanel();
        this.add(layerWidgetsPanel, BorderLayout.CENTER);
    }








}
