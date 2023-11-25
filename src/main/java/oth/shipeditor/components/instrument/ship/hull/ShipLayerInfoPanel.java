package oth.shipeditor.components.instrument.ship.hull;

import oth.shipeditor.components.instrument.AbstractLayerInfoPanel;
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
public final class ShipLayerInfoPanel extends AbstractLayerInfoPanel {

    private final HullDataControlPanel hullDataPanel;

    private final JPanel weaponSlotsSummaryPanel;

    public ShipLayerInfoPanel() {
        JPanel dataContainer = new JPanel();
        dataContainer.setLayout(new BorderLayout());

        hullDataPanel = new HullDataControlPanel();
        hullDataPanel.setAlignmentY(0);

        dataContainer.add(hullDataPanel, BorderLayout.PAGE_START);

        weaponSlotsSummaryPanel = new JPanel();
        weaponSlotsSummaryPanel.setLayout(new BoxLayout(weaponSlotsSummaryPanel, BoxLayout.PAGE_AXIS));
        dataContainer.add(weaponSlotsSummaryPanel, BorderLayout.CENTER);

        this.add(dataContainer, BorderLayout.CENTER);
    }

    @Override
    protected boolean isValidLayer(LayerPainter layerPainter) {
        return layerPainter instanceof ShipPainter shipPainter && !shipPainter.isUninitialized();
    }

    @Override
    protected void clearData() {
        weaponSlotsSummaryPanel.removeAll();
        hullDataPanel.clearData();
    }


    @Override
    protected void refreshData(ViewerLayer selected) {
        weaponSlotsSummaryPanel.add(Box.createVerticalStrut(8));
        ShipPainter shipPainter = (ShipPainter) selected.getPainter();
        weaponSlotsSummaryPanel.add(ShipLayerInfoPanel.createSlotsSummaryPanel(shipPainter));

        hullDataPanel.refreshData((ShipLayer) selected);
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

        Map<String, Integer> slotSummary = ShipLayerInfoPanel.generateSlotConfigSummary(slotPointList);

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
            String slotConfig = weaponSize.getDisplayedName() + " " + weaponType.getDisplayedName();
            slotConfigSummary.put(slotConfig, slotConfigSummary.getOrDefault(slotConfig, 0) + 1);
        }
        return slotConfigSummary;
    }



}
