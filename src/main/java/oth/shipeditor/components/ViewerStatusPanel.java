package oth.shipeditor.components;

import com.formdev.flatlaf.ui.FlatRoundBorder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.control.ViewerTransformRotated;
import oth.shipeditor.communication.events.viewer.control.ViewerZoomChanged;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.status.CoordsModeChanged;
import oth.shipeditor.components.viewer.ShipViewable;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.utility.MouseoverLabelListener;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
@Log4j2
final class ViewerStatusPanel extends JPanel {

    private static double zoomLevel = 1.0f;

    private static double rotationDegrees;

    @Getter
    private CoordsDisplayMode mode = CoordsDisplayMode.WORLD;

    private final ShipViewable viewer;

    private final JLabel dimensions;

    private final JLabel cursorCoords;

    private final JLabel zoom;

    private final JLabel rotation;

    private Point2D cursorPoint;

    ViewerStatusPanel(ShipViewable viewable) {
        this.viewer = viewable;

        FontIcon dimensionIcon = FontIcon.of(FluentUiRegularMZ.SLIDE_SIZE_24, 20);
        dimensions = new JLabel("", dimensionIcon, SwingConstants.TRAILING);
        dimensions.setToolTipText("Width / height of active layer");

        this.add(dimensions);

        this.addSeparator();

        FontIcon mouseIcon = FontIcon.of(FluentUiRegularAL.CURSOR_HOVER_20, 20);
        cursorCoords = new JLabel("", mouseIcon, SwingConstants.TRAILING);
        cursorCoords.setBorder(ViewerStatusPanel.createLabelBorder());
        cursorCoords.setToolTipText("Right-click to change coordinate system");
        JPopupMenu popupMenu = this.createCoordsMenu();
        cursorCoords.addMouseListener(new MouseoverLabelListener(popupMenu, cursorCoords));
        this.add(cursorCoords);

        this.addSeparator();

        FontIcon zoomIcon = FontIcon.of(FluentUiRegularMZ.ZOOM_IN_20, 20);
        this.zoom = new JLabel("", zoomIcon, SwingConstants.TRAILING);
        this.zoom.setToolTipText("Zoom level");
        this.add(this.zoom);

        this.addSeparator();

        FontIcon rotationIcon = FontIcon.of(FluentUiRegularAL.ARROW_ROTATE_CLOCKWISE_20, 20);
        this.rotation = new JLabel("", rotationIcon, SwingConstants.TRAILING);
        this.rotation.setToolTipText("Rotation degrees");
        this.add(this.rotation);

        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        this.initListeners();
        this.setDimensionsLabel(null);
        this.setZoomLabel(zoomLevel);
        this.setRotationLabel(rotationDegrees);
        this.cursorPoint = new Point2D.Double();
        this.updateCursorCoordsLabel();
    }

