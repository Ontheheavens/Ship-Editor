package oth.shipeditor.components.layering;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.WindowRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerRemovalQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.ViewerLayerRemovalConfirmed;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.weapons.WeaponLayerCreated;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponSprites;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.components.SortableTabbedPane;
import oth.shipeditor.utility.graphics.Sprite;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public final class ViewerLayersPanel extends SortableTabbedPane {

    private static final String LAYER = "Layer #";

    /**
     * Expected to be the same instance that is originally created and assigned in viewer;
     * Reference in this class is present for both conceptual and convenience purposes.
     */
    private final LayerManager layerManager;

    private final Map<ViewerLayer, LayerTab> tabIndex;

    public ViewerLayersPanel(LayerManager manager) {
        this.layerManager = manager;
        this.tabIndex = new HashMap<>();

        this.putClientProperty("JTabbedPane.tabClosable", true);
        this.putClientProperty("JTabbedPane.tabCloseToolTipText", "Remove this layer");
        this.putClientProperty( "JTabbedPane.tabCloseCallback", (IntConsumer) index -> {
            LayerTab tab = (LayerTab) getComponentAt(index);
            ViewerLayer layer = getLayerByTab(tab);
            EventBus.publish(new LayerRemovalQueued(layer));
        });

        this.initLayerListeners();
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.addChangeListener(event -> {
            ViewerLayer newlySelected = getLayerByTab((LayerTab) getSelectedComponent());
            log.info("Layer panel change!");
            // If the change results from the last layer being removed and the newly selected layer is null,
            // call to set active layer is unnecessary as this case is handled directly by layer manager.
            if (newlySelected != null) {
                layerManager.setActiveLayer(newlySelected);
            }
        });
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
    }

    @SuppressWarnings({"OverlyCoupledMethod", "ChainOfInstanceofChecks"})
    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                ShipLayer layer = checked.newLayer();
                Icon tabIcon = FontIcon.of(BoxiconsRegular.ROCKET, 20);
                ShipLayerTab created = new ShipLayerTab(layer);
                tabIndex.put(layer, created);
                String tooltip = created.getTabTooltip();
                this.addTab(LAYER + getTabCount(), tabIcon, tabIndex.get(layer), tooltip);
                EventBus.publish(new WindowRepaintQueued());
            }
            else if (event instanceof WeaponLayerCreated checked) {
                WeaponLayer layer = checked.newLayer();
                Icon tabIcon = FontIcon.of(BoxiconsRegular.TARGET_LOCK, 20);
                WeaponLayerTab created = new WeaponLayerTab(layer);
                tabIndex.put(layer, created);
                String tooltip = created.getTabTooltip();
                this.addTab(LAYER + getTabCount(), tabIcon, tabIndex.get(layer), tooltip);
                EventBus.publish(new WindowRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                this.handleTabUpdates(checked);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerLayerRemovalConfirmed checked) {
                ViewerLayer layer = checked.removed();
                closeLayer(layer);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer newlySelected = checked.selected();
                ViewerLayer selectedTabLayer = getLayerByTab((LayerTab) getSelectedComponent());
                if (newlySelected == selectedTabLayer) return;
                this.setSelectedIndex(indexOfComponent(tabIndex.get(newlySelected)));
            }
        });
    }

    private void handleTabUpdates(ActiveLayerUpdated event) {
        ViewerLayer eventLayer = event.updated();
        LayerTab updated = tabIndex.get(eventLayer);
        if (updated instanceof ShipLayerTab checkedShipTab && eventLayer instanceof ShipLayer checkedLayer) {
            this.updateShipTab(checkedShipTab, checkedLayer);
        } else {
            if (updated instanceof WeaponLayerTab checkedWeaponTab && eventLayer instanceof WeaponLayer checkedLayer) {
                this.updateWeaponTab(checkedWeaponTab, checkedLayer);
            }
        }
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private void updateShipTab(ShipLayerTab tab, ShipLayer layer) {
        LayerPainter painter = layer.getPainter();
        BufferedImage sprite = painter.getSprite();
        if (sprite != null) {
            tab.setSpriteFileName(layer.getSpriteFileName());
            this.setToolTipTextAt(indexOfComponent(tab), tab.getTabTooltip());
        }
        ShipData shipData = layer.getShipData();
        if (shipData == null) return;
        HullSpecFile hullSpecFile = shipData.getHullSpecFile();
        if (hullSpecFile == null) return;

        tab.setHullFileName(layer.getHullFileName());
        this.setTitleAt(indexOfComponent(tab), hullSpecFile.getHullName());

        ShipPainter layerPainter = layer.getPainter();
        if (layerPainter == null) return;

        tab.setSkinFileNames(layer.getSkinFileNames());
        ShipSkin activeSkin = layerPainter.getActiveSkin();
        if (activeSkin == null || activeSkin.isBase()) {
            tab.setActiveSkinFileName("");
            this.setTitleAt(indexOfComponent(tab), hullSpecFile.getHullName());
        } else {
            String skinFileName = activeSkin.getFileName();
            tab.setActiveSkinFileName(skinFileName);
            if (activeSkin.getHullName() != null) {
                this.setTitleAt(indexOfComponent(tab), activeSkin.getHullName());
            }
        }
        this.setToolTipTextAt(indexOfComponent(tab), tab.getTabTooltip());
    }

    private void updateWeaponTab(WeaponLayerTab tab, WeaponLayer layer) {
        WeaponPainter painter = layer.getPainter();
        WeaponMount mount = painter.getMount();
        WeaponSprites weaponSprites = painter.getWeaponSprites();

        Sprite mainSprite = weaponSprites.getMainSprite(mount);
        if (mainSprite != null) {
            tab.setSpriteName(mainSprite.name());
        }

        Sprite underSprite = weaponSprites.getUnderSprite(mount);
        if (underSprite != null) {
            tab.setUnderSpriteName(underSprite.name());
        }

        Sprite gunSprite = weaponSprites.getGunSprite(mount);
        if (gunSprite != null) {
            tab.setGunSpriteName(gunSprite.name());
        }

        Sprite glowSprite = weaponSprites.getGlowSprite(mount);
        if (glowSprite != null) {
            tab.setGlowSpriteName(glowSprite.name());
        }

        WeaponSpecFile specFile = layer.getSpecFile();
        if (specFile != null) {
            tab.setSpecFileName(layer.getSpecFileName());
        }

        String weaponName = layer.getWeaponName();
        if (weaponName != null && !weaponName.isEmpty()) {
            this.setTitleAt(indexOfComponent(tab), weaponName);
        }
        this.setToolTipTextAt(indexOfComponent(tab), tab.getTabTooltip());
    }

    @Override
    protected void sortTabObjects() {
        List<ViewerLayer> layers = new ArrayList<>(tabIndex.keySet());

        ToIntFunction<ViewerLayer> intFunction = layer -> indexOfComponent(tabIndex.get(layer));
        layers.sort(Comparator.comparingInt(intFunction));
        layerManager.setLayers(layers);
    }

    private void closeLayer(ViewerLayer layer) {
        this.removeTabAt(indexOfComponent(tabIndex.get(layer)));
        tabIndex.remove(layer);
        EventBus.publish(new WindowRepaintQueued());
    }

    private ViewerLayer getLayerByTab(LayerTab value) {
        ViewerLayer result;
        for (Map.Entry<ViewerLayer, LayerTab> entry : tabIndex.entrySet()) {
            LayerTab entryValue = entry.getValue();
            if (entryValue.equals(value)) {
                result = entry.getKey();
                return result;
            }
        }
        return null;
    }

}
