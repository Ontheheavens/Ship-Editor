package oth.shipeditor.components.viewer.painters.points.ship.features;

import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author Ontheheavens
 * @since 03.12.2023
 */
public class InstalledFeatureComparator implements Comparator<InstalledFeature>, Serializable {

    @Override
    public int compare(InstalledFeature first, InstalledFeature second) {
        int typeComparison = InstalledFeatureComparator.compareByType(first, second);
        if (typeComparison != 0) {
            return typeComparison;
        }

        int sizeComparison = InstalledFeatureComparator.compareBySize(first, second);
        if (sizeComparison != 0) {
            return sizeComparison;
        }

        return InstalledFeatureComparator.compareAlphabetically(first, second);
    }

    private static int compareByType(InstalledFeature first, InstalledFeature second) {
        return InstalledFeatureComparator.getTypeOrder(first) -
                InstalledFeatureComparator.getTypeOrder(second);
    }

    private static int getTypeOrder(InstalledFeature feature) {
        WeaponType type = ((WeaponCSVEntry) feature.getDataEntry()).getType();
        if (Objects.requireNonNull(type) == WeaponType.BUILT_IN) {
            return 0;
        }
        return 1;
    }

    private static int compareBySize(InstalledFeature first, InstalledFeature second) {
        return InstalledFeatureComparator.getSizeOrder(first) -
                InstalledFeatureComparator.getSizeOrder(second);
    }

    private static int getSizeOrder(InstalledFeature feature) {
        WeaponSize size = ((WeaponCSVEntry) feature.getDataEntry()).getSize();
        return size.getNumericSize();
    }

    private static int compareAlphabetically(InstalledFeature first, InstalledFeature second) {
        String firstName = first.getName();
        return firstName.compareToIgnoreCase(second.getName());
    }

}
