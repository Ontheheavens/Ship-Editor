package oth.shipeditor.components.instrument;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentSplitterResized;
import oth.shipeditor.utility.components.MinimizeListener;
import oth.shipeditor.utility.components.MinimizerWidget;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 28.07.2023
 */
public abstract class AbstractInstrumentsPane extends JTabbedPane {

    /**
     * Panel that is currently selected; depending on which panel it is interactivity of certain entities is resolved.
     */
    private JPanel activePanel;

    @Getter
    private final MinimizerWidget minimizer;

    @Getter @Setter
    private boolean instrumentPaneMinimized;

    private final Dimension maximumPanelSize = new Dimension(300, Integer.MAX_VALUE);

    protected AbstractInstrumentsPane() {
        this.minimizer = new MinimizerWidget(this.minimizeTabbedPane(), this.restoreTabbedPane());
        minimizer.setPanelSwitched(false);
        this.initListeners();
        this.setTabPlacement(SwingConstants.LEFT);
        this.setMinimumSize(maximumPanelSize);
        this.setMaximumSize(maximumPanelSize);
    }

    protected String getMinimizePrompt() {
        String minimizePrompt = "(Left-click to minimize panel)";
        if (minimizer.isMinimized()) {
            minimizePrompt = "(Left-click to expand panel)";
        }
        return minimizePrompt;
    }

    protected abstract void dispatchModeChange(JPanel active);

    protected abstract void updateTooltipText();

    private void initListeners() {
        this.addChangeListener(event -> {
            activePanel = (JPanel) getSelectedComponent();
            this.dispatchModeChange(activePanel);
            if (minimizer.isMinimized()) {
                minimizer.setRestorationQueued(true);
            }
            minimizer.setPanelSwitched(true);
        });
        this.addMouseListener(new MinimizeListener(this, this.minimizer));
    }

    private Runnable minimizeTabbedPane() {
        return () -> {
            minimizer.setMinimized(true);
            Dimension preferred = this.getPreferredSize();
            Dimension minimizedSize = new Dimension(0, preferred.height);
            this.setMinimumSize(minimizedSize);
            this.setMaximumSize(minimizedSize);
            updateTooltipText();
            EventBus.publish(new InstrumentSplitterResized(this, true));
        };
    }

    private Runnable restoreTabbedPane() {
        return () -> {
            minimizer.setMinimized(false);
            this.setMinimumSize(maximumPanelSize);
            this.setMaximumSize(maximumPanelSize);
            updateTooltipText();
            EventBus.publish(new InstrumentSplitterResized(this, false));
        };
    }

}
