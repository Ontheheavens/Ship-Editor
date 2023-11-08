package oth.shipeditor.components.instrument.ship;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.instrument.LayerCircumstancePanel;
import oth.shipeditor.components.instrument.ViewerLayerWidgetsPanel;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
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
    private final HullDataControlPanel hullDataPanel;

    private final LayerCircumstancePanel layerCircumstancePanel;

    ShipLayerPropertiesPanel() {
        this.setLayout(new BorderLayout());
        JPanel layerWidgetsPanel = new ViewerLayerWidgetsPanel();
        this.add(layerWidgetsPanel, BorderLayout.PAGE_START);

        JPanel dataContainer = new JPanel();
        dataContainer.setLayout(new BorderLayout());

        hullDataPanel = new HullDataControlPanel();
        hullDataPanel.setAlignmentY(0);

        dataContainer.add(hullDataPanel, BorderLayout.PAGE_START);

        weaponSlotsSummaryPanel = new JPanel();
        weaponSlotsSummaryPanel.setLayout(new BoxLayout(weaponSlotsSummaryPanel, BoxLayout.PAGE_AXIS));
        dataContainer.add(weaponSlotsSummaryPanel, BorderLayout.CENTER);

        this.add(dataContainer, BorderLayout.CENTER);

        layerCircumstancePanel = new LayerCircumstancePanel();
        this.add(layerCircumstancePanel, BorderLayout.PAGE_END);

        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                weaponSlotsSummaryPanel.removeAll();

                hullDataPanel.clearData();

                ViewerLayer selected = checked.selected();
                boolean layerPainterPresent = selected != null && selected.getPainter() != null;
                if (!layerPainterPresent) return;
                LayerPainter layerPainter = selected.getPainter();
                if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) return;
                this.refreshData(shipPainter.getParentLayer());

                layerCircumstancePanel.refresh(selected.getPainter());
            }
        });
    }

    private void refreshData(ShipLayer shipLayer) {
        weaponSlotsSummaryPanel.add(Box.createVerticalStrut(8));
        ShipPainter shipPainter = shipLayer.getPainter();
        weaponSlotsSummaryPanel.add(ShipLayerPropertiesPanel.createSlotsSummaryPanel(shipPainter));

        hullDataPanel.refreshData(shipLayer);
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
