package oth.shipeditor.components;

import lombok.Getter;
import oth.shipeditor.components.entities.WorldPoint;

import javax.swing.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
public class ViewerPointsPanel extends JPanel {



    private final JList<WorldPoint> pointContainer;

    private final ShipViewerPanel viewerPanel;

    @Getter
    private final DefaultListModel<WorldPoint> model = new DefaultListModel<>();

    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PointsPainter painter = viewerPanel.getPointsPainter();
                painter.addPoint(new WorldPoint(new Point2D.Double(0, 0)));
                painter.addPoint(new WorldPoint(new Point2D.Double(25, 50)));

            }
        });
    }

    public ViewerPointsPanel(ShipViewerPanel viewerPanel) {
        this.viewerPanel = viewerPanel;
        pointContainer = new JList<>(model);
        this.add(pointContainer);
    }



}
