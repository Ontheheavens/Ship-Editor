package oth.shipeditor.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.icons.FlatAbstractIcon;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.PrimaryWindow;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerBackgroundChanged;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.utility.Errors;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
@Log4j2
public final class Initializations {

    public static final String FILE_CHOOSER_SHORTCUTS_FILES_FUNCTION = "FileChooser.shortcuts.filesFunction";
    public static final String SHELL_FOLDER_0_X_12 = "ShellFolder: 0x12";

    private Initializations() {
    }

    @SuppressWarnings({"ProhibitedExceptionThrown", "CallToPrintStackTrace"})
    public static void updateStateFromSettings(PrimaryWindow window) {
        Settings settings = SettingsManager.getSettings();
        EventBus.publish(new ViewerBackgroundChanged(settings.getBackgroundColor()));
        try {
            Initializations.installGameFolderShortcut(window);
            Initializations.installWindowIcon(window);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Customization of file chooser failed!", e);
        }
    }

    private static void installGameFolderShortcut(PrimaryWindow window) throws URISyntaxException, IOException {
        Class<? extends PrimaryWindow> windowClass = window.getClass();
        ClassLoader classLoader = windowClass.getClassLoader();
        String iconName = "gamefolder_icon64.png";

        File iconFile;
        BufferedImage iconImage;
        URL iconPath = Objects.requireNonNull(classLoader.getResource(iconName));
        URI checked = iconPath.toURI();
        if (checked.isOpaque()) {
            try ( InputStream inputStream = Initializations.class.getResourceAsStream("/" + iconName)) {
                if (inputStream != null) {
                    iconImage = ImageIO.read(inputStream);
                } else {
                    throw new RuntimeException("Game folder icon not found!");
                }
            }
        } else {
            iconFile = new File(checked);
            log.info("Loading game folder icon...");
            iconImage = ImageIO.read(iconFile);
        }

        Settings settings = SettingsManager.getSettings();
        String folderPath = settings.getGameFolderPath();

        UIManager.put(FILE_CHOOSER_SHORTCUTS_FILES_FUNCTION, (Function<File[], File[]>) files -> {
            ArrayList<File> list = new ArrayList<>( Arrays.asList( files ) );
            list.removeIf(next -> SHELL_FOLDER_0_X_12.equals(next.getPath()));
            list.add( 0, new File(folderPath));
            return list.toArray(new File[0]);
        } );
        UIManager.put( "FileChooser.shortcuts.displayNameFunction", (Function<File, String>) file -> {
            String absolutePath = file.getAbsolutePath();
            if (absolutePath.equals(folderPath)) {
                return "Game folder";
            }
            return null;
        } );
        UIManager.put( "FileChooser.shortcuts.iconFunction", (Function<File, Icon>) file -> {
            String absolutePath = file.getAbsolutePath();
            if (absolutePath.equals(folderPath)) {
                return new GameFolderIcon(iconImage);
            }
            return null;
        } );
    }

    private static void installWindowIcon(PrimaryWindow window) throws URISyntaxException, IOException {
        Class<? extends PrimaryWindow> windowClass = window.getClass();
        ClassLoader classLoader = windowClass.getClassLoader();
        String iconName = "icon.png";

        File iconFile;
        BufferedImage iconImage;
        URL iconPath = Objects.requireNonNull(classLoader.getResource(iconName));
        URI checked = iconPath.toURI();
        if (checked.isOpaque()) {
            try ( InputStream inputStream = Initializations.class.getResourceAsStream("/" + iconName)) {
                if (inputStream != null) {
                    iconImage = ImageIO.read(inputStream);
                } else {
                    throw new RuntimeException("Window icon not found!");
                }
            }
        } else {
            iconFile = new File(checked);
            log.info("Loading window icon...");
            iconImage = ImageIO.read(iconFile);
        }

        ImageIcon icon = new ImageIcon(iconImage);
        window.setIconImage(icon.getImage());
    }

    private static class GameFolderIcon extends FlatAbstractIcon {

        private final BufferedImage iconImage;

        GameFolderIcon(BufferedImage image) {
            super(64, 64, Color.WHITE);
            this.iconImage = image;
        }

        @Override
        protected void paintIcon(Component c, Graphics2D g2) {
            g2.drawImage(iconImage, 0, 0, width, height, null);
        }

    }

