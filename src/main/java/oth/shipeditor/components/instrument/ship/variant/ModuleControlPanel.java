package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.instrument.ship.centers.ModuleAnchorPanel;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 08.10.2023
 */
class ModuleControlPanel extends JPanel {

    private final InstalledFeature module;

    private final VariantModulesPanel parent;

    private final JPanel visibilitiesPanel;

    private final JPanel anchorWidgetWrapper;

    ModuleControlPanel(InstalledFeature feature, VariantModulesPanel modulesPanel) {
        this.module = feature;
        this.parent = modulesPanel;

        this.setLayout(new BorderLayout());
        visibilitiesPanel = new JPanel();
        visibilitiesPanel.setLayout(new GridBagLayout());
        this.add(visibilitiesPanel, BorderLayout.CENTER);

        anchorWidgetWrapper = new JPanel();
        anchorWidgetWrapper.setLayout(new BorderLayout());
        anchorWidgetWrapper.setAlignmentX(0.5f);
        anchorWidgetWrapper.setAlignmentY(0);
        String tooltip = Utility.getWithLinebreaks("Mockup editing, changes are not saved to file",
                "Create separate ship layer from module to edit module anchor offset");
        anchorWidgetWrapper.setToolTipText(tooltip);

        this.add(anchorWidgetWrapper, BorderLayout.PAGE_END);

        this.addContent();
    }

    private void addContent() {
        this.addCollisionVisibilityPanel();
        this.addBoundsVisibilityPanel();
        this.addSlotsVisibilityPanel();

        ShipPainter modulePainter = (ShipPainter) module.getFeaturePainter();
        var centersPainter = modulePainter.getCenterPointPainter();

        // TODO: This does not work, make everything persistent and mutable.

        ModuleAnchorPanel anchorPanel = new ModuleAnchorPanel();
        anchorWidgetWrapper.add(anchorPanel);
        anchorPanel.setCenterPainter(centersPainter);
        anchorPanel.refresh(modulePainter);
    }

    private void addCollisionVisibilityPanel() {
        Consumer<JComboBox<PainterVisibility>> chooserAction = comboBox -> {
            PainterVisibility changedValue = (PainterVisibility) comboBox.getSelectedItem();
            actOnSelectedModules(shipPainter -> {
                var pointPainter = shipPainter.getCenterPointPainter();
                pointPainter.setVisibilityMode(changedValue);
            });
            EventBus.publish(new ViewerRepaintQueued());
        };
        Function<ShipPainter, PainterVisibility> currentSupplier = modulePainter -> {
            var centerPointPainter = modulePainter.getCenterPointPainter();
            return centerPointPainter.getVisibilityMode();
        };
        this.addVisibilityPanel(StringValues.COLLISION_VIEW, 0, chooserAction, currentSupplier);
    }

    private void addBoundsVisibilityPanel() {
        Consumer<JComboBox<PainterVisibility>> chooserAction = comboBox -> {
            PainterVisibility changedValue = (PainterVisibility) comboBox.getSelectedItem();
            actOnSelectedModules(shipPainter -> {
                var pointPainter = shipPainter.getBoundsPainter();
                pointPainter.setVisibilityMode(changedValue);
            });
            EventBus.publish(new ViewerRepaintQueued());
        };
        Function<ShipPainter, PainterVisibility> currentSupplier = modulePainter -> {
            var boundsPainter = modulePainter.getBoundsPainter();
            return boundsPainter.getVisibilityMode();
        };
        this.addVisibilityPanel("Bounds view:", 1, chooserAction, currentSupplier);
    }

    private void addSlotsVisibilityPanel() {
        Consumer<JComboBox<PainterVisibility>> chooserAction = comboBox -> {
            PainterVisibility changedValue = (PainterVisibility) comboBox.getSelectedItem();
            actOnSelectedModules(shipPainter -> {
                var pointPainter = shipPainter.getWeaponSlotPainter();
                pointPainter.setVisibilityMode(changedValue);
            });
            EventBus.publish(new ViewerRepaintQueued());
        };
        Function<ShipPainter, PainterVisibility> currentSupplier = modulePainter -> {
            var slotPainter = modulePainter.getWeaponSlotPainter();
            return slotPainter.getVisibilityMode();
        };
        this.addVisibilityPanel("Slots view:", 2, chooserAction, currentSupplier);
    }

    private void addVisibilityPanel(String labelText, int position,
                                    Consumer<JComboBox<PainterVisibility>> chooserAction,
                                    Function<ShipPainter, PainterVisibility> currentSupplier) {
        JLabel label = new JLabel(labelText);
        label.setToolTipText(StringValues.CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);

        JComponent right = ComponentUtilities.getNoSelected();

        if (module != null) {
            JComboBox<PainterVisibility> visibilityBox = new JComboBox<>(PainterVisibility.values());
            ShipPainter modulePainter = (ShipPainter) module.getFeaturePainter();
            var current = currentSupplier.apply(modulePainter);
            visibilityBox.setSelectedItem(current);

            right = visibilityBox;

            visibilityBox.setRenderer(PainterVisibility.createCellRenderer());
            visibilityBox.addActionListener(e -> chooserAction.accept(visibilityBox));
            visibilityBox.setMaximumSize(visibilityBox.getPreferredSize());
        }
        ComponentUtilities.addLabelAndComponent(visibilitiesPanel, label, right, position);
    }

    private void actOnSelectedModules(Consumer<ShipPainter> action) {
        VariantModulesPanel.ModuleList moduleList = parent.getModulesList();
        if (moduleList == null) return;
        List<InstalledFeature> selectedValuesList = moduleList.getSelectedValuesList();
        if (selectedValuesList != null && !selectedValuesList.isEmpty()) {
            for (InstalledFeature feature : selectedValuesList) {
                action.accept((ShipPainter) feature.getFeaturePainter());
            }
            EventBus.publish(new ViewerRepaintQueued());
        }
    }

}
