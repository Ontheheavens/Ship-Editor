package oth.shipeditor.utility.components;

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

    public MouseoverLabelListener(JPopupMenu menu, JLabel inputLabel) {
        this(menu, inputLabel, Color.LIGHT_GRAY);

    }

    public MouseoverLabelListener(JPopupMenu menu, JLabel inputLabel, Color color) {
        this.label = inputLabel;
        this.popupMenu = menu;
        this.highlight = color;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        label.setBackground(highlight);
        label.setOpaque(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        label.setBackground(Color.WHITE);
        label.setOpaque(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) && popupMenu != null) {
            popupMenu.show(label, e.getX(), e.getY());
        }
    }

}
