package oth.shipeditor.components;

import de.javagl.viewer.Viewer;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.Utility;

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

    enum CoordsDisplayMode {
        WORLD, SCREEN, SPRITE_CENTER
    }

    private CoordsDisplayMode mode = CoordsDisplayMode.WORLD;

    private final JLabel dimensions;

    private final JLabel cursorCoords;

    private final JLabel zoom;

    private final Border normal = this.createLabelBorder(false);
    private final Border hovered = this.createLabelBorder(true);
    {
        SwingUtilities.invokeLater(() -> {
            ShipViewerPanel viewerPanel = PrimaryWindow.getInstance().getShipView();
            setDimensionsLabel(viewerPanel.getShipSprite());
            setCursorCoordsLabel(new Point2D.Double(0,0));
            setZoomLabel(viewerPanel.getControls().getZoomLevel());
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
    }

    private JPopupMenu createCoordsMenu() {
        String axes = "X , Y from ";
        JPopupMenu popupMenu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem world = new JRadioButtonMenuItem(axes + "top left corner of sprite (World 0,0)");
        world.addActionListener(e -> {
            ViewerStatusPanel.this.mode = CoordsDisplayMode.WORLD;
            world.setSelected(true);
        });
        group.add(world);
        world.setSelected(true);
        popupMenu.add(world);

        JRadioButtonMenuItem screen = new JRadioButtonMenuItem(axes + "top left corner of viewer (Screen 0,0)");
        screen.addActionListener(e -> {
            ViewerStatusPanel.this.mode = CoordsDisplayMode.SCREEN;
            screen.setSelected(true);
        });
        group.add(screen);
        popupMenu.add(screen);

        JRadioButtonMenuItem sprite = new JRadioButtonMenuItem(axes + "sprite center (Sprite 0,0)");
        sprite.addActionListener(e -> {
            ViewerStatusPanel.this.mode = CoordsDisplayMode.SPRITE_CENTER;
            sprite.setSelected(true);
        });
        group.add(sprite);
        popupMenu.add(sprite);
        return popupMenu;
    }

    public void setDimensionsLabel(BufferedImage sprite) {
        dimensions.setText(sprite.getWidth() + " x " + sprite.getHeight());
    }

    public void setCursorCoordsLabel(Point2D adjustedCursor) {
        Point2D cursor = adjustedCursor;
        ShipViewerPanel viewerPanel = PrimaryWindow.getInstance().getShipView();
        switch (mode) {
            case WORLD -> {
            }
            case SCREEN -> {
                Viewer viewer = viewerPanel.getViewer();
                Point2D viewerLoc = viewer.getLocation();
                Point2D mouse = viewerPanel.getControls().getMousePoint();
                cursor = new Point2D.Double(
                        mouse.getX() - viewerLoc.getX(),
                        mouse.getY() - viewerLoc.getY()
                );
                double roundedX = Math.round(cursor.getX() * 2) / 2.0;
                double roundedY = Math.round(cursor.getY() * 2) / 2.0;
                cursor =  new Point2D.Double(roundedX, roundedY);
            }
            case SPRITE_CENTER -> {
                Point2D center = viewerPanel.getSpriteCenter();
                cursor = new Point2D.Double(
                        adjustedCursor.getX() - center.getX(),
                        adjustedCursor.getY() - center.getY()
                );
            }
        }
        cursorCoords.setText(cursor.getX() + "," + cursor.getY());
    }

    public void setZoomLabel(double zoomLevel) {
        int rounded = (int) Math.round(zoomLevel * 100);
        zoom.setText(rounded + "%");
    }

}
