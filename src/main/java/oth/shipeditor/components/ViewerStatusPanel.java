package oth.shipeditor.components;

import lombok.Getter;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.Window;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.ViewerCursorMoved;
import oth.shipeditor.communication.events.viewer.control.ViewerZoomChanged;
import oth.shipeditor.components.painters.LayerPainter;
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
public class ViewerStatusPanel extends JPanel {

    public enum CoordsDisplayMode {
        WORLD, SCREEN, SPRITE_CENTER, SHIPCENTER_ANCHOR, SHIP_CENTER
    }

    private static double zoomLevel = 1f;

    @Getter
    private CoordsDisplayMode mode = CoordsDisplayMode.WORLD;

    private final JLabel dimensions;

    private final JLabel cursorCoords;

    private final JLabel zoom;

    private final Border normal = this.createLabelBorder(false);
    private final Border hovered = this.createLabelBorder(true);

    public ViewerStatusPanel() {
        super();
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
        zoom = new JLabel("", zoomIcon, JLabel.TRAILING);
        zoom.setToolTipText("Sprite Scale");
        this.add(zoom);

        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ViewerZoomChanged checked) {
                zoomLevel = checked.newValue();
                setZoomLabel(zoomLevel);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerCursorMoved checked) {
                setCursorCoordsLabel(checked.adjustedAndCorrected());
            }
        });
        setDimensionsLabel(null);
        setCursorCoordsLabel(new Point2D.Double(0,0));
        setZoomLabel(zoomLevel);
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

    private void repaintPointsPanel() {
        BoundPointsPanel boundsPanel = Window.getFrame().getPointsPanel();
        if (boundsPanel != null) {
            boundsPanel.repaint();
        }
    }

    private JRadioButtonMenuItem createCoordsOption(String text, ButtonGroup group,
                                                    CoordsDisplayMode displayMode, boolean selected) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(text);
        menuItem.addActionListener(e -> {
            ViewerStatusPanel.this.mode = displayMode;
            this.repaintPointsPanel();
            menuItem.setSelected(true);
        });
        group.add(menuItem);
        menuItem.setSelected(selected);
        return menuItem;
    }

    private JPopupMenu createCoordsMenu() {
        String axes = "X , Y from ";
        JPopupMenu popupMenu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem world = createCoordsOption(axes + "top left corner of sprite (World 0,0)",
                group, CoordsDisplayMode.WORLD, true);
        popupMenu.add(world);

        JRadioButtonMenuItem screen = createCoordsOption(axes + "top left corner of viewer (Screen 0,0)",
                group, CoordsDisplayMode.SCREEN, false);
        popupMenu.add(screen);

        JRadioButtonMenuItem sprite = createCoordsOption(axes + "sprite center (Sprite 0,0)",
                group, CoordsDisplayMode.SPRITE_CENTER, false);
        popupMenu.add(sprite);

        JRadioButtonMenuItem shipCenterAnchor = createCoordsOption(axes + "bottom left corner of sprite (Ship Center Anchor 0,0)",
                group, CoordsDisplayMode.SHIPCENTER_ANCHOR, false);
        popupMenu.add(shipCenterAnchor);

        JRadioButtonMenuItem shipCenter = createCoordsOption(axes + "designated ship center (Ship Center 0,0)",
                group, CoordsDisplayMode.SHIP_CENTER, false);
        popupMenu.add(shipCenter);

        return popupMenu;
    }

    public void setDimensionsLabel(BufferedImage sprite) {
        if (sprite != null) {
            dimensions.setText(sprite.getWidth() + " x " + sprite.getHeight());
        } else {
            dimensions.setText("Sprite not loaded.");
        }

    }

    public void setCursorCoordsLabel(Point2D adjustedCursor) {
        Point2D cursor = adjustedCursor;
        ShipViewerPanel viewerPanel = Window.getFrame().getShipView();
        LayerPainter selectedLayer = viewerPanel.getSelectedLayer();
        switch (mode) {
            case SCREEN -> {
                Point2D viewerLoc = viewerPanel.getLocation();
                Point2D mouse = viewerPanel.getControls().getMousePoint();
                double roundedX = Math.round((mouse.getX() - viewerLoc.getX()) * 2) / 2.0;
                double roundedY = Math.round((mouse.getY() - viewerLoc.getY()) * 2) / 2.0;
                cursor = new Point2D.Double(roundedX, roundedY);
            }
            case SPRITE_CENTER -> {
                Point2D center = selectedLayer.getSpriteCenter();
                cursor = adjustCursorCoordinates(adjustedCursor, center);
            }
            case SHIPCENTER_ANCHOR -> {
                if (selectedLayer.getShipSprite() == null) return;
                Point2D center = viewerPanel.getShipCenterAnchor();
                cursor = adjustCursorCoordinates(adjustedCursor, center);
            }
            case SHIP_CENTER -> {
                if (selectedLayer == null) return;
                Point2D center = selectedLayer.getTranslatedCenter();
                cursor = adjustCursorCoordinates(adjustedCursor, center);
            }
        }

        cursorCoords.setText(cursor.getX() + "," + cursor.getY());
    }

    private Point2D adjustCursorCoordinates(Point2D cursor, Point2D center) {
        return new Point2D.Double(
                cursor.getX() - center.getX(),
                cursor.getY() - center.getY()
        );
    }

    public void setZoomLabel(double zoomLevel) {
        int rounded = (int) Math.round(zoomLevel * 100);
        zoom.setText(rounded + "%");
    }

}
