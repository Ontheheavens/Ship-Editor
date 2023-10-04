package oth.shipeditor.components.instrument.ship.variant;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.VariantPanelRepaintQueued;
import oth.shipeditor.communication.events.components.WeaponEntryPicked;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.rendering.CustomTreeNode;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 26.09.2023
 */
public class VariantWeaponsPanel extends AbstractVariantPanel {

    private final VariantWeaponsTree weaponsTree;

    private final JPanel contentPanel;

    private final JPanel northPanel;

    private JPanel pickedWeaponPanel;


    public VariantWeaponsPanel() {
        this.setLayout(new BorderLayout());

        northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        this.add(northPanel, BorderLayout.PAGE_START);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);

        CustomTreeNode weaponGroups = new CustomTreeNode("Weapon Groups");
        weaponsTree = new VariantWeaponsTree(weaponGroups);
        ToolTipManager.sharedInstance().registerComponent(weaponsTree);

        JScrollPane scroller = new JScrollPane(weaponsTree);
        contentPanel.add(scroller, BorderLayout.CENTER);

        ViewerLayer layer = StaticController.getActiveLayer();
        this.refreshPanel(layer);
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
                this.refreshWeaponPicker();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof WeaponEntryPicked) {
                this.refreshWeaponPicker();
            }
        });
    }

    private void refreshWeaponPicker() {
        if (pickedWeaponPanel != null) {
            contentPanel.remove(pickedWeaponPanel);
        }

        WeaponCSVEntry pickedForInstall = FeaturesOverseer.getWeaponForInstall();
        if (pickedForInstall != null) {
            pickedWeaponPanel = pickedForInstall.createPickedWeaponPanel();
        } else {
            FontIcon hintIcon = FontIcon.of(FluentUiRegularAL.INFO_28, 28);
            String weaponHint = "Use right-click context menu of game data widget to add entries.";
            pickedWeaponPanel = ComponentUtilities.createHintPanel(weaponHint, hintIcon);
        }
        contentPanel.add(pickedWeaponPanel, BorderLayout.PAGE_START);

        this.revalidate();
        this.repaint();
    }

    @Override
    public void refreshPanel(ViewerLayer selected) {
        weaponsTree.clearRoot();
        northPanel.removeAll();

        if (!(selected instanceof ShipLayer checkedLayer)) {
            return;
        }

        ShipData shipData = checkedLayer.getShipData();
        if (shipData == null) {
            return;
        }
        ShipPainter painter = checkedLayer.getPainter();

        ShipVariant activeVariant = painter.getActiveVariant();
        if (activeVariant != null && !activeVariant.isEmpty()) {
            weaponsTree.setSlotPainter(painter.getWeaponSlotPainter());
            weaponsTree.repopulateTree(activeVariant, checkedLayer);

            northPanel.add(VariantWeaponsPanel.createDataSummary(activeVariant), BorderLayout.CENTER);
        }
        this.revalidate();
        this.repaint();
    }

    private static JPanel createDataSummary(ShipVariant activeVariant) {
        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());

        var allWeapons = activeVariant.getAllFittedWeapons();
        int totalOP = 0;

        for (InstalledFeature feature : allWeapons.values()) {
            totalOP += feature.getOPCost();
        }

        JLabel totalOPLabel = new JLabel("Total OP in weapons:");
        JLabel value = new JLabel(String.valueOf(totalOP));

        ComponentUtilities.addLabelAndComponent(container, totalOPLabel, value, 0);

        return container;
    }

}
