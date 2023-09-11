package oth.shipeditor.components.instrument.ship.builtins;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
public class BuiltInWingsPanel extends AbstractBuiltInsPanel<WingCSVEntry> {

    private JPanel bayPanel;

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

        if (gameData.isShipDataLoaded()) {
            this.announceBayCapacity(layer);
        }

        this.revalidate();
        this.repaint();
    }

    @Override
    void installPlaceholderLabel(String text) {
        super.installPlaceholderLabel(text);
        if (bayPanel != null) {
            this.remove(bayPanel);
        }

    }

    private void announceBayCapacity(ShipLayer layer) {
        if (bayPanel != null) {
            this.remove(bayPanel);
        }

        ShipHull layerHull = layer.getHull();
        ShipPainter painter = layer.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        Collection<WingCSVEntry> totalEntries = new ArrayList<>(layerHull.getBuiltInWings());

        if (activeSkin != null && !activeSkin.isBase()) {
            var skinEntries = activeSkin.getBuiltInWings();
            totalEntries.addAll(skinEntries);
        }
        var shipData = layer.getShipData();
        var hullSpecFile = shipData.getHullSpecFile();

        var gameData = SettingsManager.getGameData();
        var allShipEntries = gameData.getAllShipEntries();
        var selectedCSVEntry = allShipEntries.get(hullSpecFile.getHullId());
        var entryRowData = selectedCSVEntry.getRowData();
        String fighterBays = entryRowData.get("fighter bays");
        int bayCount = 0;
        if (fighterBays != null && !fighterBays.isEmpty()) {
            bayCount = Integer.parseInt(fighterBays);
        }

        int wingsSize = totalEntries.size();
        String text;
        FontIcon icon;
        String capacity = wingsSize + " wings / " + bayCount + " bays.";
        if (bayCount < wingsSize) {
            text = "Capacity exceeded: " + capacity;
            icon = FontIcon.of(FluentUiRegularMZ.WARNING_24, 28, Color.RED);
        } else {
            text = "Capacity: " + capacity;
            icon = FontIcon.of(FluentUiRegularAL.INFO_28, 28);
        }
        this.bayPanel = AbstractBuiltInsPanel.createHintPanel(text, icon);
        this.add(bayPanel, BorderLayout.PAGE_END);
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
                EditDispatch.postWingRemoved(entryList, cachedLayer, entry);
                this.refreshPanel(cachedLayer);
            });
            if (panelMutator != null) {
                panelMutator.accept(modPanel);
            }
            container.add(modPanel);
        }
    }

    private static JPanel addWingPanel(WingCSVEntry wing, ActionListener removeAction) {
        var wingMemberSprite = wing.getWingMemberSprite();
        BufferedImage sprite = wingMemberSprite.image();

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
