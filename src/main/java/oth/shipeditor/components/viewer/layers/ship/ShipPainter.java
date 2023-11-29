package oth.shipeditor.components.viewer.layers.ship;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.map.ListOrderedMap;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.ships.LayerShipDataInitialized;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ActiveShipSpec;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.layers.ship.data.Variant;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.ship.*;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeaturePainter;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.representation.ship.ShipTypeHints;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Distinct from parent ship layer instance: present class has to do with direct visual representation.
 * Painter instance is not concerned with loading and file interactions, and leaves that to other classes.
 * @author Ontheheavens
 * @since 29.05.2023
 */
@Getter
@SuppressWarnings({"OverlyCoupledClass", "ClassWithTooManyFields", "ClassWithTooManyMethods", "OverlyComplexClass"})
@Log4j2
public class ShipPainter extends LayerPainter {

    private static final char SPACE = ' ';

    private CenterPointPainter centerPointPainter;

    private ShieldPointPainter shieldPointPainter;

    private BoundPointsPainter boundsPainter;

    private WeaponSlotPainter weaponSlotPainter;

    private LaunchBayPainter bayPainter;

    private EngineSlotPainter enginePainter;

    @Setter
    private Map<String, InstalledFeature> builtInWeapons;

    private InstalledFeaturePainter installablesPainter;

    /**
     * Backup for when sprite is switched to skin version.
     */
    @Setter
    private Sprite baseHullSprite;

    @Setter
    private ShipSkin activeSkin;

    @Setter
    private ShipVariant activeVariant;

    @Setter
    private String baseHullId;

    public ShipPainter(ShipLayer layer) {
        super(layer);
        this.clearVariant();
        this.activateEmptySkin();
    }

    private void clearVariant() {
        this.selectVariant(VariantFile.empty());
    }

    public void selectVariant(Variant variant) {
        if (variant instanceof VariantFile checked) {
            this.installVariant(checked);
        } else {
            activeVariant = (ShipVariant) variant;
        }
        this.notifyLayerUpdate();
    }

