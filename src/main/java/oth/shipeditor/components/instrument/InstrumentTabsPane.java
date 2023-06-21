package oth.shipeditor.components.instrument;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentSplitterResized;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.viewer.InstrumentMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Dictionary;
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

    @Getter @Setter
    private boolean minimized;

    private boolean restorationQueued;

    private boolean panelSwitched;

    public InstrumentTabsPane() {
        panelMode = new HashMap<>();
        this.initListeners();
        this.setTabPlacement(SwingConstants.LEFT);
        this.createTabs();
        panelSwitched = false;
    }

    private void initListeners() {
        this.addChangeListener(event -> {
            activePanel = (JPanel) getSelectedComponent();
            this.dispatchModeChange(activePanel);
            if (minimized) {
                this.restorationQueued = true;
            }
            panelSwitched = true;
        });
        this.addMouseListener(new MinimizeListener());
    }

    private void createTabs() {
        layerPanel = new LayerPropertiesPanel();
        panelMode.put(layerPanel, InstrumentMode.LAYER);
        this.addTab("Layer",layerPanel);
        this.setToolTipTextAt(indexOfComponent(layerPanel),
                "Layer properties");
        centerPointsPanel = new HullPointsPanel();
        panelMode.put(centerPointsPanel, InstrumentMode.CENTERS);
        this.addTab("Centers",centerPointsPanel);
        this.setToolTipTextAt(indexOfComponent(centerPointsPanel),
                "Ship center, collision, shield center and radius");
        boundsPanel = new BoundPointsPanel();
        panelMode.put(boundsPanel, InstrumentMode.BOUNDS);
        this.addTab("Bounds", boundsPanel);
        this.setToolTipTextAt(indexOfComponent(boundsPanel),
                "Ship bound polygon");
    }

    private void dispatchModeChange(JPanel active) {
        InstrumentMode selected = panelMode.get(active);
        EventBus.publish(new InstrumentModeChanged(selected));
        EventBus.publish(new ViewerRepaintQueued());
    }

    /**
     * This minimize-restore listener is inspired by behavior of tabbed panels in IntelliJ;
     * It is certainly not something implemented in a matter of minutes, took me some hours to tune all interactions.
     */
    private class MinimizeListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                int tabIndex = indexAtLocation(e.getX(), e.getY());
                if (tabIndex != -1 && !panelSwitched) {
                    if (isMinimized()) {
                        this.restoreTabbedPane();
                    } else {
                        this.minimizeTabbedPane();
                    }
                }
                panelSwitched = false;
            }
        }

        private void minimizeTabbedPane() {
            setMinimized(true);
            Dimension preferred = InstrumentTabsPane.this.getPreferredSize();
            Dimension minimizedSize = new Dimension(10, preferred.height);
            InstrumentTabsPane.this.setMinimumSize(minimizedSize);
            InstrumentTabsPane.this.setMaximumSize(minimizedSize);
            EventBus.publish(new InstrumentSplitterResized(true));
        }

        private void restoreTabbedPane() {
            setMinimized(false);
            InstrumentTabsPane.this.setMinimumSize(null);
            InstrumentTabsPane.this.setMaximumSize(null);
            EventBus.publish(new InstrumentSplitterResized(false));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (InstrumentTabsPane.this.restorationQueued) {
                int tabIndex = indexAtLocation(e.getX(), e.getY());
                if (tabIndex != -1) {
                    this.restoreTabbedPane();
                }
                InstrumentTabsPane.this.restorationQueued = false;
            }
        }


    }

}
