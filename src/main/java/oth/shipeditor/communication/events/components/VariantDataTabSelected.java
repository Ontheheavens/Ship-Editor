package oth.shipeditor.communication.events.components;

import oth.shipeditor.components.instrument.ship.variant.VariantDataTab;

/**
 * @author Ontheheavens
 * @since 12.11.2023
 */
public record VariantDataTabSelected(VariantDataTab selected) implements ComponentEvent {

}
