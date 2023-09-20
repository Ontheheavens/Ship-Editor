package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.components.BuiltInsPanelsRepaintQueued;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.ship.builtins.AbstractBuiltInsPanel;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 20.09.2023
 */
public abstract class AbstractWeaponsPanel extends AbstractBuiltInsPanel {

    private JPanel weaponPickPanel;

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (AbstractWeaponsPanel.isRepaintEvent(event)) {
                this.refreshPanel(getCachedLayer());
            }
        });
    }

    private static boolean isRepaintEvent(BusEvent event) {
        return event instanceof BuiltInsPanelsRepaintQueued;
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

        WeaponCSVEntry pickedForInstall = FeaturesOverseer.getWeaponForInstall();
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

    protected abstract Map<String, InstalledFeature> getBaseHullFilteredEntries(FeaturesOverseer featuresOverseer);

    protected abstract Consumer<Map<String, InstalledFeature>> getBaseHullSortAction(FeaturesOverseer featuresOverseer);

    protected String getPlaceholderText() {
        return "Hull has no built-in weapons";
    }

    private void populateWeaponBuiltIns(ShipLayer selected) {
        FeaturesOverseer featuresOverseer = selected.getFeaturesOverseer();
        var builtInWeapons = getBaseHullFilteredEntries(featuresOverseer);
        JPanel contentPane = getContentPane();

        ShipPainter shipPainter = selected.getPainter();
        if (builtInWeapons == null || builtInWeapons.isEmpty()) {
            JPanel placeholderLabel = AbstractBuiltInsPanel.createPlaceholderLabel(getPlaceholderText());
            contentPane.add(placeholderLabel);
        } else {
            Collection<InstalledFeature> values = builtInWeapons.values();

            DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();
            listModel.addAll(values);

            Consumer<InstalledFeature> removeAction = entry -> {
                var weapons = shipPainter.getBuiltInWeapons();
                EditDispatch.postFeatureUninstalled(weapons, entry.getSlotID(), entry, null);
            };

            var slotPainter = shipPainter.getWeaponSlotPainter();

            var listContainer = new InstalledFeatureList(listModel, slotPainter,
                    removeAction, getBaseHullSortAction(featuresOverseer));
            JScrollPane scrollableContainer = new JScrollPane(listContainer);

            contentPane.add(scrollableContainer);
        }
    }

    protected abstract Map<String, InstalledFeature> getSkinFilteredEntries(FeaturesOverseer featuresOverseer);

    protected abstract Consumer<Map<String, InstalledFeature>> getSkinSortAction(FeaturesOverseer featuresOverseer);

    private void handleSkinBuiltInsChanges(ShipLayer layer) {
        FeaturesOverseer featuresOverseer = layer.getFeaturesOverseer();
        var added = getSkinFilteredEntries(featuresOverseer);

        JPanel contentPane = this.getContentPane();
        if (added != null && !added.isEmpty()) {
            DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();
            listModel.addAll(added.values());

            ShipPainter painter = layer.getPainter();
            Consumer<InstalledFeature> removeAction = entry -> {
                ShipSkin activeSkin = painter.getActiveSkin();

                String slotID = entry.getSlotID();
                var addedBySkin = activeSkin.getBuiltInWeapons();
                WeaponCSVEntry toRemove = addedBySkin.get(slotID);
                EditDispatch.postFeatureUninstalled(addedBySkin, slotID,
                        toRemove, activeSkin::invalidateBuiltIns);
            };

            contentPane.add(Box.createVerticalStrut(2));
            JPanel title = ComponentUtilities.createTitledSeparatorPanel(StringValues.ADDED_BY_SKIN);
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
            title.setAlignmentY(0);
            contentPane.add(title);

            var slotPainter = painter.getWeaponSlotPainter();

            var listContainer = new InstalledFeatureList(listModel, slotPainter, removeAction,
                    getSkinSortAction(featuresOverseer));
            JScrollPane scrollableContainer = new JScrollPane(listContainer);

            contentPane.add(scrollableContainer);
        }
    }

}
