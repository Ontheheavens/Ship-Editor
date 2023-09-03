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
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Ontheheavens
 * @since 29.08.2023
 */
public class VariantPanel extends JPanel {

    private final JPanel chooserContainer;

    public VariantPanel() {
        this.setLayout(new BorderLayout());
        chooserContainer = new JPanel();
        chooserContainer.setLayout(new BoxLayout(chooserContainer, BoxLayout.PAGE_AXIS));
        this.add(chooserContainer, BorderLayout.CENTER);
        this.initLayerListeners();
        this.recreateVariantChooser();
    }

    private void recreateVariantChooser() {
        chooserContainer.removeAll();

        ViewerLayer selected = StaticController.getActiveLayer();
        if (!(selected instanceof ShipLayer checkedLayer)) {
            chooserContainer.add(VariantPanel.createDisabledChooser());
            return;
        }

        ShipData shipData = checkedLayer.getShipData();
        if (shipData == null) {
            chooserContainer.add(VariantPanel.createDisabledChooser());
            return;
        }

        Map<String, Variant> variantFiles = new HashMap<>();
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

        ShipPainter painter = checkedLayer.getPainter();

        Vector<Variant> model = new Vector<>(variantFiles.values());

        JComboBox<Variant> variantChooser = new JComboBox<>(model);
        ShipVariant activeVariant = painter.getActiveVariant();
        if (activeVariant != null) {
            variantChooser.setSelectedItem(activeVariant);
        } else {
            variantChooser.setSelectedItem(empty);
        }
        variantChooser.addActionListener(e -> {
            Variant chosen = (Variant) variantChooser.getSelectedItem();
            if (chosen != null) {
                painter.selectVariant(chosen);
            }
        });
        variantChooser.setAlignmentX(Component.CENTER_ALIGNMENT);

        chooserContainer.add(variantChooser);
        chooserContainer.add(Box.createVerticalGlue());
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
            if (event instanceof LayerWasSelected) {
                this.recreateVariantChooser();
            } else if (event instanceof ActiveLayerUpdated) {
                this.recreateVariantChooser();
            }
        });
    }

}
