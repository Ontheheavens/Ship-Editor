package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.utility.StaticController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
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
        Collection<VariantFile> variantFiles = new ArrayList<>();
        VariantFile empty = VariantFile.empty();
        variantFiles.add(empty);
        variantFiles.addAll(GameDataRepository.getMatchingForHullID(checkedLayer.getShipID()));

        ShipPainter painter = checkedLayer.getPainter();

        Vector<VariantFile> model = new Vector<>(variantFiles);

        JComboBox<VariantFile> variantChooser = new JComboBox<>(model);
        ShipVariant activeVariant = painter.getActiveVariant();
        VariantFile activeVariantFile = null;
        if (activeVariant != null) {
            activeVariantFile = GameDataRepository.getByID(activeVariant.getVariantId());
        }
        if (activeVariantFile != null) {
            variantChooser.setSelectedItem(activeVariantFile);
        } else {
            variantChooser.setSelectedItem(empty);
        }
        variantChooser.addActionListener(e -> {
            VariantFile chosen = (VariantFile) variantChooser.getSelectedItem();
            if (chosen != null) {
                painter.installVariant(chosen);
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
