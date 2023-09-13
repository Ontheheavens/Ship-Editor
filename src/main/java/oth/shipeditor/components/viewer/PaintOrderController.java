package oth.shipeditor.components.viewer;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.painters.GuidesPainters;
import oth.shipeditor.components.viewer.painters.HotkeyHelpPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.MarkPointsPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
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

    @Setter
    private boolean repaintQueued;

    PaintOrderController(PrimaryViewer viewer) {
        this.parent = viewer;

        this.miscPointsPainter = MarkPointsPainter.create();
        this.guidesPainters = new GuidesPainters(viewer);
        this.hotkeyPainter = new HotkeyHelpPainter();

        Timer repaintTimer = new Timer(4, e -> {
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
        PaintOrderController.paintIfPresent(g, worldToScreen, w, h, guidesPainters.getAxesPaint());

        LayerManager layerManager = parent.getLayerManager();
        List<ViewerLayer> layers = layerManager.getLayers();
        for (ViewerLayer layer : layers) {
            PaintOrderController.paintLayer(g, worldToScreen, w, h, layer);
        }

        this.paintLayerDependentGuides(g, worldToScreen, w, h);

        PaintOrderController.paintIfPresent(g, worldToScreen, w, h, miscPointsPainter);

        PaintOrderController.paintIfPresent(g, worldToScreen, w, h, hotkeyPainter);
    }

    private static void paintIfPresent(Graphics2D g, AffineTransform worldToScreen, double w, double h, Painter painter) {
        if (painter != null) {
            painter.paint(g, worldToScreen, w, h);
        }
    }

    private static void paintLayer(Graphics2D g, AffineTransform worldToScreen,
                                   double w, double h, ViewerLayer layer) {
        LayerPainter layerPainter = layer.getPainter();
        if (layerPainter == null) return;

        AffineTransform transform = layerPainter.getWithRotation(worldToScreen);

        layerPainter.paint(g, transform, w, h);

        List<AbstractPointPainter> allPainters = layerPainter.getAllPainters();
        for (AbstractPointPainter pointPainter : allPainters) {
            pointPainter.paint(g, transform, w, h);
        }
    }

    private void paintLayerDependentGuides(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        LayerPainter selected = parent.getSelectedLayer();
        AffineTransform transform = new AffineTransform(worldToScreen);
        if (selected != null) {
            transform = selected.getWithRotation(worldToScreen);
        }

        PaintOrderController.paintIfPresent(g, transform, w, h, guidesPainters.getBordersPaint());
        PaintOrderController.paintIfPresent(g, transform, w, h, guidesPainters.getCenterPaint());
        if (parent.isCursorInViewer()) {
            PaintOrderController.paintIfPresent(g, transform, w, h, guidesPainters.getGuidesPaint());
        }
    }

}
