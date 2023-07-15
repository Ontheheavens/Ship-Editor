package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import de.javagl.viewer.painters.StringBoundsUtils;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.PrimaryShipViewer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * @author Ontheheavens
 * @since 02.07.2023
 */
@Log4j2
public class HotkeyHelpPainter implements Painter {

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        Font hintFont = new Font("Orbitron", Font.BOLD, 16);
        g.setFont(hintFont);
        InstrumentMode current = InstrumentTabsPane.getCurrentMode();
        // The hotkey values are hardcoded because the respective fields in control classes are int constants.
        switch (current) {
            case CENTERS -> {
                String collisionHint = "Press C to alter collision circle";
                Rectangle2D stringBounds = StringBoundsUtils.computeStringBounds(collisionHint, hintFont, null);
                int x = (int) (w - stringBounds.getWidth() - 10);
                int y = (int) (h - stringBounds.getHeight() + 10);

                g.drawString(collisionHint, x, y);
            }
            case BOUNDS -> {
                String insertHint = "Press X to insert bound";
                String appendHint = "Press Z to append bound";
                String removeHint = "Right-click + CTRL or DEL to remove bound";
                Rectangle2D insertBounds = StringBoundsUtils.computeStringBounds(insertHint, hintFont, null);
                Rectangle2D appendBounds = StringBoundsUtils.computeStringBounds(appendHint, hintFont, null);
                Rectangle2D removeBounds = StringBoundsUtils.computeStringBounds(removeHint, hintFont, null);

                int insertX = (int) (w - insertBounds.getWidth() - 10);
                int insertY = (int) (h - insertBounds.getHeight() - 40);
                int appendX = (int) (w - appendBounds.getWidth() - 10);
                int appendY = (int) (h - appendBounds.getHeight() - 15);
                int removeX = (int) (w - removeBounds.getWidth() - 6);
                int removeY = (int) (h - removeBounds.getHeight() + 10);

                g.drawString(insertHint, insertX, insertY);
                g.drawString(appendHint, appendX, appendY);
                g.drawString(removeHint, removeX, removeY);
            }
            default -> {

            }
        }
    }

}
