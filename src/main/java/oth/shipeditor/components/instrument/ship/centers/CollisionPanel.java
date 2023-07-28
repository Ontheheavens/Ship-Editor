package oth.shipeditor.components.instrument.ship.centers;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.CenterPointPainter;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.components.dialog.DialogUtilities;
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

    private JPopupMenu shipCenterMenu;
    private JPopupMenu collisionRadiusMenu;

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
                ViewerLayer selected = checked.selected();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    this.centerPainter = null;
                    this.refresh();
                    return;
                }
                boolean enableSlider = false;
                if (checkedLayer.getPainter() != null) {
                    ShipPainter selectedShipPainter = checkedLayer.getPainter();
                    this.centerPainter = selectedShipPainter.getCenterPointPainter();
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
        shipCenterMenu.setEnabled(false);
        collisionRadiusMenu.setEnabled(false);
        collisionOpacitySlider.setEnabled(false);
        Color labelColor = Color.GRAY;
        if (this.centerPainter != null) {
            ShipCenterPoint center = this.centerPainter.getCenterPoint();
            if (center != null) {
                centerPosition = center.getPositionText();
                collisionValue = Utility.round(center.getCollisionRadius(), 5) + " " + StringValues.PIXELS;
                shipCenterMenu.setEnabled(true);
                collisionRadiusMenu.setEnabled(true);
                collisionOpacitySlider.setEnabled(true);
                labelColor = Color.BLACK;
            }
        }
        centerCoords.setText(centerPosition);
        centerCoords.setForeground(labelColor);
        collisionRadius.setText(collisionValue);
        collisionRadius.setForeground(labelColor);
    }

    private JPanel createCollisionPanel() {
        JPanel hullCenterPanel = new JPanel();
        hullCenterPanel.setLayout(new BoxLayout(hullCenterPanel, BoxLayout.PAGE_AXIS));

        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            if (!(e.getSource() instanceof ShipPainter checked)) return;
            CenterPointPainter boundsPainter = checked.getCenterPointPainter();
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
        insets.top = 1;
        centerCoords.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        shipCenterMenu = new JPopupMenu();
        JMenuItem adjustPosition = new JMenuItem(StringValues.ADJUST_POSITION);
        adjustPosition.addActionListener(event -> {
            ShipCenterPoint centerPoint = centerPainter.getCenterPoint();
            DialogUtilities.showAdjustPointDialog(centerPoint);
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
        insets.top = 1;
        collisionRadius.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        collisionRadiusMenu = new JPopupMenu();
        JMenuItem adjustValue = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustValue.addActionListener(event -> {
            ShipCenterPoint centerPoint = centerPainter.getCenterPoint();
            DialogUtilities.showAdjustCollisionDialog(centerPoint);
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
                ViewerLayer selected = checked.selected();
                int defaultOpacity = (int) (CenterPointPainter.COLLISION_OPACITY * 100.0f);
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    updateCollisionOpacityLabel(defaultOpacity);
                    collisionOpacitySlider.setValue(defaultOpacity);
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                int value;
                if (painter == null) {
                    value = defaultOpacity;
                } else {
                    CenterPointPainter centerPointPainter = painter.getCenterPointPainter();
                    value = (int) (centerPointPainter.getPaintOpacity() * 100.0f);
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
