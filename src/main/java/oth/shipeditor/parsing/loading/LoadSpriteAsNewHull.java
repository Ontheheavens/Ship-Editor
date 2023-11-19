package oth.shipeditor.parsing.loading;

import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 18.11.2023
 */
public class LoadSpriteAsNewHull extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        Consumer<Sprite> layerCreator = sprite -> {
            LayerManager layerManager = StaticController.getLayerManager();
            if (layerManager == null) return;

            ShipLayer shipLayer = layerManager.createShipLayer();
            layerManager.activateLastLayer();

            PrimaryViewer viewer = StaticController.getViewer();
            viewer.loadSpriteToLayer(shipLayer, sprite);

            HullSpecFile created = new HullSpecFile();
            shipLayer.initializeHullData(created);
        };
        OpenSpriteAction.openSpriteAndDo(layerCreator);
    }

}
