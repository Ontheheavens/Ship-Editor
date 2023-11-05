package oth.shipeditor.components.instrument.ship.centers;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterOpacityChangeQueued;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.ShieldPointPainter;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.components.dialog.DialogUtilities;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

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

    private JPopupMenu shieldCenterMenu;
    private JPopupMenu shieldRadiusMenu;

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
                ViewerLayer selected = checked.selected();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    this.shieldPainter = null;
                    this.refresh();
                    return;
                }
                boolean enableSlider = false;
                if (checkedLayer.getPainter() != null) {
                    ShipPainter selectedShipPainter = checkedLayer.getPainter();
                    this.shieldPainter = selectedShipPainter.getShieldPointPainter();
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
        shieldCenterMenu.setEnabled(false);
        shieldRadiusMenu.setEnabled(false);
        shieldOpacitySlider.setEnabled(false);
        Color labelColor = Themes.getDisabledTextColor();
        if (this.shieldPainter != null) {
            ShieldCenterPoint center = this.shieldPainter.getShieldCenterPoint();
            if (center != null) {
                shieldPosition = center.getPositionText();
                shieldRadius = Utility.round(center.getShieldRadius(), 5) + " " + StringValues.PIXELS;
                shieldCenterMenu.setEnabled(true);
                shieldRadiusMenu.setEnabled(true);
                shieldOpacitySlider.setEnabled(true);
                labelColor = Themes.getTextColor();
            }
        }
        shieldCenterCoords.setText(shieldPosition);
        shieldCenterCoords.setForeground(labelColor);
        shieldRadiusLabel.setText(shieldRadius);
        shieldRadiusLabel.setForeground(labelColor);
    }

    private void updateShieldOpacityLabel(int opacity) {
        shieldOpacityLabel.setText(StringValues.PAINTER_OPACITY);
        shieldOpacityLabel.setToolTipText(StringValues.CURRENT_VALUE + opacity + "%");
    }

    private JPanel createShieldCenterInfo() {
        shieldCenterCoords = new JLabel();

        shieldCenterCoords.setToolTipText(StringValues.RIGHT_CLICK_TO_ADJUST_POSITION);
        Insets insets = ComponentUtilities.createLabelInsets();
        insets.top = 1;
        shieldCenterCoords.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        shieldCenterMenu = new JPopupMenu();
        JMenuItem adjustPosition = new JMenuItem(StringValues.ADJUST_POSITION);
        adjustPosition.addActionListener(event -> {
            ShieldCenterPoint shieldPoint = shieldPainter.getShieldCenterPoint();
            DialogUtilities.showAdjustPointDialog(shieldPoint);
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
        insets.top = 1;
        shieldRadiusLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        shieldRadiusMenu = new JPopupMenu();
        JMenuItem adjustValue = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustValue.addActionListener(event -> {
            ShieldCenterPoint shieldPoint = shieldPainter.getShieldCenterPoint();
            DialogUtilities.showAdjustShieldRadiusDialog(shieldPoint);
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
                ViewerLayer selected = checked.selected();
                int defaultOpacity = (int) (DEFAULT_SHIELD_OPACITY * 100.0f);
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    updateShieldOpacityLabel(defaultOpacity);
                    shieldOpacitySlider.setValue(defaultOpacity);
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                int value;
                if (painter == null || painter.isUninitialized()) {
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
            if (!(e.getSource() instanceof ShipPainter checked)) return;
            ShieldPointPainter shieldPointPainter = checked.getShieldPointPainter();
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
