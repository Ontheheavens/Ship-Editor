package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SkinFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.*;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.Skin;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 27.05.2023
 */
@SuppressWarnings("OverlyCoupledClass")
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

    private void activateNextLayer() {
        ShipLayer old = this.activeLayer;
        int nextIndex = layers.indexOf(old) + 1;
        if (nextIndex > layers.size() - 1) return;
        ShipLayer next = layers.get(nextIndex);
        this.setActiveLayer(next);
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
            if (event instanceof LayerCyclingQueued) {
                activateNextLayer();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerRemovalQueued) {
                ShipLayer selected = this.activeLayer;
                publishLayerRemoval(selected);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerRemovalQueued checked) {
                ShipLayer layer = checked.layer();
                publishLayerRemoval(layer);
            }
        });
        // Unsure if opacity listening belongs here conceptually, but it's not a big deal.
        EventBus.subscribe(event -> {
            if (event instanceof LayerOpacityChangeQueued checked) {
                if (activeLayer == null) return;
                LayerPainter painter = activeLayer.getPainter();
                if (painter == null) return;
                painter.setSpriteOpacity(checked.changedValue());
                EventBus.publish(new ViewerRepaintQueued());
            }
        });
    }

    private void publishLayerRemoval(ShipLayer layer) {
        if (layers.size() >= 2) {
            this.setActiveLayer(layers.get(layers.indexOf(layer) - 1));
        } else {
            this.setActiveLayer(null);
        }
        layers.remove(layer);
        EventBus.publish(new ShipLayerRemovalConfirmed(layer));
    }

    private void initOpenSpriteListener() {
        EventBus.subscribe(event -> {
            if (event instanceof SpriteOpened checked) {
                BufferedImage sprite = checked.sprite();
                if (activeLayer.getShipSprite() != null) {
                    throw new IllegalStateException("Sprite loaded onto existing sprite!");
                } else if (activeLayer != null) {
                    activeLayer.setShipSprite(sprite);
                    activeLayer.setSpriteFileName(checked.filename());
                    EventBus.publish(new ActiveLayerUpdated(activeLayer, true));
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
                    EventBus.publish(new ActiveLayerUpdated(activeLayer, false));
                } else {
                    throw new IllegalStateException("Hull file loaded into layer without a sprite!");
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SkinFileOpened checked) {
                Skin skin = checked.skin();
                if (activeLayer != null) {
                    ShipData data = activeLayer.getShipData();
                    if (data != null ) {
                        data.setSkin(skin);
                    } else {
                        throw new IllegalStateException("Skin file loaded onto a null ship data!");
                    }
                    activeLayer.setSkinFileName(checked.skinFileName());
                    EventBus.publish(new ActiveLayerUpdated(activeLayer, false));
                } else {
                    throw new IllegalStateException("Skin file loaded into a null layer!");
                }
            }
        });
    }

}
