package oth.shipeditor;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.WindowGUIShowConfirmed;
import oth.shipeditor.communication.events.components.WindowRepaintQueued;
import oth.shipeditor.components.WindowContentPanes;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.menubar.PrimaryMenuBar;
import oth.shipeditor.parsing.saving.SaveCoordinator;
import oth.shipeditor.persistence.Initializations;
import oth.shipeditor.persistence.Settings;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.objects.SimpleRectangle;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Ontheheavens
 * @since 27.04.2023
 */
@Log4j2
public final class PrimaryWindow extends JFrame {

    public static final String SHIP_EDITOR = "Ship Editor";

    @Getter
    private final PrimaryMenuBar primaryMenu;

    private final WindowContentPanes contentPanes;

    @Getter
    private boolean initialized;

    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(800, 600);

    private PrimaryWindow() {
        log.info("Application start: creating window.");
        this.setTitle(SHIP_EDITOR);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        PrimaryWindow.performStaticInits();

        this.setMinimumSize(MINIMUM_WINDOW_SIZE);

        this.initListeners();
        // This centers the frame.
        this.setLocationRelativeTo(null);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        primaryMenu = new PrimaryMenuBar();
        this.setJMenuBar(primaryMenu);

        this.contentPanes = new WindowContentPanes(this.getContentPane());
        contentPanes.loadShipView();
        contentPanes.loadLayerHandling();
        contentPanes.loadEditingPanes();

        PrimaryWindow.configureTooltips();
        this.addComponentListener(new ResizingPersistenceListener());

        this.pack();
    }

    private static void performStaticInits() {
        Initializations.selectGameFolder();
        SaveCoordinator.init();
        StaticController.init();

        ControlPredicates.initSelectionModeListening();
    }

    public static PrimaryWindow create() {
        PrimaryWindow primaryWindow = new PrimaryWindow();

        primaryWindow.addFocusListener(new FocusListener() {
            private final KeyEventDispatcher altDisabler = e -> e.getKeyCode() == 18;

            @Override
            public void focusGained(FocusEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(altDisabler);
            }

            @Override
            public void focusLost(FocusEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(altDisabler);
            }
        });

        primaryWindow.restoreSize();
        return primaryWindow;
    }

    private static void configureTooltips() {
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.setInitialDelay(10);
        toolTipManager.setDismissDelay(90000);
    }

    private void restoreSize() {
        Settings settings = SettingsManager.getSettings();
        SimpleRectangle saved = settings.getWindowBounds();

        if (saved != null) {
            int x = Math.max(0, saved.x);
            int y = Math.max(0, saved.y);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            int clampedWidth = Math.min(saved.width, screenSize.width - x);
            int clampedHeight = Math.min(saved.height, screenSize.height - y);

            int width = Math.max(MINIMUM_WINDOW_SIZE.width, clampedWidth);
            int height = Math.max(MINIMUM_WINDOW_SIZE.height, clampedHeight);

            this.setBounds(new Rectangle(x, y, width, height));
        }

        if (settings.isWindowMaximized()) {
            this.setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
        }
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof WindowRepaintQueued) {
                contentPanes.refreshContent();
            }
        });
    }

    void showGUI() {
        this.setVisible(true);
        EventBus.publish(new WindowGUIShowConfirmed());
        this.initialized = true;
    }


    private class ResizingPersistenceListener extends ComponentAdapter {

        private boolean saveScheduled;
        private final Timer resizeTimer;

        ResizingPersistenceListener() {
            ActionListener savingAction = e -> {
                if (saveScheduled) {
                    saveBounds();
                    saveScheduled = false;
                }
            };
            resizeTimer = new Timer(2500, savingAction);
            resizeTimer.setRepeats(false);
        }

        private void scheduleSave() {
            if (!PrimaryWindow.this.initialized) return;
            saveScheduled = true;
            resizeTimer.restart();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            scheduleSave();
        }

        @Override
        public void componentResized(ComponentEvent e) {
            scheduleSave();
        }

        private void saveBounds() {
            log.info("Saving modified window bounds...");
            var bounds = PrimaryWindow.this.getBounds();
            SimpleRectangle serializable = new SimpleRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
            Settings settings = SettingsManager.getSettings();
            settings.setWindowBounds(serializable);

            int state = PrimaryWindow.this.getExtendedState();
            boolean maximized = (state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
            settings.setWindowMaximized(maximized);

            SettingsManager.updateFileFromRuntime();
        }

    }

}
