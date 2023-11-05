package oth.shipeditor.components.instrument.ship.variant;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.VariantPanelRepaintQueued;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.layers.ship.data.Variant;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.HullSize;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 29.08.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Log4j2
public class VariantMainPanel extends AbstractVariantPanel {

    private final JPanel chooserContainer;

    private final JButton createVariantButton;

    private final JButton removeVariantButton;

    private JLabel shipHullIDLabel;

    private JTextField variantIDEditor;

    private Consumer<String> variantIDSetter;

    private JTextField variantDisplayNameEditor;

    private Consumer<String> variantDisplayNameSetter;

    private DataSpinnerContainer<Integer> ventsSpinner;

    private DataSpinnerContainer<Integer> capacitorsSpinner;

    private DataSpinnerContainer<Double> qualitySpinner;

    private ShipLayer selectedLayer;

    private ShipVariant cachedVariant;

    private Supplier<ShipVariant> variantToRemoveGetter;

    private JCheckBox goalVariantCheckbox;

    private Consumer<Boolean> goalVariantSetter;

    private JLabel shipOPCap;

    private JLabel usedOPTotal;

    private JLabel usedOPInHullmods;

    private JLabel usedOPInWings;

    private JLabel usedOPInWeapons;

    VariantMainPanel() {
        this.setLayout(new BorderLayout());

        JPanel variantListPanel = new JPanel();
        variantListPanel.setLayout(new GridBagLayout());
        ComponentUtilities.outfitPanelWithTitle(variantListPanel, "Variant list");

        chooserContainer = new JPanel();
        chooserContainer.setLayout(new BoxLayout(chooserContainer, BoxLayout.PAGE_AXIS));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.PAGE_START;

        variantListPanel.add(chooserContainer, constraints);

        createVariantButton = new JButton();
        createVariantButton.setText("Create");
        createVariantButton.addActionListener(e -> {
            ShipVariant created = new ShipVariant(false);
            String variantID = ShipVariant.createUniqueVariantID(selectedLayer);
            created.setShipHullId(selectedLayer.getShipID());
            created.setVariantId(variantID);
            Map<String, ShipVariant> loadedVariants = selectedLayer.getLoadedVariants();
            loadedVariants.put(created.getVariantId(), created);

            ShipPainter shipPainter = selectedLayer.getPainter();
            shipPainter.selectVariant(created);
        });

        constraints.gridwidth = 1;
        constraints.weightx = 0.5;
        constraints.gridx = 0;
        constraints.gridy = 1;
        variantListPanel.add(createVariantButton, constraints);

        removeVariantButton = new JButton();
        removeVariantButton.setText(StringValues.REMOVE);
        String tooltip = Utility.getWithLinebreaks("Remove entry from variants loaded to layer",
                "Newly created variants will be erased entirely",
                "Variants from game data files will be reloaded instead");
        removeVariantButton.setToolTipText(tooltip);
        removeVariantButton.addActionListener(e -> {
            var variantToRemove = variantToRemoveGetter.get();
            Map<String, ShipVariant> loadedVariants = selectedLayer.getLoadedVariants();
            String variantId = variantToRemove.getVariantId();
            loadedVariants.remove(variantId);

            ShipPainter shipPainter = selectedLayer.getPainter();
            var variantFile = GameDataRepository.getVariantByID(variantId);
            if (variantFile != null) {
                shipPainter.selectVariant(variantFile);
            } else {
                shipPainter.selectVariant(VariantFile.empty());
            }
        });

        constraints.gridwidth = 1;
        constraints.weightx = 0.5;
        constraints.gridx = 1;
        constraints.gridy = 1;
        variantListPanel.add(removeVariantButton, constraints);

        this.add(variantListPanel, BorderLayout.PAGE_START);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);

        JPanel dataPanel = this.createDataPanel();
        contentPanel.add(dataPanel, BorderLayout.PAGE_START);

        JPanel ordnanceContainer = this.createOrdnanceInfoPanel();
        contentPanel.add(ordnanceContainer, BorderLayout.CENTER);

