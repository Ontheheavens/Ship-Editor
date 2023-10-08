package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 08.10.2023
 */
class ModuleControlPanel extends JPanel {

    private final InstalledFeature module;

    @SuppressWarnings("FieldCanBeLocal")
    private final VariantModulesPanel parent;

    private final JPanel visibilitiesPanel;

    ModuleControlPanel(InstalledFeature feature, VariantModulesPanel modulesPanel) {
        this.module = feature;
        this.parent = modulesPanel;

        this.setLayout(new BorderLayout());

        visibilitiesPanel = new JPanel();
        visibilitiesPanel.setLayout(new GridBagLayout());
        this.add(visibilitiesPanel, BorderLayout.CENTER);
        this.addContent();
    }

    private void addContent() {
        this.addCollisionVisibilityPanel();
    }

    private void addCollisionVisibilityPanel() {
        JLabel label = new JLabel("Collision view:");
        label.setToolTipText(StringValues.CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT);

        JComponent right = ComponentUtilities.getNoSelected();

        if (module != null) {
            JComboBox<PainterVisibility> centersVisibility = new JComboBox<>(PainterVisibility.values());
            ShipPainter modulePainter = (ShipPainter) module.getFeaturePainter();
            var centerPointPainter = modulePainter.getCenterPointPainter();
            var current = centerPointPainter.getVisibilityMode();
            centersVisibility.setSelectedItem(current);

            right = centersVisibility;

            ActionListener chooserAction = e -> {
                PainterVisibility changedValue = (PainterVisibility) centersVisibility.getSelectedItem();
                centerPointPainter.setVisibilityMode(changedValue);
                EventBus.publish(new ViewerRepaintQueued());
            };

            centersVisibility.setRenderer(PainterVisibility.createCellRenderer());
            centersVisibility.addActionListener(chooserAction);
            centersVisibility.setMaximumSize(centersVisibility.getPreferredSize());
        }
        ComponentUtilities.addLabelAndComponent(visibilitiesPanel, label, right, 0);
    }

}
