package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 14.09.2023
 */
public class DecorativesPanel extends AbstractWeaponsPanel {

    @Override
    protected EditorInstrument getMode() {
        return EditorInstrument.DECORATIVES;
    }

    @Override
    Map<String, InstalledFeature> getBaseHullFilteredEntries(ShipLayer shipLayer) {
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        return featuresOverseer.getDecorativesFromBaseHull();
    }

    @Override
    Map<String, InstalledFeature> getSkinFilteredEntries(ShipLayer shipLayer) {
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        return featuresOverseer.getDecorativesFromSkin();
    }

    @Override
    Consumer<Map<String, InstalledFeature>> getBaseHullSortAction(ShipLayer shipLayer) {
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        return featuresOverseer::setBaseBuiltInsWithNewDecos;
    }

    @Override
    Consumer<Map<String, InstalledFeature>> getSkinSortAction(ShipLayer shipLayer) {
        FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
        return featuresOverseer::setSkinBuiltInsWithNewDecos;
    }

}
