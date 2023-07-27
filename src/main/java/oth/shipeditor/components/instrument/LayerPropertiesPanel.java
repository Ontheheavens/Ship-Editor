package oth.shipeditor.components.instrument;

import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.CenterPanelsRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerOpacityChangeQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.utility.Pair;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.components.dialog.DialogUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 11.06.2023
 */
final class LayerPropertiesPanel extends JPanel {

    private LayerPainter layerPainter;

    private JLabel opacityLabel;
    private JSlider opacitySlider;

    private JLabel layerAnchorLabel;
    private JPopupMenu anchorMenu;

    private JLabel layerRotationLabel;
    private JPopupMenu rotationMenu;

    LayerPropertiesPanel() {
        this.setLayout(new BorderLayout());
        JPanel layerSettingsPanel = this.createLayerPanel();
        this.add(layerSettingsPanel, BorderLayout.CENTER);
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer selected = checked.selected();
                boolean layerPainterPresent = selected != null && selected.getPainter() != null;
                opacitySlider.setEnabled(layerPainterPresent);
                anchorMenu.setEnabled(layerPainterPresent);
                rotationMenu.setEnabled(layerPainterPresent);
                if (selected == null) {
                    layerPainter = null;
                } else {
                    layerPainter = selected.getPainter();
                }
                this.updateAnchorLabel();
                this.updateRotationLabel();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof CenterPanelsRepaintQueued) {
                this.updateAnchorLabel();
                this.updateRotationLabel();
            }
        });
    }

    private void updateOpacityLabel(int opacity) {
        opacityLabel.setText("Sprite opacity: ");
        opacityLabel.setToolTipText(StringValues.CURRENT_VALUE + opacity + "%");
    }

    private void updateAnchorLabel() {
        String anchorPosition = StringValues.NOT_INITIALIZED;
        if (this.layerPainter != null) {
            Point2D anchor = this.layerPainter.getAnchorOffset();
            if (anchor != null) {
                Point2D coordinatesForDisplay = Utility.getPointCoordinatesForDisplay(anchor);
                anchorPosition = Utility.getPointPositionText(coordinatesForDisplay);

            }
        }
        layerAnchorLabel.setText(anchorPosition);
    }

    private void updateRotationLabel() {
        String rotationValue = StringValues.NOT_INITIALIZED;
        if (this.layerPainter != null) {
            double rotation = this.layerPainter.getRotationRadians();
                rotationValue = Utility.clampAngle(rotation) + "°";
        }
        layerRotationLabel.setText(rotationValue);
    }

    private JPanel createLayerPanel() {
        JPanel layerSettingsPanel = new JPanel();
        layerSettingsPanel.setLayout(new BoxLayout(layerSettingsPanel, BoxLayout.PAGE_AXIS));

        JPanel opacityWidget = this.createOpacityWidget();
        layerSettingsPanel.add(opacityWidget);

        layerSettingsPanel.add(createLayerAnchorInfo());
        layerSettingsPanel.add(createLayerRotationInfo());

        return layerSettingsPanel;
    }

    private JPanel createOpacityWidget() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        ChangeListener changeListener = e -> {
            JSlider source = (JSlider)e.getSource();
            int opacity = source.getValue();
            updateOpacityLabel(opacity);
            float changedValue = opacity / 100.0f;
            EventBus.publish(new LayerOpacityChangeQueued(changedValue));
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
                    value = (int) (painter.getSpriteOpacity() * 100.0f);
                }
                updateOpacityLabel(value);
                opacitySlider.setValue(value);
            }
        };
        Pair<JSlider, JLabel> widgetComponents = ComponentUtilities.createOpacityWidget(changeListener, eventListener);

        opacitySlider = widgetComponents.getFirst();
        opacityLabel = widgetComponents.getSecond();
        this.updateOpacityLabel(100);

        ComponentUtilities.layoutAsOpposites(container, opacityLabel, opacitySlider, 6);

        return container;
    }

    private JPanel createLayerAnchorInfo() {
        layerAnchorLabel = new JLabel();

        String anchorTooltip = Utility.getWithLinebreaks("SHIFT+Left-click to offset anchor",
                StringValues.RIGHT_CLICK_TO_ADJUST_VALUE);
        layerAnchorLabel.setToolTipText(anchorTooltip);
        Insets insets = ComponentUtilities.createLabelInsets();
        insets.top = 1;
        layerAnchorLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        anchorMenu = new JPopupMenu();
        JMenuItem adjustPosition = new JMenuItem(StringValues.ADJUST_POSITION);
        adjustPosition.addActionListener(event -> {
            ShipLayer activeLayer = StaticController.getActiveLayer();
            LayerPainter activeLayerPainter = activeLayer.getPainter();
            Point2D anchorPosition = activeLayerPainter.getAnchorOffset();
            DialogUtilities.showAdjustLayerAnchorDialog(activeLayerPainter, anchorPosition);
        });
        anchorMenu.add(adjustPosition);
        layerAnchorLabel.addMouseListener(new MouseoverLabelListener(anchorMenu, layerAnchorLabel));

        JPanel panel = ComponentUtilities.createBoxLabelPanel("Anchor position:", layerAnchorLabel);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        return panel;
    }

    private JPanel createLayerRotationInfo() {
        layerRotationLabel = new JLabel();

        String rotationTooltip = Utility.getWithLinebreaks("SHIFT+Right-click and drag to rotate layer",
                StringValues.RIGHT_CLICK_TO_ADJUST_VALUE);
        layerRotationLabel.setToolTipText(rotationTooltip);
        Insets insets = ComponentUtilities.createLabelInsets();
        insets.top = 1;
        layerRotationLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        rotationMenu = new JPopupMenu();
        JMenuItem adjustRotation = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustRotation.addActionListener(event -> {
            ShipLayer activeLayer = StaticController.getActiveLayer();
            LayerPainter activeLayerPainter = activeLayer.getPainter();
            double currentRotation = activeLayerPainter.getRotationRadians();
            double currentClamped = Utility.clampAngle(currentRotation);
            DialogUtilities.showAdjustLayerRotationDialog(activeLayerPainter, currentClamped);
        });
        rotationMenu.add(adjustRotation);
        JMenuItem resetRotation = new JMenuItem("Reset rotation");
        resetRotation.addActionListener(e -> {
            ShipLayer activeLayer = StaticController.getActiveLayer();
            LayerPainter activeLayerPainter = activeLayer.getPainter();
            activeLayerPainter.rotateLayer(0);
        });
        rotationMenu.add(resetRotation);
        layerRotationLabel.addMouseListener(new MouseoverLabelListener(rotationMenu, layerRotationLabel));

        JPanel panel = ComponentUtilities.createBoxLabelPanel("Layer rotation:", layerRotationLabel);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        return panel;
    }

}
