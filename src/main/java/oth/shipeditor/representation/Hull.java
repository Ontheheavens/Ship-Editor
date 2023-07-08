package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.ColorArrayRGBADeserializer;
import oth.shipeditor.parsing.deserialize.Point2DArrayDeserializer;
import oth.shipeditor.parsing.deserialize.Point2DDeserializer;
import oth.shipeditor.utility.StringConstants;

import java.awt.*;
import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.05.2023
 */
public class Hull {

    @JsonIgnore
    @Getter @Setter
    Path shipFilePath;

    @Getter
    @JsonProperty(StringConstants.HULL_NAME)
    String hullName;

    @Getter
    @JsonProperty("hullId")
    private String hullId;

    @Getter
    @JsonProperty("hullSize")
    private String hullSize;

    @Getter
    @JsonProperty(StringConstants.SPRITE_NAME)
    private String spriteName;

    @JsonProperty(StringConstants.STYLE)
    private String style;

    @JsonProperty("height")
    private double height;

    @JsonProperty(StringConstants.WIDTH)
    private double width;

    @Getter
    @JsonProperty("center")
    @JsonDeserialize(using = Point2DDeserializer.class)
    private Point2D.Double center;

    @JsonProperty("moduleAnchor")
    @JsonDeserialize(using = Point2DDeserializer.class)
    private Point2D.Double moduleAnchor;

    @Getter
    @JsonProperty("collisionRadius")
    private double collisionRadius;

    @JsonProperty(StringConstants.COVERS_COLOR)
    private String coversColor;

    @JsonProperty("shieldCenter")
    @JsonDeserialize(using = Point2DDeserializer.class)
    private Point2D.Double shieldCenter;

    @JsonProperty("shieldRadius")
    private double shieldRadius;

    @JsonProperty("viewOffset")
    private double viewOffset;

    @JsonProperty(StringConstants.BUILT_IN_MODS)
    private String[] builtInMods;

    @JsonProperty(StringConstants.BUILT_IN_WEAPONS)
    private Map<String, String> builtInWeapons;

    @JsonProperty(StringConstants.BUILT_IN_WINGS)
    private String[] builtInWings;

    @JsonProperty("weaponSlots")
    private WeaponSlot[] weaponSlots;

    @JsonProperty("engineSlots")
    private EngineSlot[] engineSlots;

    @Getter
    @JsonProperty("bounds")
    @JsonDeserialize(using = Point2DArrayDeserializer.class)
    Point2D.Double[] bounds;

}
