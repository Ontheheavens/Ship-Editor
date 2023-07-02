package oth.shipeditor.components.layering;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.WindowRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.*;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StringConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
@Log4j2
public final class ShipLayersPanel extends JTabbedPane {

    /**
     * Expected to be the same instance that is originally created and assigned in viewer;
     * Reference in this class is present for both conceptual and convenience purposes.
     */
    private final LayerManager layerManager;

    private final Map<ShipLayer, LayerTab> tabIndex;

    public ShipLayersPanel(LayerManager manager) {
        this.layerManager = manager;
        this.tabIndex = new HashMap<>();

        this.putClientProperty("JTabbedPane.tabClosable", true);
        this.putClientProperty("JTabbedPane.tabCloseToolTipText", "Remove this layer");
        this.putClientProperty( "JTabbedPane.tabCloseCallback", (IntConsumer) index -> {
            LayerTab tab = (LayerTab) getComponentAt(index);
            ShipLayer layer = getLayerByTab(tab);
            EventBus.publish(new LayerRemovalQueued(layer));
        });

        this.initLayerListeners();
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.addChangeListener(event -> {
            ShipLayer newlySelected = getLayerByTab((LayerTab) getSelectedComponent());
            log.info("Layer panel change!");
            // If the change results from the last layer being removed and the newly selected layer is null,
            // call to set active layer is unnecessary as this case is handled directly by layer manager.
            if (newlySelected != null) {
                layerManager.setActiveLayer(newlySelected);
            }
        });
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                Icon tabIcon = FontIcon.of(FluentUiRegularMZ.ROCKET_20, 20);
                ShipLayer layer = checked.newLayer();
                LayerTab created = new LayerTab(layer);
                tabIndex.put(layer, created);
                String tooltip = created.getTabTooltip();
                this.addTab("Layer #" + getTabCount(), tabIcon, tabIndex.get(layer), tooltip);
                EventBus.publish(new WindowRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                ShipLayer eventLayer = checked.updated();
                LayerTab updated = tabIndex.get(eventLayer);
                BufferedImage sprite = eventLayer.getShipSprite();
                ShipData shipData = eventLayer.getShipData();
                ShipLayer associatedLayer = updated.getAssociatedLayer();
                if (shipData == null) return;
                if (sprite != null) {
                    updated.setSpriteFileName(associatedLayer.getSpriteFileName());
                }
                Hull hull = shipData.getHull();
                if (hull != null) {
                    updated.setHullFileName(associatedLayer.getHullFileName());
                    this.setTitleAt(indexOfComponent(updated), hull.getHullName());
                }
                Skin skin = shipData.getSkin();
                if (skin != null) {
                    updated.setSkinFileName(associatedLayer.getSkinFileName());
                    if (skin.getHullName() != null) {
                        this.setTitleAt(indexOfComponent(updated), skin.getHullName());
                    }
                }
                this.setToolTipTextAt(indexOfComponent(updated), updated.getTabTooltip());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerRemovalConfirmed checked) {
                ShipLayer layer = checked.removed();
                closeLayer(layer);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer newlySelected = checked.selected();
                ShipLayer selectedTabLayer = getLayerByTab((LayerTab) getSelectedComponent());
                if (newlySelected == selectedTabLayer) return;
                this.setSelectedIndex(indexOfComponent(tabIndex.get(newlySelected)));
            }
        });
    }

    private void closeLayer(ShipLayer layer) {
        this.removeTabAt(indexOfComponent(tabIndex.get(layer)));
        tabIndex.remove(layer);
        EventBus.publish(new WindowRepaintQueued());
    }

    /**
     * Empty marker component, only serves to track tabs and their layers.
     */
    private static final class LayerTab extends JPanel {

        @Getter
        private final ShipLayer associatedLayer;

        @Getter @Setter
        private String spriteFileName;

        @Getter @Setter
        private String hullFileName;

        @Getter @Setter
        private String skinFileName;

        private LayerTab(ShipLayer layer) {
            this.associatedLayer = layer;
            this.spriteFileName = layer.getSpriteFileName();
            this.hullFileName = layer.getHullFileName();
            this.skinFileName = layer.getSkinFileName();
            this.setLayout(new BorderLayout());
        }

        /**
         * @return HTML-formatted string that enables multi-line tooltip setup.
         */
        private String getTabTooltip() {
            String notLoaded = "Not loaded";
            String sprite = spriteFileName;
            if (Objects.equals(sprite, "")) {
                sprite = notLoaded;
            }
            String spriteNameLine = "Sprite file: " + sprite;
            String hull = hullFileName;
            if (Objects.equals(hull, "")) {
                hull = notLoaded;
            }
            String hullNameLine = "Hull file: " + hull;
            String skin = skinFileName;
            if (Objects.equals(skin, "")) {
                skin = StringConstants.DEFAULT;
            }
            String skinNameLine = "Skin file: " + skin;
            return "<html>" + spriteNameLine + "<br>" + hullNameLine + "<br>" + skinNameLine + "</html>";
        }

    }

    private ShipLayer getLayerByTab(LayerTab value) {
        ShipLayer result;
        for (Map.Entry<ShipLayer, LayerTab> entry : tabIndex.entrySet()) {
            LayerTab entryValue = entry.getValue();
            if (entryValue.equals(value)) {
                result = entry.getKey();
                return result;
            }
        }
        return null;
    }

}
