package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
@Log4j2
public class OpenSpriteAction extends AbstractAction {

    public static void openSpriteAndDo(Consumer<Sprite> action) {
        JFileChooser spriteChooser = FileUtilities.getImageChooser();

        int returnVal = spriteChooser.showOpenDialog(null);
        FileUtilities.lastDirectory = spriteChooser.getCurrentDirectory();
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = spriteChooser.getSelectedFile();

            if (FileUtilities.isFileWithinGamePackages(file)) {
                Sprite sprite = FileLoading.loadSprite(file);
                action.accept(sprite);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        OpenSpriteAction.openSpriteAndDo(sprite -> EventBus.publish(new SpriteOpened(sprite)));
    }

}
