package oth.shipeditor.components;

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
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.components.dialog.DialogUtilities;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
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

    private JLabel zoom;

    private JLabel rotation;

    private JPanel leftsideContainer;

    ViewerStatusPanel(LayerViewer viewable) {
        this.setLayout(new BorderLayout());

        this.viewer = viewable;
        this.leftsideContainer = createLeftsidePanel();

        this.initListeners();
        this.setDimensionsLabel(null);
        this.setZoomLabel(StaticController.getZoomLevel());
        this.setRotationLabel(StaticController.getRotationDegrees());
        this.updateCursorCoordsLabel();

        this.add(leftsideContainer, BorderLayout.LINE_START);

        JPanel rightPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.weightx = 1;
        gbcRight.insets = new Insets(0, 0, 0, 10);
        gbcRight.anchor = GridBagConstraints.LINE_END;

        ProgressBarPanel progressBarContainer = new ProgressBarPanel();
        rightPanel.add(progressBarContainer, gbcRight);

        gbcRight.gridx = 1;
        gbcRight.weightx = 0;

        rightPanel.add(ViewerStatusPanel.createMirrorModePanel(), gbcRight);

        this.add(rightPanel, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
    }

    private static JPanel createMirrorModePanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        SpinnerNumberModel spinnerListModel = new SpinnerNumberModel(1, 0, 5, 1);
        JSpinner spinner = new JSpinner(spinnerListModel);

        JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) spinner.getEditor();
        JFormattedTextField textField = spinnerEditor.getTextField();
        textField.setEditable(true);
        textField.setColumns(1);

        JLabel toleranceLabel = new JLabel("Linkage tolerance:");
        toleranceLabel.setToolTipText("Determines maximum distance at which mirrored points link for interaction");

        JCheckBox mirrorModeCheckbox = new JCheckBox("Mirror mode");
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
        container.add(spinner);
        container.add(Box.createRigidArea(new Dimension(margin,0)));

        spinner.addChangeListener(e -> {
            Integer current = (Integer) spinnerListModel.getValue();
            EventBus.publish(new PointLinkageToleranceChanged(current));
        });
        spinner.setValue(5);

        return container;
    }

    private JPanel createLeftsidePanel() {
        leftsideContainer = new JPanel();

        FontIcon dimensionIcon = FontIcon.of(FluentUiRegularMZ.SLIDE_SIZE_24, 20);
        dimensions = new JLabel("", dimensionIcon, SwingConstants.TRAILING);
        dimensions.setToolTipText("Width / height of active layer");
        leftsideContainer.add(dimensions);

        this.addSeparator();

        FontIcon mouseIcon = FontIcon.of(FluentUiRegularAL.CURSOR_HOVER_20, 20);
        cursorCoords = new JLabel("", mouseIcon, SwingConstants.TRAILING);
        Insets coordsInsets = new Insets(2, 6, 2, 7);
        cursorCoords.setBorder(ComponentUtilities.createRoundCompoundBorder(coordsInsets));
        cursorCoords.setToolTipText("Right-click to change coordinate system");
        JPopupMenu coordsMenu = this.createCoordsMenu();
        cursorCoords.addMouseListener(new MouseoverLabelListener(coordsMenu, cursorCoords));
        leftsideContainer.add(cursorCoords);

        this.addSeparator();

        FontIcon zoomIcon = FontIcon.of(FluentUiRegularMZ.ZOOM_IN_20, 20);
        zoom = new JLabel("", zoomIcon, SwingConstants.TRAILING);
        Insets zoomInsets = new Insets(2, 3, 2, 5);
        zoom.setBorder(ComponentUtilities.createRoundCompoundBorder(zoomInsets));
        String zoomTooltip = Utility.getWithLinebreaks("Mousewheel to zoom viewer", StringValues.RIGHT_CLICK_TO_ADJUST_VALUE);
        zoom.setToolTipText(zoomTooltip);

        JPopupMenu zoomMenu = new JPopupMenu();
        JMenuItem adjustZoomValue = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustZoomValue.addActionListener(event -> {
            double oldZoom = StaticController.getZoomLevel();
            DialogUtilities.showAdjustZoomDialog(oldZoom);
        });
        zoomMenu.add(adjustZoomValue);
        zoom.addMouseListener(new MouseoverLabelListener(zoomMenu, zoom));

        leftsideContainer.add(this.zoom);

        this.addSeparator();

        FontIcon rotationIcon = FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 20);
        this.rotation = new JLabel("", rotationIcon, SwingConstants.TRAILING);

        Insets rotationInsets = new Insets(2, 3, 2, 5);
        rotation.setBorder(ComponentUtilities.createRoundCompoundBorder(rotationInsets));

        String rotationTooltip = Utility.getWithLinebreaks("CTRL+Mousewheel to rotate viewer",
                StringValues.RIGHT_CLICK_TO_ADJUST_VALUE);
        this.rotation.setToolTipText(rotationTooltip);

        JPopupMenu rotationMenu = ViewerStatusPanel.getRotationMenu();

        rotation.addMouseListener(new MouseoverLabelListener(rotationMenu, rotation));

        leftsideContainer.add(this.rotation);

        return leftsideContainer;
    }

    private static JPopupMenu getRotationMenu() {
        JPopupMenu rotationMenu = new JPopupMenu();
        JMenuItem adjustRotationValue = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustRotationValue.addActionListener(event -> {
            double oldRotation = StaticController.getRotationDegrees();
            DialogUtilities.showAdjustViewerRotationDialog(oldRotation);
        });
        rotationMenu.add(adjustRotationValue);

        JMenuItem resetRotation = new JMenuItem(StringValues.RESET_ROTATION);
        resetRotation.addActionListener(e -> {
            EventBus.publish(new ViewerRotationSet(0));
            Events.repaintShipView();
        });
        rotationMenu.add(resetRotation);
        return rotationMenu;
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
                this.setZoomLabel(StaticController.getZoomLevel());
            } else if (event instanceof ViewerTransformRotated) {
                this.setRotationLabel(StaticController.getRotationDegrees());
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

    private JRadioButtonMenuItem createCoordsOption(String text, ButtonGroup group,
                                                    CoordsDisplayMode displayMode) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(text);
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
        String axes = "X , Y from ";
        JPopupMenu popupMenu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem world = createCoordsOption(
                axes + "start of coordinate system (World 0,0)",
                group, CoordsDisplayMode.WORLD);
        popupMenu.add(world);

        JRadioButtonMenuItem sprite = createCoordsOption(
                axes + "selected sprite center (Sprite 0,0)",
                group, CoordsDisplayMode.SPRITE_CENTER);
        popupMenu.add(sprite);

        JRadioButtonMenuItem shipCenterAnchor = createCoordsOption(
                axes + "bottom left corner of selected sprite (Entity Center Anchor 0,0)",
                group, CoordsDisplayMode.SHIPCENTER_ANCHOR);
        popupMenu.add(shipCenterAnchor);

        JRadioButtonMenuItem shipCenter = createCoordsOption(
                axes + "designated entity center of selected layer (Entity Center 0,0)",
                group, CoordsDisplayMode.SHIP_CENTER);
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

    private void setZoomLabel(double newZoom) {
        int rounded = (int) Math.round(newZoom * 100);
        zoom.setText(rounded + "%");
    }

    private void setRotationLabel(double newRotation) {
        double rounded = Utility.round(newRotation, 3);
        if (ControlPredicates.isRotationRoundingEnabled()) {
            rounded = (int) Math.round(newRotation);
        }
        rotation.setText(rounded + "°");
    }

}
