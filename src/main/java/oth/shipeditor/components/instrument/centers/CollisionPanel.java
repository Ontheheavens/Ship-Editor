package oth.shipeditor.components.instrument.centers;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.CenterPointPainter;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Ontheheavens
 * @since 06.06.2023
 */
@Log4j2
public final class CollisionPanel extends JPanel {

    /**
     * Reference to the hull center painter of the currently active layer.
     */
    private CenterPointPainter centerPainter;

    private JLabel centerCoords;
    private JLabel collisionRadius;

    private JLabel collisionOpacityLabel;
    private JSlider collisionOpacitySlider;

    public CollisionPanel() {
        LayoutManager layout = new BorderLayout();
        this.setLayout(layout);

        JPanel hullCenterPanel = createCollisionPanel();
        this.add(hullCenterPanel, BorderLayout.CENTER);

        this.initLayerListeners();
        this.initPointListener();
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof CenterPanelsRepaintQueued) {
                this.refresh();
            }
        });
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                boolean enableSlider = false;
                if (selected != null && selected.getPainter() != null) {
                    LayerPainter selectedLayerPainter = selected.getPainter();
                    this.centerPainter = selectedLayerPainter.getCenterPointPainter();
                    enableSlider = true;
                } else {
                    this.centerPainter = null;
                }
                this.refresh();

                collisionOpacitySlider.setEnabled(enableSlider);
            }
        });
    }

    private void refresh() {
        this.updateHullLabels();
        this.repaint();
    }

    private void updateHullLabels() {
        String noInit = StringValues.NOT_INITIALIZED;
        String centerPosition = noInit;
        String collisionValue = noInit;
        if (this.centerPainter != null) {
            ShipCenterPoint center = this.centerPainter.getCenterPoint();
            if (center != null) {
                centerPosition = center.getPositionText();
                collisionValue = center.getCollisionRadius() + " " + StringValues.PIXELS;
            }
        }
        centerCoords.setText(centerPosition);
        collisionRadius.setText(collisionValue);
    }

    private JPanel createCollisionPanel() {
        JPanel hullCenterPanel = new JPanel();
        hullCenterPanel.setLayout(new BoxLayout(hullCenterPanel, BoxLayout.PAGE_AXIS));

        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            LayerPainter painter = (LayerPainter) e.getSource();
            CenterPointPainter boundsPainter = painter.getCenterPointPainter();
            PainterVisibility valueOfLayer = boundsPainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };
        JPanel visibilityWidgetContainer = ComponentUtilities.createVisibilityWidget(visibilityList,
                CenterPointPainter.class, selectionAction, "");

        hullCenterPanel.add(createCollisionOpacityPanel());

        hullCenterPanel.add(visibilityWidgetContainer);

        ComponentUtilities.addSeparatorToBoxPanel(hullCenterPanel);

        hullCenterPanel.add(createShipCenterInfo());
        hullCenterPanel.add(createCollisionInfo());

        return hullCenterPanel;
    }

    private JPanel createShipCenterInfo() {
        centerCoords = new JLabel();

        centerCoords.setToolTipText(StringValues.RIGHT_CLICK_TO_ADJUST_POSITION);
        Insets insets = ComponentUtilities.createLabelInsets();
        centerCoords.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        JPopupMenu shipCenterMenu = new JPopupMenu();
        JMenuItem adjustPosition = new JMenuItem(StringValues.ADJUST_POSITION);
        adjustPosition.addActionListener(event -> {
            ShipCenterPoint centerPoint = centerPainter.getCenterPoint();
            ComponentUtilities.showAdjustPointDialog(centerPoint);
        });
        shipCenterMenu.add(adjustPosition);
        centerCoords.addMouseListener(new MouseoverLabelListener(shipCenterMenu, centerCoords));

        JPanel panel = ComponentUtilities.createBoxLabelPanel("Center position:", centerCoords);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        return panel;
    }

    private JPanel createCollisionInfo() {
        collisionRadius = new JLabel();

        collisionRadius.setToolTipText(StringValues.RIGHT_CLICK_TO_ADJUST_VALUE);
        Insets insets = ComponentUtilities.createLabelInsets();
        collisionRadius.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        JPopupMenu collisionRadiusMenu = new JPopupMenu();
        JMenuItem adjustValue = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustValue.addActionListener(event -> {
            ShipCenterPoint centerPoint = centerPainter.getCenterPoint();
            ComponentUtilities.showAdjustCollisionDialog(centerPoint);
        });
        collisionRadiusMenu.add(adjustValue);
        collisionRadius.addMouseListener(new MouseoverLabelListener(collisionRadiusMenu, collisionRadius));

        JPanel panel = ComponentUtilities.createBoxLabelPanel("Collision radius:", collisionRadius);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));
        return panel;
    }

    private void updateCollisionOpacityLabel(int opacity) {
        collisionOpacityLabel.setText(StringValues.PAINTER_OPACITY);
        collisionOpacityLabel.setToolTipText(StringValues.CURRENT_VALUE + opacity + "%");
    }

    private JPanel createCollisionOpacityPanel() {
        JPanel container = new JPanel();

        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        ChangeListener changeListener = e -> {
            JSlider source = (JSlider)e.getSource();
            int opacity = source.getValue();
            updateCollisionOpacityLabel(opacity);
            float changedValue = opacity / 100.0f;
            EventBus.publish(new PainterOpacityChangeQueued(CenterPointPainter.class, changedValue));
        };
        BusEventListener eventListener = event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                int defaultOpacity = (int) (CenterPointPainter.COLLISION_OPACITY * 100.0f);
                if (selected == null) {
                    updateCollisionOpacityLabel(defaultOpacity);
                    collisionOpacitySlider.setValue(defaultOpacity);
                    return;
                }
                LayerPainter painter = selected.getPainter();
                int value;
                if (painter == null) {
                    value = defaultOpacity;
                } else {
                    CenterPointPainter boundsPainter = painter.getCenterPointPainter();
                    value = (int) (boundsPainter.getPaintOpacity() * 100.0f);
                }
                updateCollisionOpacityLabel(value);
                collisionOpacitySlider.setValue(value);
            }
        };
        Pair<JSlider, JLabel> widgetComponents = ComponentUtilities.createOpacityWidget(changeListener, eventListener);

        collisionOpacitySlider = widgetComponents.getFirst();
        collisionOpacityLabel = widgetComponents.getSecond();
        this.updateCollisionOpacityLabel(100);

        int sidePadding = 6;
        ComponentUtilities.layoutAsOpposites(container, collisionOpacityLabel,
                collisionOpacitySlider, sidePadding);

        return container;
    }

}
