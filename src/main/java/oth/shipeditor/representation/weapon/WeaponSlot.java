package oth.shipeditor.representation.weapon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.Point2DArrayDeserializer;
import oth.shipeditor.utility.text.StringConstants;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeaponSlot {

    @JsonProperty("id")
    private String id;

    @JsonProperty("size")
    private String size;

    @JsonProperty("type")
    private String type;

    @JsonProperty("mount")
    private String mount;

    @JsonProperty("renderOrderMod")
    private double renderOrderMod;

    @JsonProperty("arc")
    private double arc;

    @JsonProperty(StringConstants.ANGLE)
    private double angle;

    @JsonProperty("locations")
    @JsonDeserialize(using = Point2DArrayDeserializer.class)
    private Point2D.Double[] locations;

}
