package oth.shipeditor.components.viewer.layers.ship;

import lombok.Getter;
import org.apache.commons.collections4.map.ListOrderedMap;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.DeleteButtonPressed;
import oth.shipeditor.communication.events.components.ShipEntryPicked;
import oth.shipeditor.communication.events.components.WeaponEntryPicked;
import oth.shipeditor.communication.events.viewer.control.FeatureInstallQueued;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.layers.ship.data.Variant;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.FireMode;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Responsible for all non-rendering interactions with installed features of layer,
 * be it base hull or skin built-in, or variant fits.
 * @author Ontheheavens
 * @since 19.09.2023
 */
@SuppressWarnings({"WeakerAccess", "OverlyCoupledClass", "OverlyComplexClass", "ClassWithTooManyMethods"})
public class FeaturesOverseer {

    @SuppressWarnings("StaticNonFinalField")
    @Getter
    public static WeaponCSVEntry weaponForInstall;

    @SuppressWarnings("StaticNonFinalField")
    @Getter
    public static VariantFile moduleVariantForInstall;
    private final ShipLayer parent;

    @Getter
    private final List<BusEventListener> listeners = new ArrayList<>();

    FeaturesOverseer(ShipLayer layer) {
        this.parent = layer;
        this.initInstallListeners();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void setWeaponForInstall(WeaponCSVEntry entry) {
        FeaturesOverseer.weaponForInstall = entry;
        FeaturesOverseer.moduleVariantForInstall = null;
        EventBus.publish(new WeaponEntryPicked());
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void setModuleForInstall(VariantFile variant) {
        FeaturesOverseer.weaponForInstall = null;
        FeaturesOverseer.moduleVariantForInstall = variant;
        EventBus.publish(new ShipEntryPicked());
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

        Map<String, InstalledFeature> result = new ListOrderedMap<>();
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
        Map<String, InstalledFeature> combined = new ListOrderedMap<>();
        var decoratives = this.getDecorativesFromBaseHull();
        combined.putAll(rearrangedNormal);
        combined.putAll(decoratives);
        ShipPainter shipPainter = parent.getPainter();

        combined.forEach((slotID, feature) -> feature.setContainedInBuiltIns(true));

        var oldCollection = shipPainter.getBuiltInWeapons();
        EditDispatch.postBuiltInFeaturesSorted(shipPainter::setBuiltInWeapons, oldCollection, combined);
    }

    public void setSkinBuiltInsWithNewNormal(Map<String, InstalledFeature> rearrangedNormal) {
        Map<String, InstalledFeature> combined = new ListOrderedMap<>();
        combined.putAll(rearrangedNormal);
        var decoratives = this.getDecorativesFromSkin();
        combined.putAll(decoratives);
        ShipPainter shipPainter = parent.getPainter();

        ShipSkin activeSkin = shipPainter.getActiveSkin();

        var reconstructed = ShipSkin.reconstructAsEntries(combined);

        var oldCollection = activeSkin.getBuiltInWeapons();
        EditDispatch.postBuiltInFeaturesSorted(activeSkin::setBuiltInWeapons, oldCollection, reconstructed);
    }

    public void setBaseBuiltInsWithNewDecos(Map<String, InstalledFeature> rearrangedDecos) {
        Map<String, InstalledFeature> combined = new ListOrderedMap<>();
        var normal = this.getBuiltInsFromBaseHull();
        combined.putAll(normal);
        combined.putAll(rearrangedDecos);
        ShipPainter shipPainter = parent.getPainter();

        var oldCollection = shipPainter.getBuiltInWeapons();
        EditDispatch.postBuiltInFeaturesSorted(shipPainter::setBuiltInWeapons, oldCollection, combined);
    }

    public void setSkinBuiltInsWithNewDecos(Map<String, InstalledFeature> rearrangedDecos) {
        Map<String, InstalledFeature> combined = new ListOrderedMap<>();
        var normal = this.getBuiltInsFromSkin();
        combined.putAll(normal);
        combined.putAll(rearrangedDecos);
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

    private void initInstallListeners() {
        BusEventListener installListener = event -> {
            if (event instanceof FeatureInstallQueued) {
                if (StaticController.getActiveLayer() != parent) return;
                if (moduleVariantForInstall != null) {
                    addModuleToSelectedSlot(moduleVariantForInstall);
                }
                if (weaponForInstall != null) {
                    addWeaponToSelectedSlot(weaponForInstall);
                }
            }
        };
        listeners.add(installListener);
        EventBus.subscribe(installListener);

        BusEventListener uninstallListener = event -> {
            if (event instanceof DeleteButtonPressed) {
                if (StaticController.getActiveLayer() != parent) return;
                var shipPainter = parent.getPainter();
                if (shipPainter == null || shipPainter.isUninitialized()) return;
                var slotPainter = shipPainter.getWeaponSlotPainter();

                var mode = StaticController.getEditorMode();
                WeaponSlotPoint selected = slotPainter.getSelected();
                var eligibleSlots = slotPainter.getEligibleForSelection();

                if (selected == null || !eligibleSlots.contains(selected)) return;

                FeaturesOverseer.handleFeatureRemoval(selected, mode, shipPainter);
            }
        };
        listeners.add(uninstallListener);
        EventBus.subscribe(uninstallListener);
    }

    public void addModuleToSelectedSlot(VariantFile moduleVariant) {
        chooseWeaponPointAndInstall((editorInstrument, slotPoint) -> {
            boolean choseVariant = moduleVariant != null && !moduleVariant.isEmpty();
            if (editorInstrument == EditorInstrument.VARIANT_MODULES && choseVariant) {
                if (slotPoint.isModule()) {
                    installModule(slotPoint, moduleVariant);
                }
            }
        });
    }

    public void addWeaponToSelectedSlot(WeaponCSVEntry weaponEntry) {
        chooseWeaponPointAndInstall((editorInstrument, slotPoint) -> {
            if (!WeaponType.isWeaponFitting(slotPoint, weaponEntry)) return;
            switch (editorInstrument) {
                case BUILT_IN_WEAPONS -> {
                    if (slotPoint.isBuiltIn()) {
                        installBuiltIn(slotPoint, weaponEntry);
                    }
                }
                case DECORATIVES -> {
                    if (slotPoint.isDecorative()) {
                        installBuiltIn(slotPoint, weaponEntry);
                    }
                }
                case VARIANT_WEAPONS -> {
                    if (slotPoint.isFittable()) {
                        installWeapon(slotPoint, weaponEntry);
                    }
                }
            }
        });
    }

    private void chooseWeaponPointAndInstall(BiConsumer<EditorInstrument,
            WeaponSlotPoint> installAction) {
        var shipPainter = parent.getPainter();
        if (shipPainter == null || shipPainter.isUninitialized()) return;
        var slotPainter = shipPainter.getWeaponSlotPainter();

        WeaponSlotPoint selected = slotPainter.getSelected();
        var eligibleSlots = slotPainter.getEligibleForSelection();

        if (selected == null || !eligibleSlots.contains(selected)) return;
        var mode = StaticController.getEditorMode();

        installAction.accept(mode, selected);
    }

    @SuppressWarnings({"MethodWithMultipleReturnPoints", "OverlyComplexMethod"})
    private static void handleFeatureRemoval(SlotData selected, EditorInstrument mode,
                                             ShipPainter shipPainter) {
        String slotID = selected.getId();
        switch (mode) {
            case BUILT_IN_WEAPONS, DECORATIVES -> {
                ShipSkin activeSkin = shipPainter.getActiveSkin();
                if (activeSkin != null && !activeSkin.isBase()) {
                    var addedBySkin = activeSkin.getBuiltInWeapons();
                    WeaponCSVEntry toRemove = addedBySkin.get(slotID);
                    if (toRemove == null) return;
                    EditDispatch.postFeatureUninstalled(addedBySkin, slotID,
                            toRemove, activeSkin::invalidateBuiltIns);
                } else {
                    var weapons = shipPainter.getBuiltInWeapons();
                    InstalledFeature toRemove = weapons.get(slotID);
                    if (toRemove == null) return;
                    EditDispatch.postFeatureUninstalled(weapons, slotID,
                            toRemove, null);
                }
            }
            case VARIANT_WEAPONS -> {
                var variant = shipPainter.getActiveVariant();
                if (variant == null || variant.isEmpty()) return;
                FittedWeaponGroup targetGroup = variant.getGroupWithExistingMapping(slotID);
                Map<String, InstalledFeature> groupWeapons;
                if (targetGroup != null) {
                    targetGroup.removeBySlotID(slotID);
                }
            }
            case VARIANT_MODULES -> {
                var variant = shipPainter.getActiveVariant();
                if (variant == null || variant.isEmpty()) return;
                var modules = variant.getFittedModules();
                InstalledFeature toRemove = modules.get(slotID);
                if (toRemove == null) return;
                EditDispatch.postFeatureUninstalled(modules, slotID,
                        toRemove, null);
            }
        }
    }

    private void installWeapon(SlotData selected, WeaponCSVEntry forInstall) {
        var shipPainter = parent.getPainter();
        var activeVariant = shipPainter.getActiveVariant();
        if (activeVariant == null || activeVariant.isEmpty()) return;

        String slotID = selected.getId();

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
            if (weaponGroups.isEmpty()) {
                FittedWeaponGroup newGroup = new FittedWeaponGroup(activeVariant,
                        false, FireMode.ALTERNATING);
                weaponGroups.add(newGroup);
                targetGroup = newGroup;
            } else {
                targetGroup = weaponGroups.get(0);
            }
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

    private void installModule(SlotData selected, Variant variantFile) {
        var shipPainter = parent.getPainter();
        var activeVariant = shipPainter.getActiveVariant();
        if (activeVariant == null || activeVariant.isEmpty()) return;

        String slotID = selected.getId();
        var modules = activeVariant.getFittedModules();

        InstalledFeature moduleFeature = GameDataRepository.createModuleFromVariant(slotID, variantFile);

        InstalledFeature existing = modules.get(slotID);
        if (existing != null) {
            EditDispatch.postFeatureUninstalled(modules, slotID, existing, null);
        }

        FeaturesOverseer.commenceInstall(slotID, moduleFeature, modules, null);
    }

    private void installBuiltIn(SlotData selected, WeaponCSVEntry forInstall) {
        var shipPainter = parent.getPainter();
        var activeSkin = shipPainter.getActiveSkin();
        String slotID = selected.getId();

        ShipVariant activeVariant = shipPainter.getActiveVariant();

        if (activeSkin != null && !activeSkin.isBase()) {
            var skinBuiltIns = activeSkin.getBuiltInWeapons();

            Runnable invalidator = activeSkin::invalidateBuiltIns;
            FeaturesOverseer.removeExistingBeforeInstall(skinBuiltIns, activeVariant, slotID, invalidator);

            FeaturesOverseer.commenceInstall(slotID, forInstall, skinBuiltIns,
                    invalidator);
        } else {
            // Currently this adds a new entry to the built-ins map without any sorting; perhaps refactor later.
            var baseBuiltIns = shipPainter.getBuiltInWeapons();

            FeaturesOverseer.removeExistingBeforeInstall(baseBuiltIns, activeVariant, slotID, null);

            WeaponSpecFile specFile = forInstall.getSpecFile();
            WeaponPainter weaponPainter = forInstall.createPainterFromEntry(null, specFile);
            InstalledFeature feature = InstalledFeature.of(slotID, forInstall.getWeaponID(),
                    weaponPainter, forInstall);
            feature.setContainedInBuiltIns(true);

            FeaturesOverseer.commenceInstall(slotID, feature, baseBuiltIns, null);
        }
    }

    private static <T extends InstallableEntry> void removeExistingBeforeInstall(Map<String, T> collection,
                                                                                 ShipVariant activeVariant,
                                                                                 String slotID, Runnable builtInInvalidator) {
        T existingBuiltIn = collection.get(slotID);
        if (existingBuiltIn != null) {
            EditDispatch.postFeatureUninstalled(collection, slotID, existingBuiltIn, builtInInvalidator);
        } else if (activeVariant != null) {
            FittedWeaponGroup targetGroup = activeVariant.getGroupWithExistingMapping(slotID);
            Map<String, InstalledFeature> groupWeapons;
            if (targetGroup != null) {
                groupWeapons = targetGroup.getWeapons();
                InstalledFeature existing = groupWeapons.get(slotID);
                EditDispatch.postFeatureUninstalled(groupWeapons, slotID, existing, null);
            }
        }
    }

    private static <T extends InstallableEntry> void commenceInstall(String slotID, T entry,
                                                                     Map<String, T> collection,
                                                                     Runnable invalidator) {
        EditDispatch.postFeatureInstalled(collection, slotID, entry, invalidator);
    }

}
