package oth.shipeditor.utility.components.rendering;

import com.formdev.flatlaf.ui.FlatLineBorder;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

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
                setToolTipText(StringValues.INVALIDATED_WEAPON_UNFIT_FOR_SLOT);
            } else if (featureList.isBelongsToBaseHullBuiltIns()) {
                // This is not a good way to solve the issue conceptually, but I don't see a better solution at the moment.
                // Ideally, cell renderer shouldn't care about what list of what features it displays;
                // However, it needs to, because it should display removal and override status provided by skin.
                handleSkinChanges(slotPainter, slotPoint);
            }
        } else {
            Color errorColor = Color.RED;
            this.setWarningIcon(errorColor);

            foreground = errorColor;
            setToolTipText(StringValues.INVALIDATED_SLOT_NOT_FOUND);

            slotIDText.setBorder(new EmptyBorder(0, 1, 0, 0));
        }

        slotIDText.setForeground(foreground);
        featureIDText.setForeground(foreground);

        slotIDText.setText(slotID +":");

        CSVEntry dataEntry = value.getDataEntry();
        featureIDText.setText(dataEntry.toString());

        return this;
    }

    private void setWarningIcon(Color color) {
        slotTypeIcon.setIcon(FontIcon.of(BoxiconsRegular.ERROR, 18, color));
        slotTypeIcon.setOpaque(false);
        slotTypeIcon.setBorder(new EmptyBorder(1, 1, 0, 0));

        slotTypeIcon.setBackground(null);
        slotSizeIcon.setIcon(null);
        slotSizeIcon.setVisible(false);
    }

    private void handleSkinChanges(WeaponSlotPainter slotPainter, SlotData slotPoint) {
        var shipPainter = slotPainter.getParentLayer();
        var skin = shipPainter.getActiveSkin();
        if (skin == null || skin.isBase()) return;

        ShipLayer parentLayer = shipPainter.getParentLayer();
        var overseer = parentLayer.getFeaturesOverseer();

        // If both skin entry override and skin removal present, override should overwrite removal status.

        var removals = overseer.getBuiltInsRemovedBySkin();
        if (removals != null && !removals.isEmpty()) {
            removals.forEach(slotID -> {
                if (slotID.equals(slotPoint.getId()))  {
                    Color warnColor = Color.ORANGE;
                    this.setWarningIcon(warnColor);
                    setToolTipText("Overridden: slot install removed");
                }
            });
        }

        var skinBuiltIns = skin.getInitializedBuiltIns();
        if (skinBuiltIns != null && !skinBuiltIns.isEmpty()) {
            skinBuiltIns.forEach((slotID, feature1) -> {
                if (slotID.equals(slotPoint.getId()))  {
                    Color warnColor = Color.GREEN;
                    this.setWarningIcon(warnColor);

                    setToolTipText("Overridden: slot install superseded");
                }
            });
        }
    }

}
