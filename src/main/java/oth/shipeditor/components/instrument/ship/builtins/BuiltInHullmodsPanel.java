package oth.shipeditor.components.instrument.ship.builtins;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 24.08.2023
 */
public class BuiltInHullmodsPanel extends AbstractBuiltInsPanel<HullmodCSVEntry> {

    @Override
    protected void refreshPanel(ShipLayer layer) {
        JPanel contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        var gameData = SettingsManager.getGameData();
        if (!gameData.isHullmodDataLoaded()) {
            this.installPlaceholderLabel("Hullmod data not loaded");
            return;
        }

        this.populateHullBuiltIns(layer);

        ShipPainter painter = layer.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            this.handleSkinModChanges(activeSkin);
        }

        contentPane.add(Box.createVerticalGlue());

        contentPane.revalidate();
        contentPane.repaint();
    }

    private void populateHullBuiltIns(ShipLayer selected) {
        ShipHull shipHull = selected.getHull();
        var builtInMods = shipHull.getBuiltInMods();
        JPanel contentPane = getContentPane();
        if (builtInMods == null || builtInMods.isEmpty()) {
            JPanel placeholderLabel = AbstractBuiltInsPanel.createPlaceholderLabel("Hull has no built-ins");
            contentPane.add(placeholderLabel);
        }
        else {
            this.populateWithEntries(contentPane, builtInMods, null);
        }
    }

    private void handleSkinModChanges(ShipSkin skin) {
        var removed = skin.getRemoveBuiltInMods();
        super.handleSkinChanges(removed, new Color(255, 200, 200, 255), REMOVED_BY_SKIN);
        var added = skin.getBuiltInMods();
        super.handleSkinChanges(added, new Color(200, 255, 200, 255));
    }

    @Override
    protected void populateWithEntries(JPanel container, List<HullmodCSVEntry> entryList,
                                     Consumer<JPanel> panelMutator) {
        ShipLayer cachedLayer = this.getCachedLayer();
        for (HullmodCSVEntry entry : entryList) {
            JPanel modPanel = this.addModPanel(entry, e -> {
                EditDispatch.postHullmodRemoved(entryList, cachedLayer, entry);
                this.refreshPanel(cachedLayer);
            });
            if (panelMutator != null) {
                panelMutator.accept(modPanel);
            }
            container.add(modPanel);
        }
    }

    private JPanel addModPanel(HullmodCSVEntry mod, ActionListener removeAction) {
        JLabel hullmodIcon = ComponentUtilities.createHullmodIcon(mod);
        JLabel label = new JLabel(mod.toString(), SwingConstants.LEFT);

        var cachedLayer = this.getCachedLayer();
        var shipData = cachedLayer.getShipData();
        int hullmodCost = mod.getOrdnanceCostForHull(shipData.getHullSpecFile());

        JLabel middleLower = new JLabel("Base cost: " + hullmodCost);
        middleLower.setForeground(Color.GRAY);
        middleLower.setToolTipText(StringValues.BUILT_IN_DOES_NOT_COST_ORDNANCE);

        return ComponentUtilities.createCSVEntryPanel(hullmodIcon, label,
                middleLower, removeAction);
    }

}
