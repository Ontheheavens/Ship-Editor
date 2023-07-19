package oth.shipeditor.utility.graphics;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

/**
 * @author Ontheheavens
 * @since 19.07.2023
 */
@Getter @Setter
public class DrawingParameters {

    Color paintColor;
    double worldSize;
    double worldThickness;
    double screenSize;

    @SuppressWarnings("unused")
    public DrawingParameters() {
        this(null, 0, 0, 0);
    }

    @SuppressWarnings("WeakerAccess")
    public DrawingParameters(Color colorInput, double worldSizeInput,
                             double worldThicknessInput, double screenSizeInput) {
        this.paintColor = colorInput;
        this.worldSize = worldSizeInput;
        this.worldThickness = worldThicknessInput;
        this.screenSize = screenSizeInput;
    }

    private DrawingParameters(Builder builder) {
        this.paintColor = builder.paintColor;
        this.worldSize = builder.worldSize;
        this.worldThickness = builder.worldThickness;
        this.screenSize = builder.screenSize;
    }

    public static Builder builder() {
        return new Builder();
    }


    @SuppressWarnings({"unused", "PublicInnerClass", "ParameterHidesMemberVariable"})
    public static final class Builder {
        private Color paintColor;
        private double worldSize;
        private double worldThickness;
        private double screenSize;

        private Builder() {
            // Private constructor to enforce the use of builder().
        }

        public Builder withPaintColor(Color paintColor) {
            this.paintColor = paintColor;
            return this;
        }

        public Builder withWorldSize(double worldSize) {
            this.worldSize = worldSize;
            return this;
        }

        public Builder withWorldThickness(double worldThickness) {
            this.worldThickness = worldThickness;
            return this;
        }

        public Builder withScreenSize(double screenSize) {
            this.screenSize = screenSize;
            return this;
        }

        public DrawingParameters build() {
            return new DrawingParameters(this);
        }
    }

}
