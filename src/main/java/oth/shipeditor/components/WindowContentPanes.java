package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectShipDataEntry;
import oth.shipeditor.communication.events.components.SelectWeaponDataEntry;
import oth.shipeditor.components.layering.ViewerLayersPanel;
import oth.shipeditor.components.viewer.LayerViewer;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.persistence.Initializations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public final class WindowContentPanes {

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

    private JTabbedPane westTabsPane;

    public WindowContentPanes(Container pane) {
        this.primaryContentPane = pane;
        EventBus.subscribe(event -> {
            switch (event) {
                case SelectWeaponDataEntry ignored -> westTabsPane.setSelectedIndex(0);
                case SelectShipDataEntry ignored -> westTabsPane.setSelectedIndex(0);
                default -> {}
            }
        });
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

        westTabsPane = new JTabbedPane();
        westTabsPane.setTabPlacement(SwingConstants.LEFT);
        westTabsPane.addTab("Game data", new LeftsidePanelTab(LeftsideTabType.GAME_DATA));
        westTabsPane.addTab("Help", new LeftsidePanelTab(LeftsideTabType.HELP));

        westTabsPane.setToolTipTextAt(0, "Right-click to reload data");
        westTabsPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON3) return;
                int tabIndex = westTabsPane.indexAtLocation(e.getX(), e.getY());
                if (tabIndex == 0) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem reloadAllGameData = new JMenuItem("Reload all game data");
                    reloadAllGameData.addActionListener(e1 -> Initializations.loadGameData(westTabsPane));

                    menu.add(reloadAllGameData);
                    menu.show(westTabsPane, e.getPoint().x, e.getPoint().y);
                }
            }
        });

        JPanel quickButtonsPanel = new QuickButtonsPanel();
        westPanelsContainer.add(westTabsPane);
        westPanelsContainer.add(quickButtonsPanel);

        this.primaryContentPane.add(westPanelsContainer, BorderLayout.LINE_START);

        tripleSplitter = new TripleSplitContainer(westTabsPane);
        tripleSplitter.loadContentPanes(shipView);
        primaryContentPane.add(tripleSplitter, BorderLayout.CENTER);
        this.refreshContent();
    }

     @Getter
     @SuppressWarnings("PackageVisibleInnerClass")
     static class LeftsidePanelTab extends JPanel {
        private final LeftsideTabType tabType;
        LeftsidePanelTab(LeftsideTabType type) {
            this.setMaximumSize(new Dimension());
            this.setPreferredSize(new Dimension());
            this.setLayout(new BorderLayout());
            this.tabType = type;
        }
    }

}
