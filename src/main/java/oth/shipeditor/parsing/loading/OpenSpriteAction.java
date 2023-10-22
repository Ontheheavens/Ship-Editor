package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
@Log4j2
public class OpenSpriteAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        Path coreFolderPath = SettingsManager.getCoreFolderPath();
        JFileChooser spriteChooser = new JFileChooser(coreFolderPath.toString());
        if (FileUtilities.lastDirectory != null) {
            spriteChooser.setCurrentDirectory(FileUtilities.lastDirectory);
        }
        FileNameExtensionFilter spriteFilter = new FileNameExtensionFilter(
                "PNG Images", "png");
        spriteChooser.setFileFilter(spriteFilter);
        int returnVal = spriteChooser.showOpenDialog(null);
        FileUtilities.lastDirectory = spriteChooser.getCurrentDirectory();
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = spriteChooser.getSelectedFile();

            if (OpenSpriteAction.isFileWithinPackages(file)) {
                Sprite sprite = FileLoading.loadSprite(file);
                EventBus.publish(new SpriteOpened(sprite));
            } else {
                log.error("Selected file is outside of any game packages. Image loading aborted.");
                JOptionPane.showMessageDialog(null,
                        "Selected image is outside of any game packages: " + file,
                        StringValues.FILE_LOADING_ERROR,
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            log.info(FileUtilities.OPEN_COMMAND_CANCELLED_BY_USER);
        }
    }

    private static boolean isFileWithinPackages(File file) {
        Path filePath = file.toPath();

        if (filePath.startsWith(SettingsManager.getCoreFolderPath())) {
            return true;
        }

        for (Path modFolder : SettingsManager.getAllModFolders()) {
            if (filePath.startsWith(modFolder)) {
                return true;
            }
        }

        return false;
    }

}
