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
import oth.shipeditor.communication.events.viewer.layers.LayerSpriteLoadQueued;
import oth.shipeditor.communication.events.viewer.layers.ships.LayerShipDataInitialized;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipDataCreated;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ActiveShipSpec;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.painters.points.*;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Distinct from parent ship layer instance: present class has to do with direct visual representation.
 * Painter instance is not concerned with loading and file interactions, and leaves that to other classes.
 * @author Ontheheavens
 * @since 29.05.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public final class ShipPainter extends LayerPainter {

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

    /**
     * Backup for when sprite is switched to skin version.
     */
    @Getter @Setter
    private Sprite baseHullSprite;

    @Getter @Setter
    private ShipSkin activeSkin;

    public ShipPainter(ShipLayer layer) {
        super(layer);
        this.initPainterListeners(layer);
        this.activateEmptySkin();
    }

    private void activateEmptySkin() {
        ShipLayer layer = this.getParentLayer();
        List<ShipSkin> skins = layer.getSkins();
        this.setActiveSkin(skins.get(0));
    }

    /**
     * @param skin only evaluated if spec type is SKIN.
     */
    public void setActiveSpec(ActiveShipSpec type, ShipSkin skin) {
        ShipLayer parentLayer = this.getParentLayer();
        if (type == ActiveShipSpec.HULL) {
            this.setSprite(baseHullSprite.getSpriteImage());
            parentLayer.setSpriteFileName(baseHullSprite.getFileName());
            parentLayer.setSkinFileName(StringValues.NOT_LOADED);

            this.weaponSlotPainter.resetSkinSlotOverride();

            this.activateEmptySkin();
        } else {
            Sprite loadedSkinSprite = skin.getLoadedSkinSprite();
            this.setSprite(loadedSkinSprite.getSpriteImage());

            if (skin.getWeaponSlotChanges() != null) {
                this.weaponSlotPainter.toggleSkinSlotOverride(skin);
            }

            parentLayer.setSpriteFileName(loadedSkinSprite.getFileName());
            String skinFileName = skin.getSkinFilePath().getFileName().toString();
            parentLayer.setSkinFileName(skinFileName);
            this.activeSkin = skin;
        }
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

        List<AbstractPointPainter> allPainters = getAllPainters();
        allPainters.add(centerPointPainter);
        allPainters.add(shieldPointPainter);
        allPainters.add(boundsPainter);
        allPainters.add(weaponSlotPainter);
        allPainters.add(bayPainter);
    }
    void finishInitialization() {
        this.setUninitialized(false);
        log.info("{} initialized!", this);
        EventBus.publish(new LayerShipDataInitialized(this));
        EventBus.publish(new ViewerRepaintQueued());
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initPainterListeners(ShipLayer layer) {
        BusEventListener layerUpdateListener = event -> {
            if (event instanceof LayerSpriteLoadQueued checked) {
                if (checked.updated() != layer) return;
                if (layer.getSprite() != null) {
                    this.setSprite(layer.getSprite());
                }
            } else if (event instanceof ShipDataCreated checked) {
                if (checked.layer() != layer) return;
                if (layer.getShipData() != null && this.isUninitialized()) {
                    this.createPointPainters();
                    ShipPainterInitialization.loadShipData(this, layer);
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

}
