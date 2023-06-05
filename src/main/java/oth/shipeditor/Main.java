package oth.shipeditor;

import com.formdev.flatlaf.FlatIntelliJLaf;
import oth.shipeditor.menubar.Files;

import javax.swing.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatIntelliJLaf.setup();
            PrimaryWindow window = PrimaryWindow.create();
            window.showGUI();
            Main.testFiles(window);
        });
    }

    private static void testFiles(PrimaryWindow window) {
        String legionSprite = "";
        String crigSprite = "salvage_rig.png";
        String legionHull = "legion.ship";
        String crigHull = "constructionrig.ship";
        Class<? extends PrimaryWindow> windowClass = window.getClass();
        ClassLoader classLoader = windowClass.getClassLoader();
        URL spritePath = Objects.requireNonNull(classLoader.getResource(crigSprite));
        File sprite;
        try {
            sprite = new File(spritePath.toURI());
            Files.loadSprite(sprite);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        URL dataPath = Objects.requireNonNull(classLoader.getResource(crigHull));;
        try {
            URI url = dataPath.toURI();
            File hullFile = new File(url);
            Files.loadHullFile(hullFile);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
