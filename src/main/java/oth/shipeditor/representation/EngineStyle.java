package oth.shipeditor.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.parsing.deserialize.ColorArrayRGBADeserializer;
import oth.shipeditor.parsing.serialize.ColorArrayRGBASerializer;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 30.07.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Getter @Setter
public class EngineStyle {

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

    @JsonProperty("contrailParticleSizeMult")
    private double contrailParticleSizeMult;

    @JsonProperty("contrailParticleFinalSizeMult")
    private double contrailParticleFinalSizeMult;

    @JsonProperty("contrailMaxSpeedMult")
    private double contrailMaxSpeedMult;

    @JsonProperty("contrailAngularVelocityMult")
    private double contrailAngularVelocityMult;

    @JsonProperty("contrailParticleDuration")
    private double contrailParticleDuration;

}
