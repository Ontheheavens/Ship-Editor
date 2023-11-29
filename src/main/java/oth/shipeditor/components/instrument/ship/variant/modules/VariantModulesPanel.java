package oth.shipeditor.components.instrument.ship.variant.modules;

import lombok.Getter;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.components.ShipEntryPicked;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.variant.AbstractVariantPanel;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 02.10.2023
 */
public class VariantModulesPanel extends AbstractVariantPanel {

    private final JPanel contentPanel;

    private final JPanel northPanel;

    private final JPanel controlPanel;

    private JPanel pickedModulePanel;

    @Getter
    private ModuleList modulesList;

    public VariantModulesPanel() {
        this.setLayout(new BorderLayout());

        northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        this.add(northPanel, BorderLayout.PAGE_START);

        controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        northPanel.add(controlPanel, BorderLayout.CENTER);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);

        ViewerLayer layer = StaticController.getActiveLayer();
        this.refreshPanel(layer);
        this.refreshModulePicker();

        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                if (!(checked.point() instanceof WeaponSlotPoint slotPoint)) return;
                if (modulesList != null && StaticController.getEditorMode() == EditorInstrument.VARIANT_MODULES) {
                    modulesList.selectEntryByPoint(slotPoint);
                }
            }
        });
    }

    private void installPlaceholders() {
        JPanel placeholder = this.createContentPlaceholder();
        Border flatBorder = new LineBorder(Color.LIGHT_GRAY);
        placeholder.setBorder(flatBorder);
        placeholder.setBackground(Themes.getListBackgroundColor());

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
            if (event instanceof InstrumentRepaintQueued(EditorInstrument editorMode)) {
                if (editorMode == EditorInstrument.VARIANT_MODULES) {
                    this.refreshPanel(StaticController.getActiveLayer());
                    this.refreshModulePicker();
                    // TODO: remember, immutability in module widgets needs to go!
                    this.refreshControlPanel();
                }
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
        VariantFile pickedForInstall = FeaturesOverseer.getModuleVariantForInstall();
        if (pickedForInstall != null) {
            pickedModulePanel = new JPanel();
            pickedModulePanel.setLayout(new BoxLayout(pickedModulePanel, BoxLayout.LINE_AXIS));
            pickedModulePanel.setBorder(new EmptyBorder(4, 4, 4, 4));

            String moduleText = pickedForInstall.toString();
            JLabel text = new JLabel(moduleText);
            text.setAlignmentX(0.5f);
            pickedModulePanel.add(Box.createHorizontalGlue());
            pickedModulePanel.add(text);
            pickedModulePanel.add(Box.createHorizontalGlue());

            Insets insets = new Insets(1, 0, 0, 0);
            ComponentUtilities.outfitPanelWithTitle(pickedModulePanel, insets, pickedModule);
        } else {
            FontIcon hintIcon = FontIcon.of(FluentUiRegularAL.INFO_28, 28, Themes.getIconColor());
            String hint = StringValues.USE_RIGHT_CLICK_CONTEXT_MENU_OF_GAME_DATA_WIDGET_TO_ADD_ENTRIES;
            pickedModulePanel = ComponentUtilities.createHintPanel(hint, hintIcon);
            Insets insets = new Insets(1, 0, 0, 0);
            ComponentUtilities.outfitPanelWithTitle(pickedModulePanel, insets, pickedModule);
        }
        northPanel.add(pickedModulePanel, BorderLayout.PAGE_END);

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
        ShipHull shipHull = checkedLayer.getHull();
        if (shipHull == null) {
            this.installPlaceholders();
            return;
        }

        ShipPainter painter = checkedLayer.getPainter();

        ShipVariant activeVariant = painter.getActiveVariant();

        if (activeVariant != null && !activeVariant.isEmpty()) {
            var listContainer = this.createModuleList();

            JScrollPane scroller = new JScrollPane(listContainer);
            contentPanel.add(scroller, BorderLayout.CENTER);
        } else {
            this.installPlaceholders();
        }

        refreshControlPanel();

        this.revalidate();
        this.repaint();
    }

    private void refreshControlPanel() {
        controlPanel.removeAll();

        if (modulesList == null) return;
        InstalledFeature selected = modulesList.getSelectedValue();
        if (selected == null) return;

        JPanel moduleControl = new ModuleControlPanel(selected, this);
        controlPanel.add(moduleControl, BorderLayout.CENTER);

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private ModuleList createModuleList() {

        DefaultListModel<InstalledFeature> listModel = new DefaultListModel<>();

        Consumer<InstalledFeature> removeAction = feature ->
                StaticController.actOnCurrentVariant((shipLayer, variant) -> {
                    Map<String, InstalledFeature> fittedModules = variant.getFittedModules();
                    if (fittedModules == null) {
                        return;
                    }
                    EditDispatch.postFeatureUninstalled(fittedModules,
                            feature.getSlotID(), feature, null);
                });

        Consumer<Map<String, InstalledFeature>> sortAction = rearranged ->
                StaticController.actOnCurrentVariant((shipLayer, variant) ->
                        variant.sortModules(rearranged));

        modulesList = new ModuleList(this::refreshControlPanel, listModel, removeAction, sortAction);
        modulesList.setBorder(new LineBorder(Color.LIGHT_GRAY));
        return modulesList;
    }

}
