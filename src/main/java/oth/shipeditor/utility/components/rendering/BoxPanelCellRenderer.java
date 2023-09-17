package oth.shipeditor.utility.components.rendering;

import lombok.Getter;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 17.09.2023
 */
public class BoxPanelCellRenderer<E> extends PanelCellRenderer<E> {

    @Getter
    private final JPanel leftContainer;

    @Getter
    private final JPanel rightContainer;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    BoxPanelCellRenderer() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        leftContainer = new JPanel();
        leftContainer.setOpaque(false);
        leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.LINE_AXIS));
        rightContainer = new JPanel();
        rightContainer.setOpaque(false);
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.LINE_AXIS));

        ComponentUtilities.layoutAsOpposites(this, leftContainer, rightContainer, 4);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends E> list,
                                                  E value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Color background = super.getBackground();
        leftContainer.setBackground(background);
        rightContainer.setBackground(background);
        return this;
    }

}
