package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentSplitterResized;
import oth.shipeditor.communication.events.components.ShipViewableCreated;
import oth.shipeditor.components.datafiles.GameDataPanel;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.layering.ShipLayersPanel;
import oth.shipeditor.components.viewer.ShipViewable;
import oth.shipeditor.components.viewer.PrimaryShipViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@SuppressWarnings("FieldCanBeLocal")
@Log4j2
public class WindowContentPanes {

    /**
     * Parent pane for layers panel and others.
     */
    private JPanel northPane;

    /**
     * Complex component responsible for ship layers display.
     */
    @Getter
    private ShipViewable shipView;

    /**
     * Parent pane for ship data editing tabs.
     */
    private InstrumentTabsPane instrumentPane;


    private ShipLayersPanel layersPanel;

    /**
     * Parent pane for various status panels.
     */
    private JPanel southPane;

    /**
     * Status line panel for ship sprite viewer.
     */
    @Getter
    private ViewerStatusPanel statusPanel;

    private final Container primaryContentPane;

    /**
     * Holds datafile panel on the left and secondary split pane on the right.
     */
    @Getter
    private JSplitPane primaryLevel;

    /**
     * Holds viewer panel on the left and instrument panel on the right.
     */
    @Getter
    private JSplitPane secondaryLevel;

    private boolean instrumentPaneMinimized;

    private int cachedDividerLocation = -1;

    public WindowContentPanes(Container pane) {
        this.primaryContentPane = pane;
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentSplitterResized checked) {
                boolean minimize = checked.minimized();
                if (secondaryLevel == null) return;
                if (minimize) {
                    cachedDividerLocation = secondaryLevel.getDividerLocation();
                }
                instrumentPaneMinimized = minimize;
                relocateDivider();
            }
        });
    }

    /**
     * This technique does not use reflection unlike the others, but has a downside of having hardcoded magic number.
     */
    private void relocateDivider() {
        if (secondaryLevel == null) return;
        if (instrumentPaneMinimized) {
            secondaryLevel.setDividerLocation(secondaryLevel.getWidth() - 70);
            secondaryLevel.setEnabled(false);
        } else {
            int maximum = secondaryLevel.getMaximumDividerLocation();
            secondaryLevel.setDividerLocation(Math.min(cachedDividerLocation, maximum));
            secondaryLevel.setEnabled(true);
        }
        this.refreshContent();
    }

    public void loadLayerHandling() {
        this.northPane = new JPanel();
        this.northPane.setLayout(new BorderLayout());
        this.northPane.setBorder(null);
        if (shipView == null) {
            // We want to fail fast here, just to be safe and find out quick.
            throw new IllegalStateException("Ship view was null at the time of layer panel initialization!");
        }
        this.layersPanel = new ShipLayersPanel(shipView.getLayerManager());
        this.northPane.add(layersPanel, BorderLayout.CENTER);
        primaryContentPane.add(northPane, BorderLayout.PAGE_START);
    }

    public void loadShipView() {
        this.shipView = new PrimaryShipViewer();
        this.southPane = new JPanel();
        this.southPane.setLayout(new GridLayout());
        this.statusPanel = new ViewerStatusPanel(this.shipView);
        this.statusPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.southPane.add(this.statusPanel);
        primaryContentPane.add(this.southPane, BorderLayout.PAGE_END);
        this.refreshContent();
    }

    public void refreshContent() {
        primaryContentPane.revalidate();
        primaryContentPane.repaint();
        if (instrumentPane != null) {
            instrumentPane.repaint();
        }
    }

    public void loadEditingPanes() {
        primaryLevel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        this.instrumentPane = new InstrumentTabsPane();
        instrumentPane.setOpaque(true);
        secondaryLevel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        secondaryLevel.setLeftComponent((Component) shipView);
        secondaryLevel.setRightComponent(instrumentPane);
        secondaryLevel.setResizeWeight(1.0f);
        secondaryLevel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (instrumentPaneMinimized) {
                    relocateDivider();
                }
            }
        });

        GameDataPanel dataPanel = new GameDataPanel();
        secondaryLevel.setMinimumSize(new Dimension(480, primaryContentPane.getHeight()));

        primaryLevel.setLeftComponent(dataPanel);
        primaryLevel.setRightComponent(secondaryLevel);
        primaryLevel.setResizeWeight(0.1f);

        primaryContentPane.add(primaryLevel, BorderLayout.CENTER);
        this.refreshContent();
    }

    public void dispatchLoaderEvents() {
        EventBus.publish(new ShipViewableCreated(shipView));
    }

}
