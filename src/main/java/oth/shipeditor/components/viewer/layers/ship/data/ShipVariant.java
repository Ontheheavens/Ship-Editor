package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.representation.VariantFile;
import oth.shipeditor.utility.text.StringValues;

import java.nio.file.Path;

/**
 * @author Ontheheavens
 * @since 28.08.2023
 */
@Getter @Setter
public class ShipVariant implements Variant {

    public static final String EMPTY_VARIANT = "Empty variant";
    private boolean empty;

    public ShipVariant() {
        this(true);
    }

    @SuppressWarnings("BooleanParameter")
    public ShipVariant(boolean isEmpty) {
        this.empty = isEmpty;
    }

    private Path variantFilePath;

    public String getFileName() {
        if (variantFilePath == null) return StringValues.EMPTY;
        return variantFilePath.getFileName().toString();
    }

    private Path containingPackage;

    /**
     * Can be either ID of base hull or skin hull ID.
     */
    private String shipHullId;

    private String variantId;

    private String displayName;

    public void initialize(VariantFile file) {
        this.setVariantId(variantId);
        this.setShipHullId(file.getHullId());
        this.setVariantFilePath(file.getVariantFilePath());
        this.setContainingPackage(file.getContainingPackage());
        this.setDisplayName(file.getDisplayName());
    }

    @Override
    public String toString() {
        if (empty) {
            return EMPTY_VARIANT;
        }
        return displayName;
    }

}
