package oth.shipeditor.components;

import com.formdev.flatlaf.ui.FlatLineBorder;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.control.*;
import oth.shipeditor.communication.events.viewer.layers.LayerSpriteLoadConfirmed;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.status.CoordsModeChanged;
import oth.shipeditor.components.viewer.LayerViewer;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.components.widgets.IncrementType;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
@Log4j2
final class ViewerStatusPanel extends JPanel {

    private final LayerViewer viewer;

    private JLabel dimensions;

    private JLabel cursorCoords;

    private SpinnerNumberModel zoomModel;

    private SpinnerNumberModel rotationModel;

    private JPanel leftsideContainer;

    private boolean widgetsAcceptChange;

    ViewerStatusPanel(LayerViewer viewable) {
        this.setLayout(new BorderLayout());

        this.viewer = viewable;
        this.leftsideContainer = createLeftsidePanel();

        this.initListeners();
        this.setDimensionsLabel(null);
        this.setZoomLevel(StaticController.getZoomLevel());
        this.setRotationDegrees(StaticController.getRotationDegrees());
        this.updateCursorCoordsLabel();

        this.add(leftsideContainer, BorderLayout.LINE_START);

        JPanel rightPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.weightx = 1;
        gbcRight.ipadx = 80;
        gbcRight.insets = new Insets(0, 0, 0, 10);
        gbcRight.anchor = GridBagConstraints.LINE_END;

        ProgressBarPanel progressBarContainer = new ProgressBarPanel();
        rightPanel.add(progressBarContainer, gbcRight);

        gbcRight.gridx = 1;
        gbcRight.weightx = 0;
        gbcRight.ipadx = 0;

        rightPanel.add(ViewerStatusPanel.createMirrorModePanel(), gbcRight);

        this.add(rightPanel, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Themes.getBorderColor()));
    }

    private static JPanel createMirrorModePanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        double linkageSpinnerMax = 5.0d;
        double linkageSpinnerMin = 0.0d;

        SpinnerNumberModel linkageSpinnerModel = new SpinnerNumberModel(1.0d,
                linkageSpinnerMin, linkageSpinnerMax, 1.0d);
        JSpinner linkageSpinner = Spinners.createWheelable(linkageSpinnerModel, IncrementType.UNARY);
        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) linkageSpinner.getEditor();
        JFormattedTextField textField = spinnerEditor.getTextField();
        textField.setEditable(true);
        textField.setColumns(1);

        JLabel toleranceLabel = new JLabel("Distance:");
        toleranceLabel.setToolTipText("Determines maximum distance at which mirrored points link for interaction");

        JCheckBox mirrorModeCheckbox = new JCheckBox("Mirroring");
        mirrorModeCheckbox.addItemListener(e -> {
            boolean mirrorModeOn = mirrorModeCheckbox.isSelected();
            EventBus.publish(new MirrorModeChange(mirrorModeOn));
        });
        mirrorModeCheckbox.setSelected(true);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            int keyCode = ke.getKeyCode();
            if (ke.getID() == KeyEvent.KEY_RELEASED && keyCode == KeyEvent.VK_SPACE) {
                mirrorModeCheckbox.setSelected(!mirrorModeCheckbox.isSelected());
            }
            return false;
        });
        mirrorModeCheckbox.setMnemonic(KeyEvent.VK_SPACE);
        mirrorModeCheckbox.setToolTipText("Spacebar to toggle");

        int margin = 6;

        container.add(mirrorModeCheckbox);
        container.add(Box.createRigidArea(new Dimension(margin,0)));
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 24));
        separator.setForeground(Color.GRAY);
        container.add(separator);
        container.add(Box.createRigidArea(new Dimension(margin,0)));
        container.add(toleranceLabel);
        container.add(Box.createRigidArea(new Dimension(margin,0)));
        container.add(linkageSpinner);
        container.add(Box.createRigidArea(new Dimension(margin,0)));

        linkageSpinner.addChangeListener(e -> {
            double current = (Double) linkageSpinnerModel.getValue();
            EventBus.publish(new PointLinkageToleranceChanged((int) current));
        });
        linkageSpinner.setValue(5.0d);

        return container;
    }

    private JPanel createLeftsidePanel() {
        leftsideContainer = new JPanel();

        FontIcon dimensionIcon = FontIcon.of(FluentUiRegularMZ.SLIDE_SIZE_24, 20, Themes.getIconColor());
        dimensions = new JLabel("", dimensionIcon, SwingConstants.TRAILING);
        dimensions.setToolTipText("Width / height of active layer");
        leftsideContainer.add(dimensions);

        this.addSeparator();

        FontIcon mouseIcon = FontIcon.of(FluentUiRegularAL.CURSOR_HOVER_20, 20, Themes.getIconColor());
        cursorCoords = new JLabel("", mouseIcon, SwingConstants.TRAILING);
        Insets coordsInsets = new Insets(2, 6, 2, 7);
        cursorCoords.setBorder(new FlatLineBorder(coordsInsets, Themes.getBorderColor()));
        cursorCoords.setToolTipText("Right-click to change coordinate system");
        JPopupMenu coordsMenu = this.createCoordsMenu();
        cursorCoords.addMouseListener(new MouseoverLabelListener(coordsMenu,
                cursorCoords));
        leftsideContainer.add(cursorCoords);

        this.addSeparator();

        this.addZoomWidget();

        this.addSeparator();

        FontIcon rotationIcon = FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 20, Themes.getIconColor());
        JLabel rotationLabel = new JLabel("", rotationIcon, SwingConstants.TRAILING);

        double minimum = 0.0d;
        double maximum = 360.0d;
        double initial = 0.0d;
        rotationModel = new SpinnerNumberModel(initial, minimum, maximum, 1.0d);
        JSpinner rotationSpinner = createRotationSpinner();

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(rotationSpinner,"0°");
        rotationSpinner.setEditor(editor);

        JPopupMenu rotationResetMenu = new JPopupMenu();
        JMenuItem resetRotation = new JMenuItem("Reset rotation degrees");
        resetRotation.addActionListener(e -> rotationSpinner.setValue(initial));
        rotationResetMenu.add(resetRotation);

        JFormattedTextField formattedField = editor.getTextField();
        formattedField.setColumns(2);

        MouseListener labelResetListener = new ResetMenuListener(rotationResetMenu, rotationLabel);
        rotationLabel.addMouseListener(labelResetListener);
        MouseListener spinnerResetListener = new ResetMenuListener(rotationResetMenu, formattedField);
        formattedField.addMouseListener(spinnerResetListener);

        String rotationTooltip = Utility.getWithLinebreaks("CTRL+Mousewheel to rotate viewer",
                StringValues.RIGHT_CLICK_TO_RESET_VALUE);
        rotationLabel.setToolTipText(rotationTooltip);

        leftsideContainer.add(rotationLabel);
        leftsideContainer.add(rotationSpinner);

        return leftsideContainer;
    }

    private JSpinner createRotationSpinner() {
        JSpinner rotationSpinner = Spinners.createWheelable(rotationModel, IncrementType.UNARY);
        rotationSpinner.addChangeListener(e -> {
            if (widgetsAcceptChange) {
                Number modelNumber = rotationModel.getNumber();
                double currentValue = modelNumber.doubleValue();

                PrimaryViewer primaryViewer = StaticController.getViewer();
                ViewerControl viewerControls = primaryViewer.getViewerControls();
                viewerControls.rotateExact(currentValue);
            }
        });
        return rotationSpinner;
    }

    private void addZoomWidget() {
        FontIcon zoomIcon = FontIcon.of(FluentUiRegularMZ.ZOOM_IN_20, 20, Themes.getIconColor());
        JLabel zoomLabel = new JLabel("", zoomIcon, SwingConstants.TRAILING);
        String zoomTooltip = Utility.getWithLinebreaks("Mousewheel to zoom viewer",
                StringValues.RIGHT_CLICK_TO_RESET_VALUE);
        zoomLabel.setToolTipText(zoomTooltip);

        double minimum = ControlPredicates.MINIMUM_ZOOM;
        double maximum = ControlPredicates.MAXIMUM_ZOOM;
        double initial = 1.0d;
        zoomModel = new SpinnerNumberModel(initial, minimum, maximum, 0.1);
        JSpinner zoomSpinner = createZoomSpinner();

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(zoomSpinner,"0%");
        zoomSpinner.setEditor(editor);

        zoomSpinner.addMouseWheelListener(e -> {
            if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                return;
            }
            double value = (Double) zoomSpinner.getValue();
            double newValue = value * Math.pow(1 + (ControlPredicates.ZOOMING_SPEED * 0.25d), -e.getUnitsToScroll());
            newValue = Math.min(maximum, Math.max(minimum, newValue));
            zoomSpinner.setValue(newValue);
        });

        JPopupMenu zoomResetMenu = new JPopupMenu();
        JMenuItem resetZoom = new JMenuItem("Reset zoom level");
        resetZoom.addActionListener(e -> zoomSpinner.setValue(initial));
        zoomResetMenu.add(resetZoom);

        JFormattedTextField formattedField = editor.getTextField();
        formattedField.setColumns(4);
        MouseListener labelResetListener = new ResetMenuListener(zoomResetMenu, zoomLabel);
        zoomLabel.addMouseListener(labelResetListener);
        MouseListener spinnerResetListener = new ResetMenuListener(zoomResetMenu, formattedField);
        formattedField.addMouseListener(spinnerResetListener);

        leftsideContainer.add(zoomLabel);
        leftsideContainer.add(zoomSpinner);
    }

    private JSpinner createZoomSpinner() {
        JSpinner zoomSpinner = new JSpinner(zoomModel);
        zoomSpinner.addChangeListener(e -> {
            if (widgetsAcceptChange) {
                Number modelNumber = zoomModel.getNumber();
                double currentValue = modelNumber.doubleValue();

                PrimaryViewer primaryViewer = StaticController.getViewer();
                ViewerControl viewerControls = primaryViewer.getViewerControls();
                viewerControls.setZoomExact(currentValue);
            }
        });
        return zoomSpinner;
    }

    private void addSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 24));
        separator.setForeground(Color.GRAY);
        leftsideContainer.add(separator);
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerZoomChanged) {
                this.setZoomLevel(StaticController.getZoomLevel());
            } else if (event instanceof ViewerTransformRotated) {
                this.setRotationDegrees(StaticController.getRotationDegrees());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved) {
                this.updateCursorCoordsLabel();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerSpriteLoadConfirmed checked) {
                Sprite sprite = checked.sprite();
                this.setDimensionsLabel(sprite.getImage());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer layer = checked.selected();
                if (layer == null) return;
                LayerPainter layerPainter = layer.getPainter();
                if (layerPainter != null) {
                    BufferedImage layerSprite = layerPainter.getSpriteImage();
                    this.setDimensionsLabel(layerSprite);
                } else {
                    this.setDimensionsLabel(null);
                }
            }
        });
    }

    private JRadioButtonMenuItem createCoordsOption(ButtonGroup group,
                                                    CoordsDisplayMode displayMode) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(displayMode.getDisplayedText());
        menuItem.addActionListener(e -> {
            menuItem.setSelected(true);
            this.updateCursorCoordsLabel();
            EventBus.publish(new CoordsModeChanged(displayMode));
            Events.repaintShipView();
        });
        group.add(menuItem);
        return menuItem;
    }

    private JPopupMenu createCoordsMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem world = createCoordsOption(group, CoordsDisplayMode.WORLD);
        popupMenu.add(world);

        JRadioButtonMenuItem sprite = createCoordsOption(group, CoordsDisplayMode.SPRITE_CENTER);
        popupMenu.add(sprite);

        JRadioButtonMenuItem shipCenterAnchor = createCoordsOption(group, CoordsDisplayMode.SHIPCENTER_ANCHOR);
        popupMenu.add(shipCenterAnchor);

        JRadioButtonMenuItem shipCenter = createCoordsOption(group, CoordsDisplayMode.SHIP_CENTER);
        shipCenter.setSelected(true);
        popupMenu.add(shipCenter);

        return popupMenu;
    }

    private void setDimensionsLabel(BufferedImage sprite) {
        if (sprite != null) {
            dimensions.setText(sprite.getWidth() + " × " + sprite.getHeight());
            log.trace("Layer selected: sprite dimensions loaded.");
        } else {
            dimensions.setText("Sprite not loaded.");
        }

    }

    private void updateCursorCoordsLabel() {
        Point2D cursorPoint = StaticController.getCorrectedCursor();
        Point2D cursor = cursorPoint;
        LayerPainter selectedLayer = this.viewer.getSelectedLayer();
        switch (StaticController.getCoordsMode()) {
            case SPRITE_CENTER -> {
                if (selectedLayer == null) break;
                Point2D center = selectedLayer.getSpriteCenter();
                cursor = ViewerStatusPanel.adjustCursorCoordinates(cursorPoint, center);
            }
            case SHIPCENTER_ANCHOR -> {
                if (!(selectedLayer instanceof ShipPainter checkedPainter)) break;
                Point2D center = checkedPainter.getCenterAnchor();
                cursor = ViewerStatusPanel.adjustCursorCoordinates(cursorPoint, center);
                cursor = new Point2D.Double(cursor.getX(), -cursor.getY());
                if (cursor.getY() == -0.0) {
                    cursor.setLocation(cursor.getX(), 0);
                }
            }
            // This case uses different coordinate system alignment to be consistent with game files.
            // Otherwise, user might be confused as shown point coordinates won't match with those in file.
            case SHIP_CENTER -> {
                if (selectedLayer == null || selectedLayer.isUninitialized()) break;
                Point2D entityCenter = selectedLayer.getEntityCenter();
                Point2D adjusted = ViewerStatusPanel.adjustCursorCoordinates(cursorPoint, entityCenter);
                cursor = new Point2D.Double(-adjusted.getY(), -adjusted.getX());
                if (cursor.getX() == -0.0) {
                    cursor.setLocation(0, cursor.getY());
                }
                if (cursor.getY() == -0.0) {
                    cursor.setLocation(cursor.getX(), 0);
                }
            }
        }
        double cursorX = Utility.round(cursor.getX(), 5);
        double cursorY = Utility.round(cursor.getY(), 5);
        cursorCoords.setText(cursorX + "," + cursorY);
    }

    private static Point2D adjustCursorCoordinates(Point2D cursor, Point2D center) {
        return new Point2D.Double(
                cursor.getX() - center.getX(),
                cursor.getY() - center.getY()
        );
    }

    private void setZoomLevel(double newZoom) {
        this.widgetsAcceptChange = false;
        zoomModel.setValue(newZoom);
        this.widgetsAcceptChange = true;
    }

    private void setRotationDegrees(double newRotation) {
        this.widgetsAcceptChange = false;
        int rounded = (int) Utility.round(newRotation, 3);
        if (ControlPredicates.isRotationRoundingEnabled()) {
            rounded = (int) Math.round(newRotation);
        }
        rotationModel.setValue((double) rounded);
        this.widgetsAcceptChange = true;
    }

    private static final class ResetMenuListener extends MouseAdapter {

        private final JPopupMenu resetMenu;
        private final JComponent sourceComponent;

        private ResetMenuListener(JPopupMenu menu, JComponent component) {
            this.resetMenu = menu;
            this.sourceComponent = component;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                resetMenu.show(sourceComponent, e.getX(), e.getY());
            }
        }

    }

}
