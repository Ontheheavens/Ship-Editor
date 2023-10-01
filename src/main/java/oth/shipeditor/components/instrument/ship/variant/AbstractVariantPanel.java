package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.components.instrument.ship.RefreshablePanel;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 27.09.2023
 */
abstract class AbstractVariantPanel extends RefreshablePanel {

    @SuppressWarnings("MethodMayBeStatic")
    JPanel createContentPlaceholder() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        JLabel placeholder = new JLabel("Variant not initialized");
        container.add(Box.createHorizontalGlue());
        container.add(placeholder);
        container.add(Box.createHorizontalGlue());
        return container;
    }

}
