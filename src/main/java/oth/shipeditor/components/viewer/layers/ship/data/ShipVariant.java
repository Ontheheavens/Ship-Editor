package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.features.InstalledSlotFeaturePainter;
import oth.shipeditor.representation.*;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.text.StringValues;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 28.08.2023
 */
@Getter @Setter
public class ShipVariant implements Variant {

    public static final String EMPTY_VARIANT = "Empty variant";
    private boolean empty;

    private Path variantFilePath;


    private Path containingPackage;

    /**
     * Can be either ID of base hull or skin hull ID.
     */
    private String shipHullId;

    private String variantId;

    private String displayName;

    private InstalledSlotFeaturePainter slotFeaturePainter;

    public ShipVariant() {
        this(true);
    }

    @SuppressWarnings("BooleanParameter")
    public ShipVariant(boolean isEmpty) {
        this.empty = isEmpty;
    }

    public String getFileName() {
        if (variantFilePath == null) return StringValues.EMPTY;
        return variantFilePath.getFileName().toString();
    }

    public void cleanupForRemoval() {
        InstalledSlotFeaturePainter featurePainter = this.getSlotFeaturePainter();
        Map<String, LayerPainter> installedFeatures = featurePainter.getInstalledFeatures();
        installedFeatures.forEach((slotID, layerPainter) -> layerPainter.cleanupForRemoval());
    }

    @SuppressWarnings("OverlyCoupledMethod")
    public void initialize(VariantFile file) {
        this.setVariantId(variantId);
        this.setShipHullId(file.getHullId());
        this.setVariantFilePath(file.getVariantFilePath());
        this.setContainingPackage(file.getContainingPackage());
        this.setDisplayName(file.getDisplayName());

        this.slotFeaturePainter = new InstalledSlotFeaturePainter();

        Map<String, String> allInstalledWeapons = new HashMap<>();
        List<WeaponGroup> weaponGroups = file.getWeaponGroups();
        weaponGroups.forEach(weaponGroup -> allInstalledWeapons.putAll(weaponGroup.getWeapons()));

        var installedFeatures = slotFeaturePainter.getInstalledFeatures();

        allInstalledWeapons.forEach((slotID, weaponID) -> {
            WeaponCSVEntry weaponEntry = GameDataRepository.getWeaponByID(weaponID);
            WeaponSpecFile specFile = weaponEntry.getSpecFile();
            WeaponPainter weaponPainter = weaponEntry.createPainterFromEntry(null, specFile);
            installedFeatures.put(slotID, weaponPainter);
        });

        var installedModules = file.getModules();
        if (installedModules != null) {
            installedModules.forEach((slotID, variantID) -> {
                VariantFile variant = GameDataRepository.getVariantByID(variantID);
                ShipSpecFile specFile = GameDataRepository.retrieveSpecByID(variant.getHullId());
                String baseHullId;
                SkinSpecFile skinSpec = null;
                if (specFile instanceof SkinSpecFile checkedSkin) {
                    baseHullId = checkedSkin.getBaseHullId();
                    skinSpec = checkedSkin;
                } else {
                    baseHullId = specFile.getHullId();
                }
                ShipCSVEntry csvEntry = GameDataRepository.retrieveShipCSVEntryByID(baseHullId);
                ShipPainter modulePainter = csvEntry.createPainterFromEntry(null);

                if (skinSpec != null) {
                    ShipSkin shipSkin = ShipSkin.createFromSpec(skinSpec);
                    modulePainter.setActiveSpec(ActiveShipSpec.SKIN, shipSkin);
                }

                modulePainter.selectVariant(variant);
                installedFeatures.put(slotID, modulePainter);
            });
        }

    }

    @Override
    public String toString() {
        if (empty) {
            return EMPTY_VARIANT;
        }
        return displayName;
    }

}
