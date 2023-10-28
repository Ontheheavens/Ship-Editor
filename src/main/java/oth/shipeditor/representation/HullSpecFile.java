package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.Point2DArrayDeserializer;
import oth.shipeditor.parsing.deserialize.Point2DDeserializer;
import oth.shipeditor.parsing.serialize.BaseNumberSerializer;
import oth.shipeditor.parsing.serialize.points.Point2DArraySerializer;
import oth.shipeditor.parsing.serialize.points.Point2DSerializer;
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
@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods"})
@Getter @Setter
public class HullSpecFile implements ShipSpecFile {

    @JsonIgnore
    @Setter
    Path filePath;

    @JsonIgnore
    @Setter
    Path tableFilePath;

    @JsonProperty(StringConstants.HULL_NAME)
    String hullName = "New Hull";

    @JsonProperty(StringConstants.HULL_ID)
    private String hullId = "new_hull";

    @JsonProperty("hullSize")
    private String hullSize = StringConstants.DEFAULT_ID;

    @JsonProperty(StringConstants.STYLE)
    private String style = StringConstants.LOW_TECH;

    @JsonProperty(StringConstants.SPRITE_NAME)
    private String spriteName;

    @JsonProperty("height")
    private int height;

    @JsonProperty(StringConstants.WIDTH)
    private int width;

    @JsonProperty(StringConstants.CENTER)
    @JsonDeserialize(using = Point2DDeserializer.class)
    @JsonSerialize(using = Point2DSerializer.class)
    private Point2D.Double center;

    @JsonProperty("moduleAnchor")
    @JsonDeserialize(using = Point2DDeserializer.class)
    @JsonSerialize(using = Point2DSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Point2D.Double moduleAnchor;

    @JsonProperty(StringConstants.COLLISION_RADIUS)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private double collisionRadius;

    @JsonProperty(StringConstants.COVERS_COLOR)
    private String coversColor;

    @JsonProperty("shieldCenter")
    @JsonDeserialize(using = Point2DDeserializer.class)
    @JsonSerialize(using = Point2DSerializer.class)
    private Point2D.Double shieldCenter;

    @JsonProperty("shieldRadius")
    @JsonSerialize(using = BaseNumberSerializer.class)
    private double shieldRadius;

    // Deemed not significant enough so far; GUI editing implementation postponed.
    @JsonProperty("viewOffset")
    private int viewOffset;

    @JsonProperty(StringConstants.BUILT_IN_MODS)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] builtInMods;

    @JsonProperty(StringConstants.BUILT_IN_WEAPONS)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> builtInWeapons;

    @JsonProperty(StringConstants.BUILT_IN_WINGS)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] builtInWings = new String[0];

    @JsonProperty("weaponSlots")
    private WeaponSlot[] weaponSlots = new WeaponSlot[0];

    @JsonProperty("engineSlots")
    private EngineSlot[] engineSlots = new EngineSlot[0];

    @JsonProperty("bounds")
    @JsonDeserialize(using = Point2DArrayDeserializer.class)
    @JsonSerialize(using = Point2DArraySerializer.class)
    Point2D.Double[] bounds = new Point2D.Double[0];

}