    private void installVariant(VariantFile file) {
        boolean empty = file.isEmpty();

        if (!empty) {
            GameDataRepository gameData = SettingsManager.getGameData();
            if (!gameData.isWeaponsDataLoaded() || !gameData.isHullmodDataLoaded()) {
                JOptionPane.showMessageDialog(null,
                        "Weapon or hullmod data is not loaded to the editor, aborting variant initialization.",
                        StringValues.VARIANT_INITIALIZATION_ERROR,
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        activeVariant = new ShipVariant(empty);
        if (!empty) {
            String variantId = file.getVariantId();

            activeVariant.initialize(file);

            var parentLayer = getParentLayer();
            if (parentLayer != null) {
                var loadedVariants = parentLayer.getLoadedVariants();
                loadedVariants.put(variantId, activeVariant);
            }
        }
    }

    @Override
    public void cleanupForRemoval() {
        super.cleanupForRemoval();

        var allInstallables = this.getAllLoadedInstallables();
        allInstallables.forEach((s, installedFeature) -> {
            var painter = installedFeature.getFeaturePainter();
            painter.cleanupForRemoval();
        });

        var parentLayer = getParentLayer();
        if (parentLayer != null) {
            var featuresOverseer = parentLayer.getFeaturesOverseer();
            featuresOverseer.cleanupListeners();
        }
    }

    void activateEmptySkin() {
        this.setActiveSkin(ShipSkin.EMPTY);
    }

    /**
     * @param skin only evaluated if spec type is SKIN.
     */
    public void setActiveSpec(ActiveShipSpec type, ShipSkin skin) {
        ShipLayer parentLayer = this.getParentLayer();
        if (type == ActiveShipSpec.HULL) {
            this.setSprite(baseHullSprite);

            if (parentLayer != null) {
                parentLayer.setActiveSkinFileName(StringValues.NOT_LOADED);
            }

            this.weaponSlotPainter.resetSkinSlotOverride();
            this.enginePainter.resetSkinSlotOverride();

            this.activateEmptySkin();
        } else {
            if (skin == null) {
                throw new IllegalArgumentException("Attempted to activate invalid skin!");
            }
            Sprite loadedSkinSprite = skin.getLoadedSkinSprite();
            if (loadedSkinSprite != null) {
                this.setSprite(loadedSkinSprite);
            } else {
                this.setSprite(baseHullSprite);
            }

            if (skin.getWeaponSlotChanges() != null) {
                this.weaponSlotPainter.toggleSkinSlotOverride(skin);
            } else {
                this.weaponSlotPainter.resetSkinSlotOverride();
            }

            if (skin.getEngineSlotChanges() != null) {
                this.enginePainter.toggleSkinSlotOverride(skin);
            } else {
                this.enginePainter.resetSkinSlotOverride();
            }

            if (parentLayer != null) {
                String skinFileName = skin.getSkinFilePath().getFileName().toString();
                parentLayer.setActiveSkinFileName(skinFileName);
            }

            this.activeSkin = skin;
        }
        this.selectVariant(VariantFile.empty());
    }

    private void notifyLayerUpdate() {
        ShipLayer parentLayer = this.getParentLayer();
        if (parentLayer != null) {
            EventBus.publish(new ActiveLayerUpdated(parentLayer));
        }
        EventBus.publish(new InstrumentRepaintQueued(EditorInstrument.SKIN_DATA));
        EventBus.publish(new InstrumentRepaintQueued(EditorInstrument.SKIN_SLOTS));
        Events.repaintShipView();
    }

    @Override
    public ShipLayer getParentLayer() {
        ViewerLayer parentLayer = super.getParentLayer();
        if (parentLayer instanceof ShipLayer checked) {
            return checked;
        } else if (parentLayer != null) {
            throw new IllegalStateException("Found illegal parent layer of ShipPainter!");
        }
        return null;
    }

    private void createPointPainters() {
        if (!isUninitialized()) {
            cleanupPointPainters();
        }
        this.centerPointPainter = new CenterPointPainter(this);
        this.shieldPointPainter = new ShieldPointPainter(this);
        this.boundsPainter = new BoundPointsPainter(this);
        this.weaponSlotPainter = new WeaponSlotPainter(this);
        this.bayPainter = new LaunchBayPainter(this);
        this.enginePainter = new EngineSlotPainter(this);

        this.installablesPainter = new InstalledFeaturePainter();
        this.builtInWeapons = new ListOrderedMap<>();

        List<AbstractPointPainter> allPainters = getAllPainters();
        allPainters.add(centerPointPainter);
        allPainters.add(shieldPointPainter);
        allPainters.add(boundsPainter);
        allPainters.add(weaponSlotPainter);
        allPainters.add(bayPainter);
        allPainters.add(enginePainter);
    }

    void finishInitialization() {
        this.setUninitialized(false);
        log.trace("{} initialized!", this);
        EventBus.publish(new LayerShipDataInitialized(this));
        EventBus.publish(new ViewerRepaintQueued());
    }

    public void initFromHullSpec(HullSpecFile hullSpecFile) {
        this.createPointPainters();
        ShipPainterInitialization.loadHullData(this, hullSpecFile);
    }

    private List<BaseWorldPoint> getAllShipPoints() {
        List<BaseWorldPoint> result = new ArrayList<>();

        ShieldPointPainter shieldPainter = this.getShieldPointPainter();
        result.add(shieldPainter.getShieldCenterPoint());

        BoundPointsPainter boundPointsPainter = this.getBoundsPainter();
        result.addAll(boundPointsPainter.getPointsIndex());

        WeaponSlotPainter slotPainter = this.getWeaponSlotPainter();
        result.addAll(slotPainter.getPointsIndex());

        LaunchBayPainter launchBayPainter = this.getBayPainter();
        result.addAll(launchBayPainter.getPointsIndex());

        EngineSlotPainter engineSlotPainter = this.getEnginePainter();
        result.addAll(engineSlotPainter.getPointsIndex());

        return result;
    }

    public void flipShipPointsHorizontally() {
        EditDispatch.postShipPointsFlipped(getAllShipPoints(), this.getShipCenter());
    }

    public ShipCenterPoint getShipCenter() {
        return this.centerPointPainter.getCenterPoint();
    }

    @Override
    public Point2D getEntityCenter() {
        ShipCenterPoint shipCenter = this.getShipCenter();
        return shipCenter.getPosition();
    }

    @Override
    protected Point2D getRotationAnchor() {
        CenterPointPainter pointPainter = this.getCenterPointPainter();
        if (pointPainter == null) {
            return this.getSpriteCenter();
        }
        Point2D moduleAnchorOffset = pointPainter.getModuleAnchorOffset();
        if (moduleAnchorOffset == null) {
            return getEntityCenter();
        } else {
            Point2D entityCenter = getEntityCenter();
            double x = entityCenter.getX() - moduleAnchorOffset.getY();
            double y = entityCenter.getY() - moduleAnchorOffset.getX();
            return new Point2D.Double(x, y);
        }
    }

    public Point2D getCenterAnchor() {
        Point2D anchor = getAnchor();
        BufferedImage sprite = getSpriteImage();
        return new Point2D.Double( anchor.getX(), anchor.getY() + sprite.getHeight());
    }

    private Set<String> getAllSlotIDs() {
        WeaponSlotPainter slotPainter = this.getWeaponSlotPainter();
        List<WeaponSlotPoint> slotPoints = slotPainter.getSlotPoints();

        Set<String> slotIDs = slotPoints.stream()
                .map(WeaponSlotPoint::getId)
                .collect(Collectors.toSet());

        LaunchBayPainter launchBayPainter = this.getBayPainter();
        List<LaunchBay> layerBays = launchBayPainter.getBaysList();

        Set<String> bayIDs = layerBays.stream()
                .map(LaunchBay::getId)
                .collect(Collectors.toSet());

        slotIDs.addAll(bayIDs);

        return slotIDs;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isGeneratedIDUnassigned(String newId) {
        Set<String> existingIDs = this.getAllSlotIDs();

        for (String slotPointId : existingIDs) {
            if (slotPointId.equals(newId)) {
                JOptionPane.showMessageDialog(null,
                        "Input ID already assigned to slot.",
                        "Duplicate ID",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public String generateUniqueSlotID(String baseID) {
        Set<String> existingIDs = this.getAllSlotIDs();

        int suffix = 0;

        while (true) {
            String newID = baseID + " " + String.format("%03d", suffix);
            if (!existingIDs.contains(newID)) {
                return newID;
            }
            suffix++;
        }
    }

    public String incrementUniqueSlotID(String id) {
        Set<String> existingIDs = this.getAllSlotIDs();

        String baseID = id.substring(0, id.lastIndexOf(SPACE) + 1);
        int suffix = Integer.parseInt(id.substring(id.lastIndexOf(SPACE) + 1));

        while (true) {
            suffix++;
            String newID = baseID + String.format("%03d", suffix);
            if (!existingIDs.contains(newID)) {
                return newID;
            }
        }
    }

    public List<ShipTypeHints> getHintsModified() {
        ShipCSVEntry dataEntry = GameDataRepository.retrieveShipCSVEntryByID(this.getBaseHullId());

        var skin  = this.getActiveSkin();
        if (skin != null && !skin.isBase()) {
            return skin.getHintsModifiedBySkin();
        } else if (dataEntry != null) {
            return dataEntry.getBaseHullHints();
        }
        return null;
    }

    @SuppressWarnings("BooleanParameter")
    public Map<String, InstalledFeature> getBuiltInsWithSkin(boolean includeDecorative,
                                                             boolean includeNonDecorative) {
        Map<String, InstalledFeature> builtIns = this.getBuiltInWeapons();

        Map<String, InstalledFeature> result = new LinkedHashMap<>();
        var slotPainter = this.getWeaponSlotPainter();
        if (builtIns != null) {
            builtIns.forEach((slotID, feature) -> {
                boolean isSlotDecorative = slotPainter.isSlotDecorative(slotID);
                if (isSlotDecorative && includeDecorative) {
                    result.put(slotID, feature);
                } else if (slotPainter.getSlotByID(slotID) != null && !isSlotDecorative && includeNonDecorative) {
                    result.put(slotID, feature);
                }
            });
        }

        if (activeSkin != null && !activeSkin.isBase()) {
            addSkinEntriesToBuiltInList(includeDecorative, includeNonDecorative, result, slotPainter);
        }

        return result;
    }

    private void addSkinEntriesToBuiltInList(boolean includeDecorative, boolean includeNonDecorative,
                                             Map<String, InstalledFeature> result, WeaponSlotPainter slotPainter) {
        var removedBuiltIns = activeSkin.getRemoveBuiltInWeapons();
        if (removedBuiltIns != null) {
            removedBuiltIns.forEach(result::remove);
        }

        var addedBuiltIns = activeSkin.getInitializedBuiltIns();
        if (!addedBuiltIns.isEmpty()) {
            addedBuiltIns.forEach((slotID, feature) -> {
                boolean isSlotDecorative = slotPainter.isSlotDecorative(slotID);
                if (isSlotDecorative && includeDecorative) {
                    result.put(slotID, feature);
                } else if (slotPainter.getSlotByID(slotID) != null && !isSlotDecorative && includeNonDecorative) {
                    result.put(slotID, feature);
                }
            });
        }
    }

    private Map<String, InstalledFeature> getAllLoadedInstallables() {
        var builtIns = this.getBuiltInsWithSkin(true, true);
        Map<String, InstalledFeature> allFeatures = new LinkedHashMap<>(builtIns);

        ShipVariant shipVariant = this.getActiveVariant();
        Collection<ShipVariant> allLoaded = new HashSet<>();
        allLoaded.add(shipVariant);

        var parentLayer = this.getParentLayer();
        if (parentLayer != null) {
            var loadedToLayer = parentLayer.getLoadedVariants();
            allLoaded.addAll(loadedToLayer.values());
        }

        allLoaded.forEach(variant -> {
            if (variant != null && !variant.isEmpty()) {
                var modules = variant.getFittedModules();
                if (modules != null) {
                    allFeatures.putAll(modules);
                }
                var allWeapons = variant.getAllFittedWeapons();
                if (allWeapons != null) {
                    allFeatures.putAll(allWeapons);
                }
            }
        });

        return allFeatures;
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!isShouldDrawPainter()) return;
        if (this.isUninitialized()) {
            super.paint(g, worldToScreen, w, h);
        } else {
            var installedFeaturePainter = this.getInstallablesPainter();
            installedFeaturePainter.updateRenderQueue(this);
            installedFeaturePainter.paintUnderParent(g, worldToScreen, w, h);
            super.paint(g, worldToScreen, w, h);
            installedFeaturePainter.paintNormal(g, worldToScreen, w, h);
        }

    }

}
