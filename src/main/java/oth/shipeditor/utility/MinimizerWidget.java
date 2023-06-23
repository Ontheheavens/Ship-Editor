package oth.shipeditor.utility;

import lombok.Getter;
import lombok.Setter;

/**
 * This is a utility class which provides a frame for maximization/minimization of panels of tabbed panes via composition.
 * Actual implementation of effects is passed as Runnable arguments.
 * @author Ontheheavens
 * @since 23.06.2023
 */
public class MinimizerWidget {

    @Getter @Setter
    private boolean minimized;

    @Getter @Setter
    private boolean restorationQueued;

    @Getter @Setter
    private boolean panelSwitched;

    private final Runnable minimizeAction;

    private final Runnable maximizeAction;

    public MinimizerWidget(Runnable minimize, Runnable maximize) {
        this.minimizeAction = minimize;
        this.maximizeAction = maximize;
    }

    public void minimize() {
        this.minimizeAction.run();
    }

    public void maximize() {
        this.maximizeAction.run();
    }

}
