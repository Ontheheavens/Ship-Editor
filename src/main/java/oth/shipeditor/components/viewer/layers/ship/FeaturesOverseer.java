package oth.shipeditor.components.viewer.layers.ship;

import lombok.Getter;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.control.FeatureInstallQueued;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.FireMode;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Responsible for all non-rendering interactions with installed features of layer,
 * be it base hull or skin built-in, or variant fits.
 * @author Ontheheavens
 * @since 19.09.2023
 */
@SuppressWarnings({"WeakerAccess", "OverlyCoupledClass"})
public class FeaturesOverseer {

    @SuppressWarnings("StaticNonFinalField")
    @Getter
    public static WeaponCSVEntry weaponForInstall;

    @SuppressWarnings("StaticNonFinalField")
    @Getter
    public static ShipCSVEntry moduleForInstall;
    private final ShipLayer parent;

    @Getter
    private final List<BusEventListener> listeners = new ArrayList<>();

    FeaturesOverseer(ShipLayer layer) {
        this.parent = layer;
        this.initInstallListeners();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void setWeaponForInstall(WeaponCSVEntry entry) {
        weaponForInstall = entry;
        var mode = StaticController.getEditorMode();
        var repainter = StaticController.getRepainter();
        if (mode == EditorInstrument.BUILT_IN_WEAPONS || mode == EditorInstrument.DECORATIVES) {
            repainter.queueBuiltInsRepaint();
        }
    }

    // This getter business is not good at all.
    // However, given the constraints, best to let it be and move on to finish the project.

    public Map<String, InstalledFeature> getBuiltInsFromBaseHull() {
        var painter = parent.getPainter();
        Supplier<Map<String, InstalledFeature>> getter = painter::getBuiltInWeapons;
        return getFilteredInstallables(getter, (slotPainter, featureEntry) -> {
            InstalledFeature installedFeature = featureEntry.getValue();
            return installedFeature.isNormalWeapon();
        });
    }

    public Map<String, InstalledFeature> getDecorativesFromBaseHull() {
        var painter = parent.getPainter();
        Supplier<Map<String, InstalledFeature>> getter = painter::getBuiltInWeapons;
        return getFilteredInstallables(getter, (slotPainter, featureEntry) -> {
            InstalledFeature installedFeature = featureEntry.getValue();
            return installedFeature.isDecoWeapon();
        });
    }

    @SuppressWarnings("unused")
    public List<String> getBuiltInsRemovedBySkin() {
        ShipPainter painter = parent.getPainter();
        if (painter == null) return null;
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            return activeSkin.getRemoveBuiltInWeapons();

        }
        return null;
    }

