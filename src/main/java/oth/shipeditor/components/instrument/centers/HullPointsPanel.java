package oth.shipeditor.components.instrument.centers;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CentersPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.CenterPointPainter;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.ShieldPointPainter;
import oth.shipeditor.utility.ApplicationDefaults;
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
 * @since 06.06.2023
 */
@SuppressWarnings({"DuplicatedCode", "ClassWithTooManyFields"})
@Log4j2
public final class HullPointsPanel extends JPanel {

    private static final String PIXELS = "pixels";
    @Getter
    private static CenterPointMode mode = CenterPointMode.COLLISION;

    /**
     * Reference to the hull center painter of the currently active layer.
     */
    private CenterPointPainter centerPainter;

    private ShieldPointPainter shieldPainter;

    private JLabel centerCoords;
    private JLabel collisionRadius;

    private JLabel shieldCenterCoords;
    private JLabel shieldRadiusLabel;

    private JLabel collisionOpacityLabel;
    private JSlider collisionOpacitySlider;
    private JRadioButton collisionModeButton;

    private JLabel shieldOpacityLabel;
    private JSlider shieldOpacitySlider;
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
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                boolean enableSlider = false;
                if (selected != null && selected.getPainter() != null) {
                    LayerPainter selectedLayerPainter = selected.getPainter();
                    this.centerPainter = selectedLayerPainter.getCenterPointPainter();
                    this.shieldPainter = selectedLayerPainter.getShieldPointPainter();
                    enableSlider = true;
                } else {
                    this.centerPainter = null;
                    this.shieldPainter = null;
                }
                this.refresh();

