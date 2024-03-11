package oth.shipeditor.components.datafiles.entities;

import oth.shipeditor.components.viewer.layers.ViewerLayer;

/**
 * @author Ontheheavens
 * @since 11.03.2024
 */
public interface LayerableEntry extends CSVEntry{

    ViewerLayer loadLayerFromEntry();

}
