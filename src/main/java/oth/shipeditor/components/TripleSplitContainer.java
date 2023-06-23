package oth.shipeditor.components;

import com.formdev.flatlaf.ui.FlatArrowButton;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentSplitterResized;
import oth.shipeditor.components.datafiles.GameDataPanel;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.ShipViewable;
import oth.shipeditor.utility.MinimizeListener;
import oth.shipeditor.utility.MinimizerWidget;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Ontheheavens
 * @since 23.06.2023
 */
@SuppressWarnings("FieldCanBeLocal")
@Log4j2
final class TripleSplitContainer extends JSplitPane {

    /**
     * Holds viewer panel on the left and instrument panel on the right.
     */
    @Getter
    private JSplitPane secondaryLevel;

    private boolean instrumentPaneMinimized;

    private int cachedDividerLocation = -1;

    /**
     * Parent pane for ship data editing tabs.
     */
    private InstrumentTabsPane instrumentPane;

    private final JPanel defaultLeftsidePanel;

    private GameDataPanel gameDataPanel;

    private FlatArrowButton dividerLeftButton;

    private FlatArrowButton dividerRightButton;

    private final MinimizerWidget minimizer;

    TripleSplitContainer(JTabbedPane westPane) {
        super((JSplitPane.HORIZONTAL_SPLIT));
        this.setOneTouchExpandable(true);
        this.putClientProperty("JSplitPane.expandableSide", "right");
        this.initListeners();
        this.minimizer = new MinimizerWidget(getMinimizeAction(), getMaximizeAction());
        defaultLeftsidePanel = new JPanel();
        defaultLeftsidePanel.add(new JLabel("Default"));
        westPane.addChangeListener(e -> {
            WindowContentPanes.LeftsidePanelTab selected = (WindowContentPanes.LeftsidePanelTab) westPane.getSelectedComponent();
            switch (selected.getTabType()) {
                case DEFAULT -> this.setLeftComponent(defaultLeftsidePanel);
                case GAME_DATA -> this.setLeftComponent(gameDataPanel);
            }
            if (minimizer.isMinimized()) {
                minimizer.setRestorationQueued(true);
            }
            minimizer.setPanelSwitched(true);
        });
        // Minimizing behaviour is still subpar for these panels, but we have more pressing matter to deal with.
        // Meticulous logging and close examination of boolean interactions will likely solve the oddities.
        westPane.addMouseListener(new MinimizeListener(westPane, minimizer));
    }

    private Runnable getMinimizeAction() {
        return () -> {
            if (dividerLeftButton.isVisible()) {
                dividerLeftButton.doClick();
                minimizer.setMinimized(true);
            }
        };
    }

    private Runnable getMaximizeAction() {
        return () -> {
            if (dividerRightButton.isVisible()) {
                dividerRightButton.doClick();
                minimizer.setMinimized(false);
            }
        };
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
        this.repaint();
    }

    void loadContentPanes(ShipViewable shipView) {
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
        // While this might look clunky, this is the least painful method for programmatic control over divider minimization.
        Component divider = this.getComponents()[0];
        BasicSplitPaneDivider casted = (BasicSplitPaneDivider) divider;
        int buttonCount = 0;
        for (Component child : casted.getComponents()) {
            buttonCount++;
            if (child instanceof FlatArrowButton checked) {
                if (buttonCount == 1) {
                    dividerLeftButton = checked;
                } else {
                    dividerRightButton = checked;
                }
            }
        }
        gameDataPanel = new GameDataPanel();
        secondaryLevel.setMinimumSize(new Dimension(480, this.getHeight()));
        this.setLeftComponent(gameDataPanel);
        this.setRightComponent(secondaryLevel);
        this.setResizeWeight(0.1f);
    }

}
