package oth.shipeditor.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.serialize.ColorRGBASerializer;
import oth.shipeditor.parsing.deserialize.ColorRGBADeserializer;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 19.06.2023
 */
public class Settings {

    Settings() {}

    @Getter @Setter(AccessLevel.PACKAGE)
    @JsonProperty("backgroundColor")
    @JsonDeserialize(using = ColorRGBADeserializer.class)
    @JsonSerialize(using = ColorRGBASerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Color backgroundColor;

}
