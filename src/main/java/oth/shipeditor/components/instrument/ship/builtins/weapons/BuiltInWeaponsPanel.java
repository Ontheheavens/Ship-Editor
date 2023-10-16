package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.painters.PainterVisibility;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeaturePainter;

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
    protected PainterVisibility getVisibilityOfBuiltInKind(InstalledFeaturePainter painter) {
        return painter.getBuiltInsVisibility();
    }

    @Override
    protected void setVisibilityOfBuiltInKind(InstalledFeaturePainter painter, PainterVisibility visibility) {
        painter.setBuiltInsVisibility(visibility);
    }

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
