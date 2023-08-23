package oth.shipeditor.components.instrument.ship.engines;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.EnginesPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.points.EngineInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.EngineSlotPainter;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
public class EnginesPanel extends JPanel {

    @Getter
    private final EngineList enginesContainer;

    private final JCheckBox reorderCheckbox;

    private DefaultListModel<EnginePoint> model = new DefaultListModel<>();

    private EngineSlotPainter cachedPainter;

    public EnginesPanel() {
        this.setLayout(new BorderLayout());

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.PAGE_AXIS));
        northContainer.add(this.createPainterVisibilityPanel());

        ComponentUtilities.addSeparatorToBoxPanel(northContainer);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        northContainer.add(infoPanel);

        enginesContainer = new EngineList(model, infoPanel);
        this.enginesContainer.refreshEngineControlPane();
        JScrollPane scrollableContainer = new JScrollPane(enginesContainer);

        ComponentUtilities.addSeparatorToBoxPanel(northContainer);

        Pair<JPanel, JCheckBox> reorderWidget = ComponentUtilities.createReorderCheckboxPanel(enginesContainer);
        reorderCheckbox = reorderWidget.getSecond();
        reorderCheckbox.setToolTipText("Warning: reordering might affect skin engine overrides mapping!");
        northContainer.add(reorderWidget.getFirst());

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);

        this.initPointListener();
        this.initLayerListeners();
    }

    @SuppressWarnings("MethodMayBeStatic")
    private JPanel createPainterVisibilityPanel() {
        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            if (!(e.getSource() instanceof ShipPainter checked)) return;
            EngineSlotPainter enginePainter = checked.getEnginePainter();
            PainterVisibility valueOfLayer = enginePainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };

        return ComponentUtilities.createVisibilityWidget(visibilityList,
                EngineSlotPainter.class, selectionAction, "");
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof EnginesPanelRepaintQueued) {
                if (cachedPainter != null) {
                    EnginePoint cachedSelected = this.enginesContainer.getSelectedValue();
                    DefaultListModel<EnginePoint> newModel = new DefaultListModel<>();
                    newModel.addAll(cachedPainter.getPointsIndex());

                    this.model = newModel;
                    this.enginesContainer.setModel(newModel);
                    this.enginesContainer.setSelectedValue(cachedSelected, true);
                }
                this.enginesContainer.refreshEngineControlPane();
                this.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof EnginePoint point) {
                model.addElement(point);
                enginesContainer.setSelectedIndex(model.indexOf(point));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof EngineInsertedConfirmed checked) {
                model.insertElementAt(checked.toInsert(), checked.precedingIndex());
                enginesContainer.setSelectedIndex(model.indexOf(checked.toInsert()));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof EnginePoint point) {
                model.removeElement(point);
            }
        });
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                DefaultListModel<EnginePoint> newModel = new DefaultListModel<>();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    reorderCheckbox.setEnabled(false);
                    this.model = newModel;
                    this.enginesContainer.setModel(newModel);
                    this.enginesContainer.setEnabled(false);
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                if (painter != null && !painter.isUninitialized()) {
                    EngineSlotPainter enginePainter = painter.getEnginePainter();
                    cachedPainter = enginePainter;
                    newModel.addAll(enginePainter.getPointsIndex());
                    reorderCheckbox.setEnabled(true);
                    this.enginesContainer.setEnabled(true);
                } else {
                    reorderCheckbox.setEnabled(false);
                    this.enginesContainer.setEnabled(false);
                }
                this.model = newModel;
                this.enginesContainer.setModel(newModel);
                this.enginesContainer.refreshEngineControlPane();
            }
        });
    }

}
