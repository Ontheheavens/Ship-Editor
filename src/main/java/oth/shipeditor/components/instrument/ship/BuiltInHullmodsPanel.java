package oth.shipeditor.components.instrument.ship;

import com.formdev.flatlaf.ui.FlatRoundBorder;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 24.08.2023
 */
public class BuiltInHullmodsPanel extends JPanel {

    private ShipLayer cachedLayer;

    public BuiltInHullmodsPanel() {
        this.initLayerListeners();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                this.removeAll();
                ViewerLayer selected = checked.selected();
                if (!(selected instanceof ShipLayer checkedLayer)) return;
                ShipPainter painter = checkedLayer.getPainter();
                if (painter == null || painter.isUninitialized()) {
                    this.installPlaceholderLabel("Layer data not initialized");
                    return;
                }
                this.cachedLayer = checkedLayer;
                this.refreshPanel(checkedLayer);
            }
        });
    }

    private void installPlaceholderLabel(String text) {
        this.removeAll();
        this.add(BuiltInHullmodsPanel.createPlaceholderLabel(text), BorderLayout.CENTER);
    }

    private static JPanel createPlaceholderLabel(String text) {
        var emptyContainer = new JPanel();
        emptyContainer.setBorder(new EmptyBorder(6, 2, 6, 2));
        emptyContainer.setLayout(new BoxLayout(emptyContainer, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        emptyContainer.add(Box.createHorizontalGlue());
        emptyContainer.add(label);
        emptyContainer.add(Box.createHorizontalGlue());
        return emptyContainer;
    }

    private void refreshPanel(ShipLayer selected) {
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        var gameData = SettingsManager.getGameData();
        if (!gameData.isHullmodDataLoaded()) {
            this.installPlaceholderLabel("Hullmod data not loaded");
            return;
        }

        JPanel hintPanel = new JPanel();
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.LINE_AXIS));

        JLabel hintIcon = new JLabel(FontIcon.of(FluentUiRegularAL.INFO_28, 28));
        hintIcon.setBorder(new EmptyBorder(4, 4, 0, 0));
        hintIcon.setAlignmentY(0.5f);
        hintPanel.add(hintIcon);

        JPanel hintInfo = ComponentUtilities.createTextPanel("Use right-click context menu of " +
                "game data widget to add hullmod entries.", 2);
        hintInfo.setBorder(new EmptyBorder(4, 0, 0, 4));
        hintInfo.setAlignmentY(0.5f);
        hintPanel.add(hintInfo);

        hintPanel.add(Box.createHorizontalGlue());

        hintPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        this.add(hintPanel);

        ComponentUtilities.addSeparatorToBoxPanel(this);
        this.add(Box.createVerticalStrut(4));

        this.populateHullBuiltIns(selected);

        ShipPainter painter = selected.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            this.handleSkinModChanges(activeSkin);
        }

        this.add(Box.createVerticalGlue());

        this.revalidate();
        this.repaint();
    }

    private void populateHullBuiltIns(ShipLayer selected) {
        ShipHull shipHull = selected.getHull();
        var builtInMods = shipHull.getBuiltInMods();
        if (builtInMods == null || builtInMods.isEmpty()) {
            JPanel placeholderLabel = BuiltInHullmodsPanel.createPlaceholderLabel("Hull has no built-ins");
            this.add(placeholderLabel);
        } else {
            this.populateWithEntries(this, builtInMods, null);
        }
    }

    private void handleSkinModChanges(ShipSkin skin) {
        var removed = skin.getRemoveBuiltInMods();
        if (removed != null && !removed.isEmpty()) {
            this.add(Box.createVerticalStrut(2));
            JPanel title = ComponentUtilities.createTitledSeparatorPanel("Removed by skin");
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
            title.setAlignmentY(0);
            this.add(title);

            this.populateWithEntries(this, removed, panel ->
                    panel.setBackground(new Color(255, 200, 200, 255)));
        }

        var added = skin.getBuiltInMods();
        if (added != null && !added.isEmpty()) {
            this.add(Box.createVerticalStrut(2));
            JPanel title = ComponentUtilities.createTitledSeparatorPanel("Added by skin");
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
            title.setAlignmentY(0);
            this.add(title);

            this.populateWithEntries(this, added, panel ->
                    panel.setBackground(new Color(200, 255, 200, 255)));
        }
    }

    private void populateWithEntries(JPanel container, Collection<HullmodCSVEntry> entryList,
                                     Consumer<JPanel> panelMutator) {
        for (HullmodCSVEntry entry : entryList) {
            JPanel modPanel = BuiltInHullmodsPanel.addModPanel(entry, e -> {
                entryList.remove(entry);
                this.refreshPanel(cachedLayer);
            });
            if (panelMutator != null) {
                panelMutator.accept(modPanel);
            }
            container.add(modPanel);
        }
    }

    private static JPanel addModPanel(HullmodCSVEntry mod, ActionListener removeAction) {
        JPanel container = new JPanel(new GridBagLayout());
        Border flatRoundBorder = new FlatRoundBorder();
        container.setBorder(flatRoundBorder);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 2, 4, 0);
        constraints.anchor = GridBagConstraints.LINE_START;

        JLabel hullmodIcon = ComponentUtilities.createHullmodIcon(mod);
        container.add(hullmodIcon, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(0, 4, 0, 0);

        JLabel label = new JLabel(mod.toString(), SwingConstants.LEFT);
        container.add(label, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        constraints.insets = new Insets(0, 0, 0, 4);
        constraints.anchor = GridBagConstraints.LINE_END;

        JButton removeButton = new JButton();

        removeButton.setIcon(FontIcon.of(FluentUiRegularAL.DISMISS_16, 16, Color.GRAY));
        removeButton.setRolloverIcon(FontIcon.of(FluentUiRegularAL.DISMISS_16, 16, Color.DARK_GRAY));
        removeButton.setPressedIcon(FontIcon.of(FluentUiRegularAL.DISMISS_16, 16, Color.BLACK));

        removeButton.addActionListener(removeAction);
        removeButton.setToolTipText("Remove from list");
        removeButton.putClientProperty("JButton.buttonType", "borderless");

        container.add(removeButton, constraints);

        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        container.setAlignmentY(0);
        return container;
    }

}
