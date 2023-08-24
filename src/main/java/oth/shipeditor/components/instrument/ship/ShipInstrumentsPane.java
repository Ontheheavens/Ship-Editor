package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.instrument.AbstractInstrumentsPane;
import oth.shipeditor.components.instrument.ship.bays.LaunchBaysPanel;
import oth.shipeditor.components.instrument.ship.centers.CollisionPanel;
import oth.shipeditor.components.instrument.ship.centers.ShieldPanel;
import oth.shipeditor.components.instrument.ship.engines.EnginesPanel;
import oth.shipeditor.components.instrument.ship.skins.SkinPanel;
import oth.shipeditor.components.instrument.ship.slots.WeaponSlotsPanel;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.06.2023
 */
@Log4j2
public final class ShipInstrumentsPane extends AbstractInstrumentsPane {

    @Getter
    private static ShipInstrument currentMode;

    private final Map<JPanel, ShipInstrument> panelMode;

    public ShipInstrumentsPane() {
        panelMode = new HashMap<>();
        this.createTabs();
        this.dispatchModeChange((JPanel) getSelectedComponent());
    }

    private void createTabs() {
        this.createTab(new ShipLayerPropertiesPanel(), ShipInstrument.LAYER);
        this.createTab(new CollisionPanel(), ShipInstrument.COLLISION);
        this.createTab(new ShieldPanel(), ShipInstrument.SHIELD);
        this.createTab(new BoundPointsPanel(), ShipInstrument.BOUNDS);
        this.createTab(new WeaponSlotsPanel(), ShipInstrument.WEAPON_SLOTS);
        this.createTab(new LaunchBaysPanel(), ShipInstrument.LAUNCH_BAYS);
        this.createTab(new EnginesPanel(), ShipInstrument.ENGINES);
        this.createTab(new BuiltInHullmodsPanel(), ShipInstrument.BUILT_IN_MODS);
        this.createTab(new SkinPanel(), ShipInstrument.SKIN);
        updateTooltipText();
    }

    private void createTab(JPanel panel, ShipInstrument mode) {
        panelMode.put(panel, mode);
        this.addTab(mode.getTitle(), panel);
    }

    @Override
    protected void dispatchModeChange(JPanel active) {
        ShipInstrument selected = panelMode.get(active);
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
