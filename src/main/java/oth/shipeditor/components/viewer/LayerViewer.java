package oth.shipeditor.components.viewer;

import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.utility.graphics.Sprite;

import java.awt.geom.AffineTransform;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
public interface LayerViewer {

    ViewerLayer loadLayer(ViewerLayer layer, Sprite sprite);

    void centerViewpoint();

    LayerPainter getSelectedLayer();

    AffineTransform getTransformWorldToScreen();

    LayerManager getLayerManager();

}
