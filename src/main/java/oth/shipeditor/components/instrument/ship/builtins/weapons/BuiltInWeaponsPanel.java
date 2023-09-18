package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BuiltInsPanelsRepaintQueued;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.ship.builtins.AbstractBuiltInsPanel;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;
import oth.shipeditor.components.viewer.painters.features.InstalledFeaturePainter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Entries are pairs of slot ID and weapon ID.
 * @author Ontheheavens
 * @since 11.09.2023
 */
public class BuiltInWeaponsPanel extends AbstractBuiltInsPanel {

    private JPanel weaponPickPanel;

    // TODO: Split into two tabs, base hull and skin, each modifiable when skin is disabled/enabled respectively.
    //  Should use JTable, removed by skin will use simple JList.

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof BuiltInsPanelsRepaintQueued) {
                this.refreshPanel(getCachedLayer());
            }
        });
    }

    @Override
    protected void refreshPanel(ShipLayer layer) {
        JPanel contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        var gameData = SettingsManager.getGameData();
        if (!gameData.isWeaponsDataLoaded()) {
            this.installPlaceholderLabel("Weapon data not loaded");
            return;
        }

        this.populateWeaponBuiltIns(layer);

        ShipPainter painter = layer.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            this.handleSkinBuiltInsChanges(layer);
        }

        contentPane.add(Box.createVerticalGlue());

        if (weaponPickPanel != null) {
            this.remove(weaponPickPanel);
        }

        WeaponCSVEntry pickedForInstall = InstalledFeaturePainter.getWeaponForInstall();
        if (pickedForInstall != null) {
            weaponPickPanel = pickedForInstall.createPickedWeaponPanel();
            this.add(weaponPickPanel, BorderLayout.PAGE_END);
        }

        this.revalidate();
        this.repaint();
    }

    @Override
    protected String getHintText() {
        return "Use game data widget to change picked weapon.";
    }

    private void populateWeaponBuiltIns(ShipLayer selected) {
        var builtInWeapons = selected.getBuiltInsFromBaseHull();
        JPanel contentPane = getContentPane();
        if (builtInWeapons == null || builtInWeapons.isEmpty()) {
            JPanel placeholderLabel = AbstractBuiltInsPanel.createPlaceholderLabel("Hull has no built-in weapons");
            contentPane.add(placeholderLabel);
        } else {
            Collection<InstalledFeature> values = builtInWeapons.values();

            DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();
            listModel.addAll(values);

            Consumer<InstalledFeature> removeAction = entry -> {
                ShipPainter painter = selected.getPainter();
                var weapons = painter.getBuiltInWeapons();
                EditDispatch.postFeatureUninstalled(weapons, entry.getSlotID(), entry);
            };

            var shipPainter = selected.getPainter();
            var slotPainter = shipPainter.getWeaponSlotPainter();

            var listContainer = new InstalledFeatureList(listModel, slotPainter, removeAction);
            JScrollPane scrollableContainer = new JScrollPane(listContainer);

            contentPane.add(scrollableContainer);
        }
    }

    private void handleSkinBuiltInsChanges(ShipLayer layer) {
        var added = layer.getBuiltInsFromSkin();
        if (added != null && !added.isEmpty()) {
            DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();
            listModel.addAll(added.values());

            ShipPainter painter = layer.getPainter();
            Consumer<InstalledFeature> removeAction = entry -> {
                ShipSkin activeSkin = painter.getActiveSkin();

                String slotID = entry.getSlotID();
                var addedBySkin = activeSkin.getBuiltInWeapons();
                WeaponCSVEntry toRemove = addedBySkin.get(slotID);
                EditDispatch.postFeatureUninstalled(addedBySkin, slotID, toRemove);
                activeSkin.invalidateBuiltIns();
            };

            JPanel contentPane = this.getContentPane();
            contentPane.add(Box.createVerticalStrut(2));
            JPanel title = ComponentUtilities.createTitledSeparatorPanel(StringValues.ADDED_BY_SKIN);
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
            title.setAlignmentY(0);
            contentPane.add(title);

            var slotPainter = painter.getWeaponSlotPainter();

            var listContainer = new InstalledFeatureList(listModel, slotPainter, removeAction);
            JScrollPane scrollableContainer = new JScrollPane(listContainer);

            contentPane.add(scrollableContainer);
        }
    }

}
