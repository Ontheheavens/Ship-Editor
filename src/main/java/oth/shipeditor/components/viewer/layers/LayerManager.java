package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SkinFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.*;
import oth.shipeditor.communication.events.viewer.layers.ships.LayerShipDataInitialized;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipDataCreated;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreationQueued;
import oth.shipeditor.communication.events.viewer.layers.weapons.WeaponLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.weapons.WeaponLayerCreationQueued;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.graphics.Sprite;

import javax.swing.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Ontheheavens
 * @since 27.05.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public class LayerManager {

    @Getter @Setter
    private List<ViewerLayer> layers = new ArrayList<>();

    @Getter
    private ViewerLayer activeLayer;

    public void initListeners() {
        this.initLayerListening();
        this.initOpenSpriteListener();
        this.initOpenHullListener();
    }

    private void activateLastLayer() {
        ViewerLayer next = layers.get(layers.size() - 1);
        this.setActiveLayer(next);
    }

    public void setActiveLayer(ViewerLayer newlySelected) {
        ViewerLayer old = this.activeLayer;
        this.activeLayer = newlySelected;
        StaticController.setActiveLayer(activeLayer);
        EventBus.publish(new LayerWasSelected(old, newlySelected));
    }

    @SuppressWarnings({"OverlyCoupledMethod", "ChainOfInstanceofChecks"})
    private void initLayerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreationQueued) {
                ShipLayer newLayer = new ShipLayer();
                layers.add(newLayer);
                EventBus.publish(new ShipLayerCreated(newLayer));
            } else if (event instanceof WeaponLayerCreationQueued) {
                WeaponLayer newLayer = new WeaponLayer();
                layers.add(newLayer);
                EventBus.publish(new WeaponLayerCreated(newLayer));
            }
        });
        // It is implicitly assumed that the last layer in list is also the one that was just created.
        EventBus.subscribe(event -> {
            if (event instanceof LastLayerSelectQueued) {
                activateLastLayer();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerRemovalQueued) {
                ViewerLayer selected = this.activeLayer;
                publishLayerRemoval(selected);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerRemovalQueued checked) {
                ViewerLayer layer = checked.layer();
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
        EventBus.subscribe(event -> {
            if (event instanceof LayerShipDataInitialized checked) {
                ShipPainter source = checked.source();
                this.setActiveLayer(source.getParentLayer());
                EventBus.publish(new ActiveLayerUpdated(this.getActiveLayer()));
            }
        });
    }

    private void publishLayerRemoval(ViewerLayer layer) {
        if (layers.size() >= 2) {
            ViewerLayer other = null;
            for (ViewerLayer checked : layers) {
                if (checked != layer) {
                    other = checked;
                }
            }
            this.setActiveLayer(other);
        } else {
            this.setActiveLayer(null);
        }
        layers.remove(layer);
        EventBus.publish(new ViewerLayerRemovalConfirmed(layer));
    }

    private void initOpenSpriteListener() {
        EventBus.subscribe(event -> {
            if (event instanceof SpriteOpened checked) {
                Sprite sprite = checked.sprite();
                if (activeLayer == null) return;
                if (activeLayer.getPainter() != null) {
                    throw new IllegalStateException("Sprite loaded onto existing sprite!");
                } else {
                    EventBus.publish(new LayerSpriteLoadQueued(activeLayer, sprite));
                }
            }
        });
    }

    private void initOpenHullListener() {
        EventBus.subscribe(event -> {
            if (event instanceof HullFileOpened checked) {
                Hull hull = checked.hull();
                if (activeLayer != null && activeLayer instanceof ShipLayer checkedLayer) {
                    ShipData data = checkedLayer.getShipData();
                    if (data != null ) {
                        data.setHull(hull);
                    } else {
                        ShipData newData = new ShipData(hull);
                        checkedLayer.setShipData(newData);
                    }
                    checkedLayer.setHullFileName(checked.hullFileName());
                    EventBus.publish(new ShipDataCreated(checkedLayer));
                } else {
                    throw new IllegalStateException("Hull file loaded onto invalid layer!");
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SkinFileOpened checked) {
                Skin skin = checked.skin();
                if (activeLayer != null && activeLayer instanceof ShipLayer checkedLayer) {
                    ShipData data = checkedLayer.getShipData();
                    if (data != null && data.getHull() != null) {
                        Hull baseHull = data.getHull();
                        String hullID = baseHull.getHullId();
                        if (!hullID.equals(skin.getBaseHullId())) {
                            Path skinFilePath = skin.getSkinFilePath();
                            JOptionPane.showMessageDialog(null,
                                    "Hull ID of active layer does not equal base hull ID of skin: " +
                                            Optional.of(skinFilePath.toString()).orElse(skin.toString()),
                                    "Ship ID mismatch!",
                                    JOptionPane.ERROR_MESSAGE);
                            throw new IllegalStateException("Illegal skin file opening operation!");
                        }
                        Map<String, Skin> skins = data.getSkins();
                        skins.put(skin.getSkinHullId(), skin);
                    } else {
                        throw new IllegalStateException("Skin file loaded onto a null ship data!");
                    }
                    EventBus.publish(new ActiveLayerUpdated(checkedLayer));
                } else {
                    throw new IllegalStateException("Skin file loaded onto invalid layer!");
                }
            }
        });
    }

}
