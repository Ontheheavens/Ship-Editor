package oth.shipeditor.undo.edits;

import oth.shipeditor.components.viewer.layers.LayerPainter;

/**
 * @author Ontheheavens
 * @since 05.09.2023
 */
public interface LayerEdit {

    LayerPainter getLayerPainter();

    void cleanupReferences();

}
