package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.layers.ship.data.Variant;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Ontheheavens
 * @since 29.08.2023
 */
public class VariantPanel extends JPanel {

    private final JPanel chooserContainer;

    private final JPanel contentPanel;

    public VariantPanel() {
        this.setLayout(new BorderLayout());

        chooserContainer = new JPanel();
        chooserContainer.setLayout(new BoxLayout(chooserContainer, BoxLayout.PAGE_AXIS));
        this.add(chooserContainer, BorderLayout.PAGE_START);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);

        this.initLayerListeners();

        ViewerLayer layer = StaticController.getActiveLayer();
        this.refreshPanel(layer);
    }

    private void installPlaceholders() {
        chooserContainer.add(VariantPanel.createDisabledChooser());

        this.installContentPlaceholder();
    }

    private void installContentPlaceholder() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        JLabel placeholder = new JLabel("Variant not initialized");
        container.add(Box.createHorizontalGlue());
        container.add(placeholder);
        container.add(Box.createHorizontalGlue());

        contentPanel.add(container, BorderLayout.CENTER);
    }

    private void refreshPanel(ViewerLayer selected) {
        chooserContainer.removeAll();
        contentPanel.removeAll();

        if (!(selected instanceof ShipLayer checkedLayer)) {
            this.installPlaceholders();
            return;
        }

        ShipData shipData = checkedLayer.getShipData();
        if (shipData == null) {
            this.installPlaceholders();
            return;
        }

        ShipVariant variant = this.recreateVariantChooser(checkedLayer);
        this.populateVariantContent(variant);
    }

    private void populateVariantContent(ShipVariant variant) {
        if (variant == null || variant.isEmpty()) {
            this.installContentPlaceholder();
            return;
        }

        JPanel weaponsPlaceholder = new JPanel();
        weaponsPlaceholder.setLayout(new BoxLayout(weaponsPlaceholder, BoxLayout.PAGE_AXIS));

        var allWeapons = variant.getAllFittedWeapons();

        if (allWeapons.isEmpty()) {
            weaponsPlaceholder.add(new JLabel("No installed weapons"));
        } else {
            allWeapons.forEach((slotID, installedFeature) -> {
                JLabel weaponEntry = new JLabel(slotID + ": " + installedFeature.getFeatureID());
                weaponsPlaceholder.add(weaponEntry);
            });
        }

        contentPanel.add(weaponsPlaceholder, BorderLayout.CENTER);
    }

    private ShipVariant recreateVariantChooser(ShipLayer checkedLayer) {
        Map<String, Variant> variantFiles = new LinkedHashMap<>();
        Variant empty = VariantFile.empty();
        variantFiles.put(StringValues.EMPTY, empty);

        String shipID = checkedLayer.getShipID();
        variantFiles.putAll(GameDataRepository.getMatchingForHullID(shipID));
        var loaded = checkedLayer.getLoadedVariants();
        loaded.forEach((variantId, shipVariant) -> {
            String variantShipHullId = shipVariant.getShipHullId();
            if (variantShipHullId.equals(shipID)) {
                variantFiles.put(variantId, shipVariant);
            }
        });

        return this.getVariantChooser(checkedLayer, variantFiles, empty);
    }

    private ShipVariant getVariantChooser(ShipLayer checkedLayer,
                                                        Map<String, Variant> variantFiles,
                                                        Variant empty) {
        ShipVariant result = null;

        ShipPainter painter = checkedLayer.getPainter();

        Vector<Variant> model = new Vector<>(variantFiles.values());

        JComboBox<Variant> variantChooser = new JComboBox<>(model);
        ShipVariant activeVariant = painter.getActiveVariant();
        if (activeVariant != null) {
            variantChooser.setSelectedItem(activeVariant);
            result = activeVariant;
        } else {
            variantChooser.setSelectedItem(empty);
        }
        variantChooser.addActionListener(action -> {
            Variant chosen = (Variant) variantChooser.getSelectedItem();
            if (chosen != null) {
                painter.selectVariant(chosen);
            }
        });
        variantChooser.setAlignmentX(Component.CENTER_ALIGNMENT);

        chooserContainer.add(variantChooser);
        chooserContainer.add(Box.createVerticalGlue());

        return result;
    }

    private static JComboBox<ShipVariant> createDisabledChooser() {
        ShipVariant[] skinSpecFileArray = {new ShipVariant()};
        JComboBox<ShipVariant> skinChooser = new JComboBox<>(skinSpecFileArray);
        skinChooser.setSelectedItem(skinSpecFileArray[0]);
        skinChooser.setEnabled(false);
        return skinChooser;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected(_, var selected)) {
                this.refreshPanel(selected);
            } else if (event instanceof ActiveLayerUpdated(var updated)) {
                this.refreshPanel(updated);
            }
        });
    }

}
