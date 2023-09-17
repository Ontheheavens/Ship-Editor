package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.ColorArrayRGBADeserializer;
import oth.shipeditor.parsing.serialize.ColorArrayRGBASerializer;

import java.awt.*;
import java.nio.file.Path;

/**
 * @author Ontheheavens
 * @since 30.07.2023
 */
@SuppressWarnings({"ClassWithTooManyFields", "TransientFieldInNonSerializableClass", "ClassWithTooManyMethods"})
@Getter @Setter
public class EngineStyle {

    @JsonIgnore
    @Setter
    private transient Path filePath;

    @JsonIgnore @Setter
    private transient Path containingPackage;

    @Setter
    private String engineStyleID;

    @JsonProperty("type")
    private String type;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("engineColor")
    private Color engineColor;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("engineCampaignColor")
    private Color engineCampaignColor;

    @JsonProperty("mode")
    private String mode;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("contrailColor")
    private Color contrailColor;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("contrailCampaignColor")
    private Color contrailCampaignColor;

    @JsonProperty("contrailDuration")
    private double contrailDuration;

    @JsonProperty("contrailWidthMult")
    private double contrailWidthMult;

    @JsonProperty("contrailWidthAddedFractionAtEnd")
    private double contrailWidthAddedFractionAtEnd;

    @JsonProperty("contrailMinSeg")
    private double contrailMinSeg;

    @JsonProperty("contrailParticleSizeMult")
    private double contrailParticleSizeMult;

    @JsonProperty("contrailParticleFinalSizeMult")
    private double contrailParticleFinalSizeMult;

    @JsonProperty("contrailMaxSpeedMult")
    private double contrailMaxSpeedMult;

    @JsonProperty("contrailAngularVelocityMult")
    private double contrailAngularVelocityMult;

    @JsonProperty("contrailSpawnDistMult")
    private double contrailSpawnDistMult;

    @JsonProperty("contrailParticleDuration")
    private double contrailParticleDuration;

    @JsonProperty("glowSprite")
    private String glowSprite;

    @JsonProperty("glowShape")
    private String glowShape;

    @JsonProperty("glowOutline")
    private String glowOutline;

    @JsonProperty("omegaMode")
    private boolean omegaMode;

    @JsonProperty("glowSizeMult")
    private double glowSizeMult;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("glowAlternateColor")
    private Color glowAlternateColor;

    @Override
    public String toString() {
        return this.engineStyleID;
    }

}
