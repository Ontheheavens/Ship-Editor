package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.components.instrument.ship.builtins.AbstractBuiltInsPanel;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Entries are pairs of slot ID and weapon ID.
 * @author Ontheheavens
 * @since 11.09.2023
 */
public class BuiltInWeaponsPanel extends AbstractBuiltInsPanel {

    // TODO: Split into two tabs, base hull and skin, each modifiable when skin is disabled/enabled respectively.
    //  Should use JTable, removed by skin will use simple JList.

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

        this.revalidate();
        this.repaint();
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
                weapons.remove(entry.getSlotID());
                this.refreshPanel(selected);
            };

            var listContainer = new InstalledFeatureList(listModel, removeAction);
            JScrollPane scrollableContainer = new JScrollPane(listContainer);

            contentPane.add(scrollableContainer);
        }
    }

    private void handleSkinBuiltInsChanges(ShipLayer layer) {
        var removed = layer.getBuiltInsRemovedBySkin();
        if (removed != null) {
            List<Pair<String, String>> removedInputPairs = new ArrayList<>();
            removed.forEach(slotID -> removedInputPairs.add(new Pair<>(slotID, "")));

            Consumer<Pair<String, String>> removeAction = entry -> {
                ShipPainter painter = layer.getPainter();

                ShipSkin shipSkin = painter.getActiveSkin();

                var removedBySkin = shipSkin.getRemoveBuiltInWeapons();
                removedBySkin.remove(entry.getFirst());
            };

            this.handleSkinChanges(removedInputPairs, new Color(255, 200, 200, 255),
                    REMOVED_BY_SKIN, removeAction);
        }

        var added = layer.getBuiltInsFromSkin();
        if (added != null) {
            List<Pair<String, String>> addedInputPairs = new ArrayList<>();
            added.forEach((slotID, feature) -> addedInputPairs.add(new Pair<>(feature.getSlotID(),
                    feature.getFeatureID())));

            Consumer<Pair<String, String>> removeAction = entry -> {
                ShipPainter painter = layer.getPainter();

                ShipSkin shipSkin = painter.getActiveSkin();

                var addedBySkin = shipSkin.getBuiltInWeapons();
                addedBySkin.remove(entry.getFirst());
                shipSkin.invalidateBuiltIns();
            };

            this.handleSkinChanges(addedInputPairs, new Color(200, 255, 200, 255),
                    StringValues.ADDED_BY_SKIN, removeAction);
        }
    }

    private void handleSkinChanges(Collection<Pair<String, String>> entryList, Color panelColor,
                                   String panelTitle, Consumer<Pair<String, String>> removeAction) {
        if (entryList != null && !entryList.isEmpty()) {
            JPanel contentPane = this.getContentPane();
            contentPane.add(Box.createVerticalStrut(2));
            JPanel title = ComponentUtilities.createTitledSeparatorPanel(panelTitle);
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
            title.setAlignmentY(0);
            contentPane.add(title);

            this.populateWithEntries(contentPane, entryList, panel -> {
                if (panelColor != null) {
                    panel.setBackground(panelColor);
                }
            }, removeAction);
        }
    }

    private void populateWithEntries(JPanel container, Iterable<Pair<String, String>> entryList,
                                     Consumer<JPanel> panelMutator, Consumer<Pair<String, String>> removeAction) {
        ShipLayer cachedLayer = this.getCachedLayer();
        for (Pair<String, String> entry : entryList) {
            JPanel modPanel = BuiltInWeaponsPanel.addBuiltInWeaponPanel(entry,
                    e -> {
                        removeAction.accept(entry);
                        this.refreshPanel(cachedLayer);
                    });
            if (panelMutator != null) {
                panelMutator.accept(modPanel);
            }
            container.add(modPanel);
        }
    }

    private static JPanel addBuiltInWeaponPanel(Pair<String, String> slotWithWeapon, ActionListener removeAction) {
        JLabel label = new JLabel(slotWithWeapon.getFirst(), SwingConstants.LEFT);
        JLabel middleLower = new JLabel(slotWithWeapon.getSecond());
        return ComponentUtilities.createCSVEntryPanel(new JLabel(), label,
                middleLower, removeAction);
    }

}
