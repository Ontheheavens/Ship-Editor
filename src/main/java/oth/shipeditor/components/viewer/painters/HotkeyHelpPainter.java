package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.instrument.ship.ShipInstrumentsPane;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.graphics.DrawUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 02.07.2023
 */
@Log4j2
public class HotkeyHelpPainter implements Painter {

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        if (activeLayer == null) return;
        LayerPainter painter = activeLayer.getPainter();
        if (painter == null || painter.isUninitialized()) return;
        Collection<String> hints = HotkeyHelpPainter.getHintsToDisplay();
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

    private static List<String> getHintsToDisplay() {
        ShipInstrument current = ShipInstrumentsPane.getCurrentMode();
        List<String> hints = new ArrayList<>();
        switch (current) {
            case COLLISION -> {
                String radiusHint = "Alter radius: C";
                hints.add(radiusHint);
            }
            case SHIELD -> {
                String radiusHint = "Alter radius: S";
                hints.add(radiusHint);
            }
            case BOUNDS -> {
                String insertHint = "Insert: X+LMB";
                String appendHint = "Append: Z+LMB";
                String removeHint = "Remove: Del";
                hints.add(removeHint);
                hints.add(appendHint);
                hints.add(insertHint);
            }
            case WEAPON_SLOTS -> {
                String angleHint = "Alter angle: A+LMB";
                String arcHint = "Alter arc: A+RMB";
                String addHint = "Add slot: W+LMB";
                hints.add(angleHint);
                hints.add(arcHint);
                hints.add(addHint);
            }
            case LAUNCH_BAYS -> {
                String addPort = "Add port: P+LMB";
                String addBay = "Add bay: B+LMB";
                hints.add(addPort);
                hints.add(addBay);
            }
            case ENGINES -> {
                String angleHint = "Alter angle: D+LMB";
                String sizeHint = "Alter size: D+RMB";
                String addHint = "Add engine: E+LMB";
                hints.add(angleHint);
                hints.add(sizeHint);
                hints.add(addHint);
            }
            default -> {
            }
        }
        return hints;
    }

}
