package oth.shipeditor.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import oth.shipeditor.parsing.deserialize.ColorRGBADeserializer;
import oth.shipeditor.parsing.serialize.ColorRGBASerializer;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
public class Settings {

    Settings() {}

    @Getter
    @JsonProperty("backgroundColor")
    @JsonDeserialize(using = ColorRGBADeserializer.class)
    @JsonSerialize(using = ColorRGBASerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Color backgroundColor;

    @Getter
    @JsonProperty("gameFolderPath")
    String gameFolderPath;

    public void setBackgroundColor(Color color) {
        if (color != null) {
            this.backgroundColor = color;
        } else {
            this.backgroundColor = Color.GRAY;
        }
        SettingsManager.updateFileFromRuntime();
    }

    public void setGameFolderPath(String path) {
        this.gameFolderPath = path;
        SettingsManager.updateFileFromRuntime();
    }

}
