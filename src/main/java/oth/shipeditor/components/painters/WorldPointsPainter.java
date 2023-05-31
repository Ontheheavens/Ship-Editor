package oth.shipeditor.components.painters;

import de.javagl.viewer.Painter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.entities.WorldPoint;

import java.util.List;

/**
 * Meant to be used to place freeform points, for measuring or visual help.
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public class WorldPointsPainter extends AbstractPointPainter implements Painter {

    @Override
    protected List<? extends WorldPoint> getPointsIndex() {
        return pointsIndex;
    }

    @Override
    protected WorldPoint getTypeReference() {
        return new WorldPoint();
    }

}
