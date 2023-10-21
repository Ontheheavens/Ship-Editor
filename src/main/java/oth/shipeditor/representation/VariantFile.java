package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.layers.ship.data.Variant;
import oth.shipeditor.parsing.deserialize.ModulesDeserializer;
import oth.shipeditor.utility.text.StringConstants;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Getter
public class VariantFile implements Variant {

    @JsonIgnore
    private static final VariantFile EMPTY = new VariantFile(true);

    public static final String DEFAULT = StringConstants.DEFAULT_ID;

    @JsonCreator
    public VariantFile() {
        empty = false;
    }

    private VariantFile(boolean isEmpty) {
        empty = isEmpty;
    }

    public static VariantFile empty() {
        return EMPTY;
    }

    @JsonIgnore
    private final boolean empty;

    @Setter
    @JsonIgnore
    private Path variantFilePath;

    @Setter
    @JsonIgnore
    private Path containingPackage;

    @JsonProperty("displayName")
    private String displayName;

    @JsonAlias("goalVariants")
    @JsonProperty("goalVariant")
    private boolean goalVariant;

    @JsonProperty("fluxCapacitors")
    private int fluxCapacitors;

    @JsonProperty("fluxVents")
    private int fluxVents;

    @JsonProperty(StringConstants.HULL_ID)
    private String hullId;

    @JsonAlias("mods")
    @JsonProperty("hullMods")
    private List<String> hullMods;

    @JsonAlias(StringConstants.BUILT_IN_MODS)
    @JsonProperty("permaMods")
    private List<String> permaMods;

    @JsonProperty("sMods")
    private List<String> sMods;

    /**
     * Is somewhat obscure; some sources claim the value contract is 1 being the best and 3 the worst quality.
     * However, my own impression on the usage in vanilla is that 0.0 is the worst and 1.0 is best.
     * According to Histidine, the field isn't used in newer Starsector versions.
     * <p>
     * Is set to -1 as default here for safety purposes.
     * Anything less than 0 will be ignored at serialization to result file.
     */
    @JsonProperty("quality")
    private double quality = -1;

    @JsonProperty("variantId")
    private String variantId;

    @JsonProperty("weaponGroups")
    private List<SpecWeaponGroup> weaponGroups;

    @JsonProperty("wings")
    private List<String> wings;

    @JsonProperty("modules")
    @JsonDeserialize(using = ModulesDeserializer.class)
    private Map<String, String> modules;

    @Override
    public String toString() {
        if (empty) {
            return ShipVariant.EMPTY_VARIANT;
        }
        var hullFile = GameDataRepository.retrieveSpecByID(this.hullId);
        if (hullFile != null) {
            String hullName = hullFile.getHullName();
            return this.displayName + " " + hullName;
        }
        return displayName;
    }

    @Override
    public String getShipHullId() {
        return hullId;
    }

}