package oth.shipeditor.components.instrument.ship.builtins.hullmods;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.instrument.ship.shared.HullmodsList;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 25.11.2023
 */
public class BaseHullmodsListPane extends JPanel {

    private final HullmodsList modsList;

    private DefaultListModel<HullmodCSVEntry> modsModel;

    private final Function<ShipHull, List<HullmodCSVEntry>> modsGetter;

    BaseHullmodsListPane(Function<ShipHull, List<HullmodCSVEntry>> getter,
                         BiConsumer<ShipHull, List<HullmodCSVEntry>> sortSetter) {
        this.modsModel = new DefaultListModel<>();
        this.modsGetter = getter;

        Consumer<HullmodCSVEntry> removeAction = entry -> StaticController.actOnCurrentShip((shipLayer) -> {
            var entryList = modsGetter.apply(shipLayer.getHull());
            EditDispatch.postHullmodRemoved(entryList, shipLayer, entry);
        });

        Consumer<List<HullmodCSVEntry>> sortAction = updatedList ->
                StaticController.actOnCurrentShip((shipLayer) -> {
                    ShipHull shipHull = shipLayer.getHull();
                    var oldMods = modsGetter.apply(shipHull);
                    EditDispatch.postHullmodsSorted(oldMods, updatedList, shipLayer,
                            list -> sortSetter.accept(shipHull, list));
                });

        this.modsList = new HullmodsList(removeAction, modsModel, sortAction);

        modsList.setBorder(new LineBorder(Color.LIGHT_GRAY));
        this.setLayout(new BorderLayout());
        this.add(modsList, BorderLayout.CENTER);
    }

    void refreshListModel(ViewerLayer selected) {
        DefaultListModel<HullmodCSVEntry> newModel = new DefaultListModel<>();
        if (!(selected instanceof ShipLayer checkedLayer)) {
            this.modsModel = newModel;
            this.modsList.setModel(newModel);
            this.modsList.setEnabled(false);
            return;
        }
        ShipHull hull = checkedLayer.getHull();
        if (hull != null) {
            List<HullmodCSVEntry> entries = modsGetter.apply(hull);
            if (entries != null) {
                newModel.addAll(entries);
                this.modsList.setEnabled(true);
            } else {
                this.modsList.setEnabled(false);
            }
        } else {
            this.modsList.setEnabled(false);
        }
        this.modsModel = newModel;
        this.modsList.setModel(newModel);
    }

}
