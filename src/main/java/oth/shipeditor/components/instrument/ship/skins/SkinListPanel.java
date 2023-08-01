package oth.shipeditor.components.instrument.ship.skins;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SkinPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ActiveShipSpec;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StaticController;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 30.07.2023
 */
public class SkinListPanel extends JPanel {

    private final JPanel chooserContainer;

    SkinListPanel() {
        this.setLayout(new BorderLayout());
        chooserContainer = new JPanel();
        chooserContainer.setLayout(new BoxLayout(chooserContainer, BoxLayout.PAGE_AXIS));
        this.add(chooserContainer, BorderLayout.CENTER);
        this.initLayerListeners();
        this.recreateSkinChooser();
    }

    private void recreateSkinChooser() {
        chooserContainer.removeAll();

        ViewerLayer selected = StaticController.getActiveLayer();
        if (!(selected instanceof ShipLayer checkedLayer)) {
            chooserContainer.add(SkinListPanel.createDisabledChooser());
            return;
        }

        ShipData shipData = checkedLayer.getShipData();
        if (shipData == null)  {
            chooserContainer.add(SkinListPanel.createDisabledChooser());
            return;
        }
        Map<String, Skin> skins = shipData.getSkins();
        ShipPainter painter = ((ShipLayer) selected).getPainter();


        Collection<Skin> values = skins.values();
        Skin[] skinArray = values.toArray(new Skin[0]);
        JComboBox<Skin> skinChooser = new JComboBox<>(skinArray);
        skinChooser.setSelectedItem(painter.getActiveSkin());
        skinChooser.addActionListener(e -> {
            Skin chosen = (Skin) skinChooser.getSelectedItem();
            ActiveShipSpec spec;
            String skinID = "";
            if (chosen != null && !chosen.isBase()) {
                spec = ActiveShipSpec.SKIN;
                skinID = chosen.getSkinHullId();
            } else {
                spec = ActiveShipSpec.HULL;
            }
            painter.setActiveSpec(spec, skinID);

        });
        skinChooser.setAlignmentX(Component.CENTER_ALIGNMENT);

        chooserContainer.add(skinChooser);
    }

    private static JComboBox<Skin> createDisabledChooser() {
        Skin[] skinArray = {Skin.empty()};
        JComboBox<Skin> skinChooser = new JComboBox<>(skinArray);
        skinChooser.setSelectedItem(skinArray[0]);
        skinChooser.setEnabled(false);
        return skinChooser;
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected) {
                this.recreateSkinChooser();
            } else if (event instanceof ActiveLayerUpdated) {
                this.recreateSkinChooser();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SkinPanelRepaintQueued) {
                this.repaint();
            }
        });
    }

}
