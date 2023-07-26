package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.GameDataPanelResized;
import oth.shipeditor.communication.events.components.InstrumentSplitterResized;
import oth.shipeditor.communication.events.components.WindowGUIShowConfirmed;
import oth.shipeditor.components.datafiles.GameDataPanel;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.ShipViewable;
import oth.shipeditor.utility.components.MinimizeListener;
import oth.shipeditor.utility.components.MinimizerWidget;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 23.06.2023
 */
@SuppressWarnings({"ClassWithTooManyFields"})
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

    private BasicArrowButton dividerLeftButton;

    private BasicArrowButton dividerRightButton;

    private final MinimizerWidget minimizer;

    private final Map<LeftsideTabType, JPanel> leftsidePanels;

    private final Map<Component, Integer> dividerLocations;

    private MouseAdapter cachedDividerHandler;

    TripleSplitContainer(JTabbedPane westPane) {
        super((JSplitPane.HORIZONTAL_SPLIT));
        this.setOneTouchExpandable(true);
        this.putClientProperty("JSplitPane.expandableSide", "right");
        this.dividerLocations = new HashMap<>();
        this.initEventListeners();
        this.minimizer = new MinimizerWidget(getMinimizeAction(), getMaximizeAction());
        leftsidePanels = new EnumMap<>(LeftsideTabType.class);

        leftsidePanels.put(LeftsideTabType.DEFAULT, new JPanel());
        this.initDividerListeners(westPane);
    }

    private void initDividerListeners(JTabbedPane westPane) {
        westPane.addChangeListener(e -> {
            WindowContentPanes.LeftsidePanelTab selected = (WindowContentPanes.LeftsidePanelTab) westPane.getSelectedComponent();
            Component toSelect = null;
            switch (selected.getTabType()) {
                case DEFAULT -> toSelect = leftsidePanels.get(LeftsideTabType.DEFAULT);
                case GAME_DATA -> toSelect = leftsidePanels.get(LeftsideTabType.GAME_DATA);
            }
            this.setLeftComponent(toSelect);
            if (dividerLocations.get(toSelect) != null) {
                int location = dividerLocations.get(toSelect);
                this.setDividerLocation(Math.max(location, this.getMinimumDividerLocation()));
            }
            if (minimizer.isMinimized()) {
                minimizer.setRestorationQueued(true);
            }
            minimizer.setPanelSwitched(true);
        });
        westPane.addMouseListener(new MinimizeListener(westPane, minimizer));
        this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            int location = getDividerLocation();
            Component selected = this.getLeftComponent();
            dividerLocations.put(selected, location);
        });
    }

    private Runnable getMinimizeAction() {
        return () -> {
            if (dividerLeftButton.isVisible()) {
                minimizer.setMinimized(true);
                dividerLeftButton.doClick();
            }
        };
    }

    private Runnable getMaximizeAction() {
        return () -> {
            if (dividerRightButton.isVisible()) {
                minimizer.setMinimized(false);
                dividerRightButton.doClick();
            }
            else if (minimizer.isRestorationQueued() && minimizer.isMinimized()) {
                minimizer.setMinimized(false);
            }
        };
    }

    private void initEventListeners() {
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
        EventBus.subscribe(event -> {
            if (event instanceof GameDataPanelResized checked) {
                int newWidth = checked.newMinimum().width;
                if (this.getDividerLocation() >= newWidth) return;
                this.setDividerLocation(newWidth);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof WindowGUIShowConfirmed ) {
                this.minimizer.minimize();
                int maximum = this.getSize().width - instrumentPane.getMinimumSize().width;
                secondaryLevel.setDividerLocation(maximum - 16);
            }
        });
    }

    /**
     * This technique does not use reflection unlike the others, but has a downside of having hardcoded magic number.
     */
    private void relocateDivider() {
        if (secondaryLevel == null) return;
        if (instrumentPaneMinimized) {
            secondaryLevel.setDividerLocation(secondaryLevel.getWidth() - 106);
            this.toggleSecondarySplitterOff();
        } else {
            int maximum = secondaryLevel.getMaximumDividerLocation();
            secondaryLevel.setDividerLocation(Math.min(cachedDividerLocation, maximum));
            this.toggleSecondarySplitterOn();
        }
        this.repaint();
    }

    private void toggleSecondarySplitterOff() {
        if (secondaryLevel.getMouseListeners().length == 0) return;
        cachedDividerHandler = (MouseAdapter) secondaryLevel.getMouseListeners()[0];
        secondaryLevel.removeMouseListener(cachedDividerHandler);
        secondaryLevel.removeMouseMotionListener(cachedDividerHandler);
        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) secondaryLevel.getUI();
        BasicSplitPaneDivider divider = splitPaneUI.getDivider();
        divider.removeMouseListener(cachedDividerHandler);
        divider.removeMouseMotionListener(cachedDividerHandler);
        divider.setEnabled(false);
    }

    private void toggleSecondarySplitterOn() {
        secondaryLevel.addMouseListener(cachedDividerHandler);
        secondaryLevel.addMouseMotionListener(cachedDividerHandler);
        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) secondaryLevel.getUI();
        BasicSplitPaneDivider divider = splitPaneUI.getDivider();
        divider.addMouseListener(cachedDividerHandler);
        divider.addMouseMotionListener(cachedDividerHandler);
        divider.setEnabled(true);
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
            if (child instanceof BasicArrowButton checked) {
                if (buttonCount == 1) {
                    dividerLeftButton = checked;
                } else {
                    dividerRightButton = checked;
                }
            }
        }
        if (dividerLeftButton != null) {
            dividerLeftButton.addActionListener(e -> minimizer.setMinimized(true));
        }
        if (dividerRightButton != null) {
            dividerRightButton.addActionListener(e -> minimizer.setMinimized(false));
        }
        leftsidePanels.put(LeftsideTabType.GAME_DATA, new GameDataPanel());
        secondaryLevel.setMinimumSize(new Dimension(480, this.getHeight()));
        this.setLeftComponent(leftsidePanels.get(LeftsideTabType.GAME_DATA));
        this.setRightComponent(secondaryLevel);
        this.setResizeWeight(0.1f);
    }

}
