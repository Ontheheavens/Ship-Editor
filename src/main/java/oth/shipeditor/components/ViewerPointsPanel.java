package oth.shipeditor.components;

import lombok.Getter;
import oth.shipeditor.components.entities.WorldPoint;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
public class ViewerPointsPanel extends JPanel {

    @Getter
    private static final List<Point2D.Double> worldPoints = new ArrayList<>();

    private JList<WorldPoint> pointContainer;

    private ShipViewerPanel viewerPanel;

    @Getter
    private final DefaultListModel<WorldPoint> model = new DefaultListModel<>();

    public ViewerPointsPanel(ShipViewerPanel viewerPanel) {
        this.viewerPanel = viewerPanel;
        pointContainer = new JList<>(model);

        this.addPoint(new WorldPoint(new Point2D.Double(0, 0)));
        this.addPoint(new WorldPoint(new Point2D.Double(25, 50)));

        this.add(pointContainer);
    }

    public void addPoint(WorldPoint point) {
        model.addElement(point);
        this.viewerPanel.getPointsPainter().getDelegates().add(point.getPainter());
    }

    public void removePoint(WorldPoint point) {
        model.removeElement(point);
        this.viewerPanel.getPointsPainter().getDelegates().remove(point);
    }

}
