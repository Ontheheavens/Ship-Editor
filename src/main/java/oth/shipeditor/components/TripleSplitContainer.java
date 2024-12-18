package oth.shipeditor.components;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.*;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.datafiles.GameDataPanel;
import oth.shipeditor.components.help.HelpMainPanel;
import oth.shipeditor.components.instrument.AbstractInstrumentsPane;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.instrument.weapon.WeaponInstrumentsPane;
import oth.shipeditor.components.logging.LogsPanel;
import oth.shipeditor.components.viewer.LayerViewer;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.utility.components.MinimizeListener;
import oth.shipeditor.utility.components.MinimizerWidget;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 23.06.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Log4j2
final class TripleSplitContainer extends JSplitPane {

    /**
     * Holds viewer panel on the left and instrument panel on the right.
     */
    @Getter
    private JSplitPane secondaryLevel;

    /**
     * Parent pane for ship data editing tabs.
     */
    private ShipInstrumentsPane shipInstrumentsPane;

    private WeaponInstrumentsPane weaponInstrumentsPane;

    private BasicArrowButton dividerLeftButton;

    private BasicArrowButton dividerRightButton;

    private final MinimizerWidget minimizer;

    private final Map<LeftsideTabType, JPanel> leftsidePanels;

    private final Map<Component, Integer> dividerLocations;

    private MouseAdapter primaryDividerHandler;

    private MouseAdapter secondaryDividerHandler;

    private final DividerDragListener dragListener = new DividerDragListener();

    private int cachedDataPanelWidth;

    TripleSplitContainer(JTabbedPane westPane) {
        super((JSplitPane.HORIZONTAL_SPLIT));
        this.setOneTouchExpandable(true);
        this.putClientProperty("JSplitPane.expandableSide", "right");
        this.dividerLocations = new HashMap<>();
        this.initEventListeners();
        this.minimizer = new MinimizerWidget(getMinimizeAction(), getMaximizeAction());
        leftsidePanels = new EnumMap<>(LeftsideTabType.class);

        leftsidePanels.put(LeftsideTabType.HELP, new HelpMainPanel());
        leftsidePanels.put(LeftsideTabType.LOG, new LogsPanel());

        this.initDividerListeners(westPane);
    }

