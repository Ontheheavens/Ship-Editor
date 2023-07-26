package oth.shipeditor.components.instrument;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentSplitterResized;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.instrument.centers.CollisionPanel;
import oth.shipeditor.components.instrument.centers.ShieldPanel;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.utility.components.MinimizeListener;
import oth.shipeditor.utility.components.MinimizerWidget;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.06.2023
 */
@Log4j2
public final class InstrumentTabsPane extends JTabbedPane {

    @Getter
    private static InstrumentMode currentMode;

    /**
     * Panel that is currently selected; depending on which panel it is interactivity of certain entities is resolved.
     */
    private JPanel activePanel;

    /**
     * Panel for data representation of ship bounds.
     */
    @Getter
    private BoundPointsPanel boundsPanel;

    private CollisionPanel collisionPanel;

    private ShieldPanel shieldPanel;

    private LayerPropertiesPanel layerPanel;

    private final Map<JPanel, InstrumentMode> panelMode;

    private final MinimizerWidget minimizer;

    private final Dimension maximumPanelSize = new Dimension(300, Integer.MAX_VALUE);

    public InstrumentTabsPane() {
        panelMode = new HashMap<>();
        this.minimizer = new MinimizerWidget(this.minimizeTabbedPane(), this.restoreTabbedPane());
        minimizer.setPanelSwitched(false);
        this.initListeners();
        this.setTabPlacement(SwingConstants.LEFT);
        this.createTabs();
        this.setMinimumSize(maximumPanelSize);
        this.setMaximumSize(maximumPanelSize);
        this.dispatchModeChange((JPanel) getSelectedComponent());
    }

    private void initListeners() {
        this.addChangeListener(event -> {
            activePanel = (JPanel) getSelectedComponent();
            this.dispatchModeChange(activePanel);
            if (minimizer.isMinimized()) {
                minimizer.setRestorationQueued(true);
            }
            minimizer.setPanelSwitched(true);
        });
        this.addMouseListener(new MinimizeListener(this, this.minimizer));
    }

    private void createTabs() {
        layerPanel = new LayerPropertiesPanel();
        panelMode.put(layerPanel, InstrumentMode.LAYER);
        this.addTab("Layer",layerPanel);

        collisionPanel = new CollisionPanel();
        panelMode.put(collisionPanel, InstrumentMode.COLLISION);
        this.addTab(StringValues.COLLISION, collisionPanel);

        shieldPanel = new ShieldPanel();
        panelMode.put(shieldPanel, InstrumentMode.SHIELD);
        this.addTab(StringValues.SHIELD, shieldPanel);

        boundsPanel = new BoundPointsPanel();
        panelMode.put(boundsPanel, InstrumentMode.BOUNDS);
        this.addTab("Bounds", boundsPanel);

        JPanel weaponSlotsPanel = new JPanel();
        panelMode.put(weaponSlotsPanel, InstrumentMode.WEAPON_SLOTS);
        this.addTab("Weapon Slots", weaponSlotsPanel);

        JPanel engineSlotsPanel = new JPanel();
        panelMode.put(engineSlotsPanel, InstrumentMode.ENGINES);
        this.addTab("Engines", engineSlotsPanel);

        updateTooltipText();
    }

    private void dispatchModeChange(JPanel active) {
        InstrumentMode selected = panelMode.get(active);
        currentMode = selected;
        EventBus.publish(new InstrumentModeChanged(selected));
        EventBus.publish(new ViewerRepaintQueued());
    }

    private Runnable minimizeTabbedPane() {
        return () -> {
            minimizer.setMinimized(true);
            Dimension preferred = this.getPreferredSize();
            Dimension minimizedSize = new Dimension(0, preferred.height);
            this.setMinimumSize(minimizedSize);
            this.setMaximumSize(minimizedSize);
            updateTooltipText();
            EventBus.publish(new InstrumentSplitterResized(true));
        };
    }

    private Runnable restoreTabbedPane() {
        return () -> {
            minimizer.setMinimized(false);
            this.setMinimumSize(maximumPanelSize);
            this.setMaximumSize(maximumPanelSize);
            updateTooltipText();
            EventBus.publish(new InstrumentSplitterResized(false));
        };
    }

    private void updateTooltipText() {
        String minimizePrompt = "(Left-click to minimize panel)";
        if (minimizer.isMinimized()) {
            minimizePrompt = "(Left-click to expand panel)";
        }
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
