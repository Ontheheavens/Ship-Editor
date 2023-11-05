package oth.shipeditor.utility.components;

import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Ontheheavens
 * @since 10.07.2023
 */
public class MouseoverLabelListener extends MouseAdapter {

    private final JLabel label;

    private final JPopupMenu popupMenu;

    private final Color highlight;

    private final Runnable action;

    public MouseoverLabelListener(Runnable clickAction, JLabel inputLabel) {
        this(null, clickAction, inputLabel, Themes.getPanelHighlightColor());
    }

    public MouseoverLabelListener(JPopupMenu menu, JLabel inputLabel) {
        this(menu, null, inputLabel, Themes.getPanelHighlightColor());
    }

    MouseoverLabelListener(JPopupMenu menu, JLabel inputLabel, Color color) {
        this(menu, null, inputLabel, color);
    }

    private MouseoverLabelListener(JPopupMenu menu, Runnable clickAction, JLabel inputLabel, Color color) {
        this.label = inputLabel;
        this.action = clickAction;
        this.popupMenu = menu;
        this.highlight = color;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        if (popupMenu != null) {
            if (!popupMenu.isEnabled()) return;
        }
        label.setBackground(highlight);
        label.setOpaque(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        label.setBackground(Themes.getPanelBackgroundColor());
        label.setOpaque(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (popupMenu != null && SwingUtilities.isRightMouseButton(e)) {
            if (!popupMenu.isEnabled()) return;
            popupMenu.show(label, e.getX(), e.getY());
        } else if (SwingUtilities.isLeftMouseButton(e) && action != null) {
            action.run();
        }
    }

}
