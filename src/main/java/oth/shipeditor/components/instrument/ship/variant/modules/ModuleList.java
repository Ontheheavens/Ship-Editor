package oth.shipeditor.components.instrument.ship.variant.modules;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectShipDataEntry;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 29.11.2023
 */
public final class ModuleList extends InstalledFeatureList {

    private final Runnable refresher;

    ModuleList(Runnable refreshAction, ListModel<InstalledFeature> dataModel,
               Consumer<InstalledFeature> removeAction,
               Consumer<Map<String, InstalledFeature>> sortAction) {
        super(dataModel, removeAction, sortAction, null);
        this.refresher = refreshAction;
    }

    @Override
    protected void handleEntrySelection(InstalledFeature feature) {
        refresher.run();
    }

    @Override
    protected boolean isSupported(Transferable transferable) {
        DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
        boolean isFeature = dataFlavors[0].equals(FEATURE_FLAVOR);

        String humanPresentableName = dataFlavors[1].getHumanPresentableName();
        boolean isSameList = humanPresentableName.equals(String.valueOf(this.hashCode()));
        return isFeature && isSameList;
    }

    @Override
    protected JMenuItem getSelectEntryOption(InstalledFeature selected) {
        JMenuItem selectEntry = new JMenuItem(StringValues.SELECT_SHIP_ENTRY);
        selectEntry.addActionListener(event -> actOnSelectedEntry(feature -> {
            CSVEntry dataEntry = feature.getDataEntry();
            if (dataEntry instanceof ShipCSVEntry shipEntry) {
                EventBus.publish(new SelectShipDataEntry(shipEntry));
            }
        }));
        if (!(selected.getDataEntry() instanceof ShipCSVEntry)) {
            selectEntry.setEnabled(false);
        }
        return selectEntry;
    }

}
