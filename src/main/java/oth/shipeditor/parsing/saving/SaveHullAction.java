package oth.shipeditor.parsing.saving;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.BoundPointsPainter;
import oth.shipeditor.components.viewer.painters.points.ship.CenterPointPainter;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.representation.HullSpecFile;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
@Log4j2
final class SaveHullAction {

    private SaveHullAction() {
    }

    @SuppressWarnings("CallToPrintStackTrace")
    static void saveHullFromLayer(ShipLayer shipLayer) {
        JFileChooser fileChooser = SaveHullAction.getSaveHullFileChooser();
        int returnVal = fileChooser.showSaveDialog(null);
        FileUtilities.setLastDirectory(fileChooser.getCurrentDirectory());

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String extension = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
            File result = FileUtilities.ensureFileExtension(fileChooser, extension);

            log.info("Commencing hull saving: {}", result);

            ObjectMapper objectMapper = FileUtilities.getConfigured();
            HullSpecFile toSerialize = SaveHullAction.rebuildHullFile(shipLayer);
            try {
                objectMapper.writeValue(result, toSerialize);
            } catch (IOException e) {
                log.error("Hull file saving failed: {}", result.getName());
                JOptionPane.showMessageDialog(null,
                        "Hull file saving failed, exception thrown at: " + result,
                        StringValues.FILE_SAVING_ERROR,
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private static HullSpecFile rebuildHullFile(ShipLayer shipLayer) {
        HullSpecFile result = new HullSpecFile();

        var shipPainter = shipLayer.getPainter();
        Point2D.Double[] serializableBounds = SaveHullAction.rebuildBounds(shipPainter);

        result.setBounds(serializableBounds);

        return result;
    }

    private static Point2D.Double[] rebuildBounds(ShipPainter shipPainter) {
        BoundPointsPainter boundsPainter = shipPainter.getBoundsPainter();
        var boundPoints = boundsPainter.getPointsIndex();
        CenterPointPainter centerPointPainter = shipPainter.getCenterPointPainter();
        ShipCenterPoint shipCenterPoint = centerPointPainter.getCenterPoint();
        var centerPosition = shipCenterPoint.getPosition();

        // Initialize the array for serializable bounds
        Point2D.Double[] serializableBounds = new Point2D.Double[boundPoints.size()];

        // Rebuild each bound point
        for (int i = 0; i < boundPoints.size(); i++) {
            BoundPoint boundPoint = boundPoints.get(i);
            Point2D locationRelativeToCenter = Utility.getPointCoordinatesForDisplay(boundPoint.getPosition(),
                    shipPainter, CoordsDisplayMode.SHIP_CENTER);
            serializableBounds[i] = (Point2D.Double) locationRelativeToCenter;
        }

        return serializableBounds;
    }

    private static JFileChooser getSaveHullFileChooser() {
        FileNameExtensionFilter shipFileFilter = new FileNameExtensionFilter(
                StringValues.JSON_SHIP_FILES, "ship");
        return FileUtilities.getFileChooser(shipFileFilter);
    }

}
