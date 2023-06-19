package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import oth.shipeditor.parsing.deserialize.Point2DDeserializer;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
public class EngineSlot {

    @JsonProperty("location")
    @JsonDeserialize(using = Point2DDeserializer.class)
    private Point2D.Double location;

    @JsonProperty("length")
    private double length;

    @JsonProperty("width")
    private double width;

    @JsonProperty("angle")
    private double angle;

    @JsonProperty("contrailSize")
    private double contrailSize;

    @JsonProperty("style")
    private String style;

}
