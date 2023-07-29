package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundsPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.BoundPointsPainter;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public final class BoundPointsPanel extends JPanel {

    @Getter
    private final BoundList boundPointContainer;

    private static final int sidePadding = 6;

    private JLabel opacityLabel;
    private JSlider opacitySlider;

    private JCheckBox reorderCheckbox;

    private DefaultListModel<BoundPoint> model = new DefaultListModel<>();

    public BoundPointsPanel() {
        this.setLayout(new BorderLayout());
        boundPointContainer = new BoundList(model);
        JScrollPane scrollableContainer = new JScrollPane(boundPointContainer);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.PAGE_AXIS));
        northContainer.add(this.createPainterOpacityPanel());
        northContainer.add(this.createPainterVisibilityPanel());

        ComponentUtilities.addSeparatorToBoxPanel(northContainer);

        northContainer.add(this.createReorderCheckboxPanel());

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);

        this.initPointListener();
        this.initLayerListeners();
    }

    private void updateOpacityLabel(int opacity) {
        opacityLabel.setText(StringValues.PAINTER_OPACITY);
        opacityLabel.setToolTipText(StringValues.CURRENT_VALUE + opacity + "%");
    }

    private JPanel createReorderCheckboxPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        container.setBorder(new EmptyBorder(2, 0, 2, 0));

        reorderCheckbox = new JCheckBox("Reorder by drag");
        reorderCheckbox.addItemListener(e -> {
            boolean reorderOn = reorderCheckbox.isSelected();
            boundPointContainer.setDragEnabled(reorderOn);
        });
        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));
        container.add(reorderCheckbox);
        container.add(Box.createHorizontalGlue());

        return container;
    }

    private JPanel createPainterOpacityPanel() {
        JPanel container = new JPanel();

        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        ChangeListener changeListener = e -> {
            JSlider source = (JSlider)e.getSource();
            int opacity = source.getValue();
            updateOpacityLabel(opacity);
            float changedValue = opacity / 100.0f;
            EventBus.publish(new PainterOpacityChangeQueued(BoundPointsPainter.class, changedValue));
        };
        BusEventListener eventListener = event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    updateOpacityLabel(100);
                    opacitySlider.setValue(100);
                    opacitySlider.setEnabled(false);
                    reorderCheckbox.setEnabled(false);
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                int value;
                if (painter == null || painter.isUninitialized()) {
                    value = 100;
                    opacitySlider.setEnabled(false);
                    reorderCheckbox.setEnabled(false);
                } else {
                    BoundPointsPainter boundsPainter = painter.getBoundsPainter();
                    value = (int) (boundsPainter.getPaintOpacity() * 100.0f);
                }
                updateOpacityLabel(value);
                opacitySlider.setValue(value);
            }
        };
        Pair<JSlider, JLabel> widgetComponents = ComponentUtilities.createOpacityWidget(changeListener, eventListener);

        opacitySlider = widgetComponents.getFirst();
        opacityLabel = widgetComponents.getSecond();
        this.updateOpacityLabel(100);

        ComponentUtilities.layoutAsOpposites(container, opacityLabel, opacitySlider, sidePadding);

        return container;
    }

    @SuppressWarnings("MethodMayBeStatic")
    private JPanel createPainterVisibilityPanel() {
        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            if (!(e.getSource() instanceof ShipPainter checked)) return;
            BoundPointsPainter boundsPainter = checked.getBoundsPainter();
            PainterVisibility valueOfLayer = boundsPainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };

        return ComponentUtilities.createVisibilityWidget(visibilityList,
                BoundPointsPainter.class, selectionAction, "");
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof BoundsPanelRepaintQueued) {
                this.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof BoundPoint point) {
                model.addElement(point);
                boundPointContainer.setSelectedIndex(model.indexOf(point));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof BoundInsertedConfirmed checked) {
                model.insertElementAt(checked.toInsert(), checked.precedingIndex());
                boundPointContainer.setSelectedIndex(model.indexOf(checked.toInsert()));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof BoundPoint point) {
                model.removeElement(point);
            }
        });
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                DefaultListModel<BoundPoint> newModel = new DefaultListModel<>();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    opacitySlider.setEnabled(false);
                    reorderCheckbox.setEnabled(false);
                    this.model = newModel;
                    this.boundPointContainer.setModel(newModel);
                    this.boundPointContainer.setEnabled(false);
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                if (painter != null && !painter.isUninitialized()) {
                    BoundPointsPainter newBoundsPainter = painter.getBoundsPainter();
                    newModel.addAll(newBoundsPainter.getPointsIndex());
                    opacitySlider.setEnabled(true);
                    reorderCheckbox.setEnabled(true);
                    this.boundPointContainer.setEnabled(true);
                } else {
                    opacitySlider.setEnabled(false);
                    reorderCheckbox.setEnabled(false);
                    this.boundPointContainer.setEnabled(false);
                }
                this.model = newModel;
                this.boundPointContainer.setModel(newModel);
            }
        });
    }

}