    private void addSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 24));
        separator.setForeground(Color.GRAY);
        this.add(separator);
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerZoomChanged checked) {
                zoomLevel = checked.newValue();
                this.setZoomLabel(zoomLevel);
            } else if (event instanceof ViewerTransformRotated checked) {
                rotationDegrees = checked.degrees();
                this.setRotationLabel(rotationDegrees);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                this.cursorPoint = checked.adjustedAndCorrected();
                this.updateCursorCoordsLabel();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                if (!checked.spriteChanged()) return;
                ShipLayer layer = checked.updated();
                BufferedImage shipSprite = layer.getShipSprite();
                this.setDimensionsLabel(shipSprite);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer layer = checked.selected();
                if (layer != null) {
                    BufferedImage shipSprite = layer.getShipSprite();
                    this.setDimensionsLabel(shipSprite);
                } else {
                    this.setDimensionsLabel(null);
                }
            }
        });
    }

    private static Border createLabelBorder() {
        Border empty = BorderFactory.createEmptyBorder(2, 6, 2, 7);
        Border lineBorder = new FlatRoundBorder();
        return BorderFactory.createCompoundBorder(lineBorder, empty);
    }

    private JRadioButtonMenuItem createCoordsOption(String text, ButtonGroup group,
                                                    CoordsDisplayMode displayMode, boolean selected) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(text);
        menuItem.addActionListener(e -> {
            this.mode = displayMode;
            menuItem.setSelected(true);
            this.updateCursorCoordsLabel();
            EventBus.publish(new CoordsModeChanged(displayMode));
            EventBus.publish(new ViewerRepaintQueued());
        });
        group.add(menuItem);
        menuItem.setSelected(selected);
        return menuItem;
    }

    private JPopupMenu createCoordsMenu() {
        String axes = "X , Y from ";
        JPopupMenu popupMenu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem world = createCoordsOption(
                axes + "start of coordinate system (World 0,0)",
                group, CoordsDisplayMode.WORLD, true);
        popupMenu.add(world);

        JRadioButtonMenuItem sprite = createCoordsOption(
                axes + "selected sprite center (Sprite 0,0)",
                group, CoordsDisplayMode.SPRITE_CENTER, false);
        popupMenu.add(sprite);

        JRadioButtonMenuItem shipCenterAnchor = createCoordsOption(
                axes + "bottom left corner of selected sprite (Ship Center Anchor 0,0)",
                group, CoordsDisplayMode.SHIPCENTER_ANCHOR, false);
        popupMenu.add(shipCenterAnchor);

        JRadioButtonMenuItem shipCenter = createCoordsOption(
                axes + "designated ship center of selected layer (Ship Center 0,0)",
                group, CoordsDisplayMode.SHIP_CENTER, false);
        popupMenu.add(shipCenter);

        return popupMenu;
    }

    private void setDimensionsLabel(BufferedImage sprite) {
        if (sprite != null) {
            dimensions.setText(sprite.getWidth() + " x " + sprite.getHeight());
            log.info("Sprite dimensions loaded.");
        } else {
            dimensions.setText("Sprite not loaded.");
        }

    }

    private void updateCursorCoordsLabel() {
        Point2D cursor = this.cursorPoint;
        LayerPainter selectedLayer = this.viewer.getSelectedLayer();
        switch (mode) {
            case SPRITE_CENTER -> {
                if (selectedLayer == null) break;
                Point2D center = selectedLayer.getSpriteCenter();
                cursor = ViewerStatusPanel.adjustCursorCoordinates(this.cursorPoint, center);
            }
            case SHIPCENTER_ANCHOR -> {
                if (selectedLayer == null) break;
                Point2D center = selectedLayer.getCenterAnchor();
                cursor = ViewerStatusPanel.adjustCursorCoordinates(this.cursorPoint, center);
                cursor = new Point2D.Double(cursor.getX(), -cursor.getY());
                if (cursor.getY() == -0.0) {
                    cursor.setLocation(cursor.getX(), 0);
                }
            }
            // This case uses different coordinate system alignment to be consistent with game files.
            // Otherwise, user might be confused as shown point coordinates won't match with those in file.
            case SHIP_CENTER -> {
                if (selectedLayer == null || selectedLayer.getShipCenter() == null) break;
                ShipCenterPoint shipCenter = selectedLayer.getShipCenter();
                Point2D center = shipCenter.getPosition();
                Point2D adjusted = ViewerStatusPanel.adjustCursorCoordinates(this.cursorPoint, center);
                cursor = new Point2D.Double(-adjusted.getY(), -adjusted.getX());
                if (cursor.getX() == -0.0) {
                    cursor.setLocation(0, cursor.getY());
                }
                if (cursor.getY() == -0.0) {
                    cursor.setLocation(cursor.getX(), 0);
                }
            }
        }
        cursorCoords.setText(cursor.getX() + "," + cursor.getY());
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
        int rounded = (int) Math.round(newRotation);
        rotation.setText(rounded + "°");
    }

}
