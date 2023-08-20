package oth.shipeditor.components.instrument.ship;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.instrument.ViewerLayerWidgetsPanel;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 11.06.2023
 */
final class ShipLayerPropertiesPanel extends JPanel {

    private final JPanel weaponSlotsSummaryPanel;

    ShipLayerPropertiesPanel() {
        this.setLayout(new BorderLayout());
        JPanel layerWidgetsPanel = new ViewerLayerWidgetsPanel();
        this.add(layerWidgetsPanel, BorderLayout.PAGE_START);

        weaponSlotsSummaryPanel = new JPanel();
        weaponSlotsSummaryPanel.setLayout(new BoxLayout(weaponSlotsSummaryPanel, BoxLayout.PAGE_AXIS));
        this.add(weaponSlotsSummaryPanel, BorderLayout.CENTER);

        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                weaponSlotsSummaryPanel.removeAll();
                ViewerLayer selected = checked.selected();
                boolean layerPainterPresent = selected != null && selected.getPainter() != null;
                if (!layerPainterPresent) return;
                LayerPainter layerPainter = selected.getPainter();
                if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) return;
                this.repopulateSlotsSummaryPanel(shipPainter);
            }
        });

    }

    private void repopulateSlotsSummaryPanel(ShipPainter shipPainter) {
        weaponSlotsSummaryPanel.add(Box.createVerticalStrut(12));
        weaponSlotsSummaryPanel.add(ShipLayerPropertiesPanel.createSlotsSummaryPanel(shipPainter));
    }

    private static JPanel createSlotsSummaryPanel(ShipPainter shipPainter) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);

        ComponentUtilities.outfitPanelWithTitle(container,
                new Insets(1, 0, 0, 0), "Weapon slots summary");

        WeaponSlotPainter slotPainter = shipPainter.getWeaponSlotPainter();
        List<WeaponSlotPoint> slotPointList = slotPainter.getSlotPoints();

        Map<String, Integer> slotSummary = ShipLayerPropertiesPanel.generateSlotConfigSummary(slotPointList);

        container.add(Box.createVerticalStrut(4));

        for (Map.Entry<String, Integer> entry : slotSummary.entrySet()) {
            JLabel slotKind = new JLabel(entry.getValue() + "× " + entry.getKey());
            ComponentUtilities.layoutAsOpposites(container, slotKind, new JLabel(""), 4);
            container.add(Box.createVerticalStrut(4));
        }

        return container;
    }

    private static Map<String, Integer> generateSlotConfigSummary(Iterable<WeaponSlotPoint> slots) {
        Map<String, Integer> slotConfigSummary = new HashMap<>();
        for (SlotData slot : slots) {
            WeaponSize weaponSize = slot.getWeaponSize();
            WeaponType weaponType = slot.getWeaponType();
            String slotConfig = weaponSize.getDisplayName() + " " + weaponType.getDisplayName();
            slotConfigSummary.put(slotConfig, slotConfigSummary.getOrDefault(slotConfig, 0) + 1);
        }
        return slotConfigSummary;
    }



}
