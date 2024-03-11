package oth.shipeditor.components.instrument.ship;

import com.formdev.flatlaf.FlatLaf;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.instrument.AbstractInstrumentsPane;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.bays.LaunchBaysPanel;
import oth.shipeditor.components.instrument.ship.bounds.BoundsPanel;
import oth.shipeditor.components.instrument.ship.builtins.hullmods.BuiltInHullmodsPanel;
import oth.shipeditor.components.instrument.ship.builtins.weapons.BuiltInWeaponsPanel;
import oth.shipeditor.components.instrument.ship.builtins.weapons.DecorativesPanel;
import oth.shipeditor.components.instrument.ship.builtins.wings.BuiltInWingsPanel;
import oth.shipeditor.components.instrument.ship.centers.CollisionPanel;
import oth.shipeditor.components.instrument.ship.centers.ShieldPanel;
import oth.shipeditor.components.instrument.ship.engines.EnginesPanel;
import oth.shipeditor.components.instrument.ship.hull.ShipLayerInfoPanel;
import oth.shipeditor.components.instrument.ship.skins.SkinDataPanel;
import oth.shipeditor.components.instrument.ship.skins.SkinSlotOverridesPanel;
import oth.shipeditor.components.instrument.ship.slots.WeaponSlotsPanel;
import oth.shipeditor.components.instrument.ship.variant.VariantDataPanel;
import oth.shipeditor.components.instrument.ship.variant.VariantWeaponsPanel;
import oth.shipeditor.components.instrument.ship.variant.modules.VariantModulesPanel;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Locale;
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
        FlatLaf.showMnemonics(this);

        this.createTab(new ShipLayerInfoPanel(), EditorInstrument.LAYER);
        this.setMnemonicAt(0, KeyEvent.VK_Y);

        this.createTab(new CollisionPanel(), EditorInstrument.COLLISION);
        this.setMnemonicAt(1, KeyEvent.VK_C);

        this.createTab(new ShieldPanel(), EditorInstrument.SHIELD);
        this.setMnemonicAt(2, KeyEvent.VK_S);

        this.createTab(new BoundsPanel(), EditorInstrument.BOUNDS);
        this.setMnemonicAt(3, KeyEvent.VK_B);

        this.createTab(new WeaponSlotsPanel(), EditorInstrument.WEAPON_SLOTS);
        this.setMnemonicAt(4, KeyEvent.VK_W);

        this.createTab(new LaunchBaysPanel(), EditorInstrument.LAUNCH_BAYS);
        this.setMnemonicAt(5, KeyEvent.VK_L);

        this.createTab(new EnginesPanel(), EditorInstrument.ENGINES);
        this.setMnemonicAt(6, KeyEvent.VK_E);

        this.createTab(new BuiltInHullmodsPanel(), EditorInstrument.BUILT_IN_MODS);
        this.setMnemonicAt(7, KeyEvent.VK_H);

        this.createTab(new BuiltInWingsPanel(), EditorInstrument.BUILT_IN_WINGS);
        this.setMnemonicAt(8, KeyEvent.VK_N);

        this.createTab(new BuiltInWeaponsPanel(), EditorInstrument.BUILT_IN_WEAPONS);
        this.setMnemonicAt(9, KeyEvent.VK_U);

        this.createTab(new DecorativesPanel(), EditorInstrument.DECORATIVES);
        this.setMnemonicAt(10, KeyEvent.VK_R);

        this.createTab(new SkinDataPanel(), EditorInstrument.SKIN_DATA);
        this.setMnemonicAt(11, KeyEvent.VK_K);

        this.createTab(new SkinSlotOverridesPanel(), EditorInstrument.SKIN_SLOTS);
        this.setMnemonicAt(12, KeyEvent.VK_O);

        this.createTab(new VariantDataPanel(), EditorInstrument.VARIANT_DATA);
        this.setMnemonicAt(13, KeyEvent.VK_V);

        this.createTab(new VariantWeaponsPanel(), EditorInstrument.VARIANT_WEAPONS);
        this.setMnemonicAt(14, KeyEvent.VK_T);

        this.createTab(new VariantModulesPanel(), EditorInstrument.VARIANT_MODULES);
        this.setMnemonicAt(15, KeyEvent.VK_M);

        updateTooltipText();

        // Temporary until skin editing is implemented.
        String skinSlotsTitle = EditorInstrument.SKIN_SLOTS.getTitle();
        int skinSlotsIndex = this.indexOfTab(skinSlotsTitle);
        this.setEnabledAt(skinSlotsIndex, false);
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
        for (int i = 0; i < size; i++) {
            String mnemonic = KeyEvent.getKeyText(this.getMnemonicAt(i)).toUpperCase(Locale.ROOT);
            String tooltip = Utility.getWithLinebreaks(minimizePrompt, "Hotkey: ALT + " + mnemonic);
            this.setToolTipTextAt(i, tooltip);
        }
    }

}
