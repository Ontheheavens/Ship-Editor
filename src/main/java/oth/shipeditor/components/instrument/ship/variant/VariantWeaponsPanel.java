package oth.shipeditor.components.instrument.ship.variant;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.components.WeaponEntryPicked;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.rendering.CustomTreeNode;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

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
            if (event instanceof InstrumentRepaintQueued(EditorInstrument editorMode)) {
                if (editorMode == EditorInstrument.VARIANT_WEAPONS) {
                    this.refreshPanel(StaticController.getActiveLayer());
                    this.refreshWeaponPicker();
                }
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
            FontIcon hintIcon = FontIcon.of(FluentUiRegularAL.INFO_28, 28, Themes.getIconColor());
            String weaponHint = StringValues.USE_RIGHT_CLICK_CONTEXT_MENU_OF_GAME_DATA_WIDGET_TO_ADD_ENTRIES;
            pickedWeaponPanel = ComponentUtilities.createHintPanel(weaponHint, hintIcon);
            Insets insets = new Insets(1, 0, 0, 0);
            ComponentUtilities.outfitPanelWithTitle(pickedWeaponPanel, insets, StringValues.PICKED_WEAPON);
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

        ShipHull shipHull = checkedLayer.getHull();
        if (shipHull == null) {
            return;
        }
        ShipPainter painter = checkedLayer.getPainter();

        ShipVariant activeVariant = painter.getActiveVariant();
        if (activeVariant != null && !activeVariant.isEmpty()) {
            weaponsTree.setSlotPainter(painter.getWeaponSlotPainter());
            weaponsTree.repopulateTree(activeVariant, checkedLayer);

            northPanel.add(VariantWeaponsPanel.createDataSummary(checkedLayer, activeVariant),
                    BorderLayout.CENTER);
        }
        this.revalidate();
        this.repaint();
    }

    private static JPanel createDataSummary(ShipLayer shipLayer, ShipVariant activeVariant) {
        JPanel container = new JPanel();
        ComponentUtilities.outfitPanelWithTitle(container, "Fitted weapons");
        container.setLayout(new GridBagLayout());

        JLabel shipOPCapLabel = new JLabel(StringValues.TOTAL_OP_CAPACITY);
        int shipOPTotalValue = shipLayer.getTotalOP();
        JLabel shipOPCap = new JLabel(String.valueOf(shipOPTotalValue));

        ComponentUtilities.addLabelAndComponent(container, shipOPCapLabel, shipOPCap, 0);

        JLabel usedOPTotalLabel = new JLabel("Used OP for ship:");
        int usedOP = shipLayer.getTotalUsedOP();
        JLabel usedOPTotal = new JLabel(String.valueOf(usedOP));

        ComponentUtilities.addLabelAndComponent(container, usedOPTotalLabel, usedOPTotal, 1);

        JLabel totalOPLabel = new JLabel("Total OP in weapons:");
        int totalOPInWeapons = activeVariant.getTotalOPInWeapons();
        JLabel value = new JLabel(String.valueOf(totalOPInWeapons));

        ComponentUtilities.addLabelAndComponent(container, totalOPLabel, value, 2);

        return container;
    }

}
