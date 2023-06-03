package oth.shipeditor.components.viewer;

import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public interface ShipViewable {

    void repaintView();

    void loadLayer(ShipLayer layer);

    Point getPanelLocation();

    LayerPainter getSelectedLayer();

    ViewerControl getControls();

    AffineTransform getTransformWorldToScreen();

}
