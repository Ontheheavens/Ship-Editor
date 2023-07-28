package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.instrument.AbstractInstrumentsPane;
import oth.shipeditor.components.instrument.ship.centers.CollisionPanel;
import oth.shipeditor.components.instrument.ship.centers.ShieldPanel;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.utility.text.StringValues;

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

    /**
     * Panel for data representation of ship bounds.
     */
    @Getter
    private BoundPointsPanel boundsPanel;

    private CollisionPanel collisionPanel;

    private ShieldPanel shieldPanel;

    private ShipLayerPropertiesPanel layerPanel;

    private final Map<JPanel, ShipInstrument> panelMode;

    public ShipInstrumentsPane() {
        panelMode = new HashMap<>();
        this.createTabs();
        this.dispatchModeChange((JPanel) getSelectedComponent());
    }

    private void createTabs() {
        layerPanel = new ShipLayerPropertiesPanel();
        panelMode.put(layerPanel, ShipInstrument.LAYER);
        this.addTab(StringValues.LAYER,layerPanel);

        collisionPanel = new CollisionPanel();
        panelMode.put(collisionPanel, ShipInstrument.COLLISION);
        this.addTab(StringValues.COLLISION, collisionPanel);

        shieldPanel = new ShieldPanel();
        panelMode.put(shieldPanel, ShipInstrument.SHIELD);
        this.addTab(StringValues.SHIELD, shieldPanel);

        boundsPanel = new BoundPointsPanel();
        panelMode.put(boundsPanel, ShipInstrument.BOUNDS);
        this.addTab("Bounds", boundsPanel);

        JPanel weaponSlotsPanel = new JPanel();
        panelMode.put(weaponSlotsPanel, ShipInstrument.WEAPON_SLOTS);
        this.addTab("Weapon Slots", weaponSlotsPanel);

        JPanel engineSlotsPanel = new JPanel();
        panelMode.put(engineSlotsPanel, ShipInstrument.ENGINES);
        this.addTab("Engines", engineSlotsPanel);

        updateTooltipText();
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
        String layerPanelLabel = StringValues.LAYER_PROPERTIES;
        String collisionPanelLabel = "Ship center and collision";
        String shieldPanelLabel = "Shield center and radius";
        String boundPanelLabel = "Ship bound polygon";
        this.setToolTipTextAt(indexOfComponent(layerPanel),
                "<html>" + layerPanelLabel + "<br>" + minimizePrompt + "</html>");
        this.setToolTipTextAt(indexOfComponent(collisionPanel),
                "<html>" + collisionPanelLabel + "<br>" + minimizePrompt + "</html>");
        this.setToolTipTextAt(indexOfComponent(shieldPanel),
                "<html>" + shieldPanelLabel + "<br>" + minimizePrompt + "</html>");
        this.setToolTipTextAt(indexOfComponent(boundsPanel),
                "<html>" + boundPanelLabel + "<br>" + minimizePrompt + "</html>");
    }

}
