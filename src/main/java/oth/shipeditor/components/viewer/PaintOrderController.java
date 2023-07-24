package oth.shipeditor.components.viewer;

import de.javagl.viewer.Painter;
import lombok.Getter;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.GuidesPainters;
import oth.shipeditor.components.viewer.painters.HotkeyHelpPainter;
import oth.shipeditor.components.viewer.painters.WorldPointsPainter;

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

    private final LayerManager layerManager;

    @Getter
    private final WorldPointsPainter miscPointsPainter;

    @Getter
    private final GuidesPainters guidesPainters;

    @Getter
    private final HotkeyHelpPainter hotkeyPainter;

    PaintOrderController(LayerManager manager, PrimaryShipViewer viewer) {
        this.layerManager = manager;

        this.miscPointsPainter = WorldPointsPainter.create();
        this.guidesPainters = new GuidesPainters(viewer);
        this.hotkeyPainter = new HotkeyHelpPainter();
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        this.paintIfPresent(g, worldToScreen, w, h, guidesPainters.getAxesPaint());

        List<ShipLayer> layers = layerManager.getLayers();
        for (ShipLayer layer : layers) {
            PaintOrderController.paintLayer(g, worldToScreen, w, h, layer);
        }

        this.paintIfPresent(g, worldToScreen, w, h, guidesPainters.getBordersPaint());
        this.paintIfPresent(g, worldToScreen, w, h, guidesPainters.getCenterPaint());
        this.paintIfPresent(g, worldToScreen, w, h, guidesPainters.getGuidesPaint());

        this.paintIfPresent(g, worldToScreen, w, h, miscPointsPainter);

        this.paintIfPresent(g, worldToScreen, w, h, hotkeyPainter);
    }

    @SuppressWarnings("MethodMayBeStatic")
    private void paintIfPresent(Graphics2D g, AffineTransform worldToScreen, double w, double h, Painter painter) {
        Optional.ofNullable(painter).ifPresent(p -> p.paint(g, worldToScreen, w, h));
    }

    private static void paintLayer(Graphics2D g, AffineTransform worldToScreen,
                                   double w, double h, ShipLayer layer) {
        LayerPainter layerPainter = layer.getPainter();
        if (layerPainter == null) return;
        layerPainter.paint(g, worldToScreen, w, h);
        if (layerPainter.isUninitialized()) return;
        List<AbstractPointPainter> allPainters = layerPainter.getAllPainters();
        allPainters.forEach(pointPainter -> pointPainter.paint(g, worldToScreen, w, h));
    }

}
