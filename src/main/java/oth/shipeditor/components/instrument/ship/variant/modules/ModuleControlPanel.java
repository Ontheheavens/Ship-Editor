package oth.shipeditor.components.instrument.ship.variant.modules;

import oth.shipeditor.components.instrument.LayerPropertiesPanel;
import oth.shipeditor.components.instrument.ship.centers.ModuleAnchorPanel;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 02.12.2023
 */
public class ModuleControlPanel extends LayerPropertiesPanel {

    private final ModuleList moduleList;

    private ModuleAnchorPanel moduleAnchorWidget;

    ModuleControlPanel(ModuleList list) {
        this.moduleList = list;
    }

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        if (!(layerPainter instanceof ShipPainter shipPainter) || shipPainter.isUninitialized()) {
            fireClearingListeners(layerPainter);

            moduleAnchorWidget.setCenterPainter(null);
            moduleAnchorWidget.refresh(null);
            return;
        }

        fireRefresherListeners(layerPainter);

        moduleAnchorWidget.setCenterPainter(((ShipPainter) layerPainter).getCenterPointPainter());
        moduleAnchorWidget.refresh(layerPainter);
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        JPanel topContainer = new JPanel(new BorderLayout());

        Map<JLabel, JComponent> topWidgets = new LinkedHashMap<>();

        var collisionVisibilityWidget = createCollisionVisibilityWidget();
        topWidgets.put(collisionVisibilityWidget.getFirst(), collisionVisibilityWidget.getSecond());

        var boundsVisibilityWidget = createBoundsVisibilityWidget();
        topWidgets.put(boundsVisibilityWidget.getFirst(), boundsVisibilityWidget.getSecond());

        var slotsVisibilityWidget = createSlotsVisibilityWidget();
        topWidgets.put(slotsVisibilityWidget.getFirst(), slotsVisibilityWidget.getSecond());

        Border bottomPadding = new EmptyBorder(0, 0, 4, 0);

        JPanel topWidgetsPanel = createWidgetsPanel(topWidgets);
        topWidgetsPanel.setBorder(bottomPadding);
        topContainer.add(topWidgetsPanel, BorderLayout.PAGE_START);
        moduleAnchorWidget = new ModuleAnchorPanel();

        String tooltip = Utility.getWithLinebreaks("Mockup editing, changes are not saved to file",
                "Create separate ship layer from module to edit module anchor offset");
        moduleAnchorWidget.setToolTipText(tooltip);

        topContainer.add(moduleAnchorWidget, BorderLayout.CENTER);
        this.add(topContainer, BorderLayout.PAGE_START);
    }

    private Pair<JLabel, JComboBox<PainterVisibility>> createCollisionVisibilityWidget() {
        Function<LayerPainter, AbstractPointPainter> painterGetter = layerPainter -> {
            if (layerPainter instanceof ShipPainter shipPainter) {
                return shipPainter.getCenterPointPainter();
            }
            return null;
        };

        Consumer<PainterVisibility> additionalAction = painterVisibility -> actOnSelectedModules(shipPainter -> {
            var pointPainter = shipPainter.getCenterPointPainter();
            pointPainter.setVisibilityMode(painterVisibility);
        });
        var opacityWidget = createVisibilityWidget(painterGetter, additionalAction);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText(StringValues.COLLISION_VIEW);

        return opacityWidget;
    }

    private Pair<JLabel, JComboBox<PainterVisibility>> createBoundsVisibilityWidget() {
        Function<LayerPainter, AbstractPointPainter> painterGetter = layerPainter -> {
            if (layerPainter instanceof ShipPainter shipPainter) {
                return shipPainter.getBoundsPainter();
            }
            return null;
        };

        Consumer<PainterVisibility> additionalAction = painterVisibility -> actOnSelectedModules(shipPainter -> {
            var pointPainter = shipPainter.getBoundsPainter();
            pointPainter.setVisibilityMode(painterVisibility);
        });
        var opacityWidget = createVisibilityWidget(painterGetter, additionalAction);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText(StringValues.BOUNDS_VIEW);

        return opacityWidget;
    }

    private Pair<JLabel, JComboBox<PainterVisibility>> createSlotsVisibilityWidget() {
        Function<LayerPainter, AbstractPointPainter> painterGetter = layerPainter -> {
            if (layerPainter instanceof ShipPainter shipPainter) {
                return shipPainter.getWeaponSlotPainter();
            }
            return null;
        };

        Consumer<PainterVisibility> additionalAction = painterVisibility -> actOnSelectedModules(shipPainter -> {
            var pointPainter = shipPainter.getWeaponSlotPainter();
            pointPainter.setVisibilityMode(painterVisibility);
        });
        var opacityWidget = createVisibilityWidget(painterGetter, additionalAction);

        JLabel opacityLabel = opacityWidget.getFirst();
        opacityLabel.setText(StringValues.SLOTS_VIEW);

        return opacityWidget;
    }

    private void actOnSelectedModules(Consumer<ShipPainter> action) {
        if (moduleList == null) return;
        List<InstalledFeature> selectedValuesList = moduleList.getSelectedValuesList();
        if (selectedValuesList != null && !selectedValuesList.isEmpty()) {
            for (InstalledFeature feature : selectedValuesList) {
                action.accept((ShipPainter) feature.getFeaturePainter());
            }
        }
    }

}
