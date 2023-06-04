package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.*;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 27.05.2023
 */
@Log4j2
public class LayerManager {

    @Getter
    private final List<ShipLayer> layers = new ArrayList<>();

    @Getter
    private ShipLayer activeLayer;

    public void initListeners() {
        this.initLayerListening();
        this.initOpenSpriteListener();
        this.initOpenHullListener();
    }

    public void setActiveLayer(ShipLayer newlySelected) {
        ShipLayer old = this.activeLayer;
        this.activeLayer = newlySelected;
        EventBus.publish(new LayerWasSelected(old, newlySelected));
    }

    private void initLayerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerCreationQueued) {
                ShipLayer newLayer = new ShipLayer();
                layers.add(newLayer);
                EventBus.publish(new ShipLayerCreated(newLayer));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SelectedLayerRemovalQueued) {
                ShipLayer selected = this.activeLayer;
                if (!layers.isEmpty()) {
                    this.setActiveLayer(layers.get(layers.indexOf(selected) - 1));
                }
                layers.remove(selected);
                EventBus.publish(new ShipLayerRemovalConfirmed(selected));
            }
        });
    }

    private void initOpenSpriteListener() {
        EventBus.subscribe(event -> {
            if (event instanceof SpriteOpened checked) {
                BufferedImage sprite = checked.sprite();
                if (activeLayer != null) {
                    activeLayer.setShipSprite(sprite);
                    activeLayer.setSpriteFileName(checked.filename());
                    EventBus.publish(new ShipLayerUpdated(activeLayer, true));
                } else {
                    ShipLayer newLayer = new ShipLayer(sprite);
                    newLayer.setSpriteFileName(checked.filename());
                    activeLayer = newLayer;
                    layers.add(newLayer);
                    EventBus.publish(new ShipLayerCreated(newLayer));
                }
            }
        });
    }

    private void initOpenHullListener() {
        EventBus.subscribe(event -> {
            if (event instanceof HullFileOpened checked) {
                Hull hull = checked.hull();
                if (activeLayer != null) {
                    ShipData data = activeLayer.getShipData();
                    if (data != null ) {
                        data.setHull(hull);
                    } else {
                        data = new ShipData(hull);
                        activeLayer.setShipData(data);
                    }
                    activeLayer.setHullFileName(checked.hullFileName());
                    EventBus.publish(new ShipLayerUpdated(activeLayer, false));
                } else {
                    ShipLayer newLayer = new ShipLayer(new ShipData(hull));
                    newLayer.setHullFileName(checked.hullFileName());
                    activeLayer = newLayer;
                    layers.add(newLayer);
                    EventBus.publish(new ShipLayerCreated(newLayer));
                }
            }
        });
    }

}
