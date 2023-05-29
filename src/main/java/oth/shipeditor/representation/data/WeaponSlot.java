package oth.shipeditor.representation.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import oth.shipeditor.parsing.Point2DArrayParser;

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

    @JsonProperty("arc")
    private double arc;

    @JsonProperty("angle")
    private double angle;

    @JsonProperty("locations")
    @JsonDeserialize(using = Point2DArrayParser.class)
    private Point2D.Double[] locations;

}
