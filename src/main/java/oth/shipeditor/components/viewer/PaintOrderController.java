package oth.shipeditor.components.viewer;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.GuidesPainters;
import oth.shipeditor.components.viewer.painters.HotkeyHelpPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.ship.MarkPointsPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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
            if (backgroundImage == null) {
                backgroundImage = PaintOrderController.createCheckerboardImage();
            }
            int width = (int) w;
            int height = (int) h;

            float alpha = 0.5f;
            Composite old = Utility.setAlphaComposite(g, alpha);

            g.drawImage(backgroundImage, 0, 0, width, height,
                    0, 0, width, height, null);

            g.setComposite(old);
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
            PaintOrderController.paintDraggedEntity(g, worldToScreen, w, h);
        }

        PaintOrderController.paintIfPresent(g, worldToScreen, w, h, hotkeyPainter);
    }


    @SuppressWarnings("ChainOfInstanceofChecks")
    private static void paintDraggedEntity(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        InstallableEntry dragged = ViewerDropReceiver.getDraggedEntry();
        Point2D currentCursor = StaticController.getCorrectedWithoutRotate();

        double rotation = 0;
        WeaponMount mount = WeaponMount.TURRET;
        WeaponSlotPoint selectedWeaponSlot = StaticController.getSelectedAndEligibleSlot();
        if (selectedWeaponSlot != null) {
            rotation = selectedWeaponSlot.getAngle();
            ShipPainter weaponSlotParent = selectedWeaponSlot.getParent();
            double rotationRadians = weaponSlotParent.getRotationRadians();
            rotation -= Math.toDegrees(rotationRadians);

            rotation = Utility.flipAngle(rotation);
            mount = selectedWeaponSlot.getWeaponMount();
        }

        if (dragged instanceof ShipCSVEntry shipEntry) {
            double conditionalAngle = rotation;
            if (StaticController.getEditorMode() != EditorInstrument.VARIANT_MODULES) {
                conditionalAngle = 0;
            }
            shipEntry.paintEntry(g, worldToScreen, conditionalAngle, currentCursor);
            return;
        }

        if (dragged instanceof WeaponCSVEntry weaponEntry) {
            weaponEntry.paintEntry(g, worldToScreen,
                    rotation, currentCursor, mount);
        }
    }

    private static void paintIfPresent(Graphics2D g, AffineTransform worldToScreen, double w, double h, Painter painter) {
        if (painter != null) {
            painter.paint(g, worldToScreen, w, h);
        }
    }

    public static void paintLayer(Graphics2D g, AffineTransform worldToScreen,
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
            for (int col = 0; col < cols; col++) {
                if ((row + col) % 2 == 0) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(Color.LIGHT_GRAY);
                }

                int x = col * cellSize;
                int y = row * cellSize;

                g2d.fillRect(x, y, cellSize, cellSize);
            }
        }
        g2d.dispose();
        return image;
    }

}
