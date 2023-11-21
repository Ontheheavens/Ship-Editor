package oth.shipeditor.components.instrument.ship.engines;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.EnginesPanelRepaintQueued;
import oth.shipeditor.communication.events.files.EngineStylesLoaded;
import oth.shipeditor.components.instrument.LayerPropertiesPanel;
import oth.shipeditor.components.viewer.entities.engine.EngineData;
import oth.shipeditor.components.viewer.entities.engine.EngineDataOverride;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.EngineSlotPainter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.EngineStyle;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.objects.Size2D;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 21.11.2023
 */
public class EngineDataPanel2 extends LayerPropertiesPanel {

    EngineDataPanel2() {
        EventBus.subscribe(event -> {
            if (event instanceof EngineStylesLoaded) {
                this.populateContent();
                refresh(this.getCachedLayerPainter());
            }
        });
    }

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        if (layerPainter == null || layerPainter.isUninitialized()) {
            fireClearingListeners(null);
            return;
        }

        fireRefresherListeners(layerPainter);
    }

    @Override
    protected void addWidgetRow(JPanel contentContainer, JLabel label, JComponent component, int ordering) {
        ComponentUtilities.addLabelAndComponent(contentContainer,
                label, component, 2, 2, 0, ordering);
    }

    private static EnginePoint getSelectedFromLayer(LayerPainter layerPainter) {
        if (layerPainter instanceof ShipPainter shipPainter) {
            if (shipPainter.isUninitialized()) return null;
            var enginePainter = shipPainter.getEnginePainter();

            return enginePainter.getSelected();
        }
        return null;
    }

    @Override
    public ShipPainter getCachedLayerPainter() {
        return (ShipPainter) super.getCachedLayerPainter();
    }

    @Override
    protected void populateContent() {
        this.removeAll();

        this.setLayout(new BorderLayout());

        Map<JLabel, JComponent> widgets = new LinkedHashMap<>();

        var angleController = createAngleController();
        widgets.put(angleController.getFirst(), angleController.getSecond());

        var widthController = createWidthController();
        widgets.put(widthController.getFirst(), widthController.getSecond());

        var lengthController = createLengthController();
        widgets.put(lengthController.getFirst(), lengthController.getSecond());

        var contrailController = createContrailController();
        widgets.put(contrailController.getFirst(), contrailController.getSecond());

        var styleSelector = createStyleSelector();
        widgets.put(styleSelector.getFirst(), styleSelector.getSecond());

        JPanel widgetsPanel = createWidgetsPanel(widgets);
        this.add(widgetsPanel, BorderLayout.CENTER);
    }

    private Pair<JLabel, JComponent> createAngleController() {
        String text = StringValues.ENGINE_ANGLE;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0.0d,
                -360.0d, 360.0d, 0.5d);
        Function<EngineData, Double> getter = EngineData::getAngleBoxed;
        Consumer<Double> setter = degrees -> {
            ShipPainter shipPainter = getCachedLayerPainter();
            EngineSlotPainter enginePainter = shipPainter.getEnginePainter();
            enginePainter.changePointAngleWithMirrorCheck(enginePainter.getSelected(), degrees);
        };
        return createValueWidget(text, spinnerNumberModel, getter, setter);
    }

    private Pair<JLabel, JComponent> createWidthController() {
        String text = StringValues.ENGINE_WIDTH;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0.0d,
                0.0d, Double.MAX_VALUE, 0.5d);
        Function<EngineData, Double> getter = EngineData::getWidthBoxed;
        Consumer<Double> setter = width -> {
            ShipPainter shipPainter = getCachedLayerPainter();
            EngineSlotPainter enginePainter = shipPainter.getEnginePainter();
            EnginePoint selected = enginePainter.getSelected();
            double currentLength = selected.getLength();
            Size2D newSize = new Size2D(width, currentLength);
            enginePainter.changeEngineSizeWithMirrorCheck(selected, newSize);
        };
        return createValueWidget(text, spinnerNumberModel, getter, setter);
    }

    private Pair<JLabel, JComponent> createLengthController() {
        String text = StringValues.ENGINE_LENGTH;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0.0d,
                0.0d, Double.MAX_VALUE, 0.5d);
        Function<EngineData, Double> getter = EngineData::getLengthBoxed;
        Consumer<Double> setter = length -> {
            ShipPainter shipPainter = getCachedLayerPainter();
            EngineSlotPainter enginePainter = shipPainter.getEnginePainter();
            EnginePoint selected = enginePainter.getSelected();
            double selectedWidth = selected.getWidth();
            Size2D newSize = new Size2D(selectedWidth, length);
            enginePainter.changeEngineSizeWithMirrorCheck(selected, newSize);
        };
        return createValueWidget(text, spinnerNumberModel, getter, setter);
    }

    private Pair<JLabel, JComponent> createContrailController() {
        String text = StringValues.CONTRAIL_SIZE;
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0.0d,
                0.0d, 128.0d, 1.0d);
        Function<EngineData, Double> getter = EngineData::getContrailSizeBoxed;
        Consumer<Double> setter = contrailValue -> {
            ShipPainter shipPainter = getCachedLayerPainter();
            EngineSlotPainter enginePainter = shipPainter.getEnginePainter();
            EnginePoint selected = enginePainter.getSelected();
            enginePainter.changeEngineContrailWithMirrorCheck(selected, (int) Math.round(contrailValue));
        };
        return createValueWidget(text, spinnerNumberModel, getter, setter);
    }

    private Pair<JLabel, JComponent> createStyleSelector() {
        JLabel selectorLabel = new JLabel(StringValues.ENGINE_STYLE_LABEL);
        selectorLabel.setToolTipText(StringValues.CHANGE_APPLIES_TO_ALL_SELECTED_SLOTS);

        JComponent widget;

        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, EngineStyle> allEngineStyles = gameData.getAllEngineStyles();
        if (allEngineStyles != null) {
            Collection<EngineStyle> styleCollection = allEngineStyles.values();
            JComboBox<EngineStyle> styleSelector  = new JComboBox<>(styleCollection.toArray(new EngineStyle[0]));

            styleSelector.addActionListener(e -> {
                if (isWidgetsReadyForInput()) {
                    ShipPainter slotParent = getCachedLayerPainter();
                    EngineSlotPainter enginePainter = slotParent.getEnginePainter();
                    EngineStyle selectedValue = (EngineStyle) styleSelector.getSelectedItem();
                    enginePainter.changeEngineStyleWithMirrorCheck(enginePainter.getSelected(), selectedValue);
                }
            });

            widget = styleSelector;

            registerWidgetListeners(styleSelector, layerPainter -> {
                styleSelector.setSelectedItem(null);
                styleSelector.setToolTipText("");
                styleSelector.setEnabled(false);
            }, layerPainter -> {
                var selectedEngine = EngineDataPanel2.getSelectedFromLayer(layerPainter);

                if (selectedEngine != null) {
                    EngineDataOverride skinOverride = selectedEngine.getSkinOverride();
                    if (skinOverride != null && skinOverride.getStyle() != null) {
                        styleSelector.setToolTipText(StringValues.LOCKED_STYLE_OVERRIDDEN_BY_SKIN);
                        styleSelector.setEnabled(false);
                    } else {
                        styleSelector.setSelectedItem(selectedEngine.getStyle());
                        styleSelector.setEnabled(true);
                    }
                } else {
                    styleSelector.setSelectedItem(null);
                    styleSelector.setToolTipText("");
                    styleSelector.setEnabled(false);
                }
            });
        } else {
            JTextField idField = new JTextField();
            idField.setToolTipText(StringValues.STYLES_NOT_LOADED_DEFAULTED_TO_ID_TEXT);

            idField.addActionListener(e -> {
                String textValue = idField.getText();
                ShipPainter slotParent = getCachedLayerPainter();
                EngineSlotPainter enginePainter = slotParent.getEnginePainter();
                EnginePoint selected = enginePainter.getSelected();
                selected.setStyleID(textValue);
                EventBus.publish(new EnginesPanelRepaintQueued());
            });

            widget = idField;

            registerWidgetListeners(idField, layerPainter -> {
                idField.setText("");
                idField.setToolTipText("");
                idField.setEnabled(false);
            }, layerPainter -> {
                var selectedEngine = EngineDataPanel2.getSelectedFromLayer(layerPainter);

                if (selectedEngine != null) {
                    EngineDataOverride skinOverride = selectedEngine.getSkinOverride();
                    if (skinOverride != null && skinOverride.getStyle() != null) {
                        idField.setToolTipText(StringValues.LOCKED_STYLE_OVERRIDDEN_BY_SKIN);
                        idField.setEnabled(false);
                    } else {
                        idField.setText(selectedEngine.getStyleID());
                        idField.setEnabled(true);
                    }
                } else {
                    idField.setText("");
                    idField.setToolTipText("");
                    idField.setEnabled(false);
                }
            });
        }

        return new Pair<>(selectorLabel, widget);
    }

    private Pair<JLabel, JComponent> createValueWidget(String labelText, SpinnerNumberModel model,
                                                       Function<EngineData, Double> getter, Consumer<Double> setter) {
        JLabel label = new JLabel(labelText);

        String changeApplies = StringValues.CHANGE_APPLIES_TO_FIRST_SELECTED_SLOT;
        String tooltip = Utility.getWithLinebreaks(changeApplies, StringValues.MOUSEWHEEL_TO_CHANGE);
        label.setToolTipText(tooltip);

        JSpinner spinner =  Spinners.createWheelable(model);

        spinner.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                Number modelNumber = model.getNumber();
                double current = modelNumber.doubleValue();
                setter.accept(current);
            }
        });

        registerWidgetListeners(spinner, layerPainter -> {
            spinner.setValue(0.0d);
            spinner.setToolTipText("");
            spinner.setEnabled(false);
        }, layerPainter -> {
            var selectedEngine = EngineDataPanel2.getSelectedFromLayer(layerPainter);

            if (selectedEngine != null) {
                EngineDataOverride skinOverride = selectedEngine.getSkinOverride();

                if (skinOverride != null && getter.apply(skinOverride) != null) {
                    spinner.setToolTipText("Locked: overridden by skin");
                    spinner.setEnabled(false);
                } else {
                    spinner.setValue(getter.apply(selectedEngine));
                    spinner.setEnabled(true);
                }
            } else {
                spinner.setValue(0.0d);
                spinner.setToolTipText("");
                spinner.setEnabled(false);
            }
        });

        return new Pair<>(label, spinner);
    }

}
