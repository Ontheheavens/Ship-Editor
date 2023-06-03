package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.WindowRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.ShipLayerUpdated;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.representation.ShipData;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
@Log4j2
public final class ShipLayersPanel extends JTabbedPane {

    private LayerManager layerManager;

    private Map<ShipLayer, LayerTab> tabIndex;

    public ShipLayersPanel(LayerManager manager) {
        this.layerManager = manager;
        this.tabIndex = new HashMap<>();
        this.initLayerListeners();
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                Icon tabIcon = FontIcon.of(FluentUiRegularMZ.ROCKET_20, 20);
                ShipLayer layer = checked.newLayer();
                tabIndex.put(layer,new LayerTab(layer));
                String tooltip = "<html>" + "Line One" +"<br>" + "Line 2" + "</html>";
                this.addTab("Layer #" + getTabCount(), tabIcon, tabIndex.get(layer), tooltip);
                EventBus.publish(new WindowRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerUpdated checked) {
                ShipLayer eventLayer = checked.updated();
                LayerTab updated = tabIndex.get(eventLayer);
                BufferedImage sprite = eventLayer.getShipSprite();
                ShipData hullFile = eventLayer.getShipData();
                ShipLayer associatedLayer = updated.getAssociatedLayer();
                if (sprite != null) {
                    updated.setSpriteFileName(associatedLayer.getSpriteFileName());

                }
                if (hullFile != null) {
                    updated.setHullFileName(associatedLayer.getHullFileName());
                }
            }
        });
    }

    private final class LayerTab extends JPanel {

        @Getter
        private final ShipLayer associatedLayer;

        private final JLabel spriteFileName;

        private final JLabel hullFileName;

        private LayerTab(ShipLayer layer) {
            this.associatedLayer = layer;
            this.spriteFileName = new JLabel(layer.getSpriteFileName());
            this.hullFileName = new JLabel(layer.getHullFileName());
            this.spriteFileName.setHorizontalAlignment(SwingConstants.CENTER);
            this.setLayout(new FlowLayout());
            this.add(spriteFileName);
            this.add(hullFileName);
        }

        void setSpriteFileName(String fileName) {
            this.spriteFileName.setText(fileName);
        }

        void setHullFileName(String fileName) {
            this.hullFileName.setText(fileName);
        }

    }



}