    private void initDividerListeners(JTabbedPane westPane) {
        westPane.addChangeListener(e -> {
            WindowContentPanes.LeftsidePanelTab selected = (WindowContentPanes.LeftsidePanelTab) westPane.getSelectedComponent();
            Component toSelect = null;
            switch (selected.getTabType()) {
                case HELP -> toSelect = leftsidePanels.get(LeftsideTabType.HELP);
                case GAME_DATA -> toSelect = leftsidePanels.get(LeftsideTabType.GAME_DATA);
                case LOG -> toSelect = leftsidePanels.get(LeftsideTabType.LOG);
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
                togglePrimarySplitterOff();
            }
        };
    }

    private Runnable getMaximizeAction() {
        return () -> {
            if (dividerRightButton.isVisible()) {
                minimizer.setMinimized(false);
                dividerRightButton.doClick();
                togglePrimarySplitterOn();
            }
            else if (minimizer.isRestorationQueued() && minimizer.isMinimized()) {
                minimizer.setMinimized(false);
                togglePrimarySplitterOn();
            }
        };
    }

    private void togglePrimarySplitterOff() {
        if (this.getMouseListeners().length == 0) return;
        primaryDividerHandler = (MouseAdapter) this.getMouseListeners()[0];
        TripleSplitContainer.removeListenerFromSplitter(this, primaryDividerHandler);
    }

    private void togglePrimarySplitterOn() {
        TripleSplitContainer.addListenerToSplitter(this, primaryDividerHandler);
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyCoupledMethod"})
    private void initEventListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentSplitterResized checked) {
                boolean minimize = checked.minimized();
                if (secondaryLevel == null) return;
                if (secondaryLevel.getRightComponent() instanceof AbstractInstrumentsPane instrumentsPane) {
                    instrumentsPane.setInstrumentPaneMinimized(minimize);
                }
                relocateDivider();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof GameDataPanelResized checked) {
                cachedDataPanelWidth = checked.newMinimum().width;
                if (this.getDividerLocation() >= cachedDataPanelWidth) return;
                this.setDividerLocation(cachedDataPanelWidth);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof WindowGUIShowConfirmed ) {
                this.minimizer.minimize();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SelectWeaponDataEntry) {
                this.minimizer.maximize();
                minimizer.setMinimized(false);
            } else if (event instanceof SelectShipDataEntry) {
                this.minimizer.maximize();
                minimizer.setMinimized(false);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                if (selected instanceof WeaponLayer) {
                    if (secondaryLevel.getRightComponent() instanceof ShipInstrumentsPane) {
                        secondaryLevel.setRightComponent(weaponInstrumentsPane);
                        relocateDivider();
                    }
                } else {
                    if (secondaryLevel.getRightComponent() instanceof WeaponInstrumentsPane) {
                        secondaryLevel.setRightComponent(shipInstrumentsPane);
                        relocateDivider();
                    }
                }
            }
        });
    }

    /**
     * This technique does not use reflection unlike the others, but has a downside of having hardcoded magic number.
     * Generally all of this class is a <strong>highly</strong> unsatisfactory mess, but it works!
     */
    private void relocateDivider() {
        if (secondaryLevel == null) return;
        Component rightComp = secondaryLevel.getRightComponent();
        if (rightComp instanceof AbstractInstrumentsPane instrumentsPane) {
            boolean minimized = instrumentsPane.isInstrumentPaneMinimized();

            int remainder = 125;
            if (rightComp instanceof WeaponInstrumentsPane) {
                remainder = 70;
            }
            if (minimized) {
                secondaryLevel.setDividerLocation(secondaryLevel.getWidth() - remainder);
                this.toggleSecondarySplitterOff();
            } else {
                Dimension splitPaneSize = secondaryLevel.getSize();
                int maximum = splitPaneSize.width - instrumentsPane.getTargetWidth();
                secondaryLevel.setDividerLocation(maximum);
                this.toggleSecondarySplitterOn();
                if (secondaryLevel.getWidth() - secondaryLevel.getDividerLocation() <= rightComp.getMinimumSize().width) {
                    Component leftComp = this.getLeftComponent();
                    int minimum = this.getSize().width - leftComp.getWidth();
                    secondaryLevel.setDividerLocation(minimum - rightComp.getMinimumSize().width);
                }
            }
        }
        this.repaint();
    }

    private void toggleSecondarySplitterOn() {
        TripleSplitContainer.addListenerToSplitter(secondaryLevel, secondaryDividerHandler);
        TripleSplitContainer.addListenerToSplitter(secondaryLevel, dragListener);
    }

    private void toggleSecondarySplitterOff() {
        if (secondaryLevel.getMouseListeners().length == 0) return;
        secondaryDividerHandler = (MouseAdapter) secondaryLevel.getMouseListeners()[0];
        TripleSplitContainer.removeListenerFromSplitter(secondaryLevel, secondaryDividerHandler);
        TripleSplitContainer.removeListenerFromSplitter(secondaryLevel, dragListener);
    }

    private static void removeListenerFromSplitter(JSplitPane splitter, MouseAdapter listener) {
        splitter.removeMouseListener(listener);
        splitter.removeMouseMotionListener(listener);
        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitter.getUI();
        BasicSplitPaneDivider divider = splitPaneUI.getDivider();
        divider.removeMouseListener(listener);
        divider.removeMouseMotionListener(listener);
        divider.setEnabled(false);
    }

    private static void addListenerToSplitter(JSplitPane splitter, MouseAdapter dividerHandler) {
        splitter.addMouseListener(dividerHandler);
        splitter.addMouseMotionListener(dividerHandler);
        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitter.getUI();
        BasicSplitPaneDivider divider = splitPaneUI.getDivider();
        divider.addMouseListener(dividerHandler);
        divider.addMouseMotionListener(dividerHandler);
        divider.setEnabled(true);
    }

    void loadContentPanes(LayerViewer shipView) {
        this.shipInstrumentsPane = new ShipInstrumentsPane();
        this.weaponInstrumentsPane = new WeaponInstrumentsPane();
        secondaryLevel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        secondaryLevel.setLeftComponent((Component) shipView);
        secondaryLevel.setRightComponent(shipInstrumentsPane);
        secondaryLevel.setResizeWeight(1.0f);
        ComponentListener adapter = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relocateDivider();
            }
        };
        secondaryLevel.addComponentListener(adapter);
        this.addComponentListener(adapter);
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

    private class DividerDragListener extends MouseAdapter {

        private final Supplier<AbstractInstrumentsPane> paneSupplier = () ->
                (AbstractInstrumentsPane) secondaryLevel.getRightComponent();

        @Override
        public void mouseDragged(MouseEvent e) {
            Supplier<Integer> width = paneSupplier.get()::getWidth;
            setTargetWidth(width.get());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            Supplier<Integer> width = paneSupplier.get()::getWidth;
            setTargetWidth(width.get());
        }

        private void setTargetWidth(Integer width) {
            AbstractInstrumentsPane instrumentsPane = paneSupplier.get();
            boolean minimized = instrumentsPane.isInstrumentPaneMinimized();
            if (minimized) return;
            instrumentsPane.setTargetWidth(width);
        }

    }

}
