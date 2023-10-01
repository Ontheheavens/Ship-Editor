package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.VariantPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.utility.components.rendering.CustomTreeNode;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 26.09.2023
 */
public class VariantWeaponsPanel extends AbstractVariantPanel {

    private final JPanel contentPanel;

    private VariantWeaponsTree weaponsTree;

    public VariantWeaponsPanel() {
        this.setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);

        ViewerLayer layer = StaticController.getActiveLayer();
        this.refreshPanel(layer);
    }

    private void installPlaceholders() {
        JPanel placeholder = this.createContentPlaceholder();
        contentPanel.add(placeholder, BorderLayout.CENTER);
    }

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                if (weaponsTree != null) {
                    weaponsTree.selectNode(checked.point());
                }
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof VariantPanelRepaintQueued) {
                this.refreshPanel(StaticController.getActiveLayer());
            }
        });
    }

    @Override
    public void refreshPanel(ViewerLayer selected) {
        contentPanel.removeAll();
        weaponsTree = null;

        if (!(selected instanceof ShipLayer checkedLayer)) {
            this.installPlaceholders();
            return;
        }

        ShipData shipData = checkedLayer.getShipData();
        if (shipData == null) {
            this.installPlaceholders();
            return;
        }

        ShipPainter painter = checkedLayer.getPainter();

        ShipVariant activeVariant = painter.getActiveVariant();
        if (activeVariant != null && !activeVariant.isEmpty()) {
            CustomTreeNode weaponGroups = new CustomTreeNode("Weapon Groups");
            weaponsTree = new VariantWeaponsTree(weaponGroups, painter.getWeaponSlotPainter());
            ToolTipManager.sharedInstance().registerComponent(weaponsTree);
            contentPanel.add(weaponsTree, BorderLayout.CENTER);
            weaponsTree.repopulateTree(activeVariant, checkedLayer);
        } else {
            this.installPlaceholders();
        }
    }

    private void populateVariantWeapons(ShipVariant variant) {
        JPanel weaponsPlaceholder = new JPanel();
        weaponsPlaceholder.setLayout(new BoxLayout(weaponsPlaceholder, BoxLayout.PAGE_AXIS));

        var allWeapons = variant.getAllFittedWeapons();

        if (allWeapons.isEmpty()) {
            weaponsPlaceholder.add(new JLabel("No installed weapons"));
        } else {
            allWeapons.forEach((slotID, installedFeature) -> {
                JLabel weaponEntry = new JLabel(slotID + ": " + installedFeature.getFeatureID());
                weaponsPlaceholder.add(weaponEntry);
            });
        }

        contentPanel.add(weaponsPlaceholder, BorderLayout.CENTER);
    }



}
