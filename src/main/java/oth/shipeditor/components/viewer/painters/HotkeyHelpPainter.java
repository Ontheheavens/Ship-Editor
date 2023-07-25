package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.instrument.InstrumentTabsPane;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.utility.graphics.DrawUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Ontheheavens
 * @since 02.07.2023
 */
@Log4j2
public class HotkeyHelpPainter implements Painter {

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        InstrumentMode current = InstrumentTabsPane.getCurrentMode();

        Collection<String> hints = new ArrayList<>();
        // The hotkey values are hardcoded because the respective fields in control classes are int constants.
        switch (current) {
            case COLLISION -> {
                String radiusHint = "Alter collision radius: C";

                hints.add(radiusHint);
            }
            case SHIELD -> {
                String radiusHint = "Alter shield radius: S";
                hints.add(radiusHint);
            }
            case BOUNDS -> {
                String insertHint = "Insert bound: X";
                String appendHint = "Append bound: Z";
                String removeHint = "Remove bound: Del";
                hints.add(removeHint);
                hints.add(appendHint);
                hints.add(insertHint);
            }
            default -> {
            }
        }

        if (hints.isEmpty()) return;
        double verticalPadding = 0;
        double x = w - 10;
        double y = h - verticalPadding;
        Point2D anchor = new Point2D.Double(x, y);
        for (String hint : hints) {
            // This is a bit of an unsightly hack, but we got more important matters to deal with.
            // Perhaps refactor later.
            boolean lastCharShort = hint.endsWith("l");
            if (lastCharShort) {
                anchor.setLocation(anchor.getX() + 1, anchor.getY());
            }

            Shape drawResult = DrawUtilities.paintScreenTextOutlined(g, hint, anchor);
            Rectangle2D resultBounds = drawResult.getBounds2D();
            double topRightX = resultBounds.getX() + resultBounds.getWidth();

            if (lastCharShort) {
                topRightX -= 2;
            }

            double topRightY = resultBounds.getY();
            anchor = new Point2D.Double(topRightX, topRightY - verticalPadding);
        }
    }

}
