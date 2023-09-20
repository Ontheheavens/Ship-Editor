package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Entries are pairs of slot ID and weapon ID.
 * @author Ontheheavens
 * @since 11.09.2023
 */
public class BuiltInWeaponsPanel extends AbstractWeaponsPanel {

    @Override
    protected Map<String, InstalledFeature> getBaseHullFilteredEntries(FeaturesOverseer featuresOverseer) {
        return featuresOverseer.getBuiltInsFromBaseHull();
    }

    @Override
    protected Consumer<Map<String, InstalledFeature>> getBaseHullSortAction(FeaturesOverseer featuresOverseer) {
        return featuresOverseer::setBaseBuiltInsWithNewNormal;
    }

    @Override
    protected Map<String, InstalledFeature> getSkinFilteredEntries(FeaturesOverseer featuresOverseer) {
        return featuresOverseer.getBuiltInsFromSkin();
    }

    @Override
    protected Consumer<Map<String, InstalledFeature>> getSkinSortAction(FeaturesOverseer featuresOverseer) {
        return featuresOverseer::setSkinBuiltInsWithNewNormal;
    }

}