                collisionOpacitySlider.setEnabled(enableSlider);
                shieldOpacitySlider.setEnabled(enableSlider);
            }
        });
    }

    private void refresh() {
        this.updateHullLabels();
        this.updateShieldLabels();
        this.repaint();
    }

    private void updateHullLabels() {
        String noInit = StringConstants.NOT_INITIALIZED;
        String centerPosition = noInit;
        String collisionValue = noInit;
        if (this.centerPainter != null) {
            ShipCenterPoint center = this.centerPainter.getCenterPoint();
            if (center != null) {
                centerPosition = center.getPositionText();
                collisionValue = center.getCollisionRadius() + " " + PIXELS;
            }
        }
        centerCoords.setText(centerPosition);
        collisionRadius.setText(collisionValue);
    }

    private void updateShieldLabels() {
        String noInit = StringConstants.NOT_INITIALIZED;
        String shieldPosition = noInit;
        String shieldRadius = noInit;
        if (this.centerPainter != null) {
            ShieldCenterPoint center = this.shieldPainter.getShieldCenterPoint();
            if (center != null) {
                shieldPosition = center.getPositionText();
                shieldRadius = center.getShieldRadius() + " " + PIXELS;
            }
        }
        shieldCenterCoords.setText(shieldPosition);
        shieldRadiusLabel.setText(shieldRadius);
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
        JPanel visibilityWidgetContainer = HullPointsPanel.createVisibilityWidget(visibilityList,
                CenterPointPainter.class, selectionAction, "Collision visibility:");

        collisionModeButton = new JRadioButton("Edit hull collision parameters");
        collisionModeButton.addActionListener(e -> HullPointsPanel.setMode(CenterPointMode.COLLISION));

        hullCenterPanel.add(HullPointsPanel.createToggleEditPanel(collisionModeButton));

        ComponentUtilities.addSeparatorToBoxPanel(hullCenterPanel);

        hullCenterPanel.add(createCollisionOpacityPanel());

        hullCenterPanel.add(visibilityWidgetContainer);

        ComponentUtilities.addSeparatorToBoxPanel(hullCenterPanel);

        hullCenterPanel.add(createShipCenterInfo());
        hullCenterPanel.add(createCollisionInfo());

        hullCenterPanel.setBorder(BorderFactory.createTitledBorder("Collision"));

        return hullCenterPanel;
    }

    private JPanel createShipCenterInfo() {
        centerCoords = new JLabel();
        JPanel panel = ComponentUtilities.createBoxLabelPanel("Ship center position:", centerCoords);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        return panel;
    }

    private JPanel createCollisionInfo() {
        collisionRadius = new JLabel();
        JPanel panel = ComponentUtilities.createBoxLabelPanel("Collision radius:", collisionRadius);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));
        return panel;
    }

    private JPanel createShieldCenterInfo() {
        shieldCenterCoords = new JLabel();
        JPanel panel = ComponentUtilities.createBoxLabelPanel("Shield center position:", shieldCenterCoords);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        return panel;
    }

    private JPanel createShieldRadiusInfo() {
        shieldRadiusLabel = new JLabel();
        JPanel panel = ComponentUtilities.createBoxLabelPanel("Shield radius:", shieldRadiusLabel);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));
        return panel;
    }

    private static JPanel createVisibilityWidget(JComboBox<PainterVisibility> visibilityList,
                                                 Class<? extends AbstractPointPainter> painterClass,
                                                 ActionListener selectionAction, String labelName) {
        JPanel widgetPanel = new JPanel();
        widgetPanel.setLayout(new BoxLayout(widgetPanel, BoxLayout.LINE_AXIS));

        visibilityList.setRenderer(PainterVisibility.createCellRenderer());
        visibilityList.addActionListener(PainterVisibility.createActionListener(visibilityList, painterClass));
        EventBus.subscribe(PainterVisibility.createBusEventListener(visibilityList, selectionAction));

        visibilityList.setMaximumSize(visibilityList.getPreferredSize());

        JLabel visibilityWidgetLabel = new JLabel(labelName);
        visibilityWidgetLabel.setToolTipText(StringConstants.TOGGLED_ON_PER_LAYER_BASIS);
        widgetPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        int sidePadding = 6;
        ComponentUtilities.layoutAsOpposites(widgetPanel, visibilityWidgetLabel,
                visibilityList, sidePadding);

        return widgetPanel;
    }

    private static JPanel createToggleEditPanel(JRadioButton modeToggleButton) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        int sidePadding = 6;
        modeToggleButton.setBorder(new EmptyBorder(4, 0, 8,0));

        container.add(Box.createRigidArea(new Dimension(sidePadding,0)));
        container.add(modeToggleButton);
        container.add(Box.createHorizontalGlue());

        return container;
    }

    private void updateCollisionOpacityLabel(int opacity) {
        collisionOpacityLabel.setText("Collision opacity: " + opacity + "%");
    }

    private void updateShieldOpacityLabel(int opacity) {
        shieldOpacityLabel.setText("Shield opacity: " + opacity + "%");
    }

    private JPanel createCollisionOpacityPanel() {
        JPanel container = new JPanel();

        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        container.setBorder(new EmptyBorder(4, 0, 0, 0));

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
        Pair<JSlider, JLabel> widgetComponents = ComponentUtilities.createOpacityWidget(changeListener, eventListener);

        collisionOpacitySlider = widgetComponents.getFirst();
        collisionOpacityLabel = widgetComponents.getSecond();
        this.updateCollisionOpacityLabel(100);

        int sidePadding = 6;
        ComponentUtilities.layoutAsOpposites(container, collisionOpacityLabel,
                collisionOpacitySlider, sidePadding);

        return container;
    }


    private JPanel createShieldOpacityPanel() {
        JPanel container = new JPanel();

        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        container.setBorder(new EmptyBorder(4, 0, 0, 0));

        ChangeListener changeListener = e -> {
            JSlider source = (JSlider)e.getSource();
            int opacity = source.getValue();
            updateShieldOpacityLabel(opacity);
            float changedValue = opacity / 100.0f;
            EventBus.publish(new PainterOpacityChangeQueued(ShieldPointPainter.class, changedValue));
        };
        BusEventListener eventListener = event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                int defaultOpacity = (int) (ApplicationDefaults.DEFAULT_SHIELD_OPACITY * 100.0f);
                if (selected == null) {
                    updateShieldOpacityLabel(defaultOpacity);
                    shieldOpacitySlider.setValue(defaultOpacity);
                    return;
                }
                LayerPainter painter = selected.getPainter();
                int value;
                if (painter == null) {
                    value = defaultOpacity;
                } else {
                    ShieldPointPainter shieldPointPainter = painter.getShieldPointPainter();
                    value = (int) (shieldPointPainter.getPaintOpacity() * 100.0f);
                }
                updateShieldOpacityLabel(value);
                shieldOpacitySlider.setValue(value);
            }
        };
        Pair<JSlider, JLabel> widgetComponents = ComponentUtilities.createOpacityWidget(changeListener, eventListener);

        shieldOpacitySlider = widgetComponents.getFirst();
        shieldOpacityLabel = widgetComponents.getSecond();
        this.updateShieldOpacityLabel(100);

        int sidePadding = 6;
        ComponentUtilities.layoutAsOpposites(container, shieldOpacityLabel,
                shieldOpacitySlider, sidePadding);

        return container;
    }

    private JPanel createShieldPanel() {
        JPanel shieldPanel = new JPanel();
        shieldPanel.setLayout(new BoxLayout(shieldPanel, BoxLayout.PAGE_AXIS));

        shieldModeButton = new JRadioButton("Edit hull shield parameters");
        shieldModeButton.addActionListener(e -> HullPointsPanel.setMode(CenterPointMode.SHIELD));

        shieldPanel.add(HullPointsPanel.createToggleEditPanel(shieldModeButton));

        ComponentUtilities.addSeparatorToBoxPanel(shieldPanel);

        shieldPanel.add(createShieldOpacityPanel());

        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            LayerPainter painter = (LayerPainter) e.getSource();
            ShieldPointPainter shieldPointPainter = painter.getShieldPointPainter();
            PainterVisibility valueOfLayer = shieldPointPainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };
        JPanel visibilityWidgetContainer = HullPointsPanel.createVisibilityWidget(visibilityList,
                ShieldPointPainter.class, selectionAction, "Shield visibility:");

        shieldPanel.add(visibilityWidgetContainer);

        ComponentUtilities.addSeparatorToBoxPanel(shieldPanel);

        shieldPanel.add(createShieldCenterInfo());
        shieldPanel.add(createShieldRadiusInfo());

        shieldPanel.setBorder(BorderFactory.createTitledBorder("Shield"));
        return shieldPanel;
    }

}
