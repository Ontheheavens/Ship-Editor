package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.layering.ViewerLayersPanel;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.components.viewer.LayerViewer;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public final class WindowContentPanes {

    private static final String DEFAULT_LEFTSIDE_PANE = StringValues.DEFAULT;

    /**
     * Complex component responsible for ship layers display.
     */
    @Getter
    private LayerViewer shipView;

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
        ViewerLayersPanel layersPanel = new ViewerLayersPanel(shipView.getLayerManager());
        northPane.add(layersPanel, BorderLayout.CENTER);
        primaryContentPane.add(northPane, BorderLayout.PAGE_START);
    }

    public void loadShipView() {
        this.shipView = new PrimaryViewer().commenceInitialization();
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
        JPanel westPanelsContainer = new JPanel();

        LayoutManager layout = new GridLayout(2, 1);
        westPanelsContainer.setLayout(layout);

        JTabbedPane westTabsPane = new JTabbedPane();
        westTabsPane.setTabPlacement(SwingConstants.LEFT);
        westTabsPane.addTab("Game data", new LeftsidePanelTab(LeftsideTabType.GAME_DATA));
        westTabsPane.addTab(DEFAULT_LEFTSIDE_PANE, new LeftsidePanelTab(LeftsideTabType.DEFAULT));

        // TODO: sort out later.

        JPanel placeholder = new JPanel();
        placeholder.setBackground(Color.LIGHT_GRAY);

        westPanelsContainer.add(westTabsPane);
        westPanelsContainer.add(placeholder);

        this.primaryContentPane.add(westPanelsContainer, BorderLayout.LINE_START);

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
