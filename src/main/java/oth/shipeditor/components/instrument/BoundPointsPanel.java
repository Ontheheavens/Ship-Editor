package oth.shipeditor.components.instrument;

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
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.BoundPointsPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@SuppressWarnings("DuplicatedCode")
@Log4j2
public final class BoundPointsPanel extends JPanel {

    @Getter
    private final BoundList boundPointContainer;

    private static final int sidePadding = 6;

    private JLabel opacityLabel;
    private JSlider opacitySlider;

    private DefaultListModel<BoundPoint> model = new DefaultListModel<>();

    public BoundPointsPanel() {
        this.setLayout(new BorderLayout());
        boundPointContainer = new BoundList(model);
        JScrollPane scrollableContainer = new JScrollPane(boundPointContainer);
        Dimension listSize = new Dimension(boundPointContainer.getPreferredSize().width + 30,
                boundPointContainer.getPreferredSize().height);
        scrollableContainer.setPreferredSize(listSize);
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.PAGE_AXIS));
        northContainer.add(this.createPainterOpacityPanel());
        northContainer.add(this.createPainterVisibilityPanel());
        northContainer.add(Box.createRigidArea(new Dimension(0,6)));
        this.add(northContainer, BorderLayout.PAGE_START);
        this.add(scrollableContainer, BorderLayout.CENTER);

        this.initPointListener();
        this.initLayerListeners();
    }

    private void updateOpacityLabel(int opacity) {
        opacityLabel.setText("Painter opacity: " + opacity + "%");
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
                ShipLayer selected = checked.selected();
                if (selected == null) {
                    updateOpacityLabel(100);
                    opacitySlider.setValue(100);
                    return;
                }
                LayerPainter painter = selected.getPainter();
                int value;
                if (painter == null) {
                    value = 100;
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

        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));
        container.add(opacityLabel);
        container.add(Box.createHorizontalGlue());
        container.add(opacitySlider);
        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));

        return container;
    }

    @SuppressWarnings("MethodMayBeStatic")
    private JPanel createPainterVisibilityPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        container.setBorder(new EmptyBorder(4, 0, 0, 0));

        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        visibilityList.setRenderer(PainterVisibility.createCellRenderer());
        visibilityList.addActionListener(PainterVisibility.createActionListener(visibilityList,
                BoundPointsPainter.class));
        ActionListener selectionAction = e -> {
            LayerPainter painter = (LayerPainter) e.getSource();
            BoundPointsPainter boundsPainter = painter.getBoundsPainter();
            PainterVisibility valueOfLayer = boundsPainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };
        EventBus.subscribe(PainterVisibility.createBusEventListener(visibilityList, selectionAction));

        JLabel widgetLabel = new JLabel("Bounds visibility:");
        widgetLabel.setToolTipText(StringConstants.TOGGLED_ON_PER_LAYER_BASIS);

        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));
        container.add(widgetLabel);
        container.add(Box.createHorizontalGlue()); // Add glue to push components to opposite sides.
        container.add(visibilityList);
        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));

        return container;
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
                ShipLayer selected = checked.selected();
                DefaultListModel<BoundPoint> newModel = new DefaultListModel<>();
                if (selected != null && selected.getPainter() != null) {
                    LayerPainter selectedLayerPainter = selected.getPainter();
                    BoundPointsPainter newBoundsPainter = selectedLayerPainter.getBoundsPainter();
                    newModel.addAll(newBoundsPainter.getBoundPoints());
                }
                this.model = newModel;
                this.boundPointContainer.setModel(newModel);

                opacitySlider.setEnabled(selected != null && selected.getPainter() != null);
            }
        });
    }

}
