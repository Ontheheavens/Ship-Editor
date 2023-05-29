package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.HullFileOpened;
import oth.shipeditor.communication.events.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.representation.data.Hull;
import oth.shipeditor.representation.data.ShipData;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 27.05.2023
 */
public class LayerManager {

    @Getter
    private final List<ShipLayer> layers = new ArrayList<>();

    @Getter @Setter
    private ShipLayer activeLayer;

    public LayerManager() {
        this.initListeners();
    }

    public void initListeners() {
        this.initOpenSpriteListener();
        this.initOpenHullListener();
    }

    private void initOpenSpriteListener() {
        EventBus.subscribe(SpriteOpened.class, event -> {
            BufferedImage sprite = event.sprite();
            if (activeLayer != null) {
                activeLayer.setShipSprite(sprite);
                EventBus.publish(new ShipLayerUpdated(activeLayer));
            } else {
                ShipLayer newLayer = new ShipLayer(sprite);
                activeLayer = newLayer;
                layers.add(newLayer);
                EventBus.publish(new ShipLayerCreated(newLayer));
            }
        });
    }

    private void initOpenHullListener() {
        EventBus.subscribe(HullFileOpened.class, event -> {
            Hull hull = event.hull();
            if (activeLayer != null) {
                ShipData data = activeLayer.getShipData();
                if (data != null ) {
                    data.setHull(hull);
                } else {
                    data = new ShipData(hull);
                    activeLayer.setShipData(data);
                }
                EventBus.publish(new ShipLayerUpdated(activeLayer));
            } else {
                ShipLayer newLayer = new ShipLayer(new ShipData(hull));
                activeLayer = newLayer;
                layers.add(newLayer);
                EventBus.publish(new ShipLayerCreated(newLayer));
            }
        });
    }

}
