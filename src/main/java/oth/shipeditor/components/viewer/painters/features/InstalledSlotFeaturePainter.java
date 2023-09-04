package oth.shipeditor.components.viewer.painters.features;

import de.javagl.viewer.Painter;
import lombok.Getter;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
public class InstalledSlotFeaturePainter implements Painter {

    @Getter
    private final Map<String, LayerPainter> installedFeatures;

    public InstalledSlotFeaturePainter() {
        installedFeatures = new HashMap<>();
    }

    public void refreshSlotData(WeaponSlotPainter slotPainter) {
        installedFeatures.forEach((slotID, layerPainter) -> layerPainter.setShouldDrawPainter(false));

        for (Map.Entry<String, LayerPainter> entry : installedFeatures.entrySet()) {
            String slotID = entry.getKey();
            LayerPainter painter = entry.getValue();
            WeaponSlotPoint slotPoint = slotPainter.getSlotByID(slotID);
            if (slotPoint == null) {
                continue;
            }
            painter.setShouldDrawPainter(true);

            Point2D position = slotPoint.getPosition();
            Point2D entityCenter = painter.getEntityCenter();
            if (painter instanceof WeaponPainter weaponPainter) {
                entityCenter = weaponPainter.getWeaponCenter();
            } else if (painter instanceof ShipPainter shipPainter) {
                entityCenter = shipPainter.getCenterAnchorDifference();
            }
            double x = position.getX() - entityCenter.getX();
            double y = position.getY() - entityCenter.getY();
            Point2D newAnchor = new Point2D.Double(x, y);
            Point2D painterAnchor = painter.getAnchor();
            if (!painterAnchor.equals(newAnchor)) {
                painter.setAnchor(newAnchor);
            }

            double transformedAngle = Utility.transformAngle(slotPoint.getAngle());
            painter.setRotationRadians(Math.toRadians(transformedAngle + 90));
        }
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        installedFeatures.forEach((slotID, painter) -> {
            AffineTransform transform = painter.getWithRotation(worldToScreen);
            painter.paint(g, transform, w, h);

            List<AbstractPointPainter> allPainters = painter.getAllPainters();
            allPainters.forEach(pointPainter -> pointPainter.paint(g, transform, w, h));
        });
    }

}
