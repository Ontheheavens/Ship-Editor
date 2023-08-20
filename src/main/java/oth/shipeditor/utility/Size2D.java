package oth.shipeditor.utility;

import java.awt.geom.Dimension2D;

/**
 * @author Ontheheavens
 * @since 20.08.2023
 */
@SuppressWarnings("ParameterHidesMemberVariable")
public class Size2D extends Dimension2D {

    private double width;

    private double height;

    public Size2D(double width, double height) {
        this.height = height;
        this.width = width;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setSize(double width, double height) {
        this.height = height;
        this.width = width;
    }

}
