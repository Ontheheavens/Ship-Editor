package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.overseers.StaticController;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 02.07.2023
 */
@Log4j2
public class HotkeyHelpPainter implements Painter {

    private final Map<EditorInstrument, BufferedImage> hintsByMode = new EnumMap<>(EditorInstrument.class);

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        ViewerLayer activeLayer = StaticController.getActiveLayer();
        if (activeLayer == null) return;
        LayerPainter painter = activeLayer.getPainter();
        if (painter == null || painter.isUninitialized()) return;

        Pair<EditorInstrument, List<String>> hints = HotkeyHelpPainter.getHintsToDisplay();
        EditorInstrument current = hints.getFirst();
        List<String> hintList = hints.getSecond();
        if (hintList.isEmpty()) return;

        BufferedImage cachedImage = hintsByMode.computeIfAbsent(current,
                k -> HotkeyHelpPainter.generateImage(current, hintList));

        int imageX = (int) (w - cachedImage.getWidth());
        int imageY = (int) (h - cachedImage.getHeight());
        g.drawImage(cachedImage, imageX, imageY, null);
    }

    private static BufferedImage generateImage(EditorInstrument mode, Iterable<String> hintList) {
        double verticalPadding = 10;
        int imageWidth = 300;
        int imageHeight = 150;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        int anchorY = imageHeight;

        boolean isWeaponsMode = mode == EditorInstrument.BUILT_IN_WEAPONS || mode == EditorInstrument.VARIANT_WEAPONS;
        if (isWeaponsMode) {
            anchorY += 3;
        }

        Point2D anchor = new Point2D.Double(imageWidth - verticalPadding, anchorY);

        for (String hint : hintList) {
            Shape drawResult = DrawUtilities.paintScreenTextOutlined(g, hint, anchor);
            Rectangle2D resultBounds = drawResult.getBounds2D();
            double topRightX = resultBounds.getX() + resultBounds.getWidth();
            double topRightY = resultBounds.getY();

            if (isWeaponsMode) {
                topRightY += 3;
            }

            anchor = new Point2D.Double(topRightX, topRightY - verticalPadding);
        }

        g.dispose();
        return image;
    }


    private static Pair<EditorInstrument, List<String>> getHintsToDisplay() {
        EditorInstrument current = StaticController.getEditorMode();
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
                String appendHint = "Add: Z+LMB";
                String insertHint = "Insert: X+LMB";
                String removeHint = "Remove: Del";
                hints.add(removeHint);
                hints.add(insertHint);
                hints.add(appendHint);
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
            case BUILT_IN_WEAPONS, VARIANT_WEAPONS -> {
                String installHint = "Install weapon: CTRL+LMB";
                String removeHint = "Uninstall weapon: Del";
                hints.add(installHint);
                hints.add(removeHint);
            }
            case VARIANT_MODULES -> {
                String installHint = "Install module: CTRL+LMB";
                String removeHint = "Uninstall module: Del";
                hints.add(installHint);
                hints.add(removeHint);
            }
            case DECORATIVES -> {
                String installHint = "Install decorative: CTRL+LMB";
                String removeHint = "Uninstall decorative: Del";
                hints.add(installHint);
                hints.add(removeHint);
            }
            default -> {
            }
        }
        return new Pair<>(current, hints);
    }

}
