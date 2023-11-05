package oth.shipeditor.representation.ship;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.Point2DDeserializer;
import oth.shipeditor.parsing.serialize.BaseNumberSerializer;
import oth.shipeditor.parsing.serialize.points.EngineLocationSerializer;
import oth.shipeditor.utility.text.StringConstants;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
@Getter @Setter
public class EngineSlot {

    @JsonProperty("location")
    @JsonDeserialize(using = Point2DDeserializer.class)
    @JsonSerialize(using = EngineLocationSerializer.class)
    private Point2D.Double location;

    @JsonProperty(StringConstants.LENGTH)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double length;

    @JsonProperty(StringConstants.WIDTH)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double width;

    @JsonProperty(StringConstants.ANGLE)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double angle;

    @JsonProperty("contrailSize")
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailSize;

    @JsonProperty(StringConstants.STYLE)
    private String style;

    /**
     * This field is used to specify a custom style if style field is set to CUSTOM.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("styleId")
    private String styleId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("styleSpec")
    private EngineStyle styleSpec;

}
