package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.painters.features.InstalledFeature;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 14.09.2023
 */
public class DecorativesPanel extends AbstractWeaponsPanel {

    @Override
    protected String getPlaceholderText() {
        return "Hull has no built-in decoratives";
    }

    @Override
    protected Map<String, InstalledFeature> getBaseHullFilteredEntries(FeaturesOverseer featuresOverseer) {
        return featuresOverseer.getDecorativesFromBaseHull();
    }

    @Override
    protected Consumer<Map<String, InstalledFeature>> getBaseHullSortAction(FeaturesOverseer featuresOverseer) {
        return featuresOverseer::setBaseBuiltInsWithNewDecos;
    }

    @Override
    protected Map<String, InstalledFeature> getSkinFilteredEntries(FeaturesOverseer featuresOverseer) {
        return featuresOverseer.getDecorativesFromSkin();
    }

    @Override
    protected Consumer<Map<String, InstalledFeature>> getSkinSortAction(FeaturesOverseer featuresOverseer) {
        return featuresOverseer::setSkinBuiltInsWithNewDecos;
    }

}
