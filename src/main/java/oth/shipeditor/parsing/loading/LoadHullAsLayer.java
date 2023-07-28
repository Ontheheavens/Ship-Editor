package oth.shipeditor.parsing.loading;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.SpriteOpened;
import oth.shipeditor.communication.events.viewer.layers.LastLayerSelectQueued;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreationQueued;
import oth.shipeditor.representation.Hull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
@Log4j2
public class LoadHullAsLayer extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        FileLoading.openHullAndDo(e1 -> {
            JFileChooser shipDataChooser = (JFileChooser) e1.getSource();
            File file = shipDataChooser.getSelectedFile();
            Hull hull = FileLoading.loadHullFile(file);

            String spriteName = hull.getSpriteName();

            Path spriteFilePath = Path.of(spriteName);
            File spriteFile = FileLoading.fetchDataFile(spriteFilePath, null);

            if (spriteFile == null) {
                log.error("Failed to find sprite for ship file {}", file);
                JOptionPane.showMessageDialog(null,
                        "No sprite in game data is found for the selected ship file.",
                        "Sprite not found",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            EventBus.publish(new ShipLayerCreationQueued());
            EventBus.publish(new LastLayerSelectQueued());
            BufferedImage sprite = FileLoading.loadSprite(spriteFile);
            EventBus.publish(new SpriteOpened(sprite, spriteFile.getName()));
            EventBus.publish(new HullFileOpened(hull, file.getName()));
        });
    }

}
