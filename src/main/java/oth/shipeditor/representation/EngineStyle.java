package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.ColorArrayRGBADeserializer;
import oth.shipeditor.parsing.serialize.BaseNumberSerializer;
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

    @JsonIgnore
    @Setter
    private transient Path containingPackage;

    @Setter
    @JsonIgnore
    private String engineStyleID;

    @JsonProperty("type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String type;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("engineColor")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Color engineColor;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("engineCampaignColor")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Color engineCampaignColor;

    @JsonProperty("mode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mode;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("contrailColor")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Color contrailColor;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("contrailCampaignColor")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Color contrailCampaignColor;

    @JsonProperty("contrailDuration")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailDuration;

    @JsonProperty("contrailWidthMult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailWidthMult;

    @JsonProperty("contrailWidthAddedFractionAtEnd")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailWidthAddedFractionAtEnd;

    @JsonProperty("contrailMinSeg")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailMinSeg;

    @JsonProperty("contrailParticleSizeMult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailParticleSizeMult;

    @JsonProperty("contrailParticleFinalSizeMult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailParticleFinalSizeMult;

    @JsonProperty("contrailMaxSpeedMult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailMaxSpeedMult;

    @JsonProperty("contrailAngularVelocityMult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailAngularVelocityMult;

    @JsonProperty("contrailSpawnDistMult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailSpawnDistMult;

    @JsonProperty("contrailParticleDuration")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double contrailParticleDuration;

    @JsonProperty("glowSprite")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String glowSprite;

    @JsonProperty("glowShape")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String glowShape;

    @JsonProperty("glowOutline")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String glowOutline;

    @JsonProperty("omegaMode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean omegaMode;

    @JsonProperty("glowSizeMult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double glowSizeMult;

    @JsonProperty("engineGlowGlowMult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BaseNumberSerializer.class)
    private Double engineGlowGlowMult;

    @JsonDeserialize(using = ColorArrayRGBADeserializer.class)
    @JsonSerialize(using = ColorArrayRGBASerializer.class)
    @JsonProperty("glowAlternateColor")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Color glowAlternateColor;

    @Override
    public String toString() {
        return this.engineStyleID;
    }

}
