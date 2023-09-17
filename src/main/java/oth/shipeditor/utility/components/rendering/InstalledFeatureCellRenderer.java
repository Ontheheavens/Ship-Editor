package oth.shipeditor.utility.components.rendering;

import oth.shipeditor.components.viewer.painters.features.InstalledFeature;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 17.09.2023
 */
public class InstalledFeatureCellRenderer extends BoxPanelCellRenderer<InstalledFeature> {

    private final JLabel slotIDText;

    private final JLabel featureIDText;

    public InstalledFeatureCellRenderer() {
        slotIDText = new JLabel();
        featureIDText = new JLabel();

        JPanel leftContainer = getLeftContainer();
        leftContainer.add(slotIDText);

        JPanel rightContainer = getRightContainer();
        rightContainer.add(featureIDText);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends InstalledFeature> list,
                                                  InstalledFeature value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        Color foreground = list.getForeground();
        if (isSelected) {
            foreground = list.getSelectionForeground();
        }

        slotIDText.setForeground(foreground);
        featureIDText.setForeground(foreground);

        slotIDText.setText(value.getSlotID() +":");
        featureIDText.setText(value.getFeatureID());

        return this;
    }

}
