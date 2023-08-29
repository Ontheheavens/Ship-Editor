package oth.shipeditor.components.instrument.ship.skins;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SkinPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ActiveShipSpec;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.utility.StaticController;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

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
        Collection<ShipSkin> skins = checkedLayer.getSkins();
        ShipPainter painter = checkedLayer.getPainter();

        Vector<ShipSkin> model = new Vector<>(skins);

        JComboBox<ShipSkin> skinChooser = new JComboBox<>(model);
        skinChooser.setSelectedItem(painter.getActiveSkin());
        skinChooser.addActionListener(e -> {
            ShipSkin chosen = (ShipSkin) skinChooser.getSelectedItem();
            ActiveShipSpec spec;
            if (chosen != null && !chosen.isBase()) {
                spec = ActiveShipSpec.SKIN;
            } else {
                spec = ActiveShipSpec.HULL;
            }
            painter.setActiveSpec(spec, chosen);

        });
        skinChooser.setAlignmentX(Component.CENTER_ALIGNMENT);

        chooserContainer.add(skinChooser);
        chooserContainer.add(Box.createVerticalGlue());

    }

    private static JComboBox<ShipSkin> createDisabledChooser() {
        ShipSkin[] skinSpecFileArray = {new ShipSkin()};
        JComboBox<ShipSkin> skinChooser = new JComboBox<>(skinSpecFileArray);
        skinChooser.setSelectedItem(skinSpecFileArray[0]);
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
