package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectShipDataEntry;
import oth.shipeditor.communication.events.components.SelectWeaponDataEntry;
import oth.shipeditor.communication.events.viewer.points.InstrumentModeChanged;
import oth.shipeditor.components.layering.ViewerLayersPanel;
import oth.shipeditor.components.logging.LogsPanel;
import oth.shipeditor.components.viewer.LayerViewer;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.themes.Themes;

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
                case InstrumentModeChanged ignored -> westTabsPane.setSelectedIndex(0);
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

        northPane.setBackground(Themes.getTabBackgroundColor());

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
        westTabsPane.addTab("Logs", new LeftsidePanelTab(LeftsideTabType.LOG));

        westTabsPane.setToolTipTextAt(0, "Right-click to reload data");
        westTabsPane.addMouseListener(new WestTabsMouseListener());

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

    private class WestTabsMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                int tabIndex = westTabsPane.indexAtLocation(e.getX(), e.getY());
                if (tabIndex == 2) {
                    LogsPanel.scrollToBottom();
                }
            }

            if (!SwingUtilities.isRightMouseButton(e)) return;
            int tabIndex = westTabsPane.indexAtLocation(e.getX(), e.getY());
            if (tabIndex == 0) {
                JPopupMenu menu = new JPopupMenu();

                JMenuItem reloadAllGameData = new JMenuItem("Reload all game data");
                reloadAllGameData.setIcon(FontIcon.of(FluentUiRegularAL.ARROW_DOWNLOAD_20, 16, Themes.getIconColor()));
                reloadAllGameData.addActionListener(event -> FileLoading.loadGameData());
                if (FileLoading.isLoadingInProgress()) {
                    reloadAllGameData.setEnabled(false);
                }
                menu.add(reloadAllGameData);

                Settings settings = SettingsManager.getSettings();

                JMenuItem autoLoadData = new JCheckBoxMenuItem("Auto-load data at start");
                autoLoadData.setSelected(SettingsManager.isDataAutoloadEnabled());
                autoLoadData.setIcon(FontIcon.of(FluentUiRegularAL.DOCUMENT_AUTOSAVE_24, 16, Themes.getIconColor()));
                autoLoadData.addActionListener(event ->
                        settings.setLoadDataAtStart(autoLoadData.isSelected())
                );
                menu.add(autoLoadData);

                JMenuItem toggleFileErrorPopups = new JCheckBoxMenuItem("Enable file error pop-ups");
                toggleFileErrorPopups.setSelected(SettingsManager.areFileErrorPopupsEnabled());
                toggleFileErrorPopups.setIcon(FontIcon.of(FluentUiRegularAL.DOCUMENT_ERROR_20, 16, Themes.getIconColor()));
                toggleFileErrorPopups.addActionListener(event ->
                        settings.setShowLoadingErrors(toggleFileErrorPopups.isSelected())
                );
                menu.add(toggleFileErrorPopups);

                menu.show(westTabsPane, e.getPoint().x, e.getPoint().y);
            }
        }

    }

}
