package oth.shipeditor.communication.events.components;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 14.07.2023
 */
public record GameDataPanelResized(Dimension newMinimum) implements ComponentEvent {
}
