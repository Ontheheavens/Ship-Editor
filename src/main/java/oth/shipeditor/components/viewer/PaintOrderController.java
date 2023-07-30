package oth.shipeditor.components.viewer;

import de.javagl.viewer.Painter;
import lombok.Getter;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.painters.GuidesPainters;
import oth.shipeditor.components.viewer.painters.HotkeyHelpPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.MarkPointsPainter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Optional;

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

    PaintOrderController(PrimaryViewer viewer) {
        this.parent = viewer;

        this.miscPointsPainter = MarkPointsPainter.create();
        this.guidesPainters = new GuidesPainters(viewer);
        this.hotkeyPainter = new HotkeyHelpPainter();
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.paintIfPresent(g, worldToScreen, w, h, guidesPainters.getAxesPaint());

        LayerManager layerManager = parent.getLayerManager();
        List<ViewerLayer> layers = layerManager.getLayers();
        for (ViewerLayer layer : layers) {
            PaintOrderController.paintLayer(g, worldToScreen, w, h, layer);
        }

        this.paintLayerDependentGuides(g, worldToScreen, w, h);

        this.paintIfPresent(g, worldToScreen, w, h, miscPointsPainter);

        this.paintIfPresent(g, worldToScreen, w, h, hotkeyPainter);
    }

    @SuppressWarnings("MethodMayBeStatic")
    private void paintIfPresent(Graphics2D g, AffineTransform worldToScreen, double w, double h, Painter painter) {
        Optional.ofNullable(painter).ifPresent(p -> p.paint(g, worldToScreen, w, h));
    }

    private static void paintLayer(Graphics2D g, AffineTransform worldToScreen,
                                   double w, double h, ViewerLayer layer) {
        LayerPainter shipPainter = layer.getPainter();
        if (shipPainter == null) return;

        AffineTransform transform = shipPainter.getWithRotation(worldToScreen);

        shipPainter.paint(g, transform, w, h);
        if (shipPainter.isUninitialized()) return;
        List<AbstractPointPainter> allPainters = shipPainter.getAllPainters();
        allPainters.forEach(pointPainter -> pointPainter.paint(g, transform, w, h));
    }

    private void paintLayerDependentGuides(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        LayerPainter selected = parent.getSelectedLayer();
        AffineTransform transform = new AffineTransform(worldToScreen);
        if (selected != null) {
            transform = selected.getWithRotation(worldToScreen);
        }

        this.paintIfPresent(g, transform, w, h, guidesPainters.getBordersPaint());
        this.paintIfPresent(g, transform, w, h, guidesPainters.getCenterPaint());
        if (parent.isCursorInViewer()) {
            this.paintIfPresent(g, transform, w, h, guidesPainters.getGuidesPaint());
        }
    }



}
