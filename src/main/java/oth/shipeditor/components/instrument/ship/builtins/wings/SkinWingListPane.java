package oth.shipeditor.components.instrument.ship.builtins.wings;

import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.instrument.ship.shared.WingsList;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
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
public class SkinWingListPane extends JPanel {

    private final WingsList wingsList;

    private DefaultListModel<WingCSVEntry> wingsModel;

    private final Function<ShipSkin, List<WingCSVEntry>> wingsGetter;

    SkinWingListPane(Function<ShipSkin, List<WingCSVEntry>> getter,
                     BiConsumer<ShipSkin, List<WingCSVEntry>> sortSetter) {
        this.setLayout(new BorderLayout());

        this.wingsGetter = getter;
        this.wingsModel = new DefaultListModel<>();

        BiConsumer<Integer, WingCSVEntry> removeAction = (entryIndex, wingCSVEntry) ->
                StaticController.actOnCurrentSkin(((shipLayer, shipSkin) -> {
                    var entryList = wingsGetter.apply(shipSkin);
                    EditDispatch.postWingRemoved(entryList, shipLayer, wingCSVEntry, entryIndex);
                }));

        Consumer<List<WingCSVEntry>> sortAction = updatedList ->
                StaticController.actOnCurrentSkin(((shipLayer, shipSkin) -> {
                    ShipHull shipHull = shipLayer.getHull();
                    var oldWings = wingsGetter.apply(shipSkin);
                    EditDispatch.postWingsSorted(oldWings, updatedList, shipLayer,
                            list -> sortSetter.accept(shipSkin, list));
                }));

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
        ShipSkin activeSkin = checkedLayer.getActiveSkin();
        if (activeSkin != null && !activeSkin.isBase()) {
            List<WingCSVEntry> entries = wingsGetter.apply(activeSkin);
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
