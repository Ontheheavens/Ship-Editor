package oth.shipeditor.utility.components.rendering;

import com.formdev.flatlaf.ui.FlatLineBorder;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.engine.EnginePoint;
import oth.shipeditor.representation.ship.EngineStyle;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 21.11.2023
 */
public class EngineCellRenderer extends PointCellRenderer {

    private final JLabel styleIcon;

    public EngineCellRenderer() {
        styleIcon = new JLabel();
        styleIcon.setOpaque(true);
        styleIcon.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
        styleIcon.setBackground(Color.LIGHT_GRAY);

        JPanel leftContainer = getLeftContainer();
        leftContainer.removeAll();
        leftContainer.add(styleIcon);
        JLabel textLabel = getTextLabel();
        textLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
        leftContainer.add(textLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends BaseWorldPoint> list,
                                                  BaseWorldPoint value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        this.setToolTipText(null);

        EnginePoint checked = (EnginePoint) value;
        EngineStyle engineStyle = checked.getStyle();
        String styleOrID = checked.getStyleID();
        styleIcon.setIcon(null);
        styleIcon.setVisible(false);
        if (engineStyle != null) {
            styleOrID = engineStyle.getEngineStyleID();

            Icon color = ComponentUtilities.createIconFromColor(engineStyle.getEngineColor(), 10, 10);

            styleIcon.setIcon(color);
            styleIcon.setVisible(true);
        }
        else if (checked.getCustomStyleSpec() != null) {
            styleOrID = StringConstants.CUSTOM;
            this.setToolTipText("Custom style spec detected: inline style editing not supported");
        }
        String displayText = styleOrID + " #" + index + ":";

        JLabel textLabel = getTextLabel();
        textLabel.setText(displayText);

        return this;
    }

}
