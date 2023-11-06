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

    private final JComponent component;

    private final JPopupMenu popupMenu;

    private final Color highlight;

    private final Runnable action;

    public MouseoverLabelListener(Runnable clickAction, JComponent inputComponent) {
        this(null, clickAction, inputComponent, Themes.getPanelHighlightColor());
    }

    public MouseoverLabelListener(JPopupMenu menu, JComponent inputComponent) {
        this(menu, null, inputComponent, Themes.getPanelHighlightColor());
    }

    public MouseoverLabelListener(JPopupMenu menu, JComponent inputComponent, Color color) {
        this(menu, null, inputComponent, color);
    }

    private MouseoverLabelListener(JPopupMenu menu, Runnable clickAction, JComponent inputComponent, Color color) {
        this.component = inputComponent;
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
        component.setBackground(highlight);
        component.setOpaque(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        component.setBackground(Themes.getPanelBackgroundColor());
        component.setOpaque(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (popupMenu != null && SwingUtilities.isRightMouseButton(e)) {
            if (!popupMenu.isEnabled()) return;
            popupMenu.show(component, e.getX(), e.getY());
        } else if (SwingUtilities.isLeftMouseButton(e) && action != null) {
            action.run();
        }
    }

}
