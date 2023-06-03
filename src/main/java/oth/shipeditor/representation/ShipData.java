package oth.shipeditor.representation;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
public class ShipData {

    @Getter @Setter
    private Hull hull;

    public ShipData(Hull hull) {
        this.hull = hull;
    }

}
