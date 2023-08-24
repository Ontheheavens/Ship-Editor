package oth.shipeditor.components.instrument.ship;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;

import javax.swing.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 24.08.2023
 */
public class BuiltInHullmodsPanel extends JPanel {

    public BuiltInHullmodsPanel() {
        this.initLayerListeners();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                this.removeAll();
                ViewerLayer selected = checked.selected();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                if (painter != null && !painter.isUninitialized()) {
                    ShipHull shipHull = checkedLayer.getHull();
                    shipHull.reloadBuiltInMods();
                    List<HullmodCSVEntry> builtInMods = shipHull.getBuiltInMods();
                    if (builtInMods == null) return;
                    for (HullmodCSVEntry builtIn : builtInMods) {
                        this.add(new JLabel(builtIn.toString()));
                    }
                }
            }
        });
    }

}
