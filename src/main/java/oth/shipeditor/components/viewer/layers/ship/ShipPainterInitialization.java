package oth.shipeditor.components.viewer.layers.ship;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.map.ListOrderedMap;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.ship.*;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.ship.EngineSlot;
import oth.shipeditor.representation.ship.EngineStyle;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.representation.weapon.*;
import oth.shipeditor.utility.Errors;
import oth.shipeditor.utility.text.StringConstants;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public final class ShipPainterInitialization {

    private ShipPainterInitialization() {
    }

    static void loadHullData(ShipPainter shipPainter, HullSpecFile hullSpecFile) {
        Point2D anchor = shipPainter.getCenterAnchor();
        Point2D hullCenter = hullSpecFile.getCenter();

        shipPainter.setBaseHullId(hullSpecFile.getHullId());

        Point2D translatedCenter;
        if (hullCenter != null) {
            translatedCenter = ShipPainterInitialization.rotateCenter(hullCenter, anchor);
        } else {
            translatedCenter = shipPainter.getSpriteCenter();
        }

        ShipPainterInitialization.initCentroids(shipPainter, hullSpecFile, translatedCenter);

        ShipPainterInitialization.initBounds(shipPainter, hullSpecFile, translatedCenter);

        ShipPainterInitialization.initSlots(shipPainter, hullSpecFile, translatedCenter);

        ShipPainterInitialization.initEngines(shipPainter, hullSpecFile, translatedCenter);

        ShipPainterInitialization.initBuiltIns(shipPainter, hullSpecFile);

        shipPainter.finishInitialization();
    }

    private static void initCentroids(ShipPainter shipPainter, HullSpecFile hullSpecFile, Point2D translatedCenter) {
        CenterPointPainter centerPointPainter = shipPainter.getCenterPointPainter();
        centerPointPainter.initCenterPoint(translatedCenter, hullSpecFile);

        Point2D.Double specFileModuleAnchor = hullSpecFile.getModuleAnchor();
        if (specFileModuleAnchor != null) {
            centerPointPainter.setModuleAnchorOffset(specFileModuleAnchor);
        }

        Point2D shieldCenter = hullSpecFile.getShieldCenter();
        Point2D shieldCenterTranslated;
        if (shieldCenter != null) {
            shieldCenterTranslated = ShipPainterInitialization.rotatePointByCenter(shieldCenter, translatedCenter);
        } else {
            shieldCenterTranslated = translatedCenter;
        }

        ShieldPointPainter shieldPointPainter = shipPainter.getShieldPointPainter();
        shieldPointPainter.initShieldPoint(shieldCenterTranslated, hullSpecFile);
    }

    private static void initBounds(ShipPainter shipPainter, HullSpecFile hullSpecFile, Point2D translatedCenter) {
        Point2D.Double[] bounds = hullSpecFile.getBounds();
        Stream<Point2D> boundStream = Arrays.stream(bounds);
        BoundPointsPainter boundsPainter = shipPainter.getBoundsPainter();
        boundsPainter.clearPoints();
        boundStream.forEach(bound -> {
            Point2D rotatedPosition = ShipPainterInitialization.rotatePointByCenter(bound, translatedCenter);
            BoundPoint boundPoint = new BoundPoint(rotatedPosition, shipPainter);
            boundsPainter.addPoint(boundPoint);
        });
    }

    @SuppressWarnings("OverlyCoupledMethod")
    private static void initSlots(ShipPainter shipPainter, HullSpecFile hullSpecFile, Point2D translatedCenter) {
        WeaponSlot[] weaponSlots = hullSpecFile.getWeaponSlots();
        if (weaponSlots == null || weaponSlots.length == 0) return;
        Stream<WeaponSlot> slotStream = Arrays.stream(weaponSlots);

        LaunchBayPainter bayPainter = shipPainter.getBayPainter();
        WeaponSlotPainter slotPainter = shipPainter.getWeaponSlotPainter();
        bayPainter.clearPoints();
        slotPainter.clearPoints();

        slotStream.forEach(weaponSlot -> {
            Integer renderOrderMod = weaponSlot.getRenderOrderMod();
            if (Objects.equals(weaponSlot.getType(), StringConstants.LAUNCH_BAY)) {


                Point2D[] locations = weaponSlot.getLocations();

                LaunchBay bay = new LaunchBay(weaponSlot.getId(), bayPainter);

                bay.setAngle(weaponSlot.getAngle());
                bay.setArc(weaponSlot.getArc());

                if (renderOrderMod != null) {
                    bay.setRenderOrderMod(renderOrderMod);
                }

                String weaponSize = weaponSlot.getSize();
                WeaponSize sizeInstance = WeaponSize.valueOf(weaponSize);
                bay.setWeaponSize(sizeInstance);

                String weaponMount = weaponSlot.getMount();
                WeaponMount mountInstance = WeaponMount.valueOf(weaponMount);
                bay.setWeaponMount(mountInstance);

                bayPainter.addBay(bay);

                for (Point2D location : locations) {
                    Point2D rotatedPosition = ShipPainterInitialization.rotatePointByCenter(location, translatedCenter);

                    LaunchPortPoint portPoint = new LaunchPortPoint(rotatedPosition, shipPainter, bay);
                    bayPainter.addPoint(portPoint);
                }
            } else {

                Point2D location = weaponSlot.getLocations()[0];
                Point2D rotatedPosition = ShipPainterInitialization.rotatePointByCenter(location, translatedCenter);

                WeaponSlotPoint slotPoint = new WeaponSlotPoint(rotatedPosition, shipPainter);
                slotPoint.setId(weaponSlot.getId());

                slotPoint.setAngle(weaponSlot.getAngle());
                slotPoint.setArc(weaponSlot.getArc());

                if (renderOrderMod != null) {
                    slotPoint.setRenderOrderMod(renderOrderMod);
                }

                String weaponType = weaponSlot.getType();
                WeaponType typeInstance = WeaponType.valueOf(weaponType);
                slotPoint.setWeaponType(typeInstance);

                String weaponSize = weaponSlot.getSize();
                WeaponSize sizeInstance = WeaponSize.valueOf(weaponSize);
                slotPoint.setWeaponSize(sizeInstance);

                String weaponMount = weaponSlot.getMount();
                WeaponMount mountInstance = WeaponMount.valueOf(weaponMount);
                slotPoint.setWeaponMount(mountInstance);

                slotPainter.addPoint(slotPoint);
            }

        });
    }

    private static void initEngines(ShipPainter shipPainter, HullSpecFile hullSpecFile, Point2D translatedCenter) {
        EngineSlotPainter engineSlotPainter = shipPainter.getEnginePainter();
        engineSlotPainter.clearPoints();

        EngineSlot[] engineSlots = hullSpecFile.getEngineSlots();
        if (engineSlots == null) return;
        Stream<EngineSlot> engineSlotStream = Arrays.stream(engineSlots);
        engineSlotStream.forEach(engineSlot -> {
            Point2D rawEnginePosition = engineSlot.getLocation();
            Point2D rotatedPosition = ShipPainterInitialization.rotatePointByCenter(rawEnginePosition, translatedCenter);

            EnginePoint newEnginePoint = new EnginePoint(rotatedPosition, shipPainter);

            newEnginePoint.setAngle(engineSlot.getAngle());
            newEnginePoint.setLength(engineSlot.getLength());
            newEnginePoint.setWidth(engineSlot.getWidth());

            double contrailSize = engineSlot.getContrailSize();
            newEnginePoint.setContrailSize((int) contrailSize);

            String styleID = engineSlot.getStyle();
            String customStyle = engineSlot.getStyleId();
            EngineStyle customStyleSpec = engineSlot.getStyleSpec();

            if (StringConstants.CUSTOM.equals(styleID) && customStyleSpec != null) {
                newEnginePoint.setCustomStyleSpec(customStyleSpec);
            } else if (StringConstants.CUSTOM.equals(styleID) && customStyle != null && !customStyle.isEmpty()) {
                newEnginePoint.setStyleID(customStyle);
                newEnginePoint.setStyleIsCustom(true);
            } else {
                newEnginePoint.setStyleID(styleID);
            }

            engineSlotPainter.addPoint(newEnginePoint);
        });
    }

    private static void initBuiltIns(ShipPainter shipPainter, HullSpecFile hullSpecFile) {
        var specBuiltIns = hullSpecFile.getBuiltInWeapons();
        if (specBuiltIns == null || specBuiltIns.isEmpty()) return;

        var runtimeBuiltIns = shipPainter.getBuiltInWeapons();
        runtimeBuiltIns.clear();

        specBuiltIns.forEach((slotID, weaponID) -> {
            WeaponCSVEntry weaponEntry = GameDataRepository.getWeaponByID(weaponID);
            if (weaponEntry != null) {
                WeaponSpecFile specFile = weaponEntry.getSpecFile();
                WeaponPainter weaponPainter = weaponEntry.createPainterFromEntry(null, specFile);
                InstalledFeature feature = InstalledFeature.of(slotID, weaponID, weaponPainter, weaponEntry);
                feature.setContainedInBuiltIns(true);
                runtimeBuiltIns.put(slotID, feature);
            } else {
                if (SettingsManager.areFileErrorPopupsEnabled()) {
                    String message = "Weapon entry for initialized ship (" + shipPainter.getBaseHullId() + ") not found: " + weaponID;
                    Errors.showFileError(message, new NoSuchElementException(message));
                }
            }

        });

        var builtInModules = hullSpecFile.getBuiltInModules();
        if (builtInModules != null) {
            Map<String, InstalledFeature> runtimeModules = new ListOrderedMap<>();
            builtInModules.forEach((slotID, variantID) -> {
                VariantFile variant = GameDataRepository.getVariantByID(variantID);
                InstalledFeature moduleFeature = GameDataRepository.createModuleFromVariant(slotID, variant);
                runtimeModules.put(slotID, moduleFeature);
            });

            shipPainter.setBuiltInModules(runtimeModules);
        }

    }

    /**
     * Rotates point 90 degrees counterclockwise around specified center point.
     * @param input point to be rotated.
     * @param translatedCenter center point around which the rotation is performed.
     * @return new {@code Point2D} representing the rotated point.
     */
    public static Point2D rotatePointByCenter(Point2D input, Point2D translatedCenter) {
        double translatedX = -input.getY() + translatedCenter.getX();
        double translatedY = -input.getX() + translatedCenter.getY();
        return new Point2D.Double(translatedX, translatedY);
    }

    private static Point2D rotateCenter(Point2D hullCenter, Point2D anchor) {
        double anchorX = anchor.getX();
        double anchorY = anchor.getY();
        return new Point2D.Double(hullCenter.getX() + anchorX, -hullCenter.getY() + anchorY);
    }

}