    public static void initializeSettingsFile() {
        ObjectMapper mapper = SettingsManager.getMapperForSettingsFile();
        Settings loaded;
        Path workingDirectory = Paths.get("").toAbsolutePath();
        log.info("Current folder: {}", workingDirectory);
        File settingsFile = SettingsManager.getSettingsPath();
        try {
            if (settingsFile.exists()) {
                log.info("Reading existing settings file...");
                loaded = mapper.readValue(settingsFile, Settings.class);
                if (loaded != null) {
                    String coreFolder = loaded.getCoreFolderPath();
                    if (coreFolder != null && !coreFolder.isEmpty()) {
                        SettingsManager.setCoreFolderName(FileUtilities.extractFolderName(coreFolder));
                    }
                    log.info("Settings read successful.");
                }
            } else {
                log.info("Settings file not found, creating default...");
                loaded = SettingsManager.createDefault();
                mapper.writeValue(settingsFile, loaded);
                if (settingsFile.exists()) {
                    log.info("Default settings file creation successful.");
                }
            }
        } catch (IOException e) {
            log.error("Failed to resolve settings file, writing default one.", e);
            loaded = SettingsManager.createDefault();
            SettingsManager.writeSettingsToFile(mapper, settingsFile, loaded);
        }
        SettingsManager.setSettings(loaded);
        SettingsManager.getCorePackage();
    }

    @SuppressWarnings({"ProhibitedExceptionThrown", "IfStatementWithNegatedCondition"})
    public static void selectGameFolder() {
        Settings settings = SettingsManager.getSettings();
        String gameFolderPath = settings.getGameFolderPath();
        if (gameFolderPath != null && !gameFolderPath.isEmpty()) {
            return;
        }

        String[] predefinedPaths = {"C:\\Games\\Starsector", "C:\\Program Files (x86)\\Fractal Softworks\\Starsector"};

        for (String predefinedPath : predefinedPaths) {
            Path predefinedFolderPath = Paths.get(predefinedPath);
            if (Initializations.checkGameFolderEligibility(predefinedFolderPath, settings)) {
                log.info("Saving game folder path from predefined paths...");
                String resultPath = predefinedFolderPath.toAbsolutePath().toString();
                settings.setGameFolderPath(resultPath);
                return;
            }
        }

        String errorMessage = "Game folder selection failed!";
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setDialogTitle("Choose folder containing installed game:");
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = folderChooser.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            settings.setGameFolderPath("");
            throw new RuntimeException(errorMessage);
        }
        else {
            File file = folderChooser.getSelectedFile();
            Path filePath = file.toPath();

            if (Initializations.checkGameFolderEligibility(filePath, settings)) {
                String absolutePath = file.getAbsolutePath();
                log.info("Saving game folder path...");
                settings.setGameFolderPath(absolutePath);
            } else {
                log.info("Selected game folder path invalid.");
                JOptionPane.showMessageDialog(null,
                        "Selected folder does not contain core and mod data folders.",
                        "Invalid folder",
                        JOptionPane.ERROR_MESSAGE);
                Initializations.selectGameFolder();
            }
        }
    }

    private static boolean checkGameFolderEligibility(Path filePath, Settings settings) {
        boolean folderHasCore = false;
        boolean folderHasMods = false;
        try (Stream<Path> pathStream = Files.walk(filePath, 5)) {
            Stream<Path> filtered = pathStream.filter(Files::isDirectory);
            for (Path path : filtered.toArray(Path[]::new)) {
                String folderName = path.getFileName().toString();
                String coreFolderPath = path.toString();

                if (Initializations.isCoreFolder(path)) {
                    settings.setCoreFolderPath(coreFolderPath);
                    SettingsManager.setCoreFolderName(FileUtilities.extractFolderName(coreFolderPath));
                    folderHasCore = true;
                }

                if ("mods".equals(folderName)) {
                    settings.setModFolderPath(coreFolderPath);
                    folderHasMods = true;
                }
            }
        } catch (IOException e) {
            Errors.printToStream(e);
            return false;
        }
        return folderHasCore && folderHasMods;
    }

    private static boolean isCoreFolder(Path folderPath) {
        if (!Files.isDirectory(folderPath)) {
            return false;
        }

        String[] validParentFolderNames = {"starsector-core", "Java", "starsector"};
        String parentFolderName = folderPath.getFileName().toString();
        boolean isValidParentFolder = false;

        for (String validName : validParentFolderNames) {
            if (parentFolderName.equals(validName)) {
                isValidParentFolder = true;
                break;
            }
        }

        if (!isValidParentFolder) {
            return false;
        }

        String[] childFolderNames = {"data", "graphics"};

        for (String childFolderName : childFolderNames) {
            Path childFolderPath = folderPath.resolve(childFolderName);
            if (!Files.isDirectory(childFolderPath)) {
                return false;
            }
        }

        return true;
    }

}
