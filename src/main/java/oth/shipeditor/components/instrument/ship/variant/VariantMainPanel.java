package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.layers.ship.data.Variant;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSize;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 29.08.2023
 */
@SuppressWarnings("NonFinalStaticVariableUsedInClassInitialization")
public class VariantMainPanel extends AbstractVariantPanel {

    private final JPanel chooserContainer;

    private final JPanel contentPanel;

    private static boolean spinnerRefreshQueued;

    static {
        Timer refreshTimer = new Timer(100, e -> {
            if (spinnerRefreshQueued) {
                EventBus.publish(new ActiveLayerUpdated(StaticController.getActiveLayer()));
            }
        });
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }

    VariantMainPanel() {
        this.setLayout(new BorderLayout());

        chooserContainer = new JPanel();
        chooserContainer.setLayout(new BoxLayout(chooserContainer, BoxLayout.PAGE_AXIS));
        this.add(chooserContainer, BorderLayout.PAGE_START);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);

        ViewerLayer layer = StaticController.getActiveLayer();
        this.refreshPanel(layer);
    }

    private void installPlaceholders() {
        chooserContainer.add(VariantMainPanel.createDisabledChooser());

        JPanel placeholder = this.createContentPlaceholder();
        contentPanel.add(placeholder, BorderLayout.CENTER);
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public void refreshPanel(ViewerLayer selected) {
        if (spinnerRefreshQueued) {
            spinnerRefreshQueued = false;
            return;
        }

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
        if (variant == null || variant.isEmpty()) {
            JPanel placeholder = this.createContentPlaceholder();
            contentPanel.add(placeholder, BorderLayout.CENTER);
        } else {
            JPanel dataPanel = VariantMainPanel.recreateDataPanel(checkedLayer, variant);
            contentPanel.add(dataPanel, BorderLayout.PAGE_START);
        }
    }

    private static JPanel recreateDataPanel(ShipLayer shipLayer, ShipVariant variant) {
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());

        ShipHull shipHull = shipLayer.getHull();
        HullSize hullSize = shipHull.getHullSize();

        VariantMainPanel.addDataSpinner(dataPanel, "Flux vents:",
                hullSize::getMaxFluxRegulators,
                variant::getFluxVents,
                variant::setFluxVents, 0);

        VariantMainPanel.addDataSpinner(dataPanel, "Flux capacitors:",
                hullSize::getMaxFluxRegulators,
                variant::getFluxCapacitors,
                variant::setFluxCapacitors, 1);

        return dataPanel;
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private static void addDataSpinner(JPanel target, String labelText,
                                       Supplier<Integer> maxValueGetter,
                                       Supplier<Integer> currentValueGetter,
                                       Consumer<Integer> setter, int row) {
        JLabel label = new JLabel(labelText);

        int minValue = 0;
        int maxValue = maxValueGetter.get();
        int currentValue = currentValueGetter.get();

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(currentValue,
                minValue, maxValue, 1);
        JSpinner spinner = new JSpinner(spinnerNumberModel);

        spinner.addChangeListener(e -> {
            Number modelNumber = spinnerNumberModel.getNumber();
            int current = modelNumber.intValue();
            setter.accept(current);
            spinnerRefreshQueued = true;
        });
        spinner.addMouseWheelListener(e -> {
            if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                return;
            }
            int value = (int) spinner.getValue();
            int newValue = value - e.getUnitsToScroll();
            newValue = Math.min(maxValue, Math.max(minValue, newValue));
            spinner.setValue(newValue);
        });

        ComponentUtilities.addLabelAndComponent(target, label, spinner, row);
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

}
