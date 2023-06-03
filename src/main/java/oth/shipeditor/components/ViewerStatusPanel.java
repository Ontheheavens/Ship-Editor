package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.control.ViewerZoomChanged;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerLoadConfirmed;
import oth.shipeditor.communication.events.viewer.status.CoordsModeChanged;
import oth.shipeditor.components.viewer.ShipViewable;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
@Log4j2
public final class ViewerStatusPanel extends JPanel {

    private static double zoomLevel = 1f;

    @Getter
    private CoordsDisplayMode mode = CoordsDisplayMode.WORLD;

    private final ShipViewable viewer;

    private final JLabel dimensions;

    private final JLabel cursorCoords;

    private final JLabel zoom;

    private final Border normal = this.createLabelBorder(false);
    private final Border hovered = this.createLabelBorder(true);

    public ViewerStatusPanel(ShipViewable viewable) {
        this.viewer = viewable;
        FontIcon dimensionIcon = FontIcon.of(FluentUiRegularMZ.SLIDE_SIZE_24, 20);
        dimensions = new JLabel("", dimensionIcon, JLabel.TRAILING);
        dimensions.setToolTipText("Width / Height");
        this.add(dimensions);
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(1, dimensionIcon.getIconHeight()));
        this.add(separator);
        FontIcon mouseIcon = FontIcon.of(FluentUiRegularAL.CURSOR_HOVER_20, 20);
        cursorCoords = new JLabel("", mouseIcon, JLabel.TRAILING);
        cursorCoords.setBorder(normal);
        cursorCoords.setToolTipText("Click to change coordinate system");
        JPopupMenu popupMenu = this.createCoordsMenu();
        cursorCoords.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                cursorCoords.setBorder(hovered);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                cursorCoords.setBorder(normal);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    popupMenu.show(cursorCoords, e.getX(), e.getY());
                }
            }
        });

        this.add(cursorCoords);
        this.add(Utility.clone(separator));
        FontIcon zoomIcon = FontIcon.of(FluentUiRegularMZ.ZOOM_IN_20, 20);
        this.zoom = new JLabel("", zoomIcon, JLabel.TRAILING);
        this.zoom.setToolTipText("Sprite Scale");
        this.add(this.zoom);

        this.initListeners();
        this.setDimensionsLabel(null);
        this.setZoomLabel(zoomLevel);
        this.setCursorCoordsLabel(new Point2D.Double());
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerZoomChanged checked) {
                zoomLevel = checked.newValue();
                this.setZoomLabel(zoomLevel);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                Point2D corrected = checked.adjustedAndCorrected();
                this.setCursorCoordsLabel(corrected);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerLoadConfirmed checked) {
                ShipLayer layer = checked.layer();
                BufferedImage shipSprite = layer.getShipSprite();
                this.setDimensionsLabel(shipSprite);
            }
        });
    }

    private Border createLabelBorder(boolean hover) {
        Border empty = BorderFactory.createEmptyBorder(0, 4, 0, 4);
        if (hover) {
            Border hovered = BorderFactory.createLoweredBevelBorder();
            return BorderFactory.createCompoundBorder(hovered, empty);
        } else {
            Border normal = BorderFactory.createRaisedBevelBorder();
            return BorderFactory.createCompoundBorder(normal, empty);
        }
    }

    private JRadioButtonMenuItem createCoordsOption(String text, ButtonGroup group,
                                                    CoordsDisplayMode displayMode, boolean selected) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(text);
        menuItem.addActionListener(e -> {
            this.mode = displayMode;
            menuItem.setSelected(true);
            EventBus.publish(new CoordsModeChanged(displayMode));
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

        JRadioButtonMenuItem screen = createCoordsOption(
                axes + "top left corner of viewer panel (Screen 0,0)",
                group, CoordsDisplayMode.SCREEN, false);
        popupMenu.add(screen);

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

    public void setDimensionsLabel(BufferedImage sprite) {
        if (sprite != null) {
            dimensions.setText(sprite.getWidth() + " x " + sprite.getHeight());
            log.info("Sprite dimensions loaded.");
        } else {
            dimensions.setText("Sprite not loaded.");
        }

    }

    private void setCursorCoordsLabel(Point2D adjustedCursor) {
        Point2D cursor = adjustedCursor;
        LayerPainter selectedLayer = this.viewer.getSelectedLayer();
        switch (mode) {
            case SCREEN -> {
                Point2D viewerLoc = this.viewer.getPanelLocation();
                ViewerControl controls = this.viewer.getControls();
                Point2D mouse = controls.getMousePoint();
                double roundedX = Math.round((mouse.getX() - viewerLoc.getX()) * 2) / 2.0;
                double roundedY = Math.round((mouse.getY() - viewerLoc.getY()) * 2) / 2.0;
                cursor = new Point2D.Double(roundedX, roundedY);
            }
            case SPRITE_CENTER -> {
                if (selectedLayer == null) break;
                Point2D center = selectedLayer.getSpriteCenter();
                cursor = ViewerStatusPanel.adjustCursorCoordinates(adjustedCursor, center);
            }
            case SHIPCENTER_ANCHOR -> {
                if (selectedLayer == null) break;
                Point2D center = selectedLayer.getCenterAnchor();
                cursor = ViewerStatusPanel.adjustCursorCoordinates(adjustedCursor, center);
            }
            case SHIP_CENTER -> {
                if (selectedLayer == null || selectedLayer.getShipCenter() == null) break;
                ShipCenterPoint shipCenter = selectedLayer.getShipCenter();
                Point2D center = shipCenter.getPosition();
                cursor = ViewerStatusPanel.adjustCursorCoordinates(adjustedCursor, center);
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

}
