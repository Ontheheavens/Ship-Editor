package oth.shipeditor.components.viewer.layers.ship.data;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.utility.text.StringValues;

import java.nio.file.Path;

/**
 * @author Ontheheavens
 * @since 28.08.2023
 */
@Getter @Setter
public class ShipVariant {

    private boolean empty;

    public ShipVariant() {
        this(true);
    }

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

}
