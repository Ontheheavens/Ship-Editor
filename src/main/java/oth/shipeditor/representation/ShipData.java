package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
@Getter @Setter
public class ShipData {

    private Hull hull;

    private Skin skin;

    public ShipData(Hull openedHull) {
        this.hull = openedHull;
    }

}
