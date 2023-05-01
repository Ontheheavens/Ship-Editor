package oth.shipeditor.components;

import lombok.Getter;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.components.entities.WorldPoint;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
public class ViewerPointsPanel extends JPanel {

    @Getter
    private final JList<WorldPoint> pointContainer;

    @Getter
    private final DefaultListModel<WorldPoint> model = new DefaultListModel<>();

    static {
        SwingUtilities.invokeLater(() -> {
            PointsPainter painter = PrimaryWindow.getInstance().getShipView().getPointsPainter();
            painter.addPoint(new WorldPoint(new Point2D.Double(0, 0)));
            painter.addPoint(new WorldPoint(new Point2D.Double(25, 50)));

        });
    }

    public ViewerPointsPanel() {
        pointContainer = new JList<>(model);
        this.add(pointContainer);
        Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
        this.setBorder(line);
    }



}
