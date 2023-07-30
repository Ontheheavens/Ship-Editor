package oth.shipeditor.components.layering;

import lombok.Getter;
import lombok.Setter;
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
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.components.SortableTabbedPane;
import oth.shipeditor.utility.text.StringValues;

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

    @SuppressWarnings({"OverlyCoupledMethod", "ChainOfInstanceofChecks", "OverlyComplexMethod"})
    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                Icon tabIcon = FontIcon.of(BoxiconsRegular.ROCKET, 20);
                ShipLayer layer = checked.newLayer();
                LayerTab created = new LayerTab(layer);
                tabIndex.put(layer, created);
                String tooltip = created.getTabTooltip();
                this.addTab(LAYER + getTabCount(), tabIcon, tabIndex.get(layer), tooltip);
                EventBus.publish(new WindowRepaintQueued());
            }
            else if (event instanceof WeaponLayerCreated checked) {
                Icon tabIcon = FontIcon.of(BoxiconsRegular.TARGET_LOCK, 20);
                WeaponLayer layer = checked.newLayer();
                LayerTab created = new LayerTab(layer);
                tabIndex.put(layer, created);
                String tooltip = created.getTabTooltip();
                this.addTab(LAYER + getTabCount(), tabIcon, tabIndex.get(layer), tooltip);
                EventBus.publish(new WindowRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                ViewerLayer eventLayer = checked.updated();
                LayerTab updated = tabIndex.get(eventLayer);
                LayerPainter painter = eventLayer.getPainter();
                BufferedImage sprite = painter.getSprite();
                if (sprite != null) {
                    updated.setSpriteFileName(eventLayer.getSpriteFileName());
                    this.setToolTipTextAt(indexOfComponent(updated), updated.getTabTooltip());
                }
                if (!(eventLayer instanceof ShipLayer checkedLayer)) return;
                ShipData shipData = checkedLayer.getShipData();
                if (shipData == null) return;
                Hull hull = shipData.getHull();
                if (hull == null) return;

                updated.setHullFileName(checkedLayer.getHullFileName());
                this.setTitleAt(indexOfComponent(updated), hull.getHullName());

                ShipPainter layerPainter = checkedLayer.getPainter();
                if (layerPainter == null) return;
                Skin skin = layerPainter.getActiveSkin();
                if (skin == null || skin.isBase()) {
                    updated.setSkinFileName("");
                    this.setTitleAt(indexOfComponent(updated), hull.getHullName());
                } else {
                    String skinFileName = skin.getSkinFilePath().getFileName().toString();
                    updated.setSkinFileName(skinFileName);
                    if (skin.getHullName() != null) {
                        this.setTitleAt(indexOfComponent(updated), skin.getHullName());
                    }
                }
                this.setToolTipTextAt(indexOfComponent(updated), updated.getTabTooltip());
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

    /**
     * Empty marker component, only serves to track tabs and their layers.
     */
    private static final class LayerTab extends JPanel {

        @Getter
        private final ViewerLayer associatedLayer;

        @Getter @Setter
        private String spriteFileName;

        @Getter @Setter
        private String hullFileName;

        @Getter @Setter
        private String skinFileName;

        private LayerTab(ViewerLayer layer) {
            this.associatedLayer = layer;
            this.spriteFileName = layer.getSpriteFileName();
            if (layer instanceof ShipLayer checked) {
                this.hullFileName = checked.getHullFileName();
                this.skinFileName = checked.getSkinFileName();
            }
            this.setLayout(new BorderLayout());
        }

        /**
         * @return HTML-formatted string that enables multi-line tooltip setup.
         */
        private String getTabTooltip() {
            String notLoaded = StringValues.NOT_LOADED;
            String sprite = spriteFileName;
            if (Objects.equals(sprite, "")) {
                sprite = notLoaded;
            }
            String spriteNameLine = StringValues.SPRITE_FILE + sprite;
            if (!(associatedLayer instanceof ShipLayer)) {
                return "<html>" + spriteNameLine + "</html>";
            }
            String hull = hullFileName;
            if (Objects.equals(hull, "")) {
                hull = notLoaded;
            }
            String hullNameLine = "Hull file: " + hull;
            String skin = skinFileName;
            if (Objects.equals(skin, "")) {
                skin = StringValues.NOT_LOADED;
            }
            String skinNameLine = "Skin file: " + skin;
            return "<html>" + spriteNameLine + "<br>" + hullNameLine + "<br>" + skinNameLine + "</html>";
        }

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
