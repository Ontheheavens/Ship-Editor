package oth.shipeditor.components.viewer.layers.ship;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.ships.LayerShipDataInitialized;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.points.*;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Distinct from parent ship layer instance: present class has to do with direct visual representation.
 * Painter instance is not concerned with loading and file interactions, and leaves that to other classes.
 * @author Ontheheavens
 * @since 29.05.2023
 */
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

    public ShipPainter(ShipLayer layer) {
        super(layer);
        this.initPainterListeners(layer);
    }

    private void createPointPainters() {
        this.centerPointPainter = new CenterPointPainter(this);
        this.shieldPointPainter = new ShieldPointPainter(this);
        this.boundsPainter = new BoundPointsPainter(this);
        this.weaponSlotPainter = new WeaponSlotPainter(this);

        List<AbstractPointPainter> allPainters = getAllPainters();
        allPainters.add(centerPointPainter);
        allPainters.add(shieldPointPainter);
        allPainters.add(boundsPainter);
        allPainters.add(weaponSlotPainter);
    }
    void finishInitialization() {
        this.setUninitialized(false);
        log.info("{} initialized!", this);
        EventBus.publish(new LayerShipDataInitialized(this));
        EventBus.publish(new ViewerRepaintQueued());
    }

    private void initPainterListeners(ShipLayer layer) {
        BusEventListener layerUpdateListener = event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                if (checked.updated() != layer) return;
                if (layer.getSprite() != null) {
                    this.setSprite(layer.getSprite());
                }
                if (layer.getShipData() != null && this.isUninitialized()) {
                    this.createPointPainters();
                    ShipPainterInitialization.loadShipData(this, layer.getShipData());
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
