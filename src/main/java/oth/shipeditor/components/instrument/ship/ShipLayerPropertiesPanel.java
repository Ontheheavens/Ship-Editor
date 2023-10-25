package oth.shipeditor.components.instrument.ship;

import com.formdev.flatlaf.ui.FlatLineBorder;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.instrument.ViewerLayerWidgetsPanel;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.graphics.ColorUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    private JLabel coversColorValue;

    ShipLayerPropertiesPanel() {
        this.setLayout(new BorderLayout());
        JPanel layerWidgetsPanel = new ViewerLayerWidgetsPanel();
        this.add(layerWidgetsPanel, BorderLayout.PAGE_START);

        JPanel dataContainer = new JPanel();
        dataContainer.setLayout(new BoxLayout(dataContainer, BoxLayout.PAGE_AXIS));

        dataContainer.add(Box.createVerticalStrut(8));
        JPanel hullDataPanel = createHullDataPanel();
        dataContainer.add(hullDataPanel);

        weaponSlotsSummaryPanel = new JPanel();
        weaponSlotsSummaryPanel.setLayout(new BoxLayout(weaponSlotsSummaryPanel, BoxLayout.PAGE_AXIS));
        dataContainer.add(weaponSlotsSummaryPanel);

        this.add(dataContainer, BorderLayout.CENTER);

        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                weaponSlotsSummaryPanel.removeAll();

                coversColorValue.setIcon(null);
                coversColorValue.setOpaque(false);
                coversColorValue.setBorder(new EmptyBorder(0, 2, 0, 4));
                coversColorValue.setBackground(null);
                coversColorValue.setToolTipText(null);
                coversColorValue.setText(StringValues.NOT_INITIALIZED);

                ViewerLayer selected = checked.selected();
                boolean layerPainterPresent = selected != null && selected.getPainter() != null;
                if (!layerPainterPresent) return;
                LayerPainter layerPainter = selected.getPainter();
                if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) return;
                this.refreshData(shipPainter);
            }
        });
    }

    private JPanel createHullDataPanel() {
        JPanel container = new JPanel();
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        ComponentUtilities.outfitPanelWithTitle(container,
                new Insets(1, 0, 0, 0), "Hull data");

        JPanel coversColorContainer = new JPanel();
        coversColorContainer.setLayout(new BoxLayout(coversColorContainer, BoxLayout.LINE_AXIS));
        coversColorContainer.setBorder(new EmptyBorder(4, 0, 0, 0));

        coversColorValue = new JLabel();
        JLabel coversColorLabel = new JLabel("Covers color:");

        coversColorLabel.setToolTipText("Right-click to change color");
        JPopupMenu colorChooserMenu = ShipLayerPropertiesPanel.getColorChooserMenu();
        coversColorLabel.addMouseListener(new MouseoverLabelListener(colorChooserMenu, coversColorLabel));

        Insets insets = ComponentUtilities.createLabelInsets();
        insets.top = 1;
        coversColorLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        ComponentUtilities.layoutAsOpposites(coversColorContainer, coversColorLabel,
                coversColorValue, 0);

        container.add(coversColorContainer);

        return container;
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private static JPopupMenu getColorChooserMenu() {
        JPopupMenu colorChooserMenu = new JPopupMenu();
        JMenuItem adjustColor = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustColor.addActionListener(event -> {
            var activeLayer = StaticController.getActiveLayer();
            if (activeLayer instanceof ShipLayer shipLayer) {
                ShipHull shipHull = shipLayer.getHull();
                if (shipHull != null) {
                    Color chosen;
                    var current = shipHull.getCoversColor();
                    if (current != null) {
                        chosen = ColorUtilities.showColorChooser(current);
                    } else {
                        chosen = ColorUtilities.showColorChooser();
                    }
                    shipHull.setCoversColor(chosen);
                    StaticController.reselectCurrentLayer();
                } else {
                    ShipLayerPropertiesPanel.abortColorInteraction();
                }
            } else {
                ShipLayerPropertiesPanel.abortColorInteraction();
            }
        });
        colorChooserMenu.add(adjustColor);

        JMenuItem removeColor = new JMenuItem("Clear value");
        removeColor.addActionListener(event -> {
            var activeLayer = StaticController.getActiveLayer();
            if (activeLayer instanceof ShipLayer shipLayer) {
                ShipHull shipHull = shipLayer.getHull();
                if (shipHull != null) {
                    shipHull.setCoversColor(null);
                    StaticController.reselectCurrentLayer();
                } else {
                    ShipLayerPropertiesPanel.abortColorInteraction();
                }
            } else {
                ShipLayerPropertiesPanel.abortColorInteraction();
            }
        });
        colorChooserMenu.add(removeColor);

        return colorChooserMenu;
    }

    private static void abortColorInteraction() {
        JOptionPane.showMessageDialog(null,
                "Current layer invalid, color interaction aborted.",
                "Color interaction",
                JOptionPane.ERROR_MESSAGE);
    }

    private void refreshData(ShipPainter shipPainter) {
        weaponSlotsSummaryPanel.add(Box.createVerticalStrut(8));
        weaponSlotsSummaryPanel.add(ShipLayerPropertiesPanel.createSlotsSummaryPanel(shipPainter));

        var layer = shipPainter.getParentLayer();
        ShipHull shipHull = layer.getHull();
        var coversColor = shipHull.getCoversColor();
        if (coversColor != null) {
            ImageIcon colorIcon = ComponentUtilities.createIconFromColor(coversColor, 10, 10);
            coversColorValue.setIcon(colorIcon);
            coversColorValue.setOpaque(true);
            coversColorValue.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
            coversColorValue.setBackground(Color.LIGHT_GRAY);
            coversColorValue.setToolTipText(ColorUtilities.getColorBreakdown(coversColor));
            coversColorValue.setText(null);
        } else {
            coversColorValue.setText("Not defined");
        }
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
