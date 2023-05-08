package oth.shipeditor.components.control;

import de.javagl.viewer.InputEventPredicates;
import de.javagl.viewer.Predicates;
import oth.shipeditor.components.BoundPointsPanel;
import oth.shipeditor.components.ShipViewerPanel;
import oth.shipeditor.components.entities.BoundPoint;
import oth.shipeditor.components.painters.BoundPointsPainter;
import oth.shipeditor.components.painters.PointsPainter;
import oth.shipeditor.utility.Utility;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */
public class BoundEditingControl {

    private final ShipViewerControls parentControls;

    private final Predicate<MouseEvent> appendPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.shiftDown()
    );

    private final Predicate<MouseEvent> insertPointPredicate = Predicates.and(
            InputEventPredicates.buttonDown(1),
            InputEventPredicates.altDown()
    );

    protected BoundEditingControl(ShipViewerControls parent) {
        this.parentControls = parent;
    }

    protected void checkForBoundCreation(MouseEvent event, BoundPointsPanel pointsPanel,
                                         PointsPainter painter, ShipViewerPanel parentViewer) {
        if (pointsPanel == null) return;
        if (pointsPanel.getMode() != BoundPointsPanel.PointsMode.CREATE) return;

        BoundPointsPainter boundPainter = painter.getBoundPainter();
        Point2D screenPoint = parentControls.getAdjustedCursor();
        AffineTransform screenToWorld = parentViewer.getScreenToWorld();

        Point2D rounded = Utility.correctAdjustedCursor(screenPoint, screenToWorld);
        if (!painter.pointAtCoordsExists(rounded)) {
            if (appendPointPredicate.test(event)) {
                BoundPoint wrapped = new BoundPoint(rounded);
                painter.addPoint(wrapped);
                parentViewer.repaint();
            } else if (insertPointPredicate.test(event) && boundPainter.getBoundPoints().size() >= 2) {
                List<BoundPoint> twoClosest = boundPainter.findClosestBoundPoints(rounded);
                List<BoundPoint> allPoints = boundPainter.getBoundPoints();
                int index = boundPainter.getLowestBoundPointIndex(twoClosest);
                if (index >= 0) index += 1;
                if (index > allPoints.size() - 1) index = 0;
                if (boundPainter.getHighestBoundPointIndex(twoClosest) == allPoints.size() - 1 &&
                        boundPainter.getLowestBoundPointIndex(twoClosest) == 0) index = 0;
                BoundPoint preceding = boundPainter.getBoundPoints().get(index);
                BoundPoint wrapped = new BoundPoint(rounded);
                boundPainter.insertPoint(wrapped, preceding);
                parentViewer.repaint();
            }
        }

    }

}
