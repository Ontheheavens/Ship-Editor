package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Entries are pairs of slot ID and weapon ID.
 * @author Ontheheavens
 * @since 11.09.2023
 */
public class BuiltInWeaponsPanel extends AbstractWeaponsPanel {

    @Override
    protected EditorInstrument getMode() {
        return EditorInstrument.BUILT_IN_WEAPONS;
    }

    @Override
    Map<String, InstalledFeature> getBaseHullFilteredEntries(ShipLayer shipLayer) {
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        return featuresOverseer.getBuiltInsFromBaseHull();
    }

    @Override
    Map<String, InstalledFeature> getSkinFilteredEntries(ShipLayer shipLayer) {
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        return featuresOverseer.getBuiltInsFromSkin();
    }

    @Override
    Consumer<Map<String, InstalledFeature>> getBaseHullSortAction(ShipLayer shipLayer) {
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        return featuresOverseer::setBaseBuiltInsWithNewNormal;
    }

    @Override
    Consumer<Map<String, InstalledFeature>> getSkinSortAction(ShipLayer shipLayer) {
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        return featuresOverseer::setSkinBuiltInsWithNewNormal;
    }

}
