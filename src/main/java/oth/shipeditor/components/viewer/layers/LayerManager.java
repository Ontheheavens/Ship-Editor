package oth.shipeditor.components.viewer.layers;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.*;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.*;
import oth.shipeditor.communication.events.viewer.layers.ships.LayerShipDataInitialized;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreationQueued;
import oth.shipeditor.communication.events.viewer.layers.weapons.WeaponLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.weapons.WeaponLayerCreationQueued;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ActiveShipSpec;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.SkinSpecFile;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author Ontheheavens
 * @since 27.05.2023
 */
@Getter
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public class LayerManager {

    @Setter
    private List<ViewerLayer> layers = new ArrayList<>();

    private ViewerLayer activeLayer;

    public void initListeners() {
        this.initLayerListening();
        this.initOpenSpriteListener();
        this.initOpenHullListener();
    }

    public boolean isEmpty() {
        return layers.isEmpty();
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

    public ShipLayer createShipLayer() {
        ShipLayer newLayer = new ShipLayer();
        layers.add(newLayer);
        EventBus.publish(new ShipLayerCreated(newLayer));
        return newLayer;
    }

    public WeaponLayer createWeaponLayer() {
        WeaponLayer newLayer = new WeaponLayer();
        layers.add(newLayer);
        EventBus.publish(new WeaponLayerCreated(newLayer));
        return newLayer;
    }

    @SuppressWarnings({"OverlyCoupledMethod", "ChainOfInstanceofChecks", "OverlyComplexMethod"})
    private void initLayerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreationQueued) {
                this.createShipLayer();
            } else if (event instanceof WeaponLayerCreationQueued) {
                this.createWeaponLayer();
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
                ShipLayer parentLayer = source.getParentLayer();
                if (parentLayer != null) {
                    this.setActiveLayer(parentLayer);
                    EventBus.publish(new ActiveLayerUpdated(this.getActiveLayer()));
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                setActiveLayer(checked.updated());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof HullmodDataSet) {
                actOnAllLayerHulls(ShipHull::loadBuiltInMods);
            } else if (event instanceof WingDataSet) {
                actOnAllLayerHulls(ShipHull::loadBuiltInWings);
            }
        });
    }

    private void actOnAllLayerHulls(BiConsumer<ShipHull, HullSpecFile> action) {
        layers.forEach(layer -> {
            if (layer instanceof ShipLayer checkedLayer) {
                ShipHull hull = checkedLayer.getHull();
                if (hull != null) {
                    ShipCSVEntry shipEntry = GameDataRepository.retrieveShipCSVEntryByID(hull.getHullID());
                    action.accept(hull, shipEntry.getHullSpecFile());
                }
            }
        });
        setActiveLayer(this.getActiveLayer());
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
                EventBus.publish(new LayerSpriteLoadQueued(activeLayer, sprite));
            }
        });
    }

    private void initOpenHullListener() {
        EventBus.subscribe(event -> {
            if (event instanceof HullFileOpened checked) {
                HullSpecFile hullSpecFile = checked.hullSpecFile();
                if (activeLayer != null && activeLayer instanceof ShipLayer checkedLayer) {
                    checkedLayer.initializeHullData(hullSpecFile);
                } else {
                    throw new IllegalStateException("Hull file loaded onto invalid layer!");
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SkinFileOpened checked) {
                SkinSpecFile skinSpecFile = checked.skinSpecFile();
                if (activeLayer != null && activeLayer instanceof ShipLayer checkedLayer) {
                    ShipHull data = checkedLayer.getHull();
                    if (data != null) {
                        LayerManager.openSkinFile(checked, checkedLayer, data, skinSpecFile);
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

    private static void openSkinFile(SkinFileOpened checkedEvent, ShipLayer checkedLayer,
                                     ShipHull data, SkinSpecFile skinSpecFile) {
        String hullID = data.getHullID();
        if (!hullID.equals(skinSpecFile.getBaseHullId())) {
            Path skinFilePath = skinSpecFile.getFilePath();
            JOptionPane.showMessageDialog(null,
                    "Hull ID of active layer does not equal base hull ID of skin: " +
                            Optional.of(skinFilePath.toString()).orElse(skinSpecFile.toString()),
                    "Ship ID mismatch!",
                    JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("Illegal skin file opening operation!");
        }
        ShipSkin created = checkedLayer.addSkin(skinSpecFile);
        if (checkedEvent.setAsActive()) {
            ShipPainter shipPainter = checkedLayer.getPainter();
            shipPainter.setActiveSpec(ActiveShipSpec.SKIN, created);
        }
    }

}
