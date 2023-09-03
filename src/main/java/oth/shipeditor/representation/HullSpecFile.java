package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.Point2DArrayDeserializer;
import oth.shipeditor.parsing.deserialize.Point2DDeserializer;
import oth.shipeditor.representation.weapon.WeaponSlot;
import oth.shipeditor.utility.text.StringConstants;

import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.util.Map;

/**
 * This is a serialization/deserialization class, not intended for edit through viewer at runtime.
 * Input data from instance of this class (which is acquired via deserialization of JSON) is passed to layer,
 * which transforms and edits the data as necessary, then constructs new result instance of this class for serialization.
 * @author Ontheheavens
 * @since 05.05.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class HullSpecFile implements ShipSpecFile {

    @JsonIgnore
    @Setter
    Path filePath;

    @JsonIgnore
    @Setter
    Path tableFilePath;

    @JsonProperty(StringConstants.HULL_NAME)
    String hullName;

    @JsonProperty(StringConstants.HULL_ID)
    private String hullId;

    @JsonProperty("hullSize")
    private String hullSize;

    @JsonProperty(StringConstants.SPRITE_NAME)
    private String spriteName;

    @JsonProperty(StringConstants.STYLE)
    private String style;

    @JsonProperty("height")
    private double height;

    @JsonProperty(StringConstants.WIDTH)
    private double width;

    @JsonProperty(StringConstants.CENTER)
    @JsonDeserialize(using = Point2DDeserializer.class)
    private Point2D.Double center;

    @JsonProperty("moduleAnchor")
    @JsonDeserialize(using = Point2DDeserializer.class)
    private Point2D.Double moduleAnchor;

    @JsonProperty(StringConstants.COLLISION_RADIUS)
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

    @JsonProperty("bounds")
    @JsonDeserialize(using = Point2DArrayDeserializer.class)
    Point2D.Double[] bounds;

}
