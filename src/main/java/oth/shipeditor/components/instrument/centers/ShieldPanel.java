package oth.shipeditor.components.instrument.centers;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ShieldPointPainter;
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
 * @since 25.07.2023
 */
@Log4j2
public class ShieldPanel extends JPanel {

    private static final float DEFAULT_SHIELD_OPACITY = 0.2f;
    private ShieldPointPainter shieldPainter;

    private JLabel shieldCenterCoords;
    private JLabel shieldRadiusLabel;

    private JLabel shieldOpacityLabel;
    private JSlider shieldOpacitySlider;

    public ShieldPanel() {
        LayoutManager layout = new BorderLayout();
        this.setLayout(layout);

        JPanel shieldCenterPanel = createShieldPanel();
        this.add(shieldCenterPanel, BorderLayout.CENTER);

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
                    this.shieldPainter = selectedLayerPainter.getShieldPointPainter();
                    enableSlider = true;
                } else {
                    this.shieldPainter = null;
                }
                this.refresh();
                shieldOpacitySlider.setEnabled(enableSlider);
            }
        });
    }

    private void refresh() {
        this.updateShieldLabels();
        this.repaint();
    }

    private void updateShieldLabels() {
        String noInit = StringValues.NOT_INITIALIZED;
        String shieldPosition = noInit;
        String shieldRadius = noInit;
        if (this.shieldPainter != null) {
            ShieldCenterPoint center = this.shieldPainter.getShieldCenterPoint();
            if (center != null) {
                shieldPosition = center.getPositionText();
                shieldRadius = center.getShieldRadius() + " " + StringValues.PIXELS;
            }
        }
        shieldCenterCoords.setText(shieldPosition);
        shieldRadiusLabel.setText(shieldRadius);
    }

    private void updateShieldOpacityLabel(int opacity) {
        shieldOpacityLabel.setText(StringValues.PAINTER_OPACITY);
        shieldOpacityLabel.setToolTipText(StringValues.CURRENT_VALUE + opacity + "%");
    }

    private JPanel createShieldCenterInfo() {
        shieldCenterCoords = new JLabel();

        shieldCenterCoords.setToolTipText(StringValues.RIGHT_CLICK_TO_ADJUST_POSITION);
        Insets insets = ComponentUtilities.createLabelInsets();
        shieldCenterCoords.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        JPopupMenu shieldCenterMenu = new JPopupMenu();
        JMenuItem adjustPosition = new JMenuItem(StringValues.ADJUST_POSITION);
        adjustPosition.addActionListener(event -> {
            ShieldCenterPoint shieldPoint = shieldPainter.getShieldCenterPoint();
            ComponentUtilities.showAdjustPointDialog(shieldPoint);
        });
        shieldCenterMenu.add(adjustPosition);
        shieldCenterCoords.addMouseListener(new MouseoverLabelListener(shieldCenterMenu, shieldCenterCoords));

        JPanel panel = ComponentUtilities.createBoxLabelPanel("Shield position:", shieldCenterCoords);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        return panel;
    }

    private JPanel createShieldRadiusInfo() {
        shieldRadiusLabel = new JLabel();

        shieldRadiusLabel.setToolTipText(StringValues.RIGHT_CLICK_TO_ADJUST_VALUE);
        Insets insets = ComponentUtilities.createLabelInsets();
        shieldRadiusLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        JPopupMenu shieldRadiusMenu = new JPopupMenu();
        JMenuItem adjustValue = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustValue.addActionListener(event -> {
            ShieldCenterPoint shieldPoint = shieldPainter.getShieldCenterPoint();
            ComponentUtilities.showAdjustShieldRadiusDialog(shieldPoint);
        });
        shieldRadiusMenu.add(adjustValue);
        shieldRadiusLabel.addMouseListener(new MouseoverLabelListener(shieldRadiusMenu, shieldRadiusLabel));

        JPanel panel = ComponentUtilities.createBoxLabelPanel("Shield radius:", shieldRadiusLabel);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));
        return panel;
    }

    private JPanel createShieldOpacityPanel() {
        JPanel container = new JPanel();

        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

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
                int defaultOpacity = (int) (DEFAULT_SHIELD_OPACITY * 100.0f);
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

        shieldPanel.add(createShieldOpacityPanel());

        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        ActionListener selectionAction = e -> {
            LayerPainter painter = (LayerPainter) e.getSource();
            ShieldPointPainter shieldPointPainter = painter.getShieldPointPainter();
            PainterVisibility valueOfLayer = shieldPointPainter.getVisibilityMode();
            visibilityList.setSelectedItem(valueOfLayer);
        };
        JPanel visibilityWidgetContainer = ComponentUtilities.createVisibilityWidget(visibilityList,
                ShieldPointPainter.class, selectionAction, "");

        shieldPanel.add(visibilityWidgetContainer);

        ComponentUtilities.addSeparatorToBoxPanel(shieldPanel);

        shieldPanel.add(createShieldCenterInfo());
        shieldPanel.add(createShieldRadiusInfo());

        return shieldPanel;
    }

}
