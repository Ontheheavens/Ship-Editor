package oth.shipeditor.components.instrument.ship;

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
import oth.shipeditor.components.instrument.ship.variant.VariantDataPanel;
import oth.shipeditor.components.instrument.ship.variant.VariantModulesPanel;
import oth.shipeditor.components.instrument.ship.variant.VariantWeaponsPanel;

import javax.swing.*;
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
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    @SuppressWarnings("OverlyCoupledMethod")
    private void createTabs() {
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
        this.createTab(new SkinPanel(), EditorInstrument.SKIN_DATA);
        this.createTab(new JPanel(), EditorInstrument.SKIN_SLOTS);
        this.createTab(new VariantDataPanel(), EditorInstrument.VARIANT_DATA);
        this.createTab(new VariantWeaponsPanel(), EditorInstrument.VARIANT_WEAPONS);
        this.createTab(new VariantModulesPanel(), EditorInstrument.VARIANT_MODULES);
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
        int size = this.getTabCount();
        for (int i = 0; i < size - 1; i++) {
            this.setToolTipTextAt(i, minimizePrompt);
        }
    }

}