        ViewerLayer layer = StaticController.getActiveLayer();
        this.refreshPanel(layer);
    }

    private void installPlaceholders() {
        chooserContainer.add(VariantMainPanel.createDisabledChooser());

        disableDataSpinners();

        createVariantButton.setEnabled(false);
        removeVariantButton.setEnabled(false);
    }

    private JPanel createOrdnanceInfoPanel() {
        JPanel infoPanel = new JPanel();
        ComponentUtilities.outfitPanelWithTitle(infoPanel, "Ordnance points");
        infoPanel.setLayout(new GridBagLayout());

        JLabel shipOPCapLabel = new JLabel(StringValues.TOTAL_OP_CAPACITY);
        Border emptyBorder = new EmptyBorder(2, 0, 8, 0);
        shipOPCapLabel.setBorder(emptyBorder);
        shipOPCap = new JLabel();

        JLabel usedOPTotalLabel = new JLabel("Used OP for ship:");
        usedOPTotalLabel.setBorder(emptyBorder);
        usedOPTotal = new JLabel();

        JLabel usedOPInWeaponsLabel = new JLabel("Used OP in weapons:");
        usedOPInWeaponsLabel.setBorder(emptyBorder);
        usedOPInWeapons = new JLabel();

        JLabel usedOPInModsLabel = new JLabel(StringValues.USED_OP_IN_HULLMODS);
        usedOPInModsLabel.setBorder(emptyBorder);
        usedOPInHullmods = new JLabel();

        JLabel usedOPInWingsLabel = new JLabel(StringValues.USED_OP_IN_WINGS);
        usedOPInWingsLabel.setBorder(emptyBorder);
        usedOPInWings = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, shipOPCapLabel, shipOPCap, 0);
        ComponentUtilities.addLabelAndComponent(infoPanel, usedOPTotalLabel, usedOPTotal, 1);
        ComponentUtilities.addLabelAndComponent(infoPanel, usedOPInWeaponsLabel, usedOPInWeapons, 2);
        ComponentUtilities.addLabelAndComponent(infoPanel, usedOPInModsLabel, usedOPInHullmods, 3);
        ComponentUtilities.addLabelAndComponent(infoPanel, usedOPInWingsLabel, usedOPInWings, 4);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 6, 0, 3);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.weightx = 0.0;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        infoPanel.add(new JLabel(), constraints);

        return infoPanel;
    }

    private void refreshOrdnanceInfo(ViewerLayer selected) {
        String notInitialized = StringValues.NOT_INITIALIZED;

        if (selected instanceof ShipLayer shipLayer) {
            String totalOP = Utility.translateIntegerValue(shipLayer::getTotalOP);
            shipOPCap.setText(totalOP);

            var activeVariant = shipLayer.getActiveVariant();
            if (activeVariant == null) {
                usedOPTotal.setText(notInitialized);
                usedOPInWeapons.setText(notInitialized);
                usedOPInHullmods.setText(notInitialized);
                usedOPInWings.setText(notInitialized);
                return;
            }

            int totalUsedOP = shipLayer.getTotalUsedOP();
            usedOPTotal.setText(String.valueOf(totalUsedOP));

            int totalOPInWeapons = activeVariant.getTotalOPInWeapons();
            usedOPInWeapons.setText(String.valueOf(totalOPInWeapons));

            int totalOPInMods = activeVariant.getTotalOPInHullmods(shipLayer);
            usedOPInHullmods.setText(String.valueOf(totalOPInMods));

            int totalOPInWings = shipLayer.getTotalOPInWings();
            usedOPInWings.setText(String.valueOf(totalOPInWings));
        } else {
            shipOPCap.setText(notInitialized);
            usedOPTotal.setText(notInitialized);
            usedOPInWeapons.setText(notInitialized);
            usedOPInHullmods.setText(notInitialized);
            usedOPInWings.setText(notInitialized);
        }
    }

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof VariantPanelRepaintQueued) {
                this.refreshOrdnanceInfo(selectedLayer);
            }
        });
    }

    private void disableDataSpinners() {
        this.ventsSpinner.disableSpinner();
        this.capacitorsSpinner.disableSpinner();
        this.qualitySpinner.disableSpinner();
    }

    @Override
    public void refreshPanel(ViewerLayer selected) {
        chooserContainer.removeAll();
        selectedLayer = null;
        cachedVariant = null;

        refreshOrdnanceInfo(selected);

        removeVariantButton.setText(StringValues.REMOVE);
        shipHullIDLabel.setText(StringValues.NOT_INITIALIZED);

        variantIDEditor.setEnabled(false);
        variantIDEditor.setText("");
        variantIDSetter = null;

        variantDisplayNameEditor.setEnabled(false);
        variantDisplayNameEditor.setText("");
        variantDisplayNameSetter = null;

        goalVariantSetter = null;
        goalVariantCheckbox.setSelected(false);
        goalVariantCheckbox.setEnabled(false);

        if (!(selected instanceof ShipLayer checkedLayer)) {
            this.installPlaceholders();
            return;
        }

        ShipHull shipHull = checkedLayer.getHull();
        if (shipHull == null) {
            this.installPlaceholders();
            return;
        }

        selectedLayer = checkedLayer;
        ShipVariant variant = this.recreateVariantChooser(checkedLayer);
        variantToRemoveGetter = () -> variant;

        createVariantButton.setEnabled(true);

        if (variant == null || variant.isEmpty()) {
            disableDataSpinners();
            removeVariantButton.setEnabled(false);
        } else {
            cachedVariant = variant;

            if (variant.isLoadedFromFile()) {
                removeVariantButton.setText("Reload");
            }
            HullSize hullSize = shipHull.getHullSize();
            int maxFluxRegulators = hullSize.getMaxFluxRegulators();

            this.ventsSpinner.enableSpinner(checkedLayer, variant.getFluxVents(),
                    maxFluxRegulators, variant::setFluxVents);
            this.capacitorsSpinner.enableSpinner(checkedLayer, variant.getFluxCapacitors(),
                    maxFluxRegulators, variant::setFluxCapacitors);

            this.qualitySpinner.enableSpinner(checkedLayer, variant.getQuality(),
                    1.0d, variant::setQuality);

            removeVariantButton.setEnabled(true);

            shipHullIDLabel.setText(variant.getShipHullId());

            variantIDEditor.setText(variant.getVariantId());
            variantIDEditor.setEnabled(true);
            variantIDSetter = variant::setVariantId;

            variantDisplayNameEditor.setText(variant.getDisplayName());
            variantDisplayNameEditor.setEnabled(true);
            variantDisplayNameSetter = variant::setDisplayName;

            goalVariantSetter = variant::setGoalVariant;
            goalVariantCheckbox.setSelected(variant.isGoalVariant());
            goalVariantCheckbox.setEnabled(true);
        }
    }

    private JPanel createDataPanel() {
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());
        ComponentUtilities.outfitPanelWithTitle(dataPanel, "Variant data");

        JLabel shipHullIDConstLabel = new JLabel("Ship hull ID:");
        shipHullIDConstLabel.setBorder(new EmptyBorder(2, 0, 5, 0));
        shipHullIDLabel = new JLabel(StringValues.NOT_INITIALIZED);
        ComponentUtilities.addLabelAndComponent(dataPanel, shipHullIDConstLabel, shipHullIDLabel, 0);

        JLabel variantIDLabel = new JLabel("Variant ID:");
        variantIDEditor = new JTextField();
        String editorIDTooltip = Utility.getWithLinebreaks(StringValues.TYPE_AND_PRESS_ENTER_TO_EDIT_ID,
                "Original variant will be copied with new ID, old entry reloaded");
        variantIDEditor.setToolTipText(editorIDTooltip);

        variantIDEditor.addActionListener(e -> {
            Map<String, ShipVariant> loadedVariants = selectedLayer.getLoadedVariants();
            String variantId = cachedVariant.getVariantId();
            ShipVariant renamed = loadedVariants.remove(variantId);

            String currentText = variantIDEditor.getText();
            variantIDSetter.accept(currentText);

            loadedVariants.put(currentText, renamed);
            renamed.setLoadedFromFile(false);
            ShipPainter shipPainter = selectedLayer.getPainter();
            shipPainter.selectVariant(renamed);
        });
        ComponentUtilities.addLabelAndComponent(dataPanel, variantIDLabel, variantIDEditor, 1);

        JLabel variantDisplayNameLabel = new JLabel("Displayed filename:");
        variantDisplayNameEditor = new JTextField();
        String editorNameTooltip = Utility.getWithLinebreaks(StringValues.TYPE_AND_PRESS_ENTER_TO_EDIT_ID);
        variantDisplayNameEditor.setToolTipText(editorNameTooltip);

        variantDisplayNameEditor.addActionListener(e -> {
            String currentText = variantDisplayNameEditor.getText();
            variantDisplayNameSetter.accept(currentText);

            ShipPainter shipPainter = selectedLayer.getPainter();
            shipPainter.selectVariant(cachedVariant);
        });
        ComponentUtilities.addLabelAndComponent(dataPanel,
                variantDisplayNameLabel, variantDisplayNameEditor, 2);

        SpinnerNumberModel ventsModel = new SpinnerNumberModel(0, 0, 0, 1);
        JSpinner ventsSpinnerComponent = new JSpinner(ventsModel);
        ventsSpinner = new DataSpinnerContainer<>(ventsModel, ventsSpinnerComponent);

        this.addDataSpinner(dataPanel, "Flux vents:",
                false, ventsSpinner, 3);

        SpinnerNumberModel capacitorsModel = new SpinnerNumberModel(0, 0, 0, 1);
        JSpinner capacitorSpinnerComponent = new JSpinner(capacitorsModel);
        capacitorsSpinner = new DataSpinnerContainer<>(capacitorsModel, capacitorSpinnerComponent);

        this.addDataSpinner(dataPanel, "Flux capacitors:",
                false, capacitorsSpinner, 4);

        SpinnerNumberModel qualityModel = new SpinnerNumberModel(0.0d, -1.0d, 0.0d, 0.05d);
        JSpinner qualitySpinnerComponent = new JSpinner(qualityModel);
        qualitySpinnerComponent.setToolTipText("Values less than 0 indicate " +
                "the field is omitted from variant file.");
        qualitySpinner = new DataSpinnerContainer<>(qualityModel, qualitySpinnerComponent);

        this.addDataSpinner(dataPanel, "Variant quality:",
                true, qualitySpinner, 5);

        goalVariantCheckbox = new JCheckBox("Goal variant");
        goalVariantCheckbox.addItemListener(e -> {
            boolean enableGoal = goalVariantCheckbox.isSelected();
            if (goalVariantSetter != null) {
                goalVariantSetter.accept(enableGoal);
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 3, 0, 3);
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.weightx = 0.0;
        constraints.anchor = GridBagConstraints.LINE_START;
        dataPanel.add(goalVariantCheckbox, constraints);

        return dataPanel;
    }

    @SuppressWarnings("unchecked")
    private <T extends Number> void addDataSpinner(JPanel target, String labelText,
                                                          boolean useFloatingPoint,
                                                          DataSpinnerContainer<T> spinnerContainer,
                                                          int row) {
        JLabel label = new JLabel(labelText);

        SpinnerNumberModel spinnerNumberModel = spinnerContainer.getModel();
        JSpinner spinner = spinnerContainer.getSpinner();

        spinner.addChangeListener(e -> {
            Number modelNumber = spinnerNumberModel.getNumber();
            T result = (T) modelNumber;
            var currentSetter = spinnerContainer.getCurrentSetter();
            if (currentSetter != null) {
                currentSetter.accept(result);
            }
            refreshOrdnanceInfo(selectedLayer);
        });

        MouseWheelListener wheelListener = e -> {
            if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL || spinnerContainer.getCurrentSetter() == null) {
                return;
            }
            Number newValue;
            if (useFloatingPoint) {
                double value = (double) spinner.getValue();
                double newValueInt = value - (e.getUnitsToScroll() * 0.05d);
                double minValue = (double) spinnerContainer.getMinValue();
                double maxValue = (double) spinnerContainer.getMaxValue();
                newValue = Math.min(maxValue, Math.max(minValue, newValueInt));
            } else {
                int value = (int) spinner.getValue();
                int newValueInt = value - e.getUnitsToScroll();
                int minValue = (int) spinnerContainer.getMinValue();
                int maxValue = (int) spinnerContainer.getMaxValue();
                newValue = Math.min(maxValue, Math.max(minValue, newValueInt));
            }
            spinner.setValue(newValue);
        };
        spinner.addMouseWheelListener(wheelListener);

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
        ShipVariant[] variantsFileArray = {new ShipVariant()};
        JComboBox<ShipVariant> variantChooser = new JComboBox<>(variantsFileArray);
        variantChooser.setSelectedItem(variantsFileArray[0]);
        variantChooser.setEnabled(false);
        return variantChooser;
    }

}
