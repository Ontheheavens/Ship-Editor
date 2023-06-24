package oth.shipeditor.utility;

import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This minimize-restore listener is inspired by behavior of tabbed panels in IntelliJ;
 * It is certainly not something implemented in a matter of minutes, took me some hours to tune all interactions.
 * @author Ontheheavens
 * @since 23.06.2023
 */
@Log4j2
public class MinimizeListener extends MouseAdapter {

    private final JTabbedPane parent;
    private final MinimizerWidget minimizer;

    public MinimizeListener(JTabbedPane pane, MinimizerWidget widget) {
        this.parent = pane;
        this.minimizer = widget;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        int tabIndex = parent.indexAtLocation(e.getX(), e.getY());
        if (tabIndex != -1 && !minimizer.isPanelSwitched()) {
            if (minimizer.isMinimized()) {
                minimizer.maximize();
            } else {
                minimizer.minimize();
            }
        }
        minimizer.setPanelSwitched(false);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!minimizer.isRestorationQueued()) return;
        int tabIndex = parent.indexAtLocation(e.getX(), e.getY());
        if (tabIndex != -1) {
            minimizer.maximize();
        }
        minimizer.setRestorationQueued(false);
    }

}
