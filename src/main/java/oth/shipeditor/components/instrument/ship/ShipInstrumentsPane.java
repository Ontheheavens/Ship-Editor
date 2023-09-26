package oth.shipeditor.components.instrument.ship;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatBorder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.instrument.AbstractInstrumentsPane;
import oth.shipeditor.components.instrument.ship.bays.LaunchBaysPanel;
import oth.shipeditor.components.instrument.ship.builtins.BuiltInHullmodsPanel;
import oth.shipeditor.components.instrument.ship.builtins.BuiltInWingsPanel;
import oth.shipeditor.components.instrument.ship.builtins.weapons.BuiltInWeaponsPanel;
import oth.shipeditor.components.instrument.ship.builtins.weapons.DecorativesPanel;
import oth.shipeditor.components.instrument.ship.centers.CollisionPanel;
import oth.shipeditor.components.instrument.ship.centers.ShieldPanel;
import oth.shipeditor.components.instrument.ship.engines.EnginesPanel;
import oth.shipeditor.components.instrument.ship.skins.SkinPanel;
import oth.shipeditor.components.instrument.ship.slots.WeaponSlotsPanel;
import oth.shipeditor.components.instrument.ship.variant.VariantPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.06.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public final class ShipInstrumentsPane extends AbstractInstrumentsPane {

    @Getter
    private static EditorInstrument currentMode;

    private final Map<JPanel, EditorInstrument> panelMode;

    public ShipInstrumentsPane() {
        panelMode = new HashMap<>();
        this.createTabs();
        this.dispatchModeChange((JPanel) getSelectedComponent());
    }

    @SuppressWarnings("OverlyCoupledMethod")
    private void createTabs() {
        this.putClientProperty("JTabbedPane.tabInsets", new Insets(0, 0, 0, 0));

        this.createTab(new ShipLayerPropertiesPanel(), EditorInstrument.LAYER);
        this.createTab(new CollisionPanel(), EditorInstrument.COLLISION);
        this.createTab(new ShieldPanel(), EditorInstrument.SHIELD);
        this.createTab(new BoundPointsPanel(), EditorInstrument.BOUNDS);
        this.createTab(new WeaponSlotsPanel(), EditorInstrument.WEAPON_SLOTS);
        this.createTab(new LaunchBaysPanel(), EditorInstrument.LAUNCH_BAYS);
        this.createTab(new EnginesPanel(), EditorInstrument.ENGINES);
        this.createTab(new BuiltInHullmodsPanel(), EditorInstrument.BUILT_IN_MODS);
        this.createTab(new BuiltInWingsPanel(), EditorInstrument.BUILT_IN_WINGS);
        this.createTab(new BuiltInWeaponsPanel(), EditorInstrument.BUILT_IN_WEAPONS);
        this.createTab(new DecorativesPanel(), EditorInstrument.DECORATIVES);
        this.createTab(new SkinPanel(), EditorInstrument.SKIN);
        this.createTab(new VariantPanel(), EditorInstrument.VARIANT);

        JPanel variantTab = new JPanel();
        variantTab.setLayout(new BoxLayout(variantTab, BoxLayout.PAGE_AXIS));
        variantTab.setBorder(new FlatBorder());
//        variantTab.setPreferredSize(new Dimension(110, 80));
//        variantTab.setOpaque(false);

        JLabel variantTitle = new JLabel("Variant");
        variantTitle.setAlignmentX(0.5f);
        JPanel titleContainer = new JPanel();
        titleContainer.setBorder(new EmptyBorder(2, 0, 2, 0));
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.LINE_AXIS));
        titleContainer.add(Box.createHorizontalGlue());
        titleContainer.add(variantTitle);
        titleContainer.add(Box.createHorizontalGlue());
        variantTab.add(titleContainer);

        JButton variantData = new JButton("Data");
        variantData.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        variantTab.add(variantData);

        JButton variantWeapons = new JButton("Weapons");
        variantWeapons.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        variantTab.add(variantWeapons);

        JButton variantModules = new JButton("Modules");
        variantModules.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        variantTab.add(variantModules);

        this.setTabComponentAt(12, variantTab);
        updateTooltipText();
    }

    private void createTab(JPanel panel, EditorInstrument mode) {
        panelMode.put(panel, mode);
        this.addTab(mode.getTitle(), panel);
    }

    @Override
    protected void dispatchModeChange(JPanel active) {
        EditorInstrument selected = panelMode.get(active);
        currentMode = selected;
        EventBus.publish(new InstrumentModeChanged(selected));
        EventBus.publish(new ViewerRepaintQueued());
    }

    @Override
    protected void updateTooltipText() {
        String minimizePrompt = getMinimizePrompt();
        int size = this.getComponentCount();
        for (int i = 0; i < size - 1; i++) {
            this.setToolTipTextAt(i, minimizePrompt);
        }
    }

}
