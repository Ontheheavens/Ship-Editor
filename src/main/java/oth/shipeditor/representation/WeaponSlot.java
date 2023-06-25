package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import oth.shipeditor.parsing.deserialize.Point2DArrayDeserializer;
import oth.shipeditor.parsing.deserialize.Point2DDeserializer;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
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

    @JsonProperty("angle")
    private double angle;

    @JsonProperty("locations")
    @JsonDeserialize(using = Point2DArrayDeserializer.class)
    private Point2D.Double[] locations;

    @JsonProperty("position")
    @JsonDeserialize(using = Point2DDeserializer.class)
    private Point2D.Double position;

}
