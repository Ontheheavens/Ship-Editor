package oth.shipeditor.components.instrument.ship.builtins.weapons;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.components.BuiltInsPanelsRepaintQueued;
import oth.shipeditor.communication.events.components.WeaponEntryPicked;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.ship.builtins.AbstractBuiltInsPanel;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeaturePainter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 20.09.2023
 */
public abstract class AbstractWeaponsPanel extends AbstractBuiltInsPanel {

    private JPanel weaponPickPanel;

    private JPanel northContainer;

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (AbstractWeaponsPanel.isRepaintEvent(event)) {
                this.refreshPanel(getCachedLayer());
                this.refreshWeaponPicker();
            }
            if (event instanceof WeaponEntryPicked) {
                this.refreshWeaponPicker();
            }
        });
    }

    private void refreshWeaponPicker() {
        if (weaponPickPanel != null) {
            northContainer.remove(weaponPickPanel);
        }

        WeaponCSVEntry pickedForInstall = FeaturesOverseer.getWeaponForInstall();
        if (pickedForInstall != null) {
            weaponPickPanel = pickedForInstall.createPickedWeaponPanel();
        } else {
            FontIcon hintIcon = FontIcon.of(FluentUiRegularAL.INFO_28, 28);
            weaponPickPanel = ComponentUtilities.createHintPanel(getHintText(), hintIcon);
        }
        northContainer.add(weaponPickPanel, BorderLayout.CENTER);

        this.revalidate();
        this.repaint();
    }

    protected abstract PainterVisibility getVisibilityOfBuiltInKind(InstalledFeaturePainter painter);

    protected abstract void setVisibilityOfBuiltInKind(InstalledFeaturePainter painter, PainterVisibility visibility);

    protected void addHintPanel() {
        northContainer = new JPanel();
        northContainer.setLayout(new BorderLayout());

        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            if (!(e.getSource() instanceof ShipPainter checked)) return;
            InstalledFeaturePainter installablesPainter = checked.getInstallablesPainter();
            PainterVisibility valueOfLayer = this.getVisibilityOfBuiltInKind(installablesPainter);
            visibilityList.setSelectedItem(valueOfLayer);
        };

        ActionListener chooserAction = e -> {
            PainterVisibility changedValue = (PainterVisibility) visibilityList.getSelectedItem();

            ShipLayer cached = this.getCachedLayer();
            if (cached == null) return;
            ShipPainter painter = cached.getPainter();
            if (painter == null || painter.isUninitialized() || !painter.isLayerActive()) return;

            InstalledFeaturePainter installablesPainter = painter.getInstallablesPainter();

            setVisibilityOfBuiltInKind(installablesPainter, changedValue);

            EventBus.publish(new ViewerRepaintQueued());
        };

        JPanel visibilityWidget = ComponentUtilities.createVisibilityWidgetRaw(visibilityList,
                chooserAction, selectionAction, "");

        northContainer.add(visibilityWidget, BorderLayout.PAGE_START);

        this.add(northContainer, BorderLayout.PAGE_START);
    }

    private static boolean isRepaintEvent(BusEvent event) {
        return event instanceof BuiltInsPanelsRepaintQueued;
    }

    @Override
    protected JPanel createContentPane() {
        return new JPanel();
    }

    @Override
    protected void refreshPanel(ShipLayer layer) {
        JPanel contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new GridBagLayout());
        var gameData = SettingsManager.getGameData();
        if (!gameData.isWeaponsDataLoaded()) {
            this.installPlaceholderLabel("Weapon data not loaded");
            return;
        }

        this.populateWeaponBuiltIns(layer);

        ShipPainter painter = layer.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            this.handleSkinBuiltInsChanges(layer);
        }

        this.revalidate();
        this.repaint();
    }

    @Override
    protected String getHintText() {
        return "Use game data widget to change picked weapon.";
    }

    protected abstract Map<String, InstalledFeature> getBaseHullFilteredEntries(FeaturesOverseer featuresOverseer);

    protected abstract Consumer<Map<String, InstalledFeature>> getBaseHullSortAction(FeaturesOverseer featuresOverseer);

    protected String getPlaceholderText() {
        return "Hull has no built-in weapons";
    }

    private void populateWeaponBuiltIns(ShipLayer selected) {
        FeaturesOverseer featuresOverseer = selected.getFeaturesOverseer();
        var builtInWeapons = getBaseHullFilteredEntries(featuresOverseer);
        JPanel contentPane = getContentPane();

        ShipPainter shipPainter = selected.getPainter();
        if (builtInWeapons == null || builtInWeapons.isEmpty()) {
            JPanel placeholderLabel = AbstractBuiltInsPanel.createPlaceholderLabel(getPlaceholderText());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weighty = 0.01;
            constraints.anchor = GridBagConstraints.PAGE_START;

            contentPane.add(placeholderLabel, constraints);
        } else {
            Collection<InstalledFeature> values = builtInWeapons.values();

            DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();
            listModel.addAll(values);

            Consumer<InstalledFeature> removeAction = entry -> {
                var weapons = shipPainter.getBuiltInWeapons();
                EditDispatch.postFeatureUninstalled(weapons, entry.getSlotID(), entry, null);
            };

            var slotPainter = shipPainter.getWeaponSlotPainter();

            var listContainer = new InstalledFeatureList(listModel, slotPainter,
                    removeAction, getBaseHullSortAction(featuresOverseer));
            listContainer.setBorder(new LineBorder(Color.LIGHT_GRAY));
            listContainer.setBelongsToBaseHullBuiltIns(true);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.PAGE_START;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 1;
            constraints.weighty = 0.01;
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.insets = new Insets(0, 0, 3, 0);

            contentPane.add(listContainer, constraints);
        }
    }

    protected abstract Map<String, InstalledFeature> getSkinFilteredEntries(FeaturesOverseer featuresOverseer);

    protected abstract Consumer<Map<String, InstalledFeature>> getSkinSortAction(FeaturesOverseer featuresOverseer);

    private void handleSkinBuiltInsChanges(ShipLayer layer) {
        FeaturesOverseer featuresOverseer = layer.getFeaturesOverseer();
        var added = getSkinFilteredEntries(featuresOverseer);

        JPanel contentPane = this.getContentPane();
        if (added != null && !added.isEmpty()) {
            JPanel container = new JPanel();
            container.setLayout(new BorderLayout());

            DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();
            listModel.addAll(added.values());

            ShipPainter painter = layer.getPainter();
            Consumer<InstalledFeature> removeAction = entry -> {
                ShipSkin activeSkin = painter.getActiveSkin();

                String slotID = entry.getSlotID();
                var addedBySkin = activeSkin.getBuiltInWeapons();
                WeaponCSVEntry toRemove = addedBySkin.get(slotID);
                EditDispatch.postFeatureUninstalled(addedBySkin, slotID,
                        toRemove, activeSkin::invalidateBuiltIns);
            };

            JPanel title = ComponentUtilities.createTitledSeparatorPanel(StringValues.ADDED_BY_SKIN);
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
            title.setAlignmentY(0);
            container.add(title, BorderLayout.PAGE_START);

            var slotPainter = painter.getWeaponSlotPainter();

            var listContainer = new InstalledFeatureList(listModel, slotPainter, removeAction,
                    getSkinSortAction(featuresOverseer));
            listContainer.setBorder(new LineBorder(Color.LIGHT_GRAY));

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.PAGE_START;
            constraints.gridx = 0;
            constraints.gridy = 1;

            constraints.gridwidth = GridBagConstraints.REMAINDER;
            constraints.weightx = 1;
            constraints.weighty = 1;

            container.add(listContainer);
            contentPane.add(container, constraints);
        }
    }

}
