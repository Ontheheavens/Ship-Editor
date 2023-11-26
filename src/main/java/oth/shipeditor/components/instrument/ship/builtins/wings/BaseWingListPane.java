package oth.shipeditor.components.instrument.ship.builtins.wings;

import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.instrument.ship.shared.WingsList;
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
 * @since 26.11.2023
 */
public class BaseWingListPane extends JPanel {

    private final WingsList wingsList;

    private DefaultListModel<WingCSVEntry> wingsModel;

    private final Function<ShipHull, List<WingCSVEntry>> wingsGetter;

    BaseWingListPane(Function<ShipHull, List<WingCSVEntry>> getter,
                     BiConsumer<ShipHull, List<WingCSVEntry>> sortSetter) {
        this.setLayout(new BorderLayout());

        this.wingsGetter = getter;
        this.wingsModel = new DefaultListModel<>();

        BiConsumer<Integer, WingCSVEntry> removeAction = (entryIndex, wingCSVEntry) ->
                StaticController.actOnCurrentShip((shipLayer) -> {
                    var entryList = wingsGetter.apply(shipLayer.getHull());
                    EditDispatch.postWingRemoved(entryList, shipLayer, wingCSVEntry, entryIndex);
                });

        Consumer<List<WingCSVEntry>> sortAction = updatedList ->
                StaticController.actOnCurrentShip((shipLayer) -> {
                    ShipHull shipHull = shipLayer.getHull();
                    var oldWings = wingsGetter.apply(shipHull);
                    EditDispatch.postWingsSorted(oldWings, updatedList, shipLayer,
                            list -> sortSetter.accept(shipHull, list));
                });

        this.wingsList = new WingsList(removeAction, wingsModel, sortAction);

        wingsList.setBorder(new LineBorder(Color.LIGHT_GRAY));
        this.add(wingsList, BorderLayout.CENTER);
    }

    void refreshListModel(ViewerLayer selected) {
        DefaultListModel<WingCSVEntry> newModel = new DefaultListModel<>();
        if (!(selected instanceof ShipLayer checkedLayer)) {
            this.wingsModel = newModel;
            this.wingsList.setModel(newModel);
            this.wingsList.setEnabled(false);
            return;
        }
        ShipHull hull = checkedLayer.getHull();
        if (hull != null) {
            List<WingCSVEntry> entries = wingsGetter.apply(hull);
            if (entries != null) {
                newModel.addAll(entries);
                this.wingsList.setEnabled(true);
            } else {
                this.wingsList.setEnabled(false);
            }
        } else {
            this.wingsList.setEnabled(false);
        }
        this.wingsModel = newModel;
        this.wingsList.setModel(newModel);
    }

}
