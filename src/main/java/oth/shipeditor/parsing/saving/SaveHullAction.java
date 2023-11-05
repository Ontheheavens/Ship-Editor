package oth.shipeditor.parsing.saving;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.map.ListOrderedMap;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.painters.points.ship.*;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.representation.ship.EngineSlot;
import oth.shipeditor.representation.ship.HullSize;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.representation.ship.HullStyle;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponSlot;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.ColorUtilities;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
final class SaveHullAction {

    private SaveHullAction() {
    }

    @SuppressWarnings("CallToPrintStackTrace")
    static void saveHullFromLayer(ShipLayer shipLayer) {
        JFileChooser fileChooser = SaveHullAction.getSaveHullFileChooser();
        int returnVal = fileChooser.showSaveDialog(null);
        FileUtilities.setLastDirectory(fileChooser.getCurrentDirectory());

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String extension = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
            File result = FileUtilities.ensureFileExtension(fileChooser, extension);

            log.info("Commencing hull saving: {}", result);

            ObjectMapper objectMapper = FileUtilities.getConfigured();
            HullSpecFile toSerialize = SaveHullAction.rebuildHullFile(shipLayer);
            try {
                objectMapper.writeValue(result, toSerialize);
            } catch (IOException e) {
                log.error("Hull file saving failed: {}", result.getName());
                JOptionPane.showMessageDialog(null,
                        "Hull file saving failed, exception thrown at: " + result,
                        StringValues.FILE_SAVING_ERROR,
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("OverlyCoupledMethod")
    private static HullSpecFile rebuildHullFile(ShipLayer shipLayer) {
        HullSpecFile result = new HullSpecFile();

        var shipPainter = shipLayer.getPainter();

        Point2D.Double[] serializableBounds = SaveHullAction.rebuildBounds(shipPainter);
        result.setBounds(serializableBounds);

        EngineSlot[] serializableEngines = SaveHullAction.rebuildEngineSlots(shipPainter);
        if (serializableEngines == null) {
            String shipID = shipLayer.getShipID();
            log.error("Engine misconfiguration at hull serialization. Ship ID: {}",
                    shipID);
            JOptionPane.showMessageDialog(null,
                    "Engine misconfiguration at hull serialization. " +
                            "Ship ID: " + shipID,
                    StringValues.FILE_SAVING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
        }
        result.setEngineSlots(serializableEngines);

        WeaponSlot[] serializableWeaponSlots = SaveHullAction.rebuildWeaponSlots(shipPainter);
        result.setWeaponSlots(serializableWeaponSlots);

        ShipHull shipHull = shipLayer.getHull();
        String[] serializableBuiltInWings = SaveHullAction.rebuildBuiltInWings(shipHull);
        if (serializableBuiltInWings != null) {
            result.setBuiltInWings(serializableBuiltInWings);
        }

        @SuppressWarnings("LocalVariableNamingConvention")
        Map<String, String> serializableBuiltInWeapons = SaveHullAction.rebuildBuiltInWeapons(shipLayer);
        if (serializableBuiltInWeapons != null) {
            result.setBuiltInWeapons(serializableBuiltInWeapons);
        }

        String[] serializableBuiltInMods = SaveHullAction.rebuildBuiltInMods(shipHull);
        if (serializableBuiltInMods != null) {
            result.setBuiltInMods(serializableBuiltInMods);
        }

        var runtimeCoversColor = shipHull.getCoversColor();
        if (runtimeCoversColor != null) {
            String serializableCoversColor = ColorUtilities.convertColorToString(runtimeCoversColor);
            result.setCoversColor(serializableCoversColor);
        } else {
            result.setCoversColor("");
        }

        result.setViewOffset(shipHull.getViewOffset());

        ShieldPointPainter shieldPointPainter = shipPainter.getShieldPointPainter();
        ShieldCenterPoint shieldCenterPoint = shieldPointPainter.getShieldCenterPoint();
        float shieldRadius = shieldCenterPoint.getShieldRadius();

        result.setShieldRadius(shieldRadius);

        Point2D shieldPosition = Utility.getPointCoordinatesForDisplay(shieldCenterPoint.getPosition(),
                shipPainter, CoordsDisplayMode.SHIP_CENTER);
        result.setShieldCenter((Point2D.Double) shieldPosition);

        CenterPointPainter centerPointPainter = shipPainter.getCenterPointPainter();
        ShipCenterPoint shipCenterPoint = centerPointPainter.getCenterPoint();
        float collisionRadius = shipCenterPoint.getCollisionRadius();
        result.setCollisionRadius(collisionRadius);

        Point2D centerPosition = Utility.getPointCoordinatesForDisplay(shipCenterPoint.getPosition(),
                shipPainter, CoordsDisplayMode.SHIPCENTER_ANCHOR);
        result.setCenter((Point2D.Double) centerPosition);

        Point2D moduleAnchor = centerPointPainter.getModuleAnchorOffset();
        if (moduleAnchor != null) {
            result.setModuleAnchor((Point2D.Double) moduleAnchor);
        }

        // TODO: implement proper dimension editing and serializing later.
        result.setHeight(shipPainter.getSpriteHeight());
        result.setWidth(shipPainter.getSpriteWidth());

        HullStyle hullStyle = shipHull.getHullStyle();
        result.setStyle(hullStyle.getHullStyleID());
        HullSize hullSize = shipHull.getHullSize();
        result.setHullSize(hullSize.toString());

        result.setSpriteName(shipLayer.getRelativeSpritePath());

        result.setHullId(shipHull.getHullID());
        result.setHullName(shipHull.getHullName());

        return result;
    }

    private static Point2D.Double[] rebuildBounds(ShipPainter shipPainter) {
        BoundPointsPainter boundsPainter = shipPainter.getBoundsPainter();
        var boundPoints = boundsPainter.getPointsIndex();

        Point2D.Double[] serializableBounds = new Point2D.Double[boundPoints.size()];

        for (int i = 0; i < boundPoints.size(); i++) {
            BoundPoint boundPoint = boundPoints.get(i);
            Point2D locationRelativeToCenter = Utility.getPointCoordinatesForDisplay(boundPoint.getPosition(),
                    shipPainter, CoordsDisplayMode.SHIP_CENTER);
            serializableBounds[i] = (Point2D.Double) locationRelativeToCenter;
        }

        return serializableBounds;
    }

    private static EngineSlot[] rebuildEngineSlots(ShipPainter shipPainter) {
        EngineSlotPainter enginePainter = shipPainter.getEnginePainter();
        var enginePoints = enginePainter.getPointsIndex();

        EngineSlot[] serializableEngines = new EngineSlot[enginePoints.size()];

        for (int i = 0; i < enginePoints.size(); i++) {
            EnginePoint enginePoint = enginePoints.get(i);

            EngineSlot serializableSlot = new EngineSlot();

            Point2D locationRelativeToCenter = Utility.getPointCoordinatesForDisplay(enginePoint.getPosition(),
                    shipPainter, CoordsDisplayMode.SHIP_CENTER);
            serializableSlot.setLocation((Point2D.Double) locationRelativeToCenter);

            serializableSlot.setAngle(enginePoint.getAngle());
            serializableSlot.setWidth(enginePoint.getWidth());
            serializableSlot.setLength(enginePoint.getLength());
            serializableSlot.setContrailSize(enginePoint.getContrailSize());

            var engineStyle = enginePoint.getStyle();
            if (engineStyle == null) {
                var customStyleSpec = enginePoint.getCustomStyleSpec();
                if (customStyleSpec != null) {
                    serializableSlot.setStyle(StringConstants.CUSTOM);
                    serializableSlot.setStyleSpec(customStyleSpec);
                } else {
                    return null;
                }
            } else {
                if (enginePoint.isStyleIsCustom()) {
                    serializableSlot.setStyle(StringConstants.CUSTOM);
                    serializableSlot.setStyleId(enginePoint.getStyleID());
                } else {
                    serializableSlot.setStyle(enginePoint.getStyleID());
                }
            }

            serializableEngines[i] = serializableSlot;
        }

        return serializableEngines;
    }

    private static WeaponSlot[] rebuildWeaponSlots(ShipPainter shipPainter) {
        WeaponSlot[] slotsFromWeapons = SaveHullAction.transformSlotsFromWeapons(shipPainter);
        WeaponSlot[] slotsFromBays = SaveHullAction.transformSlotsFromBays(shipPainter);

        Stream<WeaponSlot> weaponSlotStream = Stream.of(slotsFromWeapons,
                slotsFromBays).flatMap(Stream::of);

        return weaponSlotStream.toArray(WeaponSlot[]::new);
    }

    private static WeaponSlot[] transformSlotsFromWeapons(ShipPainter shipPainter) {
        WeaponSlotPainter slotPainter = shipPainter.getWeaponSlotPainter();
        var slotPoints = slotPainter.getPointsIndex();

        WeaponSlot[] serializableSlots = new WeaponSlot[slotPoints.size()];

        for (int i = 0; i < slotPoints.size(); i++) {
            WeaponSlotPoint slotPoint = slotPoints.get(i);

            WeaponSlot serializableSlot = SaveHullAction.createSerializable(slotPoint);

            Point2D locationRelativeToCenter = Utility.getPointCoordinatesForDisplay(slotPoint.getPosition(),
                    shipPainter, CoordsDisplayMode.SHIP_CENTER);
            Point2D.Double[] location = {(Point2D.Double) locationRelativeToCenter};
            serializableSlot.setLocations(location);

            int renderOrderMod = slotPoint.getRenderOrderMod();
            if (renderOrderMod != 0) {
                serializableSlot.setRenderOrderMod(renderOrderMod);
            }

            serializableSlots[i] = serializableSlot;
        }

        return serializableSlots;
    }

    private static WeaponSlot createSerializable(SlotData slotData) {
        WeaponSlot serializableSlot = new WeaponSlot();

        serializableSlot.setAngle(slotData.getAngle());
        serializableSlot.setArc(slotData.getArc());

        serializableSlot.setId(slotData.getId());

        WeaponSize weaponSize;
        WeaponType weaponType;
        WeaponMount weaponMount;

        // This is to ensure we serialize hull with the base values and not with skin overrides.
        if (slotData instanceof WeaponSlotPoint slotPoint) {
            weaponSize = slotPoint.getBaseSize();
            weaponType = slotPoint.getBaseType();
            weaponMount = slotPoint.getBaseMount();
        } else {
            weaponSize = slotData.getWeaponSize();
            weaponType = slotData.getWeaponType();
            weaponMount = slotData.getWeaponMount();
        }

        serializableSlot.setSize(weaponSize.getId());
        serializableSlot.setType(weaponType.getId());
        serializableSlot.setMount(weaponMount.getId());

        return serializableSlot;
    }

    private static WeaponSlot[] transformSlotsFromBays(ShipPainter shipPainter) {
        LaunchBayPainter bayPainter = shipPainter.getBayPainter();
        var bays = bayPainter.getBaysList();

        WeaponSlot[] serializableSlots = new WeaponSlot[bays.size()];

        for (int i = 0; i < bays.size(); i++) {
            LaunchBay launchBay = bays.get(i);

            WeaponSlot serializableSlot = SaveHullAction.createSerializable(launchBay);

            List<Point2D.Double> portPositions = new ArrayList<>();
            List<LaunchPortPoint> portPoints = launchBay.getPortPoints();
            portPoints.forEach(portPoint -> {
                Point2D.Double locationRelativeToCenter = (Point2D.Double)
                        Utility.getPointCoordinatesForDisplay(portPoint.getPosition(),
                        shipPainter, CoordsDisplayMode.SHIP_CENTER);
                portPositions.add(locationRelativeToCenter);
            });

            Point2D.Double[] locations = portPositions.toArray(new Point2D.Double[0]);
            serializableSlot.setLocations(locations);

            int renderOrderMod = launchBay.getRenderOrderMod();
            if (renderOrderMod != 0) {
                serializableSlot.setRenderOrderMod(renderOrderMod);
            }

            serializableSlots[i] = serializableSlot;
        }

        return serializableSlots;
    }

    private static String[] rebuildBuiltInWings(ShipHull shipHull) {
        var runtimeWings = shipHull.getBuiltInWings();
        if (runtimeWings == null || runtimeWings.isEmpty()) {
            return null;
        } else {
            String[] serializableWings = new String[runtimeWings.size()];

            for (int i = 0; i < runtimeWings.size(); i++) {
                var wingEntry = runtimeWings.get(i);
                serializableWings[i] = wingEntry.getWingID();
            }

            return serializableWings;
        }
    }

    private static Map<String, String> rebuildBuiltInWeapons(ShipLayer shipLayer) {
        ShipPainter shipPainter = shipLayer.getPainter();

        // Quick and hacky way of sorting entries: built-ins first, decoratives last.
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        var builtInsFromBaseHull = featuresOverseer.getBuiltInsFromBaseHull();
        featuresOverseer.setBaseBuiltInsWithNewNormal(builtInsFromBaseHull);

        var runtimeWeapons = shipPainter.getBuiltInWeapons();

        if (runtimeWeapons == null || runtimeWeapons.isEmpty()) {
            return null;
        } else {

            Map<String, String> serializableWeapons = new ListOrderedMap<>();

            runtimeWeapons.forEach((slotID, feature) -> serializableWeapons.put(slotID, feature.getID()));

            return serializableWeapons;
        }
    }

    private static String[] rebuildBuiltInMods(ShipHull shipHull) {
        var runtimeMods = shipHull.getBuiltInMods();
        if (runtimeMods == null || runtimeMods.isEmpty()) {
            return null;
        } else {
            String[] serializableMods = new String[runtimeMods.size()];

            for (int i = 0; i < runtimeMods.size(); i++) {
                var modEntry = runtimeMods.get(i);
                serializableMods[i] = modEntry.getHullmodID();
            }

            return serializableMods;
        }
    }

    private static JFileChooser getSaveHullFileChooser() {
        FileNameExtensionFilter shipFileFilter = new FileNameExtensionFilter(
                StringValues.JSON_SHIP_FILES, "ship");
        return FileUtilities.getFileChooser(shipFileFilter);
    }

}
