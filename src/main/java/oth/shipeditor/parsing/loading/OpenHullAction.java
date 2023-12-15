package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
@Log4j2
public class OpenHullAction extends AbstractAction {

    static void openHullAndDo(ActionListener action) {
        JFileChooser shipDataChooser = FileUtilities.getHullFileChooser();
        int returnVal = shipDataChooser.showOpenDialog(null);
        FileUtilities.setLastShipDirectory(shipDataChooser.getCurrentDirectory());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ActionEvent event = new ActionEvent(shipDataChooser, ActionEvent.ACTION_PERFORMED, null);
            action.actionPerformed(event);
        }
        else {
            log.info(FileUtilities.OPEN_COMMAND_CANCELLED_BY_USER);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OpenHullAction.openHullAndDo(event -> {
            JFileChooser shipDataChooser = (JFileChooser) event.getSource();
            File file = shipDataChooser.getSelectedFile();
            HullSpecFile hullSpecFile = FileLoading.loadHullFile(file);
            if (hullSpecFile != null) {
                EventBus.publish(new HullFileOpened(hullSpecFile, file.getName()));
            }
            else {
                log.error(StringValues.FAILURE_TO_LOAD_HULL_CANCELLING_ACTION, file);
                JOptionPane.showMessageDialog(null,
                        StringValues.FAILURE_TO_LOAD_HULL_CANCELLING_ACTION_ALT + file,
                        StringValues.FILE_LOADING_ERROR,
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
