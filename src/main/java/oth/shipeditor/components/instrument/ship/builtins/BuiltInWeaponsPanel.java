package oth.shipeditor.components.instrument.ship.builtins;

import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.components.ComponentUtilities;

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
public class BuiltInWeaponsPanel extends AbstractBuiltInsPanel<Pair<String, String>> {

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
            this.handleSkinBuiltInsChanges(activeSkin);
        }

        contentPane.add(Box.createVerticalGlue());

        this.revalidate();
        this.repaint();
    }

    private void populateWeaponBuiltIns(ShipLayer selected) {
        var painter = selected.getPainter();
        var builtInWeapons = painter.getBuiltInWeapons();
        JPanel contentPane = getContentPane();
        if (builtInWeapons == null || builtInWeapons.isEmpty()) {
            JPanel placeholderLabel = AbstractBuiltInsPanel.createPlaceholderLabel("Hull has no built-in weapons");
            contentPane.add(placeholderLabel);
        } else {
            Collection<InstalledFeature> values = builtInWeapons.values();
            List<Pair<String, String>> builtInsList = new ArrayList<>();
            values.forEach(feature -> builtInsList.add(new Pair<>(feature.getSlotID(), feature.getFeatureID())));
            this.populateWithEntries(contentPane, builtInsList, null);
        }
    }

    private void handleSkinBuiltInsChanges(ShipSkin skin) {
        var removed = skin.getRemoveBuiltInWeapons();
        List<Pair<String, String>> removedInputPairs = new ArrayList<>();
        removed.forEach(slotID -> removedInputPairs.add(new Pair<>(slotID, "")));

        super.handleSkinChanges(removedInputPairs, new Color(255, 200, 200, 255), REMOVED_BY_SKIN);

        var added = skin.getInitializedBuiltIns();
        List<Pair<String, String>> addedInputPairs = new ArrayList<>();
        added.forEach((slotID, feature) -> addedInputPairs.add(new Pair<>(feature.getSlotID(), feature.getFeatureID())));

        super.handleSkinChanges(addedInputPairs, new Color(200, 255, 200, 255));
    }

    @Override
    protected void populateWithEntries(JPanel container, List<Pair<String, String>> entryList,
                                       Consumer<JPanel> panelMutator) {
        ShipLayer cachedLayer = this.getCachedLayer();
        for (Pair<String, String> entry : entryList) {
            JPanel modPanel = BuiltInWeaponsPanel.addBuiltInWeaponPanel(entry,
                    e -> this.refreshPanel(cachedLayer));
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
