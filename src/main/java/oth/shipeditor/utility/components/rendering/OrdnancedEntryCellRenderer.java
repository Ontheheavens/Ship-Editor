package oth.shipeditor.utility.components.rendering;

import com.formdev.flatlaf.ui.FlatLineBorder;
import oth.shipeditor.components.datafiles.entities.OrdnancedCSVEntry;
import oth.shipeditor.representation.HullSize;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 15.10.2023
 */
public class OrdnancedEntryCellRenderer extends PanelListCellRenderer<OrdnancedCSVEntry>{

    private final JLabel iconLabel;

    private final JLabel textLabel;

    private final JLabel ordnanceLabel;

    public OrdnancedEntryCellRenderer() {
        iconLabel = new JLabel();
        iconLabel.setOpaque(true);
        iconLabel.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
        iconLabel.setBackground(Color.LIGHT_GRAY);

        textLabel = new JLabel();
        ordnanceLabel = new JLabel();
        this.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(2, 2, 2, 0);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.fill = GridBagConstraints.VERTICAL;
        this.add(iconLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 4, 0, 0);
        constraints.weightx = 1;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        this.add(textLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        this.add(ordnanceLabel, constraints);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends OrdnancedCSVEntry> list, OrdnancedCSVEntry value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);


        Color foreground = list.getForeground();
        if (isSelected) {
            foreground = list.getSelectionForeground();
        }

        textLabel.setForeground(foreground);
        ordnanceLabel.setForeground(foreground);

        JLabel label = value.getIconLabel();
        iconLabel.setIcon(label.getIcon());

        textLabel.setText(value.toString());

        HullSize size = HullSize.getSizeOfActiveLayer();
        int ordnanceCost = value.getOrdnanceCost(size);
        ordnanceLabel.setText("OP: " + ordnanceCost);

        return this;
    }

}
