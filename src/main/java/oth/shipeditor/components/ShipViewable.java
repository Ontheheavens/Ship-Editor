package oth.shipeditor.components;

import oth.shipeditor.components.control.ViewerControl;
import oth.shipeditor.components.painters.BoundPointsPainter;
import oth.shipeditor.components.painters.LayerPainter;
import oth.shipeditor.components.painters.WorldPointsPainter;
import oth.shipeditor.representation.ShipLayer;

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

    BoundPointsPainter getBoundsPainter();

    WorldPointsPainter getMiscPointsPainter();

    AffineTransform getTransformWorldToScreen();

}
