package oth.shipeditor.representation;

import java.nio.file.Path;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
public interface ShipSpecFile {

    String getHullId();

    String getHullName();

    Path getFilePath();

    String getSpriteName();

}
