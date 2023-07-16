package oth.shipeditor.components.instrument.centers;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CentersPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerShipDataInitialized;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.CenterPointPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.utility.ApplicationDefaults;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 06.06.2023
 */
@Log4j2
public final class HullPointsPanel extends JPanel {

    @Getter
    private static CenterPointMode mode = CenterPointMode.COLLISION;

    /**
     * Reference to the hull center painter of the currently active layer.
     */
    private CenterPointPainter centersPainter;

    private JLabel centerCoords;

    private JLabel collisionRadius;

    private JLabel collisionOpacityLabel;
    private JSlider collisionOpacitySlider;

    private JRadioButton collisionModeButton;
    private JRadioButton shieldModeButton;

    public HullPointsPanel() {
        this.setBorder(new EmptyBorder(0, 6, 4, 6));
        LayoutManager layout = new GridLayout(2, 1);
        this.setLayout(layout);

        JPanel hullCenterPanel = createCollisionPanel();
        this.add(hullCenterPanel);
        JPanel shieldCenterPanel = createShieldPanel();
        this.add(shieldCenterPanel);

        ButtonGroup group = new ButtonGroup();
        group.add(collisionModeButton);
        group.add(shieldModeButton);
        collisionModeButton.setSelected(true);

        this.initLayerListeners();
        this.initPointListener();
    }

    private static void setMode(CenterPointMode inputMode) {
        HullPointsPanel.mode = inputMode;
        EventBus.publish(new ViewerRepaintQueued());
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof CentersPanelRepaintQueued) {
                this.refresh();
            }
        });
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerShipDataInitialized checked) {
                LayerPainter selectedLayerPainter = checked.source();
                this.centersPainter = selectedLayerPainter.getCenterPointPainter();
                this.refresh();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                boolean enableSlider = false;
                if (selected != null && selected.getPainter() != null) {
                    LayerPainter selectedLayerPainter = selected.getPainter();
                    this.centersPainter = selectedLayerPainter.getCenterPointPainter();
                    enableSlider = true;
                } else {
                    this.centersPainter = null;
                }
                this.refresh();

                collisionOpacitySlider.setEnabled(enableSlider);
            }
        });
    }

    private void refresh() {
        this.updateLabels();
        this.repaint();
    }

    private void updateLabels() {
        String centerPosition = "Center not initialized";
        String collisionValue = "Collision not initialized";
        if (this.centersPainter != null) {
            ShipCenterPoint center = this.centersPainter.getCenterPoint();
            if (center != null) {
                Point2D position = center.getCoordinatesForDisplay();
                centerPosition = position.toString();
                collisionValue = String.valueOf(center.getCollisionRadius());
            }
        }
        centerCoords.setText(centerPosition);
        collisionRadius.setText(collisionValue);
    }

    private JPanel createCollisionPanel() {
        JPanel hullCenterPanel = new JPanel();
        hullCenterPanel.setLayout(new BoxLayout(hullCenterPanel, BoxLayout.PAGE_AXIS));

        JPanel visibilityWidgetContainer = new JPanel();
        visibilityWidgetContainer.setLayout(new BoxLayout(visibilityWidgetContainer, BoxLayout.LINE_AXIS));

        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        visibilityList.setRenderer(PainterVisibility.createCellRenderer());
        visibilityList.addActionListener(PainterVisibility.createActionListener(visibilityList,
                CenterPointPainter.class));
        ActionListener selectionAction = e -> {
            LayerPainter painter = (LayerPainter) e.getSource();
            CenterPointPainter boundsPainter = painter.getCenterPointPainter();
            PainterVisibility valueOfLayer = boundsPainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };
        EventBus.subscribe(PainterVisibility.createBusEventListener(visibilityList, selectionAction));

        visibilityList.setMaximumSize(visibilityList.getPreferredSize());

        JLabel visibilityWidgetLabel = new JLabel("Collision visibility:");
        visibilityWidgetLabel.setToolTipText(StringConstants.TOGGLED_ON_PER_LAYER_BASIS);

        int sidePadding = 6;
        visibilityWidgetContainer.add(Box.createRigidArea(new Dimension(sidePadding,0)));
        visibilityWidgetContainer.add(visibilityWidgetLabel);
        visibilityWidgetContainer.add(Box.createHorizontalGlue()); // Add glue to push components to opposite sides.
        visibilityWidgetContainer.add(visibilityList);
        visibilityWidgetContainer.add(Box.createRigidArea(new Dimension(sidePadding,0)));

        collisionModeButton = new JRadioButton("Edit collision");
        collisionModeButton.addActionListener(e -> HullPointsPanel.setMode(CenterPointMode.COLLISION));
        hullCenterPanel.add(collisionModeButton);

        hullCenterPanel.add(createCollisionOpacityPanel());

        hullCenterPanel.add(visibilityWidgetContainer);

        centerCoords = new JLabel();
        collisionRadius = new JLabel();

//        hullCenterPanel.add(centerCoords);
//        hullCenterPanel.add(collisionRadius);

        // Collision panel needs to have a radio button that enables interaction with center point, which is otherwise locked.

        // TODO: Also - gotta beautify this, big time!
        hullCenterPanel.setBorder(BorderFactory.createTitledBorder("Collision"));

        return hullCenterPanel;
    }

    private void updateCollisionOpacityLabel(int opacity) {
        collisionOpacityLabel.setText("Collision opacity: " + opacity + "%");
    }

    @SuppressWarnings("DuplicatedCode")
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
                int defaultOpacity = (int) (ApplicationDefaults.COLLISION_OPACITY * 100.0f);
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
        Pair<JSlider, JLabel> widgetComponents = Utility.createOpacityWidget(changeListener, eventListener);

        collisionOpacitySlider = widgetComponents.getFirst();
        collisionOpacityLabel = widgetComponents.getSecond();
        this.updateCollisionOpacityLabel(100);

        int sidePadding = 6;

        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));
        container.add(collisionOpacityLabel);
        container.add(Box.createHorizontalGlue());
        container.add(collisionOpacitySlider);
        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));

        return container;
    }

    private JPanel createShieldPanel() {
        JPanel shieldPanel = new JPanel();
        shieldPanel.setLayout(new BoxLayout(shieldPanel, BoxLayout.PAGE_AXIS));

        shieldModeButton = new JRadioButton("Edit shield");
        shieldModeButton.addActionListener(e -> HullPointsPanel.setMode(CenterPointMode.SHIELD));
        shieldPanel.add(shieldModeButton);

        shieldPanel.setBorder(BorderFactory.createTitledBorder("Shield"));
        return shieldPanel;
    }

}
