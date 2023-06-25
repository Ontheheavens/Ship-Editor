package oth.shipeditor.components.instrument;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentSplitterResized;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.utility.MinimizeListener;
import oth.shipeditor.utility.MinimizerWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 08.06.2023
 */
@SuppressWarnings("FieldCanBeLocal")
@Log4j2
public final class InstrumentTabsPane extends JTabbedPane {

    /**
     * Panel that is currently selected; depending on which panel it is interactivity of certain entities is resolved.
     */
    private JPanel activePanel;

    /**
     * Panel for data representation of ship bounds.
     */
    @Getter
    private BoundPointsPanel boundsPanel;

    private HullPointsPanel centerPointsPanel;

    private LayerPropertiesPanel layerPanel;

    private final Map<JPanel, InstrumentMode> panelMode;

    private final MinimizerWidget minimizer;

    public InstrumentTabsPane() {
        panelMode = new HashMap<>();
        this.minimizer = new MinimizerWidget(this.minimizeTabbedPane(), this.restoreTabbedPane());
        minimizer.setPanelSwitched(false);
        this.initListeners();
        this.setTabPlacement(SwingConstants.LEFT);
        this.createTabs();
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
        centerPointsPanel = new HullPointsPanel();
        panelMode.put(centerPointsPanel, InstrumentMode.CENTERS);
        this.addTab("Centers",centerPointsPanel);
        boundsPanel = new BoundPointsPanel();
        panelMode.put(boundsPanel, InstrumentMode.BOUNDS);
        this.addTab("Bounds", boundsPanel);
        updateTooltipText();
    }

    private void dispatchModeChange(JPanel active) {
        InstrumentMode selected = panelMode.get(active);
        EventBus.publish(new InstrumentModeChanged(selected));
        EventBus.publish(new ViewerRepaintQueued());
    }

    private Runnable minimizeTabbedPane() {
        return () -> {
            minimizer.setMinimized(true);
            Dimension preferred = this.getPreferredSize();
            Dimension minimizedSize = new Dimension(10, preferred.height);
            this.setMinimumSize(minimizedSize);
            this.setMaximumSize(minimizedSize);
            updateTooltipText();
            EventBus.publish(new InstrumentSplitterResized(true));
        };
    }

    private Runnable restoreTabbedPane() {
        return () -> {
            minimizer.setMinimized(false);
            this.setMinimumSize(null);
            this.setMaximumSize(null);
            updateTooltipText();
            EventBus.publish(new InstrumentSplitterResized(false));
        };
    }

    private void updateTooltipText() {
        String minimizePrompt = "Left-click to minimize panel";
        if (minimizer.isMinimized()) {
            minimizePrompt = "Left-click to expand panel";
        }
        String layerPanelLabel = "Layer properties";
        String centerPanelLabel = "Ship center, collision, shield center and radius";
        String boundPanelLabel = "Ship bound polygon";
        this.setToolTipTextAt(indexOfComponent(layerPanel),
                "<html>" + layerPanelLabel + "<br>" + minimizePrompt + "</html>");
        this.setToolTipTextAt(indexOfComponent(centerPointsPanel),
                "<html>" + centerPanelLabel + "<br>" + minimizePrompt + "</html>");
        this.setToolTipTextAt(indexOfComponent(boundsPanel),
                "<html>" + boundPanelLabel + "<br>" + minimizePrompt + "</html>");
    }

}
