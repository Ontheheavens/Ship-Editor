package oth.shipeditor;

/**
 * @author Ontheheavens.
 */
public class Utility {

    /**
     * Rounds the given value to the specified number of decimal places.
     * @param value value to be rounded.
     * @param scale number of decimal places to round the value to. Needs to be positive.
     * @return rounded value.
     * @throws IllegalArgumentException if scale is less than 0.
     * @throws ArithmeticException if result will be infinite or NaN.
     */
    public static double round(double value, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("Scale cannot be negative.");
        }
        double powerOfTen = Math.pow(10, scale);
        double roundedValue = Math.round(value * powerOfTen) / powerOfTen;
        if (Double.isInfinite(roundedValue)) {
            throw new ArithmeticException("Rounded value is infinite.");
        } else if (Double.isNaN(roundedValue)) {
            throw new ArithmeticException("Rounded value is NaN.");
        }
        return roundedValue;
    }

}
