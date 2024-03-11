package oth.shipeditor.components.viewer;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.painters.DraggedObjectsPainter;
import oth.shipeditor.components.viewer.painters.GuidesPainters;
import oth.shipeditor.components.viewer.painters.HotkeyHelpPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.ship.MarkPointsPainter;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.overseers.MiscCaching;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Responsible for easy manipulation of paint order of all the painters in viewer.
 * Necessary because of exceedingly unsatisfactory implementation of paint ordering in default Viewer.
 * @author Ontheheavens
 * @since 23.07.2023
 */
public class PaintOrderController implements Painter {

    private final PrimaryViewer parent;

    @Getter
    private final MarkPointsPainter miscPointsPainter;

    @Getter
    private final GuidesPainters guidesPainters;

    @Getter
    private final HotkeyHelpPainter hotkeyPainter;

    private BufferedImage backgroundImage;

    @Getter @Setter
    private static boolean showBackgroundImage = true;

    @SuppressWarnings("TypeMayBeWeakened")
    private final DraggedObjectsPainter draggedObjectsPainter = new DraggedObjectsPainter();

    @Setter
    private boolean repaintQueued;

    PaintOrderController(PrimaryViewer viewer) {
        this.parent = viewer;

        this.miscPointsPainter = MarkPointsPainter.create();
        this.guidesPainters = new GuidesPainters(viewer);
        this.hotkeyPainter = new HotkeyHelpPainter();

        Timer repaintTimer = new Timer(6, e -> {
            if (repaintQueued) {
                repaintViewer();
            }
        });
        repaintTimer.setRepeats(true);
        repaintTimer.start();
    }

    private void repaintViewer() {
        parent.repaint();
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (showBackgroundImage) {
            paintBackgroundImage(g, (int) w, (int) h);
        }

        PaintOrderController.paintIfPresent(g, worldToScreen, w, h, guidesPainters.getAxesPaint());

        LayerManager layerManager = parent.getLayerManager();
        List<ViewerLayer> layers = layerManager.getLayers();
        for (ViewerLayer layer : layers) {
            PaintOrderController.paintLayer(g, worldToScreen, w, h, layer);
        }

        this.paintLayerDependentGuides(g, worldToScreen, w, h);

        PaintOrderController.paintIfPresent(g, worldToScreen, w, h, miscPointsPainter);

        if (ViewerDropReceiver.isDragToViewerInProgress() && parent.isCursorInViewer()) {
            draggedObjectsPainter.paint(g, worldToScreen, w, h);
        }

        PaintOrderController.paintIfPresent(g, worldToScreen, w, h, hotkeyPainter);
    }

    private void paintBackgroundImage(Graphics2D g, int w, int h) {
        if (backgroundImage == null) {
            backgroundImage = PaintOrderController.createCheckerboardImage();
        }

        float alpha = 0.5f;
        Composite old = Utility.setAlphaComposite(g, alpha);

        g.drawImage(backgroundImage, 0, 0, w, h,
                0, 0, w, h, null);

        g.setComposite(old);
    }

    private static void paintIfPresent(Graphics2D g, AffineTransform worldToScreen,
                                       double w, double h, Painter painter) {
        if (painter != null) {
            painter.paint(g, worldToScreen, w, h);
        }
    }

    public static void paintLayer(Graphics2D g, AffineTransform worldToScreen,
                                   double w, double h, ViewerLayer layer) {
        LayerPainter layerPainter = layer.getPainter();
        if (layerPainter == null) return;

        AffineTransform transform = MiscCaching.getLayerRotationTransform();
        transform = layerPainter.getWithRotation(worldToScreen, transform);

        layerPainter.paint(g, transform, w, h);

        List<AbstractPointPainter> allPainters = layerPainter.getAllPainters();
        for (AbstractPointPainter pointPainter : allPainters) {
            pointPainter.paint(g, transform, w, h);
        }
    }

    private void paintLayerDependentGuides(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        LayerPainter selected = parent.getSelectedLayer();
        AffineTransform transform = MiscCaching.getLayerRotationTransform();
        if (selected != null) {
            transform = selected.getWithRotation(worldToScreen, transform);
        }

        PaintOrderController.paintIfPresent(g, transform, w, h, guidesPainters.getBordersPaint());
        PaintOrderController.paintIfPresent(g, transform, w, h, guidesPainters.getCenterPaint());
        if (!ViewerDropReceiver.isDragToViewerInProgress() && parent.isCursorInViewer()) {
            PaintOrderController.paintIfPresent(g, transform, w, h, guidesPainters.getGuidesPaint());
        }
    }

    private static BufferedImage createCheckerboardImage() {
        int imageWidth = 3840;
        int imageHeight = 2160;
        int cellSize = 10;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        int rows = imageWidth / cellSize;
        int cols = imageHeight / cellSize;
        for (int row = 0; row < rows; row++) {
            PaintOrderController.drawRow(g2d, row, cols, cellSize);
        }
        g2d.dispose();
        return image;
    }

    private static void drawRow(Graphics2D g2d, int row, int cols, int cellSize) {
        for (int col = 0; col < cols; col++) {
            Color color = (row + col) % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY;
            int x = col * cellSize;
            int y = row * cellSize;
            g2d.setColor(color);
            g2d.fillRect(x, y, cellSize, cellSize);
        }
    }

}
