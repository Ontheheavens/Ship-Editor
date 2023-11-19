package oth.shipeditor.components.instrument.ship.skins;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SkinPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ActiveShipSpec;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

/**
 * @author Ontheheavens
 * @since 30.07.2023
 */
public class SkinDataPanel extends JPanel {

    private final JPanel chooserContainer;

    private final SkinInfoPanel infoPanel;

    public SkinDataPanel() {
        this.setLayout(new BorderLayout());
        chooserContainer = new JPanel();
        chooserContainer.setLayout(new BoxLayout(chooserContainer, BoxLayout.PAGE_AXIS));
        chooserContainer.setBorder(new EmptyBorder(4, 4, 4, 4));
        this.add(chooserContainer, BorderLayout.PAGE_START);

        infoPanel = new SkinInfoPanel();
        this.add(infoPanel, BorderLayout.CENTER);

        this.initLayerListeners();
        this.recreateSkinChooser(null);
    }

    private void recreateSkinChooser(ViewerLayer selected) {
        chooserContainer.removeAll();

        if (!(selected instanceof ShipLayer checkedLayer)) {
            chooserContainer.add(SkinDataPanel.createDisabledChooser());
            return;
        }

        ShipHull shipHull = checkedLayer.getHull();
        if (shipHull == null)  {
            chooserContainer.add(SkinDataPanel.createDisabledChooser());
            return;
        }
        JComboBox<ShipSkin> skinChooser = SkinDataPanel.getShipSkinComboBox(checkedLayer);

        chooserContainer.add(skinChooser);
        chooserContainer.add(Box.createVerticalGlue());
    }

    private static JComboBox<ShipSkin> getShipSkinComboBox(ShipLayer checkedLayer) {
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
        return skinChooser;
    }

    private void refreshPanel(ViewerLayer layer) {
        this.recreateSkinChooser(layer);
        if (layer != null) {
            infoPanel.refresh(layer.getPainter());
        } else {
            infoPanel.refresh(null);
        }

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
            if (event instanceof LayerWasSelected(ViewerLayer old, ViewerLayer selected)) {
                this.refreshPanel(selected);
            } else if (event instanceof ActiveLayerUpdated(ViewerLayer updated)) {
                this.refreshPanel(updated);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SkinPanelRepaintQueued) {
                this.repaint();
            }
        });
    }

}
