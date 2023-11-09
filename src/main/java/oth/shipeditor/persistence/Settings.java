package oth.shipeditor.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import oth.shipeditor.parsing.deserialize.ColorArrayRGBADeserializer;
import oth.shipeditor.parsing.serialize.ColorArrayRGBASerializer;
import oth.shipeditor.utility.themes.Theme;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
@Getter
public class Settings {

    Settings() {}

    @JsonProperty("editorVersion")
    String editorVersion = "0.7.2";

    @JsonProperty("backgroundColor")
    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Color backgroundColor = Color.GRAY;

    @JsonProperty("gameFolderPath")
    String gameFolderPath;

    @JsonProperty("coreFolderPath")
    String coreFolderPath;

    @JsonProperty("modFolderPath")
    String modFolderPath;

    @JsonProperty("showLoadingErrors")
    boolean showLoadingErrors;

    @JsonProperty("loadDataAtStart")
    boolean loadDataAtStart = true;

    @JsonProperty("theme")
    Theme theme = Theme.FLAT_INTELLIJ;

    @JsonProperty("dataPackages")
    private List<GameDataPackage> dataPackages = new ArrayList<>();

    public void setBackgroundColor(Color color) {
        if (color != null) {
            this.backgroundColor = color;
        } else {
            this.backgroundColor = Color.GRAY;
        }
        SettingsManager.updateFileFromRuntime();
    }

    public void setTheme(Theme inputTheme) {
        this.theme = inputTheme;
        SettingsManager.updateFileFromRuntime();
    }

    void setGameFolderPath(String path) {
        this.gameFolderPath = path;
        SettingsManager.updateFileFromRuntime();
    }

    void setCoreFolderPath(String path) {
        this.coreFolderPath = path;
        SettingsManager.updateFileFromRuntime();
    }

    void setModFolderPath(String path) {
        this.modFolderPath = path;
        SettingsManager.updateFileFromRuntime();
    }

    public void setShowLoadingErrors(boolean showErrors) {
        this.showLoadingErrors = showErrors;
        SettingsManager.updateFileFromRuntime();
    }

    public void setLoadDataAtStart(boolean loadData) {
        this.loadDataAtStart = loadData;
        SettingsManager.updateFileFromRuntime();
    }

    void addDataPackage(Path folder) {
        String folderName = folder.getFileName().toString();
        addDataPackage(folderName);
    }

    private void addDataPackage(String folderName) {
        if (getPackage(folderName) != null) {
            return;
        }
        GameDataPackage dataPackage = new GameDataPackage(folderName, false, false);
        dataPackages.add(dataPackage);
    }

    public GameDataPackage getPackage(Path folder) {
        String folderName = folder.getFileName().toString();
        return getPackage(folderName);
    }

    public GameDataPackage getPackage(String folderName) {
        if (dataPackages == null) return null;
        if (SettingsManager.isCoreFolder(folderName)) {
            return SettingsManager.getCorePackage();
        }
        for (GameDataPackage gameDataPackage : dataPackages) {
            String packageFolderName = gameDataPackage.getFolderName();
            if (packageFolderName.equals(folderName)) {
                return gameDataPackage;
            }
        }
        return null;
    }

}
