package oth.shipeditor.utility.components.rendering;

import com.formdev.flatlaf.ui.FlatLineBorder;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 17.09.2023
 */
public class InstalledFeatureCellRenderer extends BoxPanelCellRenderer<InstalledFeature> {

    private final JLabel slotSizeIcon;
    private final JLabel slotTypeIcon;
    private final JLabel slotIDText;

    private final JLabel featureIDText;

    public InstalledFeatureCellRenderer() {
        slotTypeIcon = new JLabel();
        slotTypeIcon.setOpaque(true);
        slotTypeIcon.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
        slotTypeIcon.setBackground(Color.LIGHT_GRAY);

        slotSizeIcon = new JLabel();


        slotIDText = new JLabel();
        slotIDText.setBorder(new EmptyBorder(0, 4, 0, 0));

        featureIDText = new JLabel();

        JPanel leftContainer = getLeftContainer();
        leftContainer.add(slotSizeIcon);
        leftContainer.add(slotTypeIcon);
        leftContainer.add(slotIDText);

        JPanel rightContainer = getRightContainer();
        rightContainer.add(featureIDText);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends InstalledFeature> list,
                                                  InstalledFeature value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        String slotID = value.getSlotID();

        InstalledFeatureList featureList = (InstalledFeatureList) list;
        var slotPainter = featureList.getSlotPainter();
        var slotPoint = slotPainter.getSlotByID(slotID);

        Color foreground = list.getForeground();
        setToolTipText("");
        if (isSelected) {
            foreground = list.getSelectionForeground();
        }

        if (slotPoint != null) {
            WeaponType weaponType = slotPoint.getWeaponType();
            Icon color = ComponentUtilities.createIconFromColor(weaponType.getColor(), 10, 10);
            slotTypeIcon.setIcon(color);
            slotTypeIcon.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));

            slotIDText.setBorder(new EmptyBorder(0, 4, 0, 0));

            WeaponSize size = slotPoint.getWeaponSize();
            slotSizeIcon.setVisible(true);
            slotSizeIcon.setIcon(size.getIcon());

            if (!slotPoint.canFit(value)) {
                foreground = Color.RED;
                setToolTipText("Invalidated: weapon unfit for slot");
            }
        } else {
            slotTypeIcon.setIcon(FontIcon.of(BoxiconsRegular.ERROR, 18, Color.RED));
            slotTypeIcon.setOpaque(false);
            slotTypeIcon.setBorder(new EmptyBorder(3, 1, 2, 0));
            slotTypeIcon.setBackground(null);
            slotSizeIcon.setIcon(null);
            slotSizeIcon.setVisible(false);

            foreground = Color.RED;
            setToolTipText("Invalidated: slot not found");

            slotIDText.setBorder(new EmptyBorder(0, 1, 0, 0));
        }

        slotIDText.setForeground(foreground);
        featureIDText.setForeground(foreground);

        slotIDText.setText(slotID +":");
        featureIDText.setText(value.getFeatureID());

        return this;
    }

}
