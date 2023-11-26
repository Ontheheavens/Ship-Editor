package oth.shipeditor.components.instrument.ship.builtins.hullmods;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 25.11.2023
 */
public class BuiltInHullmodsPanel extends JPanel {

    private final BaseHullmodsListPane baseBuiltInModsList;

    private final SkinHullmodsListPane addedBySkinList;

    private final SkinHullmodsListPane removedBySkinList;

    public BuiltInHullmodsPanel() {
        this.setLayout(new BorderLayout());

        this.baseBuiltInModsList = new BaseHullmodsListPane(ShipHull::getBuiltInMods, ShipHull::setBuiltInMods);
        ComponentUtilities.outfitPanelWithTitle(baseBuiltInModsList, StringValues.BASE_BUILT_INS);
        this.addedBySkinList = new SkinHullmodsListPane(ShipSkin::getBuiltInMods, ShipSkin::setBuiltInMods);
        ComponentUtilities.outfitPanelWithTitle(addedBySkinList, StringValues.ADDED_BY_SKIN);
        this.removedBySkinList = new SkinHullmodsListPane(ShipSkin::getRemoveBuiltInMods, ShipSkin::setRemoveBuiltInMods);
        ComponentUtilities.outfitPanelWithTitle(removedBySkinList, StringValues.REMOVED_BY_SKIN);

        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 0.33;
        constraints.weightx = 1;
        constraints.ipady = 40;
        constraints.gridy = 0;

        container.add(baseBuiltInModsList, constraints);
        constraints.gridy = 1;
        container.add(addedBySkinList, constraints);
        constraints.gridy = 2;
        container.add(removedBySkinList, constraints);

        JScrollPane scroller = new JScrollPane(container);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);

        this.add(scroller, BorderLayout.CENTER);

        JPanel infoPanel = ComponentUtilities.createDragInfoPanel();
        this.add(infoPanel, BorderLayout.PAGE_START);

        this.initLayerListeners();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                baseBuiltInModsList.refreshListModel(selected);
                addedBySkinList.refreshListModel(selected);
                removedBySkinList.refreshListModel(selected);
            }
        });
    }

}
