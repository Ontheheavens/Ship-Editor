package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.utility.graphics.Sprite;

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
        if (FileLoading.lastDirectory != null) {
            spriteChooser.setCurrentDirectory(FileLoading.lastDirectory);
        }
        FileNameExtensionFilter spriteFilter = new FileNameExtensionFilter(
                "PNG Images", "png");
        spriteChooser.setFileFilter(spriteFilter);
        int returnVal = spriteChooser.showOpenDialog(null);
        FileLoading.lastDirectory = spriteChooser.getCurrentDirectory();
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = spriteChooser.getSelectedFile();
            Sprite sprite = FileLoading.loadSprite(file);
            EventBus.publish(new SpriteOpened(sprite));
        }
        else {
            log.info(FileLoading.OPEN_COMMAND_CANCELLED_BY_USER);
        }
    }

}
