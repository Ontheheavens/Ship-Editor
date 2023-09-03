package oth.shipeditor.representation.weapon;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.Point2DDeserializer;
import oth.shipeditor.utility.text.StringConstants;

import java.awt.geom.Point2D;
import java.nio.file.Path;

/**
 * @author Ontheheavens
 * @since 03.09.2023
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class ProjectileSpecFile {

    @Setter
    @JsonIgnore
    private Path projectileSpecFilePath;

    @Setter
    @JsonIgnore
    private Path containingPackage;

    @JsonProperty("id")
    private String id;

    @JsonProperty(StringConstants.SPEC_CLASS)
    private String specClass;

    @JsonProperty("missileType")
    private String missileType;

    @JsonProperty(StringConstants.SPRITE)
    private String sprite;

    @JsonProperty("size")
    private int[] size;

    @JsonDeserialize(using = Point2DDeserializer.class)
    @JsonProperty(StringConstants.CENTER)
    private  Point2D.Double center;

}
