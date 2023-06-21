package oth.shipeditor.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import oth.shipeditor.parsing.deserialize.ColorArrayRGBADeserializer;
import oth.shipeditor.parsing.serialize.ColorArrayRGBASerializer;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
public class Settings {

    Settings() {}

    @Getter
    @JsonProperty("backgroundColor")
    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Color backgroundColor;

    @Getter
    @JsonProperty("gameFolderPath")
    String gameFolderPath;

    @Getter
    @JsonProperty("coreFolderPath")
    String coreFolderPath;

    @Getter
    @JsonProperty("modFolderPath")
    String modFolderPath;

    public void setBackgroundColor(Color color) {
        if (color != null) {
            this.backgroundColor = color;
        } else {
            this.backgroundColor = Color.GRAY;
        }
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

}