    public Map<String, InstalledFeature> getBuiltInsFromSkin() {
        var painter = parent.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            Supplier<Map<String, InstalledFeature>> getter = activeSkin::getInitializedBuiltIns;
            return getFilteredInstallables(getter, (slotPainter, featureEntry) -> {
                InstalledFeature installedFeature = featureEntry.getValue();
                return installedFeature.isNormalWeapon();
            });
        }
        return null;
    }

    public Map<String, InstalledFeature> getDecorativesFromSkin() {
        var painter = parent.getPainter();
        ShipSkin activeSkin = painter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            Supplier<Map<String, InstalledFeature>> getter = activeSkin::getInitializedBuiltIns;
            return getFilteredInstallables(getter, (slotPainter, featureEntry) -> {
                InstalledFeature installedFeature = featureEntry.getValue();
                return installedFeature.isDecoWeapon();
            });
        }
        return null;
    }

    Map<String, InstalledFeature> getFilteredInstallables(
            Supplier<Map<String, InstalledFeature>> getter,
            BiFunction<WeaponSlotPainter, Map.Entry<String, InstalledFeature>, Boolean> filter) {
        var painter = parent.getPainter();
        if (painter == null) return null;
        var installedFeatureMap = getter.get();
        if (installedFeatureMap == null) return null;
        var slotPainter = painter.getWeaponSlotPainter();
        if (slotPainter == null) return null;

        Map<String, InstalledFeature> result = new LinkedHashMap<>();
        Set<Map.Entry<String, InstalledFeature>> entries = installedFeatureMap.entrySet();
        Stream<Map.Entry<String, InstalledFeature>> stream = entries.stream();
        stream.forEach(featureEntry -> {
            if (filter.apply(slotPainter, featureEntry)) {
                result.put(featureEntry.getKey(), featureEntry.getValue());
            }
        });
        return result;
    }

    // Following four setters are chunky duplicated code - not a beautiful sight.
    // However, the alternative is a lot of quite tricky generics; so let's leave this be.

    public void setBaseBuiltInsWithNewNormal(Map<String, InstalledFeature> rearrangedNormal) {
        Map<String, InstalledFeature> combined = new LinkedHashMap<>(rearrangedNormal);
        var decoratives = this.getDecorativesFromBaseHull();
        combined.putAll(decoratives);
        ShipPainter shipPainter = parent.getPainter();

        combined.forEach((slotID, feature) -> feature.setContainedInBuiltIns(true));

        var oldCollection = shipPainter.getBuiltInWeapons();
        EditDispatch.postBuiltInFeaturesSorted(shipPainter::setBuiltInWeapons, oldCollection, combined);
    }

    public void setSkinBuiltInsWithNewNormal(Map<String, InstalledFeature> rearrangedNormal) {
        Map<String, InstalledFeature> combined = new LinkedHashMap<>(rearrangedNormal);
        var decoratives = this.getDecorativesFromSkin();
        combined.putAll(decoratives);
        ShipPainter shipPainter = parent.getPainter();

        ShipSkin activeSkin = shipPainter.getActiveSkin();

        var reconstructed = ShipSkin.reconstructAsEntries(combined);

        var oldCollection = activeSkin.getBuiltInWeapons();
        EditDispatch.postBuiltInFeaturesSorted(activeSkin::setBuiltInWeapons, oldCollection, reconstructed);
    }

    public void setBaseBuiltInsWithNewDecos(Map<String, InstalledFeature> rearrangedDecos) {
        Map<String, InstalledFeature> combined = new LinkedHashMap<>(rearrangedDecos);
        var normal = this.getBuiltInsFromBaseHull();
        combined.putAll(normal);
        ShipPainter shipPainter = parent.getPainter();

        var oldCollection = shipPainter.getBuiltInWeapons();
        EditDispatch.postBuiltInFeaturesSorted(shipPainter::setBuiltInWeapons, oldCollection, combined);
    }

    public void setSkinBuiltInsWithNewDecos(Map<String, InstalledFeature> rearrangedDecos) {
        Map<String, InstalledFeature> combined = new LinkedHashMap<>(rearrangedDecos);
        var normal = this.getBuiltInsFromSkin();
        combined.putAll(normal);
        ShipPainter shipPainter = parent.getPainter();

        ShipSkin activeSkin = shipPainter.getActiveSkin();

        var reconstructed = ShipSkin.reconstructAsEntries(combined);

        var oldCollection = activeSkin.getBuiltInWeapons();
        EditDispatch.postBuiltInFeaturesSorted(activeSkin::setBuiltInWeapons, oldCollection, reconstructed);
    }

    public void cleanupListeners() {
        for (BusEventListener listener : listeners) {
            EventBus.unsubscribe(listener);
        }
    }

    @SuppressWarnings("OverlyComplexMethod")
    private void initInstallListeners() {
        BusEventListener installListener = event -> {
            if (event instanceof FeatureInstallQueued) {
                if (StaticController.getActiveLayer() != parent) return;
                var shipPainter = parent.getPainter();
                if (shipPainter == null || shipPainter.isUninitialized()) return;
                var slotPainter = shipPainter.getWeaponSlotPainter();

                WeaponSlotPoint selected = slotPainter.getSelected();
                var eligibleSlots = slotPainter.getEligibleForSelection();

                if (selected == null || !eligibleSlots.contains(selected)) return;
                var mode = StaticController.getEditorMode();

                if (mode == EditorInstrument.VARIANT_MODULES && moduleForInstall != null) {
                    if (selected.isModule()) {
                        installModule(selected);
                    } else {
                        return;
                    }
                }

                if (weaponForInstall == null) return;
                if (!WeaponType.isWeaponFitting(selected, weaponForInstall)) return;

                switch (mode) {
                    case BUILT_IN_WEAPONS -> {
                        if (selected.isBuiltIn()) {
                            installBuiltIn(selected);
                        }
                    }
                    case DECORATIVES -> {
                        if (selected.isDecorative()) {
                            installBuiltIn(selected);
                        }
                    }
                    case VARIANT_WEAPONS -> {
                        if (selected.isFittable()) {
                            fitToVariant(selected);
                        }
                    }
                }
            }
        };
        listeners.add(installListener);
        EventBus.subscribe(installListener);
    }

    private void fitToVariant(SlotData selected) {
        var shipPainter = parent.getPainter();
        var activeVariant = shipPainter.getActiveVariant();
        if (activeVariant == null || activeVariant.isEmpty()) return;

        String slotID = selected.getId();
        WeaponCSVEntry forInstall = weaponForInstall;

        List<FittedWeaponGroup> weaponGroups = activeVariant.getWeaponGroups();

        WeaponSpecFile specFile = forInstall.getSpecFile();
        WeaponPainter weaponPainter = forInstall.createPainterFromEntry(null, specFile);
        InstalledFeature feature = InstalledFeature.of(slotID, forInstall.getWeaponID(),
                weaponPainter, forInstall);

        FittedWeaponGroup targetGroup = activeVariant.getGroupWithExistingMapping(slotID);
        Map<String, InstalledFeature> groupWeapons;
        if (targetGroup != null) {
            groupWeapons = targetGroup.getWeapons();
            InstalledFeature existing = groupWeapons.get(slotID);
            EditDispatch.postFeatureUninstalled(groupWeapons, slotID, existing, null);
        } else {
            targetGroup = weaponGroups.get(0);
        }

        if (targetGroup == null) {
            targetGroup = new FittedWeaponGroup(activeVariant,
                    false, FireMode.LINKED);
            weaponGroups.add(targetGroup);
        }
        groupWeapons = targetGroup.getWeapons();
        feature.setParentGroup(targetGroup);
        FeaturesOverseer.commenceInstall(slotID, feature, groupWeapons, null);
    }

    private void installModule(SlotData selected) {

    }

    private void installBuiltIn(SlotData selected) {
        var shipPainter = parent.getPainter();
        var activeSkin = shipPainter.getActiveSkin();
        String slotID = selected.getId();
        WeaponCSVEntry forInstall = weaponForInstall;
        if (activeSkin != null && !activeSkin.isBase()) {
            var skinBuiltIns = activeSkin.getBuiltInWeapons();
            FeaturesOverseer.commenceInstall(slotID, forInstall, skinBuiltIns,
                    activeSkin::invalidateBuiltIns);
        } else {
            var baseBuiltIns = shipPainter.getBuiltInWeapons();
            WeaponSpecFile specFile = forInstall.getSpecFile();
            WeaponPainter weaponPainter = forInstall.createPainterFromEntry(null, specFile);
            InstalledFeature feature = InstalledFeature.of(slotID, forInstall.getWeaponID(),
                    weaponPainter, forInstall);
            feature.setContainedInBuiltIns(true);

            FeaturesOverseer.commenceInstall(slotID, feature, baseBuiltIns, null);
        }

    }

    private static <T extends InstallableEntry> void commenceInstall(String slotID, T entry,
                                                                     Map<String, T> collection,
                                                                     Runnable invalidator) {
        EditDispatch.postFeatureInstalled(collection, slotID, entry, invalidator);
    }

}
