package oth.shipeditor.components.instrument.ship;

import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
public class BuiltInWingsPanel extends AbstractBuiltInsPanel<WingCSVEntry> {

    // TODO: add fighter bay checks and undo/redo classes for wing entries addition/removal.

    @Override
    protected void refreshPanel(ShipLayer layer) {
        JPanel contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        var gameData = SettingsManager.getGameData();
        if (!gameData.isWingDataLoaded()) {
            this.installPlaceholderLabel("Wing data not loaded");
            return;
        }

        this.populateWingBuiltIns(layer);

        ShipPainter painter = layer.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            this.handleSkinWingChanges(activeSkin);
        }

        contentPane.add(Box.createVerticalGlue());

        contentPane.revalidate();
        contentPane.repaint();
    }

    private void populateWingBuiltIns(ShipLayer selected) {
        ShipHull shipHull = selected.getHull();
        var builtInWings = shipHull.getBuiltInWings();
        JPanel contentPane = getContentPane();
        if (builtInWings == null || builtInWings.isEmpty()) {
            JPanel placeholderLabel = AbstractBuiltInsPanel.createPlaceholderLabel("Hull has no built-in wings");
            contentPane.add(placeholderLabel);
        } else {
            this.populateWithEntries(contentPane, builtInWings, null);
        }
    }

    private void handleSkinWingChanges(ShipSkin skin) {
        var added = skin.getBuiltInWings();
        super.handleSkinChanges(added, new Color(200, 255, 200, 255));
    }

    protected void populateWithEntries(JPanel container, List<WingCSVEntry> entryList,
                                     Consumer<JPanel> panelMutator) {
        ShipLayer cachedLayer = this.getCachedLayer();
        for (WingCSVEntry entry : entryList) {
            JPanel modPanel = BuiltInWingsPanel.addWingPanel(entry, e -> {
                entryList.remove(entry);
                this.refreshPanel(cachedLayer);
            });
            if (panelMutator != null) {
                panelMutator.accept(modPanel);
            }
            container.add(modPanel);
        }
    }

    private static JPanel addWingPanel(WingCSVEntry wing, ActionListener removeAction) {
        BufferedImage sprite = wing.getWingMemberSprite();

        String tooltip = wing.getEntryName();
        JLabel spriteIcon = ComponentUtilities.createIconFromImage(sprite, tooltip, 32);

        JLabel label = new JLabel(tooltip, SwingConstants.LEFT);

        int wingOrdnanceCost = wing.getOrdnanceCost();

        JLabel middleLower = new JLabel("Ordnance cost: " + wingOrdnanceCost);
        middleLower.setForeground(Color.GRAY);
        middleLower.setToolTipText(StringValues.BUILT_IN_DOES_NOT_COST_ORDNANCE);

        return ComponentUtilities.createCSVEntryPanel(spriteIcon, label,
                middleLower, removeAction);
    }

}
