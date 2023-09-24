package oth.shipeditor.utility.components.rendering;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 17.09.2023
 */
public class PanelListCellRenderer<E> extends JPanel implements ListCellRenderer<E> {

    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    PanelListCellRenderer() {
        setOpaque(false);
        setBorder(DEFAULT_NO_FOCUS_BORDER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends E> list, E value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
        }
        setOpaque(isSelected);
        return this;
    }

    @Override
    public void repaint() {}

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

    @Override
    public void repaint(Rectangle r) {}

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}

    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

}
