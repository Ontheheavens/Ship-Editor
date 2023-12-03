package oth.shipeditor.components.instrument.ship.variant.modules;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.AbstractShipPropertiesPanel;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 29.11.2023
 */
public class VariantModulesPanel extends AbstractShipPropertiesPanel {

    private ModuleList moduleList;

    private ModuleControlPanel controlPanel;

    private DefaultListModel<InstalledFeature> model;

    @SuppressWarnings({"OverlyComplexBooleanExpression", "ChainedMethodCall"})
    @Override
    public void refreshContent(LayerPainter layerPainter) {
        DefaultListModel<InstalledFeature> newModel = new DefaultListModel<>();

        if (!(layerPainter instanceof ShipPainter shipPainter)
                || shipPainter.isUninitialized()
                || shipPainter.getActiveVariant() == null
                || shipPainter.getActiveVariant().isEmpty()) {
            this.model = newModel;
            this.moduleList.setModel(newModel);

            fireClearingListeners(layerPainter);
            this.controlPanel.refresh(null);

            this.moduleList.setEnabled(false);

            return;
        }

        ShipVariant activeVariant = shipPainter.getActiveVariant();

        newModel.addAll(activeVariant.getFittedModulesList());

        this.model = newModel;
        this.moduleList.setModel(newModel);
        this.moduleList.setEnabled(true);

        fireRefresherListeners(layerPainter);
        refreshModuleControlPane();
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        this.model = new DefaultListModel<>();
        this.moduleList = createModuleList();
        this.controlPanel = new ModuleControlPanel(moduleList);

        JPanel northContainer = new JPanel(new BorderLayout());
        ComponentUtilities.outfitPanelWithTitle(northContainer, "Selected module");
        northContainer.add(controlPanel, BorderLayout.CENTER);

        this.refreshModuleControlPane();

        JScrollPane scrollableContainer = new JScrollPane(moduleList);

        this.add(northContainer, BorderLayout.PAGE_START);
        this.add(scrollableContainer, BorderLayout.CENTER);
    }

    @Override
    public ShipPainter getCachedLayerPainter() {
        LayerPainter cachedLayerPainter = super.getCachedLayerPainter();
        if (cachedLayerPainter instanceof ShipPainter shipPainter && !shipPainter.isUninitialized()) {
            return shipPainter;
        }
        return null;
    }

    private ShipVariant getCurrentVariant() {
        ShipPainter shipPainter = getCachedLayerPainter();
        ShipVariant activeVariant = shipPainter.getActiveVariant();
        if (activeVariant != null && !activeVariant.isEmpty()) {
            return activeVariant;
        }
        return null;
    }

    private void refreshModuleControlPane() {
        InstalledFeature selectedValue = moduleList.getSelectedValue();
        if (selectedValue == null) {
            this.controlPanel.refresh(null);
            return;
        }
        ShipPainter painter = (ShipPainter) selectedValue.getFeaturePainter();
        this.controlPanel.refresh(painter);
    }

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                if (!(checked.point() instanceof WeaponSlotPoint slotPoint)) return;
                if (moduleList != null && StaticController.getEditorMode() == EditorInstrument.VARIANT_MODULES) {
                    moduleList.selectEntryByPoint(slotPoint);
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentRepaintQueued(EditorInstrument editorMode)) {
                if (editorMode != EditorInstrument.VARIANT_MODULES) {
                    return;
                }
                ShipVariant currentVariant = getCurrentVariant();
                if (currentVariant != null) {
                    int[] cachedSelected = this.moduleList.getSelectedIndices();
                    DefaultListModel<InstalledFeature> newModel = new DefaultListModel<>();
                    newModel.addAll(currentVariant.getFittedModulesList());

                    this.model = newModel;
                    this.moduleList.setModel(newModel);
                    this.moduleList.setSelectedIndices(cachedSelected);
                    if (!this.model.isEmpty() && cachedSelected.length > 0) {
                        this.moduleList.ensureIndexIsVisible(cachedSelected[0]);
                    }
                }

                this.refreshModuleControlPane();
            }
        });
    }

    private ModuleList createModuleList() {
        Consumer<InstalledFeature> removeAction = feature ->
                StaticController.actOnCurrentVariant((shipLayer, variant) -> {
                    Map<String, InstalledFeature> fittedModules = variant.getFittedModules();
                    if (fittedModules == null) {
                        return;
                    }
                    EditDispatch.postFeatureUninstalled(fittedModules,
                            feature.getSlotID(), feature, null);
                });

        Consumer<Map<String, InstalledFeature>> sortAction = rearranged ->
                StaticController.actOnCurrentVariant((shipLayer, variant) ->
                        variant.sortModules(rearranged));

        moduleList = new ModuleList(this::refreshModuleControlPane, model, removeAction, sortAction);
        moduleList.setBorder(new LineBorder(Color.LIGHT_GRAY));
        return moduleList;
    }

}
