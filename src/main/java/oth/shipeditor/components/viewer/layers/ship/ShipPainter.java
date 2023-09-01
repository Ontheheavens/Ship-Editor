package oth.shipeditor.components.viewer.layers.ship;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.components.SkinPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.ships.LayerShipDataInitialized;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipDataCreated;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.data.*;
import oth.shipeditor.components.viewer.painters.points.*;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Distinct from parent ship layer instance: present class has to do with direct visual representation.
 * Painter instance is not concerned with loading and file interactions, and leaves that to other classes.
 * @author Ontheheavens
 * @since 29.05.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public class ShipPainter extends LayerPainter {

    private static final char SPACE = ' ';

    @Getter
    private BoundPointsPainter boundsPainter;
    @Getter
    private CenterPointPainter centerPointPainter;

    @Getter
    private ShieldPointPainter shieldPointPainter;

    @Getter
    private WeaponSlotPainter weaponSlotPainter;

    @Getter
    private LaunchBayPainter bayPainter;

    @Getter
    private EngineSlotPainter enginePainter;

    /**
     * Backup for when sprite is switched to skin version.
     */
    @Getter @Setter
    private Sprite baseHullSprite;

    @Getter @Setter
    private ShipSkin activeSkin;

    @Getter @Setter
    private ShipVariant activeVariant;

    public ShipPainter(ShipLayer layer) {
        super(layer);
        this.initPainterListeners(layer);
        this.activateEmptySkin();
    }

    public void selectVariant(Variant variant) {
        if (variant instanceof VariantFile checked) {
            this.installVariant(checked);
        } else {
            activeVariant = (ShipVariant) variant;
        }
    }

    @Override
    public void setAnchor(Point2D anchor) {
        super.setAnchor(anchor);
        if (this.activeVariant != null && !activeVariant.isEmpty()) {

        }
    }

    private void installVariant(VariantFile file) {
        boolean empty = file.isEmpty();
        activeVariant = new ShipVariant(empty);
        if (!empty) {
            String variantId = file.getVariantId();

            activeVariant.initialize(file);

            var parentLayer = getParentLayer();
            var loadedVariants = parentLayer.getLoadedVariants();
            loadedVariants.put(variantId, activeVariant);
        }
    }

    private void activateEmptySkin() {
        this.setActiveSkin(ShipSkin.EMPTY);
    }

    /**
     * @param skin only evaluated if spec type is SKIN.
     */
    public void setActiveSpec(ActiveShipSpec type, ShipSkin skin) {
        ShipLayer parentLayer = this.getParentLayer();
        if (type == ActiveShipSpec.HULL) {
            this.setSprite(baseHullSprite.getSpriteImage());

            if (parentLayer != null) {
                parentLayer.setSpriteFileName(baseHullSprite.getFileName());
                parentLayer.setSkinFileName(StringValues.NOT_LOADED);
            }

            this.weaponSlotPainter.resetSkinSlotOverride();
            this.enginePainter.resetSkinSlotOverride();

            this.activateEmptySkin();
        } else {
            if (skin == null) {
                throw new IllegalArgumentException("Attempted to activate invalid skin!");
            }
            Sprite loadedSkinSprite = skin.getLoadedSkinSprite();
            this.setSprite(loadedSkinSprite.getSpriteImage());

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
                parentLayer.setSpriteFileName(loadedSkinSprite.getFileName());
                String skinFileName = skin.getSkinFilePath().getFileName().toString();
                parentLayer.setSkinFileName(skinFileName);
            }

            this.activeSkin = skin;
        }
        this.notifyLayerUpdate();
    }

    private void notifyLayerUpdate() {
        EventBus.publish(new ActiveLayerUpdated(this.getParentLayer()));
        EventBus.publish(new SkinPanelRepaintQueued());
        Events.repaintView();
    }

    @Override
    public ShipLayer getParentLayer() {
        if (super.getParentLayer() instanceof ShipLayer checked) {
            return checked;
        } else throw new IllegalStateException("Found illegal parent layer of ShipPainter!");
    }

    private void createPointPainters() {
        this.centerPointPainter = new CenterPointPainter(this);
        this.shieldPointPainter = new ShieldPointPainter(this);
        this.boundsPainter = new BoundPointsPainter(this);
        this.weaponSlotPainter = new WeaponSlotPainter(this);
        this.bayPainter = new LaunchBayPainter(this);
        this.enginePainter = new EngineSlotPainter(this);

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
        log.info("{} initialized!", this);
        EventBus.publish(new LayerShipDataInitialized(this));
        EventBus.publish(new ViewerRepaintQueued());
    }

    @SuppressWarnings("WeakerAccess")
    protected void initPainterListeners(ShipLayer layer) {
        if (layer == null) return;
        BusEventListener layerUpdateListener = event -> {
            if (event instanceof ShipDataCreated checked) {
                if (checked.layer() != layer) return;
                ShipData shipData = layer.getShipData();
                if (shipData != null && this.isUninitialized()) {
                    this.createPointPainters();
                    HullSpecFile hullSpecFile = shipData.getHullSpecFile();
                    ShipHull hull = layer.getHull();
                    hull.initialize(hullSpecFile);
                    ShipPainterInitialization.loadShipData(this, hullSpecFile);
                }
            }
        };
        List<BusEventListener> listeners = getListeners();
        listeners.add(layerUpdateListener);
        EventBus.subscribe(layerUpdateListener);
    }

    public ShipCenterPoint getShipCenter() {
        return this.centerPointPainter.getCenterPoint();
    }

    public Point2D getCenterAnchor() {
        Point2D anchor = getAnchor();
        BufferedImage sprite = getSprite();
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

}
