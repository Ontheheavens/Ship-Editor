package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.VariantPanelRepaintQueued;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 02.10.2023
 */
public class VariantModulesPanel extends AbstractVariantPanel{

    private final JPanel contentPanel;

    @SuppressWarnings("FieldCanBeLocal")
    private InstalledFeatureList modulesList;

    public VariantModulesPanel() {
        this.setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);

        ViewerLayer layer = StaticController.getActiveLayer();
        this.refreshPanel(layer);
    }

    private void installPlaceholders() {
        JPanel placeholder = this.createContentPlaceholder();
        contentPanel.add(placeholder, BorderLayout.CENTER);
    }

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof VariantPanelRepaintQueued) {
                this.refreshPanel(StaticController.getActiveLayer());
            }
        });
    }

    @Override
    public void refreshPanel(ViewerLayer selected) {
        modulesList = null;
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

        ShipPainter painter = checkedLayer.getPainter();

        ShipVariant activeVariant = painter.getActiveVariant();


        if (activeVariant != null && !activeVariant.isEmpty()) {
            var listContainer = VariantModulesPanel.getModuleList(activeVariant, painter);

            contentPanel.add(listContainer, BorderLayout.CENTER);
        } else {
            this.installPlaceholders();
        }
    }

    private static InstalledFeatureList getModuleList(ShipVariant activeVariant, ShipPainter painter) {
        Map<String, InstalledFeature> fittedModules = activeVariant.getFittedModules();
        Collection<InstalledFeature> modules = fittedModules.values();

        DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();
        listModel.addAll(modules);

        Consumer<InstalledFeature> removeAction = entry -> {
            EditDispatch.postFeatureUninstalled(fittedModules, entry.getSlotID(), entry, null);
        };

        var slotPainter = painter.getWeaponSlotPainter();

        // TODO: sorter shouldn't be null!
        var listContainer = new InstalledFeatureList(listModel, slotPainter,
                removeAction, null);
        listContainer.setBorder(new LineBorder(Color.LIGHT_GRAY));
        return listContainer;
    }

}
