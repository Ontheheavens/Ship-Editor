package oth.shipeditor.components.instrument.ship.variant;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.ShipEntryPicked;
import oth.shipeditor.communication.events.components.VariantModulesRepaintQueued;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.representation.ShipData;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 02.10.2023
 */
public class VariantModulesPanel extends AbstractVariantPanel{

    private final JPanel contentPanel;

    private final JPanel northPanel;

    private JPanel pickedModulePanel;

    @SuppressWarnings("FieldCanBeLocal")
    private InstalledFeatureList modulesList;

    public VariantModulesPanel() {
        this.setLayout(new BorderLayout());

        northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        this.add(northPanel, BorderLayout.PAGE_START);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);

        ViewerLayer layer = StaticController.getActiveLayer();
        this.refreshPanel(layer);
        this.refreshModulePicker();
    }

    private void installPlaceholders() {
        JPanel placeholder = this.createContentPlaceholder();
        Border flatBorder = new LineBorder(Color.LIGHT_GRAY);
        placeholder.setBorder(flatBorder);
        placeholder.setBackground(Color.WHITE);

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(2, 2, 2, 2));
        container.add(placeholder, BorderLayout.CENTER);
        contentPanel.add(container, BorderLayout.CENTER);
    }

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof VariantModulesRepaintQueued) {
                this.refreshPanel(StaticController.getActiveLayer());
                this.refreshModulePicker();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ShipEntryPicked) {
                this.refreshModulePicker();
            }
        });
    }

    @SuppressWarnings("InstanceVariableUsedBeforeInitialized")
    private void refreshModulePicker() {
        if (pickedModulePanel != null) {
            northPanel.remove(pickedModulePanel);
        }

        String pickedModule = StringValues.PICKED_FOR_INSTALL;
        ShipCSVEntry pickedForInstall = FeaturesOverseer.getModuleForInstall();
        if (pickedForInstall != null) {
            pickedModulePanel = new JPanel();
            pickedModulePanel.setLayout(new BoxLayout(pickedModulePanel, BoxLayout.LINE_AXIS));
            pickedModulePanel.setBorder(new EmptyBorder(4, 4, 4, 4));

            String moduleText = pickedForInstall.toString();
            JLabel entry = new JLabel("Module: ");
            JLabel text = new JLabel(moduleText);
            entry.setBorder(new EmptyBorder(0, 4, 0, 0));
            pickedModulePanel.add(entry);
            pickedModulePanel.add(Box.createHorizontalGlue());
            pickedModulePanel.add(text);

            Insets insets = new Insets(1, 0, 0, 0);
            ComponentUtilities.outfitPanelWithTitle(pickedModulePanel, insets, pickedModule);
        } else {
            FontIcon hintIcon = FontIcon.of(FluentUiRegularAL.INFO_28, 28);
            String hint = StringValues.USE_RIGHT_CLICK_CONTEXT_MENU_OF_GAME_DATA_WIDGET_TO_ADD_ENTRIES;
            pickedModulePanel = ComponentUtilities.createHintPanel(hint, hintIcon);
            Insets insets = new Insets(1, 0, 0, 0);
            ComponentUtilities.outfitPanelWithTitle(pickedModulePanel, insets, pickedModule);
        }
        northPanel.add(pickedModulePanel, BorderLayout.PAGE_START);

        this.revalidate();
        this.repaint();
    }

    @Override
    public void refreshPanel(ViewerLayer selected) {
        modulesList = null;
        contentPanel.removeAll();
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
            var listContainer = VariantModulesPanel.getModuleList(activeVariant, painter);

            JScrollPane scroller = new JScrollPane(listContainer);
            contentPanel.add(scroller, BorderLayout.CENTER);
        } else {
            this.installPlaceholders();
        }
        this.revalidate();
        this.repaint();
    }

    private static InstalledFeatureList getModuleList(ShipVariant activeVariant, ShipPainter painter) {
        Map<String, InstalledFeature> fittedModules = activeVariant.getFittedModules();
        Collection<InstalledFeature> modules = fittedModules.values();

        DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();
        listModel.addAll(modules);

        Consumer<InstalledFeature> removeAction = entry ->
                EditDispatch.postFeatureUninstalled(fittedModules,
                        entry.getSlotID(), entry, null);

        var slotPainter = painter.getWeaponSlotPainter();

        var listContainer = new InstalledFeatureList(listModel, slotPainter,
                removeAction, activeVariant::sortModules);
        listContainer.setBorder(new LineBorder(Color.LIGHT_GRAY));
        return listContainer;
    }

}
