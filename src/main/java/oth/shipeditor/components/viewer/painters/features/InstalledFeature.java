package oth.shipeditor.components.viewer.painters.features;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.text.StringConstants;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 09.09.2023
 */
@Getter
public final class InstalledFeature implements InstallableEntry {

    private final String slotID;

    private final String featureID;

    private final LayerPainter featurePainter;

    private final CSVEntry dataEntry;

    @Setter
    private boolean invalidated;

    private InstalledFeature(String slot, String id, LayerPainter painter, CSVEntry entry) {
        this.slotID = slot;
        this.featureID = id;
        this.featurePainter = painter;
        this.dataEntry = entry;
        featurePainter.setShouldDrawPainter(false);
    }

    public void cleanupForRemoval() {
        featurePainter.cleanupForRemoval();
    }

    public static InstalledFeature of(String slot, String id, LayerPainter painter, CSVEntry entry) {
        if (entry instanceof InstallableEntry) {
            return new InstalledFeature(slot, id, painter, entry);
        } else throw new IllegalArgumentException("Illegal data entry passed for installable feature!");
    }

    public boolean isDecoWeapon() {
        if (dataEntry instanceof WeaponCSVEntry weaponEntry) {
            return weaponEntry.getType() == WeaponType.DECORATIVE;
        }
        return false;
    }

    public boolean isNormalWeapon() {
        if (dataEntry instanceof WeaponCSVEntry weaponEntry) {
            return weaponEntry.getType() != WeaponType.DECORATIVE;
        }
        return false;
    }

    int computeRenderOrder(WeaponSlotPoint slotPoint) {
        if (featurePainter instanceof WeaponPainter weaponPainter) {
            double slotOffset = slotPoint.getOffsetRelativeToAxis();
            double rawResult;
            switch (weaponPainter.getRenderOrderType()) {
                case BELOW_ALL -> rawResult = 0 - slotOffset + slotPoint.getRenderOrderMod();
                case ABOVE_ALL -> rawResult  = 100000 - slotOffset + slotPoint.getRenderOrderMod();
                default -> {
                    WeaponCSVEntry weaponCSVEntry = (WeaponCSVEntry) this.getDataEntry();

                    rawResult = weaponCSVEntry.getDrawOrder() * 2;
                    boolean weaponIsMissile = weaponCSVEntry.getType() == WeaponType.MISSILE;

                    WeaponSpecFile specFile = weaponCSVEntry.getSpecFile();
                    List<String> renderHints = specFile.getRenderHints();
                    boolean hasTargetHint = false;
                    if (renderHints != null && !renderHints.isEmpty()) {
                        hasTargetHint = renderHints.contains(StringConstants.RENDER_LOADED_MISSILES)
                                || renderHints.contains(StringConstants.RENDER_LOADED_MISSILES_UNLESS_HIDDEN);
                    }
                    if (weaponIsMissile && hasTargetHint) {
                        rawResult -= 1;
                    }
                    if (slotPoint.getWeaponMount() != WeaponMount.HARDPOINT) {
                        rawResult += 20;
                    }
                    rawResult += slotOffset + slotPoint.getRenderOrderMod();
                }
            }
            return (int) Math.ceil(rawResult);
        } else return Integer.MIN_VALUE;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    void refreshPaintCircumstance(WeaponSlotPoint slotPoint) {
        LayerPainter painter = this.getFeaturePainter();
        painter.setShouldDrawPainter(false);
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

    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        LayerPainter layerPainter = this.getFeaturePainter();
        AffineTransform transform = layerPainter.getWithRotation(worldToScreen);
        layerPainter.paint(g, transform, w, h);

        List<AbstractPointPainter> allPainters = layerPainter.getAllPainters();
        allPainters.forEach(pointPainter -> pointPainter.paint(g, transform, w, h));
    }

    @Override
    public String getID() {
        return featureID;
    }

}
