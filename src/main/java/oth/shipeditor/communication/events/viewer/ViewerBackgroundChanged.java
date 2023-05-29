package oth.shipeditor.communication.events.viewer;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 28.05.2023
 */
public record ViewerBackgroundChanged(Color newColor) implements ViewerEvent {

}
