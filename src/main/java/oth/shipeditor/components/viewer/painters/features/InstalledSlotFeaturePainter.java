package oth.shipeditor.components.viewer.painters.features;

import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
public final class InstalledSlotFeaturePainter {

    private InstalledSlotFeaturePainter() {
    }

    public static void refreshSlotData(ShipPainter painter) {
        var slotPainter = painter.getWeaponSlotPainter();
        var allFeatures = painter.getAllInstallables();

        for (Map.Entry<String, InstalledFeature> entry : allFeatures.entrySet()) {
            String slotID = entry.getKey();
            InstalledFeature installedFeature = entry.getValue();
            LayerPainter featurePainter = installedFeature.getFeaturePainter();
            featurePainter.setShouldDrawPainter(false);

            WeaponSlotPoint slotPoint = slotPainter.getSlotByID(slotID);
            InstalledSlotFeaturePainter.refreshInstalledPainter(slotPoint, featurePainter);
        }
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private static void refreshInstalledPainter(WeaponSlotPoint slotPoint, LayerPainter painter) {
        if (slotPoint == null) {
            return;
        }
        painter.setShouldDrawPainter(true);

        Point2D position = slotPoint.getPosition();
        Point2D entityCenter = painter.getEntityCenter();
        if (painter instanceof WeaponPainter weaponPainter) {
            entityCenter = weaponPainter.getWeaponCenter();

            weaponPainter.setMount(slotPoint.getWeaponMount());
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

    public static void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h, ShipPainter painter) {
        var allFeatures = painter.getAllInstallables();

        for (Map.Entry<String, InstalledFeature> entry : allFeatures.entrySet()) {
            InstalledFeature installedFeature = entry.getValue();
            LayerPainter featurePainter = installedFeature.getFeaturePainter();
            AffineTransform transform = featurePainter.getWithRotation(worldToScreen);
            featurePainter.paint(g, transform, w, h);

            List<AbstractPointPainter> allPainters = featurePainter.getAllPainters();
            allPainters.forEach(pointPainter -> pointPainter.paint(g, transform, w, h));
        }
    }

}
