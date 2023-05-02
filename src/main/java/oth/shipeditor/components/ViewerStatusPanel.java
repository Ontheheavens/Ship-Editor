package oth.shipeditor.components;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.Utility;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
public class ViewerStatusPanel extends JPanel {

    enum CoordsDisplayMode {
        WORLD, SCREEN, SHIP_CENTER
    }

    private final JLabel dimensions;

    private final JLabel cursorCoords;

    private final JLabel zoom;

    {
        SwingUtilities.invokeLater(() -> {
            ShipViewerPanel viewerPanel = PrimaryWindow.getInstance().getShipView();
            setDimensionsLabel(viewerPanel.getShipSprite());
            setCursorCoordsLabel(new Point2D.Double(0,0));
            setZoomLabel(viewerPanel.getControls().getZoomLevel());
        });
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
        cursorCoords.setToolTipText("X / Y from top left corner of sprite");
        this.add(cursorCoords);

        this.add(Utility.clone(separator));

        FontIcon zoomIcon = FontIcon.of(FluentUiRegularMZ.ZOOM_IN_20, 20);
        zoom = new JLabel("", zoomIcon, JLabel.TRAILING);
        zoom.setToolTipText("Sprite Scale");
        this.add(zoom);
    }

    public void setDimensionsLabel(BufferedImage sprite) {
        dimensions.setText(sprite.getWidth() + " x " + sprite.getHeight());
    }

    public void setCursorCoordsLabel(Point2D adjustedCursor) {
        cursorCoords.setText(adjustedCursor.getX() + "," + adjustedCursor.getY());
    }

    public void setZoomLabel(double zoomLevel) {
        int rounded = (int) Math.round(zoomLevel * 100);
        zoom.setText(rounded + "%");
    }

}
