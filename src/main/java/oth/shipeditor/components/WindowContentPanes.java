package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.layering.ShipLayersPanel;
import oth.shipeditor.components.viewer.PrimaryShipViewer;
import oth.shipeditor.components.viewer.ShipViewable;
import oth.shipeditor.utility.StringConstants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public final class WindowContentPanes {

    static final String DEFAULT_LEFTSIDE_PANE = StringConstants.DEFAULT;

    /**
     * Complex component responsible for ship layers display.
     */
    @Getter
    private ShipViewable shipView;

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
    private TripleSplitContainer tripleSplitter;

    public WindowContentPanes(Container pane) {
        this.primaryContentPane = pane;
    }

    public void loadLayerHandling() {
        JPanel northPane = new JPanel();
        northPane.setLayout(new BorderLayout());
        northPane.setBorder(null);
        if (shipView == null) {
            // We want to fail fast here, just to be safe and find out quick.
            throw new IllegalStateException("Ship view was null at the time of layer panel initialization!");
        }
        ShipLayersPanel layersPanel = new ShipLayersPanel(shipView.getLayerManager());
        northPane.add(layersPanel, BorderLayout.CENTER);
        primaryContentPane.add(northPane, BorderLayout.PAGE_START);
    }

    public void loadShipView() {
        this.shipView = new PrimaryShipViewer();
        JPanel southPane = new JPanel();
        southPane.setLayout(new GridLayout());
        this.statusPanel = new ViewerStatusPanel(this.shipView);
        southPane.add(this.statusPanel);
        primaryContentPane.add(southPane, BorderLayout.PAGE_END);
        this.refreshContent();
    }

    public void refreshContent() {
        primaryContentPane.revalidate();
        primaryContentPane.repaint();
    }

    public void loadEditingPanes() {
        JTabbedPane westTabsPane = new JTabbedPane();
        westTabsPane.setTabPlacement(SwingConstants.LEFT);
        westTabsPane.addTab("Game data", new LeftsidePanelTab(LeftsideTabType.GAME_DATA));
        westTabsPane.addTab(DEFAULT_LEFTSIDE_PANE, new LeftsidePanelTab(LeftsideTabType.DEFAULT));
        this.primaryContentPane.add(westTabsPane, BorderLayout.LINE_START);

        tripleSplitter = new TripleSplitContainer(westTabsPane);
        tripleSplitter.loadContentPanes(shipView);
        primaryContentPane.add(tripleSplitter, BorderLayout.CENTER);
        this.refreshContent();
    }

     @SuppressWarnings("PackageVisibleInnerClass")
     static class LeftsidePanelTab extends JPanel {
        @Getter
        private final LeftsideTabType tabType;
        LeftsidePanelTab(LeftsideTabType type) {
            this.setMaximumSize(new Dimension());
            this.setPreferredSize(new Dimension());
            this.setLayout(new BorderLayout());
            this.tabType = type;
        }
    }

}
