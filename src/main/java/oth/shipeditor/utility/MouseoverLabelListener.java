package oth.shipeditor.utility;

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

    public MouseoverLabelListener(JPopupMenu menu, JLabel inputLabel) {
        this.label = inputLabel;
        this.popupMenu = menu;
    }

    // Not entirely satisfied with the mismatch between background coloring bounds and border bounds;
    // However, this is good enough for the time being.

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        label.setBackground(Color.LIGHT_GRAY);
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
